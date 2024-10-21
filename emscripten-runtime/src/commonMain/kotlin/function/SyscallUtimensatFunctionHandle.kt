/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.function

import at.released.weh.emcripten.runtime.EmscriptenHostFunction.SYSCALL_UTIMENSAT
import at.released.weh.emcripten.runtime.ext.fromRawDirFd
import at.released.weh.emcripten.runtime.ext.negativeErrnoCode
import at.released.weh.emcripten.runtime.include.Fcntl.AT_SYMLINK_NOFOLLOW
import at.released.weh.emcripten.runtime.include.sys.SysStat.UTIME_NOW
import at.released.weh.emcripten.runtime.include.sys.SysStat.UTIME_OMIT
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.op.settimestamp.SetTimestamp
import at.released.weh.host.EmbedderHost
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.WasmPtrUtil.ptrIsNull
import at.released.weh.wasm.core.memory.ReadOnlyMemory
import at.released.weh.wasm.core.memory.readNullTerminatedString
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

public class SyscallUtimensatFunctionHandle(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle(SYSCALL_UTIMENSAT, host) {
    public fun execute(
        memory: ReadOnlyMemory,
        rawDirFd: Int,
        @IntWasmPtr(Byte::class) pathnamePtr: WasmPtr,
        @IntWasmPtr(Byte::class) times: WasmPtr,
        flags: Int,
    ): Int {
        val baseDirectory = BaseDirectory.fromRawDirFd(rawDirFd)
        val folowSymlinks: Boolean = (flags and AT_SYMLINK_NOFOLLOW) == 0
        val path = memory.readNullTerminatedString(pathnamePtr)
        val atimeNs: Long?
        val mtimeNs: Long?
        @Suppress("MagicNumber")
        if (ptrIsNull(times)) {
            atimeNs = host.clock.getCurrentTimeEpochNanoseconds()
            mtimeNs = atimeNs
        } else {
            val atimeSeconds = memory.readI64(times)
            val atimeNanoseconds = memory.readI64(times + 8)

            val mtimeSeconds = memory.readI64(times + 16)
            val mtimeNanoseconds = memory.readI64(times + 24)

            val now: Long by lazy(NONE) { host.clock.getCurrentTimeEpochNanoseconds() }
            atimeNs = parseTimeNanoseconds(atimeSeconds, atimeNanoseconds) { now }
            mtimeNs = parseTimeNanoseconds(mtimeSeconds, mtimeNanoseconds) { now }
        }
        return host.fileSystem.execute(
            operation = SetTimestamp,
            input = SetTimestamp(
                path = path,
                baseDirectory = baseDirectory,
                atimeNanoseconds = atimeNs,
                mtimeNanoseconds = mtimeNs,
                followSymlinks = folowSymlinks,
            ),
        ).negativeErrnoCode()
    }

    private fun parseTimeNanoseconds(
        seconds: Long,
        nanoseconds: Long,
        now: () -> Long,
    ): Long? = when (nanoseconds) {
        UTIME_NOW.toLong() -> now()
        UTIME_OMIT.toLong() -> null
        else -> (seconds.seconds + nanoseconds.nanoseconds).inWholeNanoseconds
    }
}
