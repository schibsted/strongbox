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
