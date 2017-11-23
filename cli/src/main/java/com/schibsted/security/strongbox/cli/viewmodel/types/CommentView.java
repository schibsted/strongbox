/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.cli.viewmodel.types;

import com.schibsted.security.strongbox.sdk.types.Comment;

/**
 * @author stiankri
 */
public class CommentView {
    public final String comment;

    CommentView(Comment comment) {
        this.comment = comment.asString();
    }

    @Override
    public String toString() {
        return comment;
    }
}
