/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.cli.viewmodel;

import com.amazonaws.util.Base64;
import com.amazonaws.util.IOUtils;
import com.schibsted.security.strongbox.cli.viewmodel.types.ListVersionsView;
import com.schibsted.security.strongbox.cli.viewmodel.types.SecretEntryView;
import com.schibsted.security.strongbox.cli.viewmodel.types.SecretIdentifierView;
import com.schibsted.security.strongbox.sdk.SecretsGroup;
import com.schibsted.security.strongbox.sdk.exceptions.DoesNotExistException;
import com.schibsted.security.strongbox.sdk.internal.converter.FormattedTimestamp;
import com.schibsted.security.strongbox.sdk.internal.converter.SecretValueConverter;
import com.schibsted.security.strongbox.sdk.internal.encryption.BestEffortShredder;
import com.schibsted.security.strongbox.sdk.internal.encryption.RandomGenerator;
import com.schibsted.security.strongbox.sdk.types.Comment;
import com.schibsted.security.strongbox.sdk.types.NewSecretEntry;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretMetadata;
import com.schibsted.security.strongbox.sdk.types.SecretType;
import com.schibsted.security.strongbox.sdk.types.SecretValue;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import com.schibsted.security.strongbox.sdk.types.State;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.name;

/**
 * Execute commands related to Secrets within a Secrets Group
 *
 * @author stiankri
 */
public class SecretModel implements AutoCloseable {
    private final SecretsGroup secretsGroup;
    private final SecretsGroupIdentifier secretsGroupIdentifier;
    private final RandomGenerator randomGenerator;

    public SecretModel(SecretsGroup secretsGroup, SecretsGroupIdentifier secretsGroupIdentifier, RandomGenerator randomGenerator) {
        this.secretsGroup = secretsGroup;
        this.randomGenerator = randomGenerator;
        this.secretsGroupIdentifier = secretsGroupIdentifier;
    }

    // TODO: return SecretEntry or Metadata as in API?
    public void createSecret(String secretName, boolean valueFromStdin, String stateName, String notBeforeDate,
                             String notAfterDate, String comment, String generate, String pathToValueFile) {
        State state = extractEnabledDisabled(stateName);
        Optional<ZonedDateTime> notBefore = extractDateIfNotNull(notBeforeDate);
        Optional<ZonedDateTime> notAfter = extractDateIfNotNull(notAfterDate);

        SecretValue extractedSecret = extractSecretValueOrThrow(valueFromStdin, generate, pathToValueFile);

        NewSecretEntry newSecretEntry = new NewSecretEntry(new SecretIdentifier(secretName), extractedSecret, state,
                notBefore, notAfter, extractComment(comment));
        secretsGroup.create(newSecretEntry);
        newSecretEntry.bestEffortShred();
    }


    private static int extractGenerate(String generate) {
        return Integer.valueOf(generate);
    }

    // TODO: return SecretEntry or Metadata as in API?
    public void addSecretVersion(String secretName, boolean valueFromStdin, String stateName, String notBeforeDate,
                                 String notAfterDate, String comment, String numBytesToGenerate, String pathToValueFile) {
        State state = extractEnabledDisabled(stateName);
        Optional<ZonedDateTime> notBefore = extractDateIfNotNull(notBeforeDate);
        Optional<ZonedDateTime> notAfter = extractDateIfNotNull(notAfterDate);

        SecretValue extractedSecret = extractSecretValueOrThrow(valueFromStdin, numBytesToGenerate, pathToValueFile);

        NewSecretEntry newSecretEntry = new NewSecretEntry(new SecretIdentifier(secretName), extractedSecret, state,
                notBefore, notAfter, extractComment(comment));
        secretsGroup.addVersion(newSecretEntry);
        newSecretEntry.bestEffortShred();
    }

    private Optional<Comment> extractComment(String comment) {
        if (comment != null && !comment.equals("")) {
            return Optional.of(new Comment(comment));
        } else {
            return Optional.empty();
        }
    }

