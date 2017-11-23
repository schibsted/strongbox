/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk;

import com.schibsted.security.strongbox.sdk.exceptions.AlreadyExistsException;
import com.schibsted.security.strongbox.sdk.exceptions.DoesNotExistException;
import com.schibsted.security.strongbox.sdk.exceptions.PotentiallyMaliciousDataException;
import com.schibsted.security.strongbox.sdk.types.SRN;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.frontend.KVStream;
import com.schibsted.security.strongbox.sdk.types.NewSecretEntry;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretMetadata;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.active;
import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.name;
import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.version;

/**
 * A {@code SecretsGroup} is a logical collection of secrets. The interface does not expose how the secrets are
 * encrypted and stored, only how to add and retrieve them.
 *
 * While the {@link #stream() stream} is the the most flexible way to retrieve Secrets, we recommend
 * using the convenience methods like {@link #getActive(SecretIdentifier, long) getActive} whenever possible.
 *
 * The convenience methods use the concept of active. A secret entry is active if it is enabled, and now (in seconds)
 * are larger or equal to not before (if present), and less than or equal to not after (if present).
 *
 * @author stiankri
 * @author kvlees
 */
public interface SecretsGroup extends AutoCloseable {
    /**
     * Create a new Secret. This method will throw if the secret already exists.
     *
     * @param newSecretEntry the secret entry to create
     * @return {@code RawSecretEntry} of the newly created entry
     * @throws AlreadyExistsException if the secret already exists
     */
    RawSecretEntry create(NewSecretEntry newSecretEntry);

    /**
     * Add a version to an existing version. This method will throw if the secret does not already exist.
     * The version will automatically be incremented and throw if there is a race on the version.
     *
     * @param newSecretEntry the Secret entry to add
     * @return {@code RawSecretEntry} of the newly created entry
     * @throws DoesNotExistException if the secret does not exist
     * @throws AlreadyExistsException if there is a race on the automatically incremented version
     */
    RawSecretEntry addVersion(NewSecretEntry newSecretEntry);

    /**
     * Update the metadata for a Secret. Optional fields that are left empty will be left unchanged.
     *
     * @param secretMetadata metadata to update
     * @return {@code RawSecretEntry} of the updated secret entry
     * @throws DoesNotExistException if the secret does not exist
     */
    RawSecretEntry update(SecretMetadata secretMetadata);

    /**
     * This method is used to list the secret identifiers in the Secret Group.
     *
     * @return The set of {@code SecretIdentifier} that are present in this Secret Group
     */
    Set<SecretIdentifier> identifiers();

    /**
     * Delete all versions of the given secret. Individual secret entries cannot be deleted.
     * This method will succeed even if the secret does not exist.
     *
     * @param secretIdentifier the identifier of the secret to delete
     */
    void delete(SecretIdentifier secretIdentifier);

    /**
     * Get a key value stream of the entries, that can be used to filter and retrieve some subset of the secrets.
     * This is the most flexible way to retrieve secrets, and is used by all the convenience methods. We recommend
     * that you use the convenience methods like {@link #getActive(SecretIdentifier, long) getActive} whenever possible.
     *
     * @return the key value stream of the {@code RawSecretEntry} for this Secret Group
     */
    KVStream<RawSecretEntry> stream();

    /**
     * Decrypts a {@code RawSecretEntry} into a {@code SecretEntry}. This includes verifying the integrity
     * of all the returned data, both encrypted and decrypted.
     *
     * This method expects the secret to be active. If you do not need to verify that property, please see
     * {@code decryptEvenIfNotActive}.
     *
     * Please see the top of the class for the definition of active.
     *
     * @param rawSecretEntry The {@code RawSecretEntry} to be decrypted and verified
     * @param expectedSecretIdentifier The @{code SecretIdentifier} you are expecting to decrypt (to verify integrity)
     * @param expectedVersion The version you are expecting to decrypt (to verify integrity)
     * @return The decrypted {@code SecretEntry}
     * @throws PotentiallyMaliciousDataException if the integrity cannot be verified
     */
    SecretEntry decrypt(RawSecretEntry rawSecretEntry, SecretIdentifier expectedSecretIdentifier, long expectedVersion);

    /**
     * Decrypts a {@code RawSecretEntry} into a {@code SecretEntry}. This includes verifying the integrity
     * of all the returned data, both encrypted and decrypted, but not if the secret is active.
     *
     * Please see the top of the class for the definition of active.
     *
     * @param rawSecretEntry The {@code RawSecretEntry} to be decrypted and verified
     * @param expectedSecretIdentifier The @{code SecretIdentifier} you are expecting to decrypt (to verify integrity)
     * @param expectedVersion The version you are expecting to decrypt (to verify integrity)
     * @return The decrypted {@code SecretEntry}
     * @throws PotentiallyMaliciousDataException if the integrity cannot be verified
     */
    SecretEntry decryptEvenIfNotActive(RawSecretEntry rawSecretEntry, SecretIdentifier expectedSecretIdentifier, long expectedVersion);

