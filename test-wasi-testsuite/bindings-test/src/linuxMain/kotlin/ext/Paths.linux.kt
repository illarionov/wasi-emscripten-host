/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test.ext

import at.released.weh.wasi.bindings.test.ext.Paths.WASI_TESTSUITE_ROOT_ENV_KEY
import kotlinx.cinterop.toKString
import kotlinx.io.files.Path
import platform.posix.getenv

internal actual val wasiTestsuiteRoot: Path
    get() {
        val envPath = getenv(WASI_TESTSUITE_ROOT_ENV_KEY)?.toKString()
        if (envPath != null) {
            return Path(envPath)
        }
        error("WASI_TESTSUITE_ROOT not set")
    }
