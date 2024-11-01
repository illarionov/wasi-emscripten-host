/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import arrow.core.getOrElse
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.WasiFdFlagsMapper
import at.released.weh.wasi.preview1.ext.WasiOpenFlagsMapper
import at.released.weh.wasi.preview1.ext.WasiRightsMapper
import at.released.weh.wasi.preview1.ext.foldToErrno
import at.released.weh.wasi.preview1.ext.readPathString
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Fdflags
import at.released.weh.wasi.preview1.type.FdflagsType
import at.released.weh.wasi.preview1.type.Lookupflags
import at.released.weh.wasi.preview1.type.LookupflagsType
import at.released.weh.wasi.preview1.type.Oflags
import at.released.weh.wasi.preview1.type.OflagsType
import at.released.weh.wasi.preview1.type.Rights
import at.released.weh.wasi.preview1.type.RightsType
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory

/**
 * Handler for the [WasiPreview1HostFunction.PATH_OPEN]: open a file or directory.
 */
public class PathOpenFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.PATH_OPEN, host) {
    public fun execute(
        memory: Memory,
        @IntFileDescriptor fd: FileDescriptor,
        @LookupflagsType dirFlags: Lookupflags,
        @IntWasmPtr(Byte::class) path: WasmPtr,
        pathSize: Int,
        @OflagsType oflags: Oflags,
        @RightsType rights: Rights,
        @RightsType rightsInheriting: Rights,
        @FdflagsType fdflags: Fdflags,
        @IntWasmPtr(FileDescriptor::class) expectedFdAddr: WasmPtr,
    ): Errno {
        val pathString = memory.readPathString(path, pathSize).getOrElse {
            return it
        }

        return host.fileSystem.execute(
            Open,
            Open(
                path = pathString,
                baseDirectory = BaseDirectory.DirectoryFd(fd),
                openFlags = WasiOpenFlagsMapper.getFsOpenFlags(oflags, rights, dirFlags),
                fdFlags = WasiFdFlagsMapper.getFsFdlags(fdflags),
                rights = Open.Rights(
                    rights = WasiRightsMapper.getFsRights(rights),
                    rightsInheriting = WasiRightsMapper.getFsRights(rightsInheriting),
                ),
            ),
        ).onRight {
            memory.writeI32(expectedFdAddr, it)
        }.foldToErrno()
    }
}
