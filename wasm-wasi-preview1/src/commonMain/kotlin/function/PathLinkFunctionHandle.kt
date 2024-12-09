/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import arrow.core.getOrElse
import arrow.core.raise.either
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.hardlink.Hardlink
import at.released.weh.filesystem.path.virtual.VirtualPath.Companion.isDirectoryRequest
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.foldToErrno
import at.released.weh.wasi.preview1.ext.readPathString
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Lookupflags
import at.released.weh.wasi.preview1.type.LookupflagsFlag.SYMLINK_FOLLOW
import at.released.weh.wasi.preview1.type.LookupflagsType
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory

public class PathLinkFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.PATH_LINK, host) {
    public fun execute(
        memory: Memory,
        @IntFileDescriptor oldFd: FileDescriptor,
        @LookupflagsType oldFlags: Lookupflags,
        @IntWasmPtr(Byte::class) oldPath: WasmPtr,
        oldPathSize: Int,
        @IntFileDescriptor newFd: FileDescriptor,
        @IntWasmPtr(Byte::class) newPath: WasmPtr,
        newPathSize: Int,
    ): Errno = either {
        val oldPathString = memory.readPathString(oldPath, oldPathSize).bind()
        val newPathString = memory.readPathString(newPath, newPathSize).bind()

        newPathString.let {
            if (it.isDirectoryRequest()) {
                raise(Errno.NOENT)
            }
        }
        if (oldFlags and SYMLINK_FOLLOW == SYMLINK_FOLLOW) {
            raise(Errno.NOENT) // SYMLINK_FOLLOW can not be used with path_link. See path_link.rs from WASI test suite.
        }

        host.fileSystem.execute(
            Hardlink,
            Hardlink(
                oldBaseDirectory = BaseDirectory.DirectoryFd(oldFd),
                oldPath = oldPathString,
                newBaseDirectory = BaseDirectory.DirectoryFd(newFd),
                newPath = newPathString,
                followSymlinks = false,
            ),
        ).foldToErrno()
    }.getOrElse { it }
}