    /**
     * Returns a globally unique identifier of the particular secret.
     * @param secretIdentifier identifier of the secret to get SRN for
     * @return The {@code SRN} of the given secret
     */
    SRN srn(SecretIdentifier secretIdentifier);

    /**
     * Convenience method to get the latest active version of a secret, i.e. the entry with the highest
     * version number that is active.
     *
     * If you know which version you want, we recommend to use {@link #getActive(SecretIdentifier, long)},
     * as this offers 'pinning' of the version.
     *
     * Please see the top of the class for the definition of active.
     *
     * @param secretIdentifier identifier of the secret to retrieve
     * @return the {@code SecretEntry} requested
     * @throws PotentiallyMaliciousDataException if the integrity of the data returned is compromised
     */
    default Optional<SecretEntry> getLatestActiveVersion(SecretIdentifier secretIdentifier) {
        Optional<RawSecretEntry> entry = stream()
                .filter(name.eq(secretIdentifier))
                .filter(active())
                .reverse()
                .findFirst();
        return entry.isPresent()
                ? Optional.of(decrypt(entry.get(), secretIdentifier, entry.get().version))
                : Optional.empty();
    }

    /**
     * Get a specific version of a secret. This is the most secure in that even a untrusted storage backend
     * cannot return a different version (but otherwise legal) of the secret, without us being able to detect it.
     *
     * Please see the top of the class for the definition of active.
     *
     * @param secretIdentifier identifier of the secret to retrieve
     * @param targetVersion version of the entry to retrieve
     * @return the {@code SecretEntry} requested
     * @throws PotentiallyMaliciousDataException if the integrity of the data returned is compromised
     */
    default Optional<SecretEntry> getActive(SecretIdentifier secretIdentifier, long targetVersion) {
        List<RawSecretEntry> entry = stream()
                .filter(name.eq(secretIdentifier).AND(version.eq(targetVersion)))
                .filter(active())
                .toList();

        if (entry.size() > 1) {
            throw new PotentiallyMaliciousDataException(String.format(
                    "Internal corruption: more than one entry for secret '%s' version '%d'",
                    secretIdentifier.name, targetVersion));
        }

        return entry.size() == 1
                ? Optional.of(decrypt(entry.get(0), secretIdentifier, targetVersion))
                : Optional.empty();
    }

    /**
     * Get all active versions of a secret.
     *
     * Please see the top of the class for the definition of active.
     *
     * @param secretIdentifier identifier of the secret to retrieve
     * @return a list of {@code SecretEntry} matching the request
     * @throws PotentiallyMaliciousDataException if the integrity of the data returned is compromised
     */
    default List<SecretEntry> getAllActiveVersions(SecretIdentifier secretIdentifier) {
        return stream()
                .filter(name.eq(secretIdentifier))
                .filter(active())
                .toJavaStream()
                .map(e -> decrypt(e, secretIdentifier, e.version))
                .collect(Collectors.toList());
    }

    /**
     * Get the latest active version of all secrets, i.e. for all secrets, the entry with the highest version
     * that is active.
     *
     * Please see the top of the class for the definition of active.
     *
     * @return a list of {@code SecretEntry} matching the request
     * @throws PotentiallyMaliciousDataException if the integrity of the data returned is compromised
     */
    default List<SecretEntry> getLatestActiveVersionOfAllSecrets() {
        return stream()
                .filter(active())
                .reverse()
                .uniquePrimaryKey()
                .toJavaStream()
                .map(e -> decrypt(e, e.secretIdentifier, e.version))
                .collect(Collectors.toList());
    }

    /**
     * Get all entries that are active for all secrets.
     *
     * Please see the top of the class for the definition of active.
     *
     * @return a list of {@code SecretEntry} matching the request
     * @throws PotentiallyMaliciousDataException if the integrity of the data returned is compromised
     */
    default List<SecretEntry> getAllActiveVersions() {
        return stream()
                .filter(active())
                .toJavaStream()
                .map(e -> decrypt(e, e.secretIdentifier, e.version))
                .collect(Collectors.toList());
    }

    /**
     * Certain Secret Groups, e.g. if backed by a file, requires the Secret Group to be closed for changes to
     * take effect. To avoid making the user of this class be aware of the distinction, we require all
     * implementations to be closed.
     */
    @Override
    void close();
}
