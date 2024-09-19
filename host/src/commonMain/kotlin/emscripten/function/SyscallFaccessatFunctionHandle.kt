/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.function

import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.op.checkaccess.CheckAccess
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.ext.fromRawDirFd
import at.released.weh.host.ext.negativeErrnoCode
import at.released.weh.host.include.Fcntl
import at.released.weh.host.include.Fcntl.AT_EACCESS
import at.released.weh.host.include.Fcntl.AT_SYMLINK_NOFOLLOW
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.ReadOnlyMemory
import at.released.weh.wasm.core.memory.readNullTerminatedString

public class SyscallFaccessatFunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(EmscriptenHostFunction.SYSCALL_FACCESSAT, host) {
    public fun execute(
        memory: ReadOnlyMemory,
        rawDirFd: Int,
        @IntWasmPtr(Byte::class) pathnamePtr: WasmPtr,
        amode: Int,
        flags: Int,
    ): Int {
        val path = memory.readNullTerminatedString(pathnamePtr)
        return host.fileSystem.execute(
            CheckAccess,
            CheckAccess(
                path = path,
                baseDirectory = BaseDirectory.fromRawDirFd(rawDirFd),
                mode = rawModeToFileAccessibilityCheck(amode),
                useEffectiveUserId = flags and AT_EACCESS == AT_EACCESS,
                allowEmptyPath = false,
                followSymlinks = flags and AT_SYMLINK_NOFOLLOW != AT_SYMLINK_NOFOLLOW,
            ),
        ).negativeErrnoCode()
    }

    private companion object {
        fun rawModeToFileAccessibilityCheck(amode: Int): Set<FileAccessibilityCheck> {
            return buildSet {
                if (amode and Fcntl.R_OK == Fcntl.R_OK) {
                    add(FileAccessibilityCheck.READABLE)
                }
                if (amode and Fcntl.W_OK == Fcntl.W_OK) {
                    add(FileAccessibilityCheck.WRITEABLE)
                }
                if (amode and Fcntl.X_OK == Fcntl.X_OK) {
                    add(FileAccessibilityCheck.EXECUTABLE)
                }
            }
        }
    }
}
