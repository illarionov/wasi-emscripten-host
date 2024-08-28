/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.either
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.ext.asLinkOptions
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError
import at.released.weh.filesystem.op.stat.FileModeType
import at.released.weh.filesystem.op.stat.Stat
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.op.stat.StructTimespec
import java.io.IOException
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import kotlin.io.path.exists
import kotlin.io.path.readAttributes
import at.released.weh.filesystem.error.NotDirectory as BaseNotDirectory

internal class NioStat(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<Stat, StatError, StructStat> {
    override fun invoke(input: Stat): Either<StatError, StructStat> {
        val path: Path = fsState.pathResolver.resolve(input.path, input.baseDirectory, false)
            .mapLeft { it.toStatError() }
            .getOrElse { return it.left() }
        return statCatching(path, input.followSymlinks)
    }

    internal companion object {
        const val ATTR_UNI_CTIME = "ctime"
        const val ATTR_UNI_DEV = "dev"
        const val ATTR_UNI_GID = "gid"
        const val ATTR_UNI_INO = "ino"
        const val ATTR_UNI_MODE = "mode"
        const val ATTR_UNI_NLINK = "nlink"
        const val ATTR_UNI_RDEV = "rdev"
        const val ATTR_UNI_UID = "uid"
        val UNIX_REQUESTED_ATTRIBUTES = "unix:" + listOf(
            ATTR_UNI_DEV,
            ATTR_UNI_INO,
            ATTR_UNI_MODE,
            ATTR_UNI_NLINK,
            ATTR_UNI_UID,
            ATTR_UNI_GID,
            ATTR_UNI_RDEV,
            ATTR_UNI_CTIME,
        ).joinToString(",")

        fun statCatching(
            path: Path,
            followSymlinks: Boolean,
            blockSize: ULong = 512UL,
        ): Either<StatError, StructStat> = either {
            val linkOptions = asLinkOptions(followSymlinks)

            if (!path.exists(options = linkOptions)) {
                raise(NoEntry("No such file file: `$path`"))
            }

            val basicFileAttrs: BasicFileAttributes = Either.catch {
                path.readAttributes<BasicFileAttributes>(options = linkOptions)
            }
                .mapLeft { it.readAttributesToStatError() }
                .bind()

            val unixAttrs: Map<String, Any?> = Either.catch {
                path.readAttributes(UNIX_REQUESTED_ATTRIBUTES, options = linkOptions)
            }
                .mapLeft { it.readAttributesToStatError() }
                .bind()

            val dev: ULong = (unixAttrs[ATTR_UNI_DEV] as? Long)?.toULong() ?: 1UL
            val ino: ULong = (unixAttrs[ATTR_UNI_INO] as? Long)?.toULong()
                ?: basicFileAttrs.fileKey().hashCode().toULong()
            val mode: FileModeType = getModeType(basicFileAttrs, unixAttrs)
            val nlink: ULong = (unixAttrs[ATTR_UNI_NLINK] as? Int)?.toULong() ?: 1UL
            val uid: ULong = (unixAttrs[ATTR_UNI_UID] as? Int)?.toULong() ?: 0UL
            val gid: ULong = (unixAttrs[ATTR_UNI_GID] as? Int)?.toULong() ?: 0UL
            val rdev: ULong = (unixAttrs[ATTR_UNI_RDEV] as? Long)?.toULong() ?: 1UL
            val size: ULong = basicFileAttrs.size().toULong()
            val blksize: ULong = blockSize
            val blocks: ULong = (size + blksize - 1UL) / blksize
            val mtim: StructTimespec = basicFileAttrs.lastModifiedTime().toTimeSpec()

            val cTimeFileTime = unixAttrs[ATTR_UNI_CTIME] ?: basicFileAttrs.creationTime()
            val ctim: StructTimespec = (cTimeFileTime as? FileTime)?.toTimeSpec()
                ?: raise(IoError("Can not get file creation time"))
            val atim: StructTimespec = basicFileAttrs.lastAccessTime().toTimeSpec()

            StructStat(
                deviceId = dev,
                inode = ino,
                mode = mode,
                links = nlink,
                usedId = uid,
                groupId = gid,
                specialFileDeviceId = rdev,
                size = size,
                blockSize = blksize,
                blocks = blocks,
                accessTime = atim,
                modificationTime = mtim,
                changeStatusTime = ctim,
            )
        }

        private fun getModeType(
            @Suppress("UnusedParameter") basicAttrs: BasicFileAttributes,
            unixAttrs: Map<String, Any?>,
        ): FileModeType {
            val unixMode = unixAttrs[ATTR_UNI_MODE] as? Int
            if (unixMode != null) {
                return FileModeType(unixMode.toUInt())
            }

            // TODO: guess from Basic mode?
            // TODO: Add type

            return FileModeType("777".toUInt(radix = 8))
        }

        private fun FileTime.toTimeSpec(): StructTimespec = toInstant().run {
            StructTimespec(
                seconds = epochSecond.toULong(),
                nanoseconds = nano.toULong(),
            )
        }

        private fun Throwable.readAttributesToStatError(): StatError = when (this) {
            is UnsupportedOperationException -> AccessDenied("Can not get BasicFileAttributeView")
            is IOException -> IoError("Can not read attributes: $message")
            is SecurityException -> AccessDenied("Can not read attributes: $message")
            else -> throw IllegalStateException("Unexpected error", this)
        }

        private fun ResolvePathError.toStatError(): StatError = when (this) {
            is ResolvePathError.EmptyPath -> NoEntry(message)
            is ResolvePathError.FileDescriptorNotOpen -> BadFileDescriptor(message)
            is ResolvePathError.InvalidPath -> BadFileDescriptor(message)
            is ResolvePathError.NotDirectory -> BaseNotDirectory(message)
            is ResolvePathError.RelativePath -> BadFileDescriptor(message)
        }
    }
}
