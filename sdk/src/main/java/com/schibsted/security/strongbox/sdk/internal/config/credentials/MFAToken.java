/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.config.credentials;

import com.amazonaws.services.identitymanagement.model.InvalidInputException;

import java.io.Console;
import java.util.Scanner;
import java.util.function.Supplier;

/**
 * @author stiankri
 */
public class MFAToken {
    public final String value;

    public MFAToken(String value) {
        this.value = value;
    }

    public static Supplier<MFAToken> defaultMFATokenSupplier() {
        return () -> {
            Console console = System.console();

            String token = null;
            if (console != null) {
                char[] secretValue = console.readPassword("Enter MFA code: ");

                if (secretValue != null) {
                    token = new String(secretValue);
                }
            } else {
                // probably running in an IDE; fallback to plaintext
                System.out.print("Enter MFA code: ");
                Scanner scanner = new Scanner(System.in);
                token = scanner.nextLine();
            }

            if (token == null || token.isEmpty()) {
                throw new InvalidInputException("A non-empty MFA code must be entered");
            }
            return new MFAToken(token);
        };
    }



    public static Supplier<MFAToken> missingMFATokenSupplier() {
        return () -> {
            throw new RuntimeException("No MFA provider specified");
        };
    }
}
