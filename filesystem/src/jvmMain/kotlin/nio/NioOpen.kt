/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.ext.asFileAttribute
import at.released.weh.filesystem.ext.asLinkOptions
import at.released.weh.filesystem.ext.fileModeToPosixFilePermissions
import at.released.weh.filesystem.fdresource.nio.nioOpenFile
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.internal.op.checkOpenFlags
import at.released.weh.filesystem.model.FdFlag
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.FdflagsType
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError
import at.released.weh.filesystem.nio.cwd.toCommonError
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.op.opencreate.OpenFileFlags
import at.released.weh.filesystem.op.opencreate.OpenFileFlagsType
import at.released.weh.filesystem.preopened.VirtualPath
import com.sun.nio.file.ExtendedOpenOption
import java.nio.file.LinkOption
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.FileAttribute
import kotlin.io.path.isDirectory

internal class NioOpen(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<Open, OpenError, FileDescriptor> {
    override fun invoke(input: Open): Either<OpenError, FileDescriptor> = either {
        val virtualPath = input.path // XXX needs better relative path checking
        val followSymlinks = input.openFlags and OpenFileFlag.O_NOFOLLOW != OpenFileFlag.O_NOFOLLOW
        val path: Path = fsState.pathResolver.resolve(
            input.path,
            input.baseDirectory,
            allowEmptyPath = true,
            followSymlinks = followSymlinks,
        )
            .mapLeft(ResolvePathError::toCommonError)
            .bind()

        val openOptionsResult = getOpenOptions(input.openFlags, input.fdFlags)
        if (openOptionsResult.notImplementedFlags != 0U) {
            raise(
                InvalidArgument(
                    "Flags 0x${openOptionsResult.notImplementedFlags.toString(16)} not implemented",
                ),
            )
        }
        val fileAttributes: Array<FileAttribute<*>> = input.mode?.let {
            arrayOf(it.fileModeToPosixFilePermissions().asFileAttribute())
        } ?: emptyArray()

        checkOpenFlags(input).bind()

        if (path.isDirectory()) {
            return openDirectory(fsState, path, virtualPath)
        }

        if (input.openFlags and OpenFileFlag.O_DIRECTORY == OpenFileFlag.O_DIRECTORY) {
            raise(NotDirectory("Path is not a directory"))
        }

        return openCreateFile(fsState, path, openOptionsResult.options, fileAttributes, input.fdFlags)
    }
}

private fun openCreateFile(
    fsState: NioFileSystemState,
    path: Path,
    options: Set<OpenOption>,
    fileAttributes: Array<FileAttribute<*>>,
    @FdflagsType originalFdflags: Fdflags,
): Either<OpenError, FileDescriptor> = fsState.addFile(path, fdflags = originalFdflags) { _ ->
    nioOpenFile(path, options, fileAttributes)
}.map { it.first }

private fun openDirectory(
    fsState: NioFileSystemState,
    path: Path,
    virtualPath: VirtualPath,
    followSymlinks: Boolean = true,
): Either<OpenError, FileDescriptor> = fsState.addDirectory(virtualPath) { _ ->
    val linkOptions: Array<LinkOption> = asLinkOptions(followSymlinks)
    @Suppress("SpreadOperator")
    if (path.isDirectory(*linkOptions)) {
        path.right()
    } else {
        NotDirectory("$virtualPath is not a directory").left()
    }
}.map { it.first }

@Suppress("CyclomaticComplexMethod", "LOCAL_VARIABLE_EARLY_DECLARATION", "LongMethod")
private fun getOpenOptions(
    @OpenFileFlagsType flags: OpenFileFlags,
    @FdflagsType fdFlags: Fdflags,
): GetOpenOptionsResult {
    val options: MutableSet<OpenOption> = mutableSetOf()
    var ignoredFlags = 0U
    var notImplementedFlags = 0U

    when (val mode = flags and OpenFileFlag.O_ACCMODE) {
        OpenFileFlag.O_RDONLY -> options += StandardOpenOption.READ
        OpenFileFlag.O_WRONLY -> options += StandardOpenOption.WRITE
        OpenFileFlag.O_RDWR -> {
            options += StandardOpenOption.READ
            options += StandardOpenOption.WRITE
        }
        else -> error("Unexpected open mode: 0x${mode.toString(16)}")
    }

    if (fdFlags and FdFlag.FD_APPEND != 0) {
        // This flag is ignored. We do not use NIO's APPEND mode because it restricts writing to arbitrary
        // positions within the file, which is necessary for implementing the fd_write function in WASI.
        ignoredFlags += FdFlag.FD_APPEND.toUInt()
    }

    if (flags and OpenFileFlag.O_CREAT != 0) {
        options += if (flags and OpenFileFlag.O_EXCL != 0) {
            StandardOpenOption.CREATE_NEW
        } else {
            StandardOpenOption.CREATE
        }
        options += StandardOpenOption.WRITE
    }

    if (flags and OpenFileFlag.O_TRUNC != 0) {
        options += StandardOpenOption.TRUNCATE_EXISTING
    }

    if (fdFlags and FdFlag.FD_NONBLOCK != 0) {
        notImplementedFlags = notImplementedFlags and FdFlag.FD_NONBLOCK.toUInt()
    }

    if (flags and OpenFileFlag.O_ASYNC != 0) {
        notImplementedFlags = notImplementedFlags and OpenFileFlag.O_ASYNC.toUInt()
    }

    if (fdFlags and (FdFlag.FD_DSYNC or FdFlag.FD_SYNC) != 0) {
        options += StandardOpenOption.SYNC
    }

    if (flags and OpenFileFlag.O_DIRECT != 0) {
        options += ExtendedOpenOption.DIRECT
    }

    if (flags and OpenFileFlag.O_NOFOLLOW != 0) {
        options += LinkOption.NOFOLLOW_LINKS
    }
    if (flags and OpenFileFlag.O_NOATIME != 0) {
        ignoredFlags = ignoredFlags and OpenFileFlag.O_NOATIME.toUInt()
    }
    if (flags and OpenFileFlag.O_CLOEXEC != 0) {
        ignoredFlags = ignoredFlags and OpenFileFlag.O_CLOEXEC.toUInt()
    }

    if (flags and OpenFileFlag.O_PATH != 0) {
        notImplementedFlags = notImplementedFlags and OpenFileFlag.O_PATH.toUInt()
    }

    if (flags and OpenFileFlag.O_TMPFILE != 0) {
        ignoredFlags = ignoredFlags and OpenFileFlag.O_TMPFILE.toUInt()
        options += StandardOpenOption.DELETE_ON_CLOSE
    }

    return GetOpenOptionsResult(options, ignoredFlags, notImplementedFlags)
}

private class GetOpenOptionsResult(
    val options: Set<OpenOption>,
    val ignoredFlags: UInt,
    val notImplementedFlags: UInt,
)