    private SecretValue extractSecretValueOrThrow(boolean valueFromStdin, String generate, String valueFile) {
        String secretValue = "";
        int sum = booleanIfExists(valueFromStdin) + booleanIfExists(generate) + booleanIfExists(valueFile);

        SecretType secretType = SecretType.OPAQUE;

        if (sum == 0) {
            throw new RuntimeException("You must specify either --value-from-stdin, --value-from-file or --generate-value");
        }
        if (sum > 1) {
            throw new RuntimeException("You must specify one and only one of --value-from-stdin, --value-from-file and --generate-value");
        }

        if (generate != null) {
            secretValue = Base64.encodeAsString(randomGenerator.generateRandom(extractGenerate(generate)));
        }

        if (valueFile != null) {
            return SecretValueConverter.inferEncoding(extractValueFromFile(valueFile), secretType);
        }

        if (valueFromStdin) {
            return SecretValueConverter.inferEncoding(fromStdin(), secretType);
        }

        return new SecretValue(secretValue, secretType);
    }

    private byte[] fromStdin() {
        try {

            InputStream inputStream = System.in;
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
            if (!inputReader.ready()) {
                // Interactive
                char[] secretValue = System.console().readPassword("Enter secret value:");

                if (secretValue == null) {
                    throw new IllegalArgumentException("A secret value must be specified");
                }
                return asBytes(secretValue);
            } else {
                // Piped in
                return IOUtils.toByteArray(inputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read secret value from stdin", e);
        }
    }

    private static byte[] extractValueFromFile(String valueFile) {
        try {
            return Files.readAllBytes(Paths.get(valueFile));
        } catch (NoSuchFileException e) {
            throw new RuntimeException(String.format("Failed to read secret value from file '%s'. The file does not exists.", valueFile), e);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to read secret value from file '%s'", valueFile), e);
        }
    }

    private byte[] asBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);

        BestEffortShredder.shred(chars);

        return byteBuffer.array();
    }

    private static int booleanIfExists(String value) {
        return (value != null) ? 1 : 0;
    }

    private static int booleanIfExists(boolean value) {
        return value ? 1 : 0;
    }

    // TODO: return same data as in SDK
    public void setMetadata(String secretName, String versionString, String stateName, String comment) {
        long version = parseVersion(versionString);
        Optional<State> state = (stateName != null) ? Optional.of(State.fromString(stateName)) : Optional.empty();

        SecretMetadata secretMetadata = new SecretMetadata(new SecretIdentifier(secretName), version, state, Optional.empty(), Optional.empty(), extractComment(comment).map(Optional::of));
        secretsGroup.update(secretMetadata);
    }

    private long parseVersion(String version) {
        try {
            return Long.parseLong(version);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Version must be an integer, not '%s'", version));
        }
    }

    private static State extractEnabledDisabled(String state) {
        State extractedState = extractStateOrDefault(state);
        if (!(extractedState.equals(State.ENABLED) || extractedState.equals(State.DISABLED))) {
            throw new IllegalArgumentException("The initial state must be either 'enabled' or 'disabled'");
        }
        return extractedState;
    }

    private static State extractStateOrDefault(String state) {
        return (state != null) ? State.fromString(state) : State.ENABLED;
    }

    private static Optional<ZonedDateTime> extractDateIfNotNull(String date) {
        return (date != null) ? Optional.of(FormattedTimestamp.fromDate(date)) : Optional.empty();
    }

    public List<ListVersionsView> listSecretVersions(String secretName, boolean decrypt) {
        SecretIdentifier secretIdentifier = new SecretIdentifier(secretName);
        List<RawSecretEntry> entries = secretsGroup.stream()
                .filter(name.eq(secretIdentifier))
                .reverse()
                .toList();

        if (decrypt) {
            return entries.stream()
                    .map(e -> secretsGroup.decryptEvenIfNotActive(e, secretIdentifier, e.version))
                    .map(ListVersionsView::new)
                    .collect(Collectors.toList());
        } else {
            return entries.stream().map(ListVersionsView::new).collect(Collectors.toList());
        }
    }

