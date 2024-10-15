/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.util.classname

import com.squareup.kotlinpoet.ClassName

internal object WehWasiPreview1ClassName {
    const val PACKAGE = "at.released.weh.wasi.preview1"
    const val MEMORY_PACKAGE = "$PACKAGE.memory"
    const val FUNCTION_PACKAGE = "$PACKAGE.function"
    const val TYPE_PACKAGE = "$PACKAGE.type"
    val WASI_MEMORY_READER = ClassName(MEMORY_PACKAGE, "WasiMemoryReader")
    val WASI_MEMORY_WRITER = ClassName(MEMORY_PACKAGE, "WasiMemoryWriter")
    val ERRNO = ClassName(TYPE_PACKAGE, "Errno")
}
