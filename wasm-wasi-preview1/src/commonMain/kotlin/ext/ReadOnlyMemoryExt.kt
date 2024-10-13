/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

import arrow.core.Either
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.ReadOnlyMemory
import at.released.weh.wasm.core.memory.sourceWithMaxSize
import kotlinx.io.EOFException
import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.bytestring.decodeToString
import kotlinx.io.readByteString

internal fun ReadOnlyMemory.readPathString(
    @IntWasmPtr(Byte::class) path: WasmPtr,
    pathSize: Int,
): Either<Errno, String> = Either.catch {
    // XXX need to validate UTF-8?
    sourceWithMaxSize(path, pathSize).buffered().use {
        it.readByteString(pathSize).decodeToString()
    }
}.mapLeft {
    when (it) {
        is IllegalArgumentException, is IllegalStateException -> Errno.INVAL
        is IOException -> Errno.IO
        is EOFException -> Errno.IO
        else -> Errno.FAULT
    }
}
