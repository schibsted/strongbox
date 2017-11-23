/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.cli.viewmodel;

import java.util.Scanner;

/**
 * For dangerous operations, ask the user to type out e.g. the resource name that is about to be deleted
 *
 * @author stiankri
 */
public class Confirmation {
    public static void acceptOrExit(String message, String challenge, Boolean force) {
        if (force != null && force) {
            return; // do not ask for confirmation
        }

        System.out.print(message);

        Scanner scanner = new Scanner(System.in);
        String response = scanner.next();
        if (!response.equals(challenge)) {
            System.out.println(String.format("You entered '%s', but '%s' was expected: aborting...", response, challenge));
            System.exit(1);
        }
    }
}
