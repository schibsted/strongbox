/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Schibsted Products & Technology AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.schibsted.security.strongbox.sdk;

import com.schibsted.security.strongbox.sdk.exceptions.DoesNotExistException;
import com.schibsted.security.strongbox.sdk.types.Principal;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupInfo;

import java.util.Set;

/**
 * This class manages {@link SecretsGroup}s. It is intended to hide away the specifics of how encryption, storage
 * and access management is done.
 *
 * Most read-only applications will only use this class to {@link #get(SecretsGroupIdentifier) get} a {@link SecretsGroup},
 * and then do the rest of the operations on the returned {@link SecretsGroup} object.
 *
 * @author stiankri
 * @author kvlees
 */
public interface SecretsGroupManager {
    /**
     * Create a new secrets group. The underlying implementation is responsible for allocating
     * any resources it might need.
     *
     * @param group the identifier of the {@code SecretsGroup} to be created
     * @return information about the Secrets Group that was created
     */
    SecretsGroupInfo create(SecretsGroupIdentifier group);

    /**
     * Get an instance of the {@code SecretsGroup}. The underlying implementation is responsible
     * for tracking the necessary resources needed to construct the object.
     *
     * @param group the identifier of the {@code SecretsGroup} to be retrieved
     * @return the desired {@code SecretsGroup}
     * @throws DoesNotExistException if the {@code SecretsGroup} does not exist
     */
    SecretsGroup get(SecretsGroupIdentifier group);

    /**
     * List the identifiers that are under management.
     *
     * @return the set of {@code SecretsGroupIdentifier} that is under management
     */
    Set<SecretsGroupIdentifier> identifiers();

    /**
     * Get information about a {@code SecretsGroup}. This method is best effort,
     * and may return partial information. This is useful if debugging the underlying
     * resources of a {@code SecretsGroup}.
     *
     * @param group the identifier of the {@code SecretsGroup} to get information about
     * @return {@code SecretsGroupInfo} related to the {@code SecretsGroup}
     */
    SecretsGroupInfo info(SecretsGroupIdentifier group);

    /**
     * Delete a {@code SecretsGroup} and its underlying resources. This method will attempt to delete all
     * resources of the given (@code SecretsGroup}, and will simply ignore any resource that does not exists.
     *
     * @param group the identifier of the {@code SecretsGroup} to delete
     */
    void delete(SecretsGroupIdentifier group);

    /**
     * Attach the {@code Principal} to the {@code SecretsGroup} as an admin. This will give the
     * {@code Principal} full access to the {@code SecretsGroup}.
     *
     * @param group identifier of the {@code SecretsGroup} to attach the {@code principal} to
     * @param principal {@code Principal} to be attached
     */
    void attachAdmin(SecretsGroupIdentifier group, Principal principal);

    /**
     * Remove the {@code Principal}'s admin privileges from the {@code SecretsGroup}.
     *
     * @param group identifier of the {@code SecretsGroup} to detach {@code principal} from
     * @param principal {@code Principal} to be detached
     */
    void detachAdmin(SecretsGroupIdentifier group, Principal principal);

    /**
     * Attach the {@code Principal} to the {@code SecretsGroup} as a read-only user. This will give the
     * {@code Principal} read-only access to the {@code SecretsGroup} (i.e. ability to read secrets).
     *
     * @param group identifier of the {@code SecretsGroup} to attach the {@code principal} to
     * @param principal {@code Principal} to be attached
     */
    void attachReadOnly(SecretsGroupIdentifier group, Principal principal);

    /**
     * Remove the {@code Principal}'s read-only privileges from the {@code SecretsGroup}.
     *
     * @param group identifier of the {@code SecretsGroup} to detach the {@code principal} from
     * @param principal {@code Principal} to be detached
     */
    void detachReadOnly(SecretsGroupIdentifier group, Principal principal);
}