    public Set<SecretIdentifierView> getIdentifiers() {
        return secretsGroup.identifiers().stream().map(SecretIdentifierView::new).collect(Collectors.toSet());
    }

    public void deleteSecret(String secretName, Boolean force) {
        SecretIdentifier secretIdentifier = new SecretIdentifier(secretName);
        String challenge = secretIdentifier.name;

        Confirmation.acceptOrExit(String.format("DANGER! Are you sure you want to permanently delete the secret '%s' in the secrets group '%s' in the region '%s'?"
                        + "\nIf yes, type the name of the secret as just shown: ",
                challenge, secretsGroupIdentifier.name, secretsGroupIdentifier.region.name), challenge, force);

        secretsGroup.delete(secretIdentifier);
    }

    public List<SecretEntryView> getActive(String secretName, String version, boolean all) {
        Optional<SecretIdentifier> secretIdentifier = (secretName != null) ?
                Optional.of(new SecretIdentifier(secretName)) :
                Optional.empty();
        Optional<Long> decodedVersion = decodeVersion(version);
        if (all) {
            if (secretIdentifier.isPresent()) {
                List<SecretEntry> entries = secretsGroup.getAllActiveVersions(secretIdentifier.get());
                return entries.stream().map(SecretEntryView::new).collect(Collectors.toList());
            } else {
                List<SecretEntry> entries = secretsGroup.getAllActiveVersions();
                return entries.stream().map(SecretEntryView::new).collect(Collectors.toList());
            }
        } else {
            if (!secretIdentifier.isPresent()) {
                throw new RuntimeException("You must specify --name, or --all");
            }
            if (!decodedVersion.isPresent()) {
                throw new RuntimeException("You must specify --version, or --all");
            }
            SecretEntry entry = secretsGroup.getActive(secretIdentifier.get(), decodedVersion.get()).get();
            return Collections.singletonList(new SecretEntryView(entry));
        }
    }

    public List<SecretEntryView> getLatestActive(String secretName, boolean all) {
        Optional<SecretIdentifier> secretIdentifier = (secretName != null) ?
                Optional.of(new SecretIdentifier(secretName)) :
                Optional.empty();
        if (all) {
            if (!secretIdentifier.isPresent()) {
                List<SecretEntry> entries = secretsGroup.getLatestActiveVersionOfAllSecrets();
                return entries.stream().map(SecretEntryView::new).collect(Collectors.toList());
            } else {
                throw new RuntimeException("You cannot specify --all and --name");
            }
        } else {
            if (secretIdentifier.isPresent()) {
                SecretEntry entry = secretsGroup.getLatestActiveVersion(secretIdentifier.get()).get();
                return Collections.singletonList(new SecretEntryView(entry));
            } else {
                throw new RuntimeException("You must specify --name, or --all");
            }
        }
    }

    public SecretEntryView get(String secretName, String version) {
        SecretIdentifier secretIdentifier = new SecretIdentifier(secretName);
        Optional<Long> decodedVersion = decodeVersion(version);

        Optional<SecretEntry> result = (decodedVersion.isPresent())
                    ? secretsGroup.getActive(secretIdentifier, decodedVersion.get())
                    : secretsGroup.getLatestActiveVersion(secretIdentifier);

        if (!result.isPresent()) {
            String errorMessage = (decodedVersion.isPresent())
                    ? String.format("No active secret named '%s' with version '%d'", secretIdentifier.name, decodedVersion.get())
                    : String.format("No active secret named '%s'", secretIdentifier.name);
            throw new DoesNotExistException(errorMessage);
        }

        return new SecretEntryView(result.get());
    }

    private Optional<Long> decodeVersion(String version) {
        return (version != null) ?
                Optional.of(parseVersion(version)) :
                Optional.empty();
    }

    @Override
    public void close() {
        secretsGroup.close();
    }
}
