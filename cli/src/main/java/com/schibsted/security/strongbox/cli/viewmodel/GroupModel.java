/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.cli.viewmodel;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.auth.profile.internal.AwsProfileNameLoader;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.schibsted.security.strongbox.sdk.internal.config.credentials.CustomCredentialsProviderChain;
import com.schibsted.security.strongbox.cli.view.OutputFormat;
import com.schibsted.security.strongbox.cli.viewmodel.types.ProxyInformation;
import com.schibsted.security.strongbox.cli.viewmodel.types.SecretsGroupIdentifierView;
import com.schibsted.security.strongbox.cli.viewmodel.types.SecretsGroupInfoView;
import com.schibsted.security.strongbox.sdk.exceptions.FailedToResolveRegionException;
import com.schibsted.security.strongbox.sdk.internal.config.AWSCLIConfigFile;
import com.schibsted.security.strongbox.sdk.internal.config.CustomRegionProviderChain;
import com.schibsted.security.strongbox.sdk.impl.DefaultSecretsGroupManager;
import com.schibsted.security.strongbox.sdk.internal.RegionResolver;
import com.schibsted.security.strongbox.sdk.internal.access.PrincipalAutoSuggestion;
import com.schibsted.security.strongbox.sdk.internal.config.credentials.MFAToken;
import com.schibsted.security.strongbox.sdk.internal.config.credentials.ProfileResolver;
import com.schibsted.security.strongbox.sdk.internal.encryption.FileEncryptionContext;
import com.schibsted.security.strongbox.sdk.internal.encryption.KMSRandomGenerator;
import com.schibsted.security.strongbox.sdk.internal.encryption.RandomGenerator;
import com.schibsted.security.strongbox.sdk.internal.srn.SecretsGroupSRN;
import com.schibsted.security.strongbox.sdk.internal.types.config.FileUserConfig;
import com.schibsted.security.strongbox.sdk.internal.types.config.UserConfig;
import com.schibsted.security.strongbox.sdk.internal.types.store.DynamoDBReference;
import com.schibsted.security.strongbox.sdk.internal.types.store.FileReference;
import com.schibsted.security.strongbox.sdk.internal.types.store.StorageReference;
import com.schibsted.security.strongbox.sdk.types.ClientConfiguration;
import com.schibsted.security.strongbox.sdk.types.EncryptionStrength;
import com.schibsted.security.strongbox.sdk.types.Principal;
import com.schibsted.security.strongbox.sdk.types.PrincipalType;
import com.schibsted.security.strongbox.sdk.types.ProfileIdentifier;
import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static com.schibsted.security.strongbox.sdk.internal.ClientConfigurationHelper.transformAndVerifyOrThrow;

/**
 * Used to resolve credentials, region, MFA, etc. before executing the individual commands
 * related to Strongbox Secrets Groups.
 *
 * @author stiankri
 * @author torarvid
 * @author hawkaa
 */
public class GroupModel {
    private final String fieldName;
    private final String saveToFilePath;
    private Region region;
    private DefaultSecretsGroupManager secretsGroupManager;
    private RandomGenerator randomGenerator;
    private PrincipalAutoSuggestion principalAutoSuggestion;
    private OutputFormat outputFormat;

    private static final Logger LOG = LoggerFactory.getLogger(GroupModel.class);

    public GroupModel(String rawProfileIdentifier, String explicitAssumeRole, String region, boolean useAES256, String outputFormat, String fieldName, String saveToFilePath) {
        this.outputFormat = extractOutput(outputFormat);
        this.fieldName = extractFieldName(this.outputFormat, fieldName);
        this.saveToFilePath = extractSaveToFilePath(saveToFilePath);

        ProfileIdentifier profileIdentifier = ProfileResolver.resolveProfile(Optional.ofNullable(rawProfileIdentifier));
        this.region = resolveRegion(region, profileIdentifier);
        RegionResolver.setRegion(this.region);

        ClientConfiguration clientConfiguration = getClientConfiguration();
        AWSCredentialsProvider baseCredentials = resolveBaseCredentials(clientConfiguration, profileIdentifier);
        AWSCredentialsProvider credentials = resolveExplicitAssumeRole(baseCredentials, clientConfiguration, explicitAssumeRole);

        UserConfig userConfig = getUserConfig();
        EncryptionStrength encryptionStrength = useAES256 ? EncryptionStrength.AES_256 : EncryptionStrength.AES_128;

        this.randomGenerator = new KMSRandomGenerator(credentials, clientConfiguration);
        this.principalAutoSuggestion = PrincipalAutoSuggestion.fromCredentials(credentials, clientConfiguration);

        this.secretsGroupManager = new DefaultSecretsGroupManager(credentials, userConfig, encryptionStrength, clientConfiguration);
    }

