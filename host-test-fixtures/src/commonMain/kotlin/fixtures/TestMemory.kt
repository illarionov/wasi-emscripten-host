/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MagicNumber")

package at.released.weh.host.test.fixtures

import at.released.weh.common.api.Logger
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.test.fixtures.TestFileSystem
import at.released.weh.test.logger.TestLogger
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import kotlinx.io.RawSink
import kotlinx.io.RawSource

public open class TestMemory(
    public val size: Int = 1_048_576,
    public val fileSystem: FileSystem = TestFileSystem(),
    public val logger: Logger = TestLogger(),
) : Memory {
    public val bytes: ByteArray = ByteArray(size) { 0xdc.toByte() }

    public fun fill(value: Byte) {
        bytes.fill(value)
    }

    override fun readI8(@IntWasmPtr addr: WasmPtr): Byte {
        return bytes[addr]
    }

    override fun readI32(@IntWasmPtr addr: WasmPtr): Int = (bytes[addr].toInt() and 0xff) or
            (bytes[addr + 1].toInt() and 0xff shl 8) or
            (bytes[addr + 2].toInt() and 0xff shl 16) or
            (bytes[addr + 3].toInt() and 0xff shl 24)

    override fun readI64(@IntWasmPtr addr: WasmPtr): Long = (bytes[addr].toLong() and 0xffL) or
            (bytes[addr + 1].toLong() and 0xffL shl 8) or
            (bytes[addr + 2].toLong() and 0xffL shl 16) or
            (bytes[addr + 3].toLong() and 0xffL shl 24) or
            (bytes[addr + 4].toLong() and 0xffL shl 32) or
            (bytes[addr + 5].toLong() and 0xffL shl 40) or
            (bytes[addr + 6].toLong() and 0xffL shl 48) or
            (bytes[addr + 7].toLong() and 0xffL shl 56)

    override fun source(@IntWasmPtr fromAddr: WasmPtr, @IntWasmPtr toAddrExclusive: WasmPtr): RawSource {
        return TestMemoryRawSource(this, fromAddr, toAddrExclusive)
    }

    override fun writeI8(@IntWasmPtr addr: WasmPtr, data: Byte) {
        bytes[addr] = data
    }

    override fun writeI32(@IntWasmPtr addr: WasmPtr, data: Int) {
        bytes[addr] = (data and 0xff).toByte()
        bytes[addr + 1] = (data ushr 8 and 0xff).toByte()
        bytes[addr + 2] = (data ushr 16 and 0xff).toByte()
        bytes[addr + 3] = (data ushr 24 and 0xff).toByte()
    }

    override fun writeI64(@IntWasmPtr addr: WasmPtr, data: Long) {
        bytes[addr] = (data and 0xffL).toByte()
        bytes[addr + 1] = (data ushr 8 and 0xff).toByte()
        bytes[addr + 2] = (data ushr 16 and 0xff).toByte()
        bytes[addr + 3] = (data ushr 24 and 0xff).toByte()
        bytes[addr + 4] = (data ushr 32 and 0xff).toByte()
        bytes[addr + 5] = (data ushr 40 and 0xff).toByte()
        bytes[addr + 6] = (data ushr 48 and 0xff).toByte()
        bytes[addr + 7] = (data ushr 56 and 0xff).toByte()
    }

    override fun sink(@IntWasmPtr fromAddr: WasmPtr, @IntWasmPtr toAddrExclusive: WasmPtr): RawSink {
        return TestMemoryRawSink(this, fromAddr, toAddrExclusive)
    }

    override fun toString(): String {
        return "TestMemory(size=$size)"
    }
}
