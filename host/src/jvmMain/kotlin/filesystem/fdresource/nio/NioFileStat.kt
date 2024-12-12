/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.ext.asLinkOptions
import at.released.weh.filesystem.ext.filetype
import at.released.weh.filesystem.ext.readOrGenerateInode
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.model.FileModeFlag.S_IRWXG
import at.released.weh.filesystem.model.FileModeFlag.S_IRWXO
import at.released.weh.filesystem.model.FileModeFlag.S_IRWXU
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.op.stat.StructTimespec
import at.released.weh.filesystem.path.real.nio.NioRealPath
import java.nio.file.LinkOption
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import kotlin.io.path.exists

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
        path: NioRealPath,
        followSymlinks: Boolean,
        blockSize: Long = 512L,
    ): Either<StatError, StructStat> = either {
        val linkOptions: Array<LinkOption> = asLinkOptions(followSymlinks)

        if (!path.nio.exists(options = linkOptions)) {
            raise(NoEntry("No such file: `$path`"))
        }

        val basicFileAttrs: BasicFileAttributes = path.nio.readBasicAttributes(followSymlinks)
            .mapLeft<StatError>(::toStatError)
            .bind()
        val unixAttrs: Map<String, Any?> = path.nio.readAttributeMapIfSupported(
            UNIX_REQUESTED_ATTRIBUTES,
            followSymlinks,
        ).mapLeft<StatError>(::toStatError).bind()

        @FileMode
        val mode: Int = getMode(unixAttrs)
        val type = basicFileAttrs.filetype
        val nlink: Long = (unixAttrs[ATTR_UNI_NLINK] as? Int)?.toLong() ?: 1L
        val uid: Long = (unixAttrs[ATTR_UNI_UID] as? Int)?.toLong() ?: 0L
        val gid: Long = (unixAttrs[ATTR_UNI_GID] as? Int)?.toLong() ?: 0L
        val rdev: Long = (unixAttrs[ATTR_UNI_RDEV] as? Long)?.toLong() ?: 1L
        val size: Long = basicFileAttrs.size()
        val blksize: Long = blockSize
        val blocks: Long = (size + blksize - 1L) / blksize
        val cTimeFileTime = unixAttrs[ATTR_UNI_CTIME] ?: basicFileAttrs.creationTime()
        val ctim: StructTimespec = (cTimeFileTime as? FileTime)?.toTimeSpec()
            ?: raise(IoError("Can not get file creation time"))
        val mtim: StructTimespec = basicFileAttrs.lastModifiedTime().toTimeSpec()
        val atim: StructTimespec = basicFileAttrs.lastAccessTime().toTimeSpec()

        val dev: Long = (unixAttrs[ATTR_UNI_DEV] as? Long) ?: 1L
        val ino: Long = path.nio.readOrGenerateInode(basicFileAttrs, unixAttrs)

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

    internal fun toStatError(attributesError: ReadAttributesError): StatError = when (attributesError) {
        is ReadAttributesError.AccessDenied -> AccessDenied(attributesError.message)
        is ReadAttributesError.IoError -> IoError(attributesError.message)
        is ReadAttributesError.NotSupported -> AccessDenied(attributesError.message)
    }
}
