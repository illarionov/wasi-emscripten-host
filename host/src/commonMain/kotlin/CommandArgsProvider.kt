/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host

/**
 * Provides command line arguments passed to an application.
 */
public fun interface CommandArgsProvider {
    /**
     * Returns the command line arguments of the application.
     * The first argument should be the "name" of the program.
     */
    public fun getCommandArgs(): List<String>
}
