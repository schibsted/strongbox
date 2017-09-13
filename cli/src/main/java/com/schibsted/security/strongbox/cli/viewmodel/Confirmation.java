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
