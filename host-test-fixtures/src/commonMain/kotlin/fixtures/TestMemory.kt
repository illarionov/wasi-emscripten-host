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
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.memory.DefaultWasiMemoryReader
import at.released.weh.host.base.memory.DefaultWasiMemoryWriter
import at.released.weh.host.base.memory.Memory
import at.released.weh.test.logger.TestLogger
import kotlinx.io.RawSink
import kotlinx.io.RawSource

public open class TestMemory(
    public val size: Int = 1_048_576,
    public val fileSystem: FileSystem = TestFileSystem(),
    public val logger: Logger = TestLogger(),
) : Memory {
    public val bytes: ByteArray = ByteArray(size) { 0xdc.toByte() }
    public val memoryReader: DefaultWasiMemoryReader = DefaultWasiMemoryReader(this, fileSystem)
    public val memoryWriter: DefaultWasiMemoryWriter = DefaultWasiMemoryWriter(this, fileSystem)

    public fun fill(value: Byte) {
        bytes.fill(value)
    }

    override fun readI8(addr: WasmPtr<*>): Byte {
        return bytes[addr.addr]
    }

    override fun readI32(addr: WasmPtr<*>): Int = (bytes[addr.addr].toInt() and 0xff) or
            (bytes[addr.addr + 1].toInt() and 0xff shl 8) or
            (bytes[addr.addr + 2].toInt() and 0xff shl 16) or
            (bytes[addr.addr + 3].toInt() and 0xff shl 24)

    override fun readI64(addr: WasmPtr<*>): Long = (bytes[addr.addr].toLong() and 0xffL) or
            (bytes[addr.addr + 1].toLong() and 0xffL shl 8) or
            (bytes[addr.addr + 2].toLong() and 0xffL shl 16) or
            (bytes[addr.addr + 3].toLong() and 0xffL shl 24) or
            (bytes[addr.addr + 4].toLong() and 0xffL shl 32) or
            (bytes[addr.addr + 5].toLong() and 0xffL shl 40) or
            (bytes[addr.addr + 6].toLong() and 0xffL shl 48) or
            (bytes[addr.addr + 7].toLong() and 0xffL shl 56)

    override fun source(fromAddr: WasmPtr<*>, toAddrExclusive: WasmPtr<*>): RawSource {
        return TestMemoryRawSource(this, fromAddr, toAddrExclusive)
    }

    override fun writeI8(addr: WasmPtr<*>, data: Byte) {
        bytes[addr.addr] = data
    }

    override fun writeI32(addr: WasmPtr<*>, data: Int) {
        bytes[addr.addr] = (data and 0xff).toByte()
        bytes[addr.addr + 1] = (data ushr 8 and 0xff).toByte()
        bytes[addr.addr + 2] = (data ushr 16 and 0xff).toByte()
        bytes[addr.addr + 3] = (data ushr 24 and 0xff).toByte()
    }

    override fun writeI64(addr: WasmPtr<*>, data: Long) {
        bytes[addr.addr] = (data and 0xffL).toByte()
        bytes[addr.addr + 1] = (data ushr 8 and 0xff).toByte()
        bytes[addr.addr + 2] = (data ushr 16 and 0xff).toByte()
        bytes[addr.addr + 3] = (data ushr 24 and 0xff).toByte()
        bytes[addr.addr + 4] = (data ushr 32 and 0xff).toByte()
        bytes[addr.addr + 5] = (data ushr 40 and 0xff).toByte()
        bytes[addr.addr + 6] = (data ushr 48 and 0xff).toByte()
        bytes[addr.addr + 7] = (data ushr 56 and 0xff).toByte()
    }

    override fun sink(fromAddr: WasmPtr<*>, toAddrExclusive: WasmPtr<*>): RawSink {
        return TestMemoryRawSink(this, fromAddr, toAddrExclusive)
    }

    override fun toString(): String {
        return "TestMemory(size=$size)"
    }
}
