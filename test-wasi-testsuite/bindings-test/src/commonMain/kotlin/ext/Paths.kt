/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test.ext

import kotlinx.io.files.Path

internal expect val wasiTestsuiteRoot: Path

internal object Paths {
    internal const val WASI_TESTSUITE_ROOT_ENV_KEY = "WASI_TESTSUITE_ROOT"
    val assemblyscriptTests: Path = Path(wasiTestsuiteRoot, "assemblyscript/testsuite")
    val cTests: Path = Path(wasiTestsuiteRoot, "c/testsuite")
    val rustTests: Path = Path(wasiTestsuiteRoot, "rust/testsuite")
}
