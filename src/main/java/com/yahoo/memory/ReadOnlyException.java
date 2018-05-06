/*
 * Copyright 2015-16, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * The exception thrown when attempting to write into a read-only Resource.
 *
 * @author Praveenkumar Venkatesan
 */
public class ReadOnlyException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public ReadOnlyException(final String message) {
      super(message);
    }
}

