/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.opencreate

import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.fdrights.FdRightsBlock
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.FdflagsType
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.op.FileSystemOperation
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.openFileFlagsToString
import at.released.weh.filesystem.path.virtual.VirtualPath

/**
 * Open or create a	file.
 *
 * The path to the file is specified by the [path]. If is relative, it will be resolved using the base directory
 * specified by the [baseDirectory] parameter.
 *
 * The [openFlags] argument may indicate the file is to be created if it does not exist (by specifying  the
 * [Fcntl.O_CREAT]	flag). In this case, file is created with mode [mode].
 */
public data class Open(
    public val path: VirtualPath,
    public val baseDirectory: BaseDirectory = CurrentWorkingDirectory,

    @OpenFileFlagsType
    public val openFlags: OpenFileFlags,

    @FdflagsType
    public val fdFlags: Fdflags,

    @FileMode
    public val mode: Int? = null,

    public val rights: FdRightsBlock? = null,
) {
    override fun toString(): String {
        return "Open(" +
                "path='$path', " +
                "baseDirectory=$baseDirectory, " +
                "openFlags=${openFileFlagsToString(openFlags)}, " +
                "fdFlags=0x${fdFlags.toString(16)}, " +
                "mode=${mode?.toString(8) ?: "null"}, " +
                "rights=$rights" +
                ")"
    }

    public companion object : FileSystemOperation<Open, OpenError, FileDescriptor> {
        override val tag: String = "open"
    }
}