    private Region resolveRegion(String region, ProfileIdentifier profileIdentifier) {
        try {
            CustomRegionProviderChain regionProvider = new CustomRegionProviderChain();
            return regionProvider.resolveRegion(Optional.ofNullable(region), profileIdentifier);
        } catch (FailedToResolveRegionException e) {
            throw new FailedToResolveRegionException("A region must be specified, e.g. with '--region <region>' or in the '~/.aws/config' file");
        }

    }

    private AWSCredentialsProvider resolveBaseCredentials(final ClientConfiguration clientConfiguration, final ProfileIdentifier profileIdentifier) {
        try {
            AWSCredentialsProvider credentialsProvider =  new CustomCredentialsProviderChain(clientConfiguration, profileIdentifier, MFAToken.defaultMFATokenSupplier());

            // Test if getCredentials will throw
            credentialsProvider.getCredentials();

            return credentialsProvider;
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to resolve credentials.\n" +
                            "\n" +
                            "If you entered an MFA token, the token was incorrect, or the MFA is misconfigured\n" +
                            "\n" +
                            "The following locations are included in the credentials chain:\n" +
                            " - environment variables\n" +
                            " - system properties\n" +
                            " - credential file (%s) and config file (%s)\n" +
                            " - ec2 container metadata\n" +
                            "\n" +
                            "Please refer to the documentation for how to configure credentials",
                    AWSCLIConfigFile.getCredentialProfilesFile().map(File::getAbsolutePath).orElse("not specified"),
                    AWSCLIConfigFile.getConfigFile().map(File::getAbsolutePath).orElse("not specified")), e);
        }
    }

    private AWSCredentialsProvider resolveExplicitAssumeRole(final AWSCredentialsProvider baseCredentials, final ClientConfiguration clientConfiguration, String assumeRole) {
        if (assumeRole != null) {
            return assumeRole(baseCredentials, clientConfiguration, assumeRole);
        } else {
            return baseCredentials;
        }
    }

    private AWSCredentialsProvider assumeRole(AWSCredentialsProvider longLivedAWSCredentials, ClientConfiguration clientConfiguration, String assumeRoleArn) {
        AWSSecurityTokenService client = AWSSecurityTokenServiceClientBuilder.standard()
                .withCredentials(longLivedAWSCredentials)
                .withClientConfiguration(transformAndVerifyOrThrow(clientConfiguration))
                .withRegion(RegionResolver.getRegion())
                .build();

        STSAssumeRoleSessionCredentialsProvider.Builder builder =
                new STSAssumeRoleSessionCredentialsProvider.Builder(assumeRoleArn, "strongbox-cli");
        builder.withStsClient(client);

        return builder.build();
    }

    private ClientConfiguration getClientConfiguration() {
        Optional<ProxyInformation> proxyInfo = ProxyInformation.fromEnvironment();
        if (proxyInfo.isPresent()) {
            ProxyInformation proxy = proxyInfo.get();
            return new ClientConfiguration(new ClientConfiguration.Proxy(proxy.username, proxy.password, proxy.nonProxyHosts, proxy.host, proxy.port));
        }
        return new ClientConfiguration();
    }

    private String extractSaveToFilePath(String saveToFilePath) {
        return saveToFilePath;
    }

    private String extractFieldName(OutputFormat outputFormat, String fieldName) {
        if (fieldName != null && !outputFormat.isCSVorRAW()) {
            throw new IllegalArgumentException("'--output-field-names' can only be specified when '--output' is set to 'raw' or 'csv'");
        }

        if (fieldName == null && outputFormat.isCSVorRAW()) {
            throw new IllegalArgumentException("'--output-field-names' must be specified when '--output' is set to 'raw' or 'csv'");
        }

        return fieldName;
    }

    private OutputFormat extractOutput(String outputFormat) {
        if (outputFormat != null) {
            switch (outputFormat) {
                case "text":
                    return OutputFormat.TEXT;
                case "json":
                    return OutputFormat.JSON;
                case "raw":
                    return OutputFormat.RAW;
                case "csv":
                    return OutputFormat.CSV;
                default:
                    throw new IllegalArgumentException(String.format("Unrecognized output format '%s', expected one of {text, json, csv, raw}", outputFormat));
            }
        } else {
            return OutputFormat.TEXT;
        }
    }

    public OutputFormat getOutputFormat() {
        return outputFormat;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getSaveToFilePath() {
        return saveToFilePath;
    }

    public RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }

    public PrincipalAutoSuggestion getPrincipalAutoSuggestion() {
        return principalAutoSuggestion;
    }

    private static UserConfig getUserConfig() {
        String smConfig = Optional.ofNullable(System.getenv("STRONGBOX_CONFIG_FILE"))
                .orElse(System.getProperty("user.home") + "/.strongbox/config");
        File smConfigFile = new File(smConfig);
        return new FileUserConfig(smConfigFile);
    }

    public DefaultSecretsGroupManager getSecretsGroupManager() {
        return secretsGroupManager;
    }

    public Region getRegion() {
        return region;
    }

    public SecretsGroupInfoView createGroup(String groupName, String storageType, String file, Boolean allowKeyReuse) {
        StorageReference storageReference = getStorageReference(storageType, file, false);

        boolean allowExistingPendingDeletedOrDisabledKey = (allowKeyReuse != null && allowKeyReuse);

        SecretsGroupIdentifier group = new SecretsGroupIdentifier(region, groupName);
        return new SecretsGroupInfoView(secretsGroupManager.create(group, storageReference, allowExistingPendingDeletedOrDisabledKey));
    }

    private static StorageReference getStorageReference(String storageType, String file, boolean requireNonNull) {
        if (requireNonNull && storageType == null) {
            throw new IllegalArgumentException("A storage type {dynamodb, file} must be specified with '--storage-type'");
        }
        if (storageType == null || storageType.equals("dynamodb")) {
            return new DynamoDBReference();
        } else if (storageType.equals("file")) {
            if (file != null) {
                return new FileReference(new java.io.File(file));
            } else {
                throw new IllegalArgumentException("When using storage type 'file', a '--path' to the file must be specified");
            }
        } else {
            throw new IllegalArgumentException(String.format("Unrecognized storage type '%s', expected one of {dynamodb, file}", storageType));
        }
    }

    public Set<SecretsGroupIdentifierView> listGroup() {
        return secretsGroupManager.identifiers()
                .stream()
                .map(SecretsGroupIdentifierView::new)
                .collect(Collectors.toSet());
    }

    public void deleteGroup(String groupName, Boolean force) {
        SecretsGroupIdentifier group = new SecretsGroupIdentifier(region, groupName);
        String challenge = String.format("%s.%s", group.region.name, group.name);
        Confirmation.acceptOrExit(String.format("DANGER! Are you sure you want to permanently delete the secrets group '%s'?"
                        + "\nIf yes, type the identifier (region.name) as just shown: ",
                challenge), challenge, force);
        secretsGroupManager.delete(group);
    }

    public SecretsGroupInfoView groupInfo(String groupName) {
        SecretsGroupIdentifier group = new SecretsGroupIdentifier(region, groupName);
        return new SecretsGroupInfoView(secretsGroupManager.info(group));
    }

    public SecretModel get(String groupName) {
        SecretsGroupIdentifier group = new SecretsGroupIdentifier(region, groupName);
        return new SecretModel(secretsGroupManager.get(group), group, randomGenerator);
    }

    public void attachAdmin(String groupName, String principalName, String principalType) {
        SecretsGroupIdentifier group = new SecretsGroupIdentifier(region, groupName);
        Principal principal = extractPrincipal(principalType, principalName, getAccountFromGroup(group));

        secretsGroupManager.attachAdmin(group, principal);
    }

    public void detachAdmin(String groupName, String principalName, String principalType) {
        SecretsGroupIdentifier group = new SecretsGroupIdentifier(region, groupName);
        Principal principal = extractPrincipal(principalType, principalName, getAccountFromGroup(group));

        secretsGroupManager.detachAdmin(group, principal);
    }

    public void attachReadOnly(String groupName, String principalName, String principalType) {
        SecretsGroupIdentifier group = new SecretsGroupIdentifier(region, groupName);
        Principal principal = extractPrincipal(principalType, principalName, getAccountFromGroup(group));

        secretsGroupManager.attachReadOnly(group, principal);
    }

    public void detachReadOnly(String groupName, String principalName, String principalType) {
        SecretsGroupIdentifier group = new SecretsGroupIdentifier(region, groupName);
        Principal principal = extractPrincipal(principalType, principalName, getAccountFromGroup(group));

        secretsGroupManager.detachReadOnly(group, principal);
    }

    private String getAccountFromGroup(SecretsGroupIdentifier group) {
        SecretsGroupSRN srn = (SecretsGroupSRN) secretsGroupManager.srn(group);
        return srn.account;
    }

    private static Principal extractPrincipal(String principalType, String name, String account) {
        return (principalType != null) ?
                new Principal(PrincipalType.fromString(principalType), name) :
                Principal.fromArn(name, account);
    }

    public void backup(String groupName, String fileName, Boolean force) {
        SecretsGroupIdentifier group = new SecretsGroupIdentifier(region, groupName);

        String challenge = String.format("%s.%s", group.region.name, group.name);
        Confirmation.acceptOrExit(String.format("DANGER! Are you sure you want to backup the group '%s' to '%s'?"
                        + "\nIf yes, type the identifier (region.name) as just shown: ",
                challenge, fileName), challenge, force);

        com.schibsted.security.strongbox.sdk.internal.kv4j.generated.File file = new com.schibsted.security.strongbox.sdk.internal.kv4j.generated.File(new java.io.File(fileName),
                secretsGroupManager.encryptor(group),
                new FileEncryptionContext(group),
                new ReentrantReadWriteLock());
        secretsGroupManager.backup(group, file, false);
    }

    public void restore(String groupName, String fileName, Boolean force) {
        SecretsGroupIdentifier group = new SecretsGroupIdentifier(region, groupName);

        String challenge = String.format("%s.%s", group.region.name, group.name);
        Confirmation.acceptOrExit(String.format("DANGER! Are you sure you want to restore the group '%s' from '%s'?"
                        + "\nIf yes, type the identifier (region.name) as just shown: ",
                challenge, fileName), challenge, force);

        com.schibsted.security.strongbox.sdk.internal.kv4j.generated.File file = new com.schibsted.security.strongbox.sdk.internal.kv4j.generated.File(new java.io.File(fileName),
                secretsGroupManager.encryptor(group),
                new FileEncryptionContext(group),
                new ReentrantReadWriteLock());
        secretsGroupManager.restore(group, file, false);
    }

    public SecretsGroupInfoView migrate(String groupName, String storageType, String file, Boolean force) {
        SecretsGroupIdentifier group = new SecretsGroupIdentifier(region, groupName);
        StorageReference storageReference = getStorageReference(storageType, file, true);

        String challenge = String.format("%s.%s", group.region.name, group.name);
        Confirmation.acceptOrExit(String.format("DANGER! Are you sure you want to migrate the group '%s' to the store '%s'?"
                        + "\nIf yes, type the identifier (region.name) as just shown: ",
                challenge, storageReference), challenge, force);

        return new SecretsGroupInfoView(secretsGroupManager.migrate(group, storageReference));
    }
}
