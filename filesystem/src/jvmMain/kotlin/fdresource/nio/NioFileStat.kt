/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.ext.asLinkOptions
import at.released.weh.filesystem.ext.toFiletype
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.model.FileModeFlag.S_IRWXG
import at.released.weh.filesystem.model.FileModeFlag.S_IRWXO
import at.released.weh.filesystem.model.FileModeFlag.S_IRWXU
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.op.stat.StructTimespec
import java.io.IOException
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import kotlin.io.path.exists
import kotlin.io.path.readAttributes

internal object NioFileStat {
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

    internal fun getStat(
        path: Path,
        followSymlinks: Boolean,
        blockSize: Long = 512L,
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

        val dev: Long = (unixAttrs[ATTR_UNI_DEV] as? Long) ?: 1L
        val ino: Long = (unixAttrs[ATTR_UNI_INO] as? Long)
            ?: basicFileAttrs.fileKey().hashCode().toLong()

        @FileMode
        val mode: Int = getMode(unixAttrs)
        val type = basicFileAttrs.toFiletype()
        val nlink: Long = (unixAttrs[ATTR_UNI_NLINK] as? Int)?.toLong() ?: 1L
        val uid: Long = (unixAttrs[ATTR_UNI_UID] as? Int)?.toLong() ?: 0L
        val gid: Long = (unixAttrs[ATTR_UNI_GID] as? Int)?.toLong() ?: 0L
        val rdev: Long = (unixAttrs[ATTR_UNI_RDEV] as? Long)?.toLong() ?: 1L
        val size: Long = basicFileAttrs.size()
        val blksize: Long = blockSize
        val blocks: Long = (size + blksize - 1L) / blksize
        val mtim: StructTimespec = basicFileAttrs.lastModifiedTime().toTimeSpec()

        val cTimeFileTime = unixAttrs[ATTR_UNI_CTIME] ?: basicFileAttrs.creationTime()
        val ctim: StructTimespec = (cTimeFileTime as? FileTime)?.toTimeSpec()
            ?: raise(IoError("Can not get file creation time"))
        val atim: StructTimespec = basicFileAttrs.lastAccessTime().toTimeSpec()

        StructStat(
            deviceId = dev,
            inode = ino,
            mode = mode,
            type = type,
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

    @FileMode
    private fun getMode(
        unixAttrs: Map<String, Any?>,
    ): Int {
        val unixMode = unixAttrs[ATTR_UNI_MODE] as? Int
        if (unixMode != null) {
            return unixMode
        }

        return S_IRWXU or S_IRWXG or S_IRWXO
    }

    private fun FileTime.toTimeSpec(): StructTimespec = toInstant().run {
        StructTimespec(
            seconds = epochSecond,
            nanoseconds = nano.toLong(),
        )
    }

    internal fun Throwable.readAttributesToStatError(): StatError = when (this) {
        is UnsupportedOperationException -> AccessDenied("Can not get BasicFileAttributeView")
        is IOException -> IoError("Can not read attributes: $message")
        is SecurityException -> AccessDenied("Can not read attributes: $message")
        else -> throw IllegalStateException("Unexpected error", this)
    }

    internal fun toStatError(pathError: ResolvePathError): StatError = when (pathError) {
        is ResolvePathError.EmptyPath -> NoEntry(pathError.message)
        is ResolvePathError.FileDescriptorNotOpen -> BadFileDescriptor(pathError.message)
        is ResolvePathError.InvalidPath -> BadFileDescriptor(pathError.message)
        is ResolvePathError.NotDirectory -> NotDirectory(pathError.message)
        is ResolvePathError.RelativePath -> BadFileDescriptor(pathError.message)
    }
}
