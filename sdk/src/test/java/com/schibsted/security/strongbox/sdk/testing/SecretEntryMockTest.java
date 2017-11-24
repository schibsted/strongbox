/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.testing;

import com.schibsted.security.strongbox.sdk.types.Comment;
import com.schibsted.security.strongbox.sdk.types.SecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretType;
import com.schibsted.security.strongbox.sdk.types.SecretValue;
import com.schibsted.security.strongbox.sdk.types.State;
import com.schibsted.security.strongbox.sdk.types.UserAlias;
import com.schibsted.security.strongbox.sdk.types.UserData;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

/**
 * @author stiankri
 */
public class SecretEntryMockTest {
    @Test
    public void identifier_and_secret() {
        SecretIdentifier secretIdentifier = new SecretIdentifier("mySecret");
        SecretValue secretValue = new SecretValue("1234", SecretType.OPAQUE);

        SecretEntry secretEntryMock = SecretEntryMock.builder()
                .secretIdentifier(secretIdentifier)
                .secretValue(secretValue).build();

        assertThat(secretEntryMock.secretIdentifier, is(secretIdentifier));
        assertThat(secretEntryMock.secretValue, is(secretValue));

        assertThat(secretEntryMock.version, is(0L));
        assertThat(secretEntryMock.created, is(nullValue()));
        assertThat(secretEntryMock.modified, is(nullValue()));
        assertThat(secretEntryMock.createdBy, is(Optional.empty()));
        assertThat(secretEntryMock.modifiedBy, is(Optional.empty()));
        assertThat(secretEntryMock.state, is(nullValue()));
        assertThat(secretEntryMock.notBefore, is(Optional.empty()));
        assertThat(secretEntryMock.notAfter, is(Optional.empty()));
        assertThat(secretEntryMock.comment, is(Optional.empty()));
        assertThat(secretEntryMock.userData, is(Optional.empty()));
    }

    @Test
    public void identifier_and_secret_string() {
        String secretIdentifier = "mySecret";
        String secretValue = "1234";

        SecretEntry secretEntryMock = SecretEntryMock.builder()
                .secretIdentifier(secretIdentifier)
                .secretValue(secretValue).build();

        assertThat(secretEntryMock.secretIdentifier, is(new SecretIdentifier(secretIdentifier)));
        assertThat(secretEntryMock.secretValue, is(new SecretValue(secretValue, SecretType.OPAQUE)));
    }

    @Test
    public void all() {
        SecretIdentifier secretIdentifier = new SecretIdentifier("mySecret");
        SecretValue secretValue = new SecretValue("1234", SecretType.OPAQUE);
        long version = 2L;

        ZonedDateTime created = ZonedDateTime.of(2017, 1, 1, 1, 1, 1, 1, ZoneId.of("UTC"));
        ZonedDateTime modified = ZonedDateTime.of(2017, 2, 1, 1, 1, 1, 1, ZoneId.of("UTC"));

        State state = State.ENABLED;
        UserAlias createdBy = new UserAlias("john");
        UserAlias modifiedBy = new UserAlias("jane");

        ZonedDateTime notBefore = ZonedDateTime.of(2017, 3, 1, 1, 1, 1, 1, ZoneId.of("UTC"));
        ZonedDateTime notAfter = ZonedDateTime.of(2017, 4, 1, 1, 1, 1, 1, ZoneId.of("UTC"));

        Comment comment = new Comment("some comment");
        UserData userData = new UserData("some userdata".getBytes());

        SecretEntry secretEntryMock = SecretEntryMock.builder()
                .secretIdentifier(secretIdentifier)
                .version(version)
                .secretValue(secretValue)
                .created(created)
                .modified(modified)
                .createdBy(createdBy)
                .modifiedBy(modifiedBy)
                .state(state)
                .notBefore(notBefore)
                .notAfter(notAfter)
                .comment(comment)
                .userData(userData).build();

        assertThat(secretEntryMock.secretIdentifier, is(secretIdentifier));
        assertThat(secretEntryMock.version, is(version));
        assertThat(secretEntryMock.secretValue, is(secretValue));
        assertThat(secretEntryMock.created, is(created));
        assertThat(secretEntryMock.modified, is(modified));
        assertThat(secretEntryMock.createdBy, is(Optional.of(createdBy)));
        assertThat(secretEntryMock.modifiedBy, is(Optional.of(modifiedBy)));
        assertThat(secretEntryMock.state, is(state));
        assertThat(secretEntryMock.notBefore, is(Optional.of(notBefore)));
        assertThat(secretEntryMock.notAfter, is(Optional.of(notAfter)));
        assertThat(secretEntryMock.comment, is(Optional.of(comment)));
        assertThat(secretEntryMock.userData, is(Optional.of(userData)));
    }
}
