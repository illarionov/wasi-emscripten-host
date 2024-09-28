/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test.ext

import at.released.weh.wasi.bindings.test.ext.Paths.WASI_TESTSUITE_ROOT_ENV_KEY
import kotlinx.io.files.Path

internal actual val wasiTestsuiteRoot: Path
    get() {
        val envPath = System.getenv()[WASI_TESTSUITE_ROOT_ENV_KEY]?.let(::Path)
        if (envPath != null) {
            return envPath
        }
        return Path(System.getProperty("user.dir"), "../wasi-testsuite/tests")
    }
