/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.Nfile
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.ext.asFileAttribute
import at.released.weh.filesystem.ext.fileModeToPosixFilePermissions
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
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
import com.sun.nio.file.ExtendedOpenOption
import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.file.FileAlreadyExistsException
import java.nio.file.LinkOption
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.concurrent.withLock

internal class NioOpen(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<Open, OpenError, FileDescriptor> {
    override fun invoke(input: Open): Either<OpenError, FileDescriptor> = either {
        val path = fsState.pathResolver.resolve(
            input.path,
            input.baseDirectory,
            allowEmptyPath = true,
            followSymlinks = input.followSymlinks,
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
        val fileAttributes = input.mode?.let {
            arrayOf(it.fileModeToPosixFilePermissions().asFileAttribute())
        } ?: emptyArray()

        fsState.fsLock.withLock {
            @Suppress("SpreadOperator")
            val nioChannel = Either.catch {
                FileChannel.open(path, openOptionsResult.options, *fileAttributes)
            }
                .mapLeft { error -> error.toOpenError(path) }
                .bind()
            val fdChannelFd = fsState.addFile(path, nioChannel, input.fdFlags)
                .mapLeft { noFileDescriptorError -> Nfile(noFileDescriptorError.message) }
                .bind()
            fdChannelFd.first
        }
    }
}

private fun Throwable.toOpenError(path: Path): OpenError = when (this) {
    is IllegalArgumentException -> InvalidArgument(
        "Can not open `$path`: invalid combination of options ($message)",
    )

    is UnsupportedOperationException -> InvalidArgument("Can not open `$path`: unsupported operation ($message)")
    is FileAlreadyExistsException -> Exists("File `$path` already exists ($message)")
    is IOException -> IoError("Can not open `$path`: I/O error ($message)")
    is SecurityException -> PermissionDenied("Can not open `$path`: Permission denied ($message)")
    else -> throw IllegalStateException("Unexpected error", this)
}

@Suppress("CyclomaticComplexMethod", "LOCAL_VARIABLE_EARLY_DECLARATION", "LongMethod")
private fun getOpenOptions(
    @OpenFileFlagsType flags: OpenFileFlags,
    @FdflagsType fdFlags: Fdflags,
): GetOpenOptionsResult {
    val options: MutableSet<OpenOption> = mutableSetOf()
    var ignoredFlags = 0U
    var notImplementedFlags = 0U

    if (flags and OpenFileFlag.O_WRONLY != 0) {
        options += StandardOpenOption.WRITE
    } else if (flags and OpenFileFlag.O_RDWR != 0) {
        options += StandardOpenOption.READ
        options += StandardOpenOption.WRITE
    }

    if (fdFlags and FdFlag.FD_APPEND != 0) {
        options += StandardOpenOption.APPEND
    }

    if (flags and OpenFileFlag.O_CREAT != 0) {
        options += if (flags and OpenFileFlag.O_EXCL != 0) {
            StandardOpenOption.CREATE_NEW
        } else {
            StandardOpenOption.CREATE
        }
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

    if (flags and OpenFileFlag.O_DIRECTORY != 0) {
        notImplementedFlags = notImplementedFlags and OpenFileFlag.O_DIRECTORY.toUInt()
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
