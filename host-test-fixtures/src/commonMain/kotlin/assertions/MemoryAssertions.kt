/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.test.assertions

import assertk.Assert
import assertk.assertions.isEqualTo
import assertk.assertions.support.appendName
import at.released.weh.host.base.IntWasmPtr
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.memory.sourceWithMaxSize
import at.released.weh.host.test.fixtures.TestMemory
import kotlinx.io.buffered
import kotlinx.io.readByteArray

public fun Assert<TestMemory>.bytesAt(
    @IntWasmPtr(Byte::class) address: WasmPtr,
    size: Int,
): Assert<ByteArray> = transform(appendName("TestMemory{$address}", separator = ".")) { testMemory ->
    testMemory.sourceWithMaxSize(address, size).buffered().use {
        it.readByteArray(size)
    }
}

public fun Assert<TestMemory>.hasBytesAt(
    @IntWasmPtr(Byte::class) address: WasmPtr,
    expectedBytes: ByteArray,
): Unit = bytesAt(address, expectedBytes.size).isEqualTo(expectedBytes)

public fun Assert<TestMemory>.byteAt(
    @IntWasmPtr(Byte::class) address: WasmPtr,
): Assert<Byte> = transform(appendName("TestMemory{$address}", separator = ".")) {
    it.readI8(address)
}
