/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.fdattributes

import androidx.annotation.LongDef
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.FD_ADVISE
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.FD_ALLOCATE
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.FD_DATASYNC
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.FD_FDSTAT_SET_FLAGS
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.FD_FILESTAT_GET
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.FD_FILESTAT_SET_SIZE
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.FD_FILESTAT_SET_TIMES
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.FD_READ
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.FD_READDIR
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.FD_SEEK
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.FD_SYNC
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.FD_TELL
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.FD_WRITE
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.PATH_CREATE_DIRECTORY
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.PATH_CREATE_FILE
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.PATH_FILESTAT_GET
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.PATH_FILESTAT_SET_SIZE
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.PATH_FILESTAT_SET_TIMES
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.PATH_LINK_SOURCE
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.PATH_LINK_TARGET
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.PATH_OPEN
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.PATH_READLINK
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.PATH_REMOVE_DIRECTORY
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.PATH_RENAME_SOURCE
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.PATH_RENAME_TARGET
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.PATH_SYMLINK
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.PATH_UNLINK_FILE
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.POLL_FD_READWRITE
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.SOCK_ACCEPT
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.SOCK_SHUTDOWN
import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.LOCAL_VARIABLE
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.PROPERTY_SETTER
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

/**
 * File descriptor rights, determining which actions may be performed.
 *
 * Copy of the WASI Preview 1 Rights
 */
public typealias FdRights = Long

@Suppress("LONG_NUMERICAL_VALUES_SEPARATED")
public object FdRightsFlag {
    /**
     * The right to invoke `fd_datasync`.  If `path_open` is set, includes the right to invoke
     * `path_open` with `fdflags::dsync`.
     */
    public const val FD_DATASYNC: Long = 0x01L

    /**
     * The right to invoke `fd_read` and `sock_recv`.  If `rights::fd_seek` is set, includes the right
     * to invoke `fd_pread`.
     */
    public const val FD_READ: Long = 0x02L

    /**
     * The right to invoke `fd_seek`. This flag implies `rights::fd_tell`.
     */
    public const val FD_SEEK: Long = 0x04L

    /**
     * The right to invoke `fd_fdstat_set_flags`.
     */
    public const val FD_FDSTAT_SET_FLAGS: Long = 0x08L

    /**
     * The right to invoke `fd_sync`.  If `path_open` is set, includes the right to invoke `path_open`
     * with `fdflags::rsync` and `fdflags::dsync`.
     */
    public const val FD_SYNC: Long = 0x10L

    /**
     * The right to invoke `fd_seek` in such a way that the file offset remains unaltered (i.e.,
     * `whence::cur` with offset zero), or to invoke `fd_tell`.
     */
    public const val FD_TELL: Long = 0x20L

    /**
     * The right to invoke `fd_write` and `sock_send`. If `rights::fd_seek` is set, includes the right
     * to invoke `fd_pwrite`.
     */
    public const val FD_WRITE: Long = 0x40L

    /**
     * The right to invoke `fd_advise`.
     */
    public const val FD_ADVISE: Long = 0x80L

    /**
     * The right to invoke `fd_allocate`.
     */
    public const val FD_ALLOCATE: Long = 0x100L

    /**
     * The right to invoke `path_create_directory`.
     */
    public const val PATH_CREATE_DIRECTORY: Long = 0x200L

    /**
     * If `path_open` is set, the right to invoke `path_open` with `oflags::creat`.
     */
    public const val PATH_CREATE_FILE: Long = 0x400L

    /**
     * The right to invoke `path_link` with the file descriptor as the source directory.
     */
    public const val PATH_LINK_SOURCE: Long = 0x800L

    /**
     * The right to invoke `path_link` with the file descriptor as the target directory.
     */
    public const val PATH_LINK_TARGET: Long = 0x1000L

    /**
     * The right to invoke `path_open`.
     */
    public const val PATH_OPEN: Long = 0x2000L

    /**
     * The right to invoke `fd_readdir`.
     */
    public const val FD_READDIR: Long = 0x4000L

    /**
     * The right to invoke `path_readlink`.
     */
    public const val PATH_READLINK: Long = 0x8000L

    /**
     * The right to invoke `path_rename` with the file descriptor as the source directory.
     */
    public const val PATH_RENAME_SOURCE: Long = 0x10000L

    /**
     * The right to invoke `path_rename` with the file descriptor as the target directory.
     */
    public const val PATH_RENAME_TARGET: Long = 0x20000L

    /**
     * The right to invoke `path_filestat_get`.
     */
    public const val PATH_FILESTAT_GET: Long = 0x40000L

    /**
     * The right to change a file's size. If `path_open` is set, includes the right to invoke
     * `path_open` with `oflags::trunc`. Note: there is no function named `path_filestat_set_size`. This
     * follows POSIX design, which only has `ftruncate` and does not provide `ftruncateat`. While such
     * function would be desirable from the API design perspective, there are virtually no use cases for
     * it since no code written for POSIX systems would use it. Moreover, implementing it would require
     * multiple syscalls, leading to inferior performance.
     */
    public const val PATH_FILESTAT_SET_SIZE: Long = 0x80000L

    /**
     * The right to invoke `path_filestat_set_times`.
     */
    public const val PATH_FILESTAT_SET_TIMES: Long = 0x100000L

    /**
     * The right to invoke `fd_filestat_get`.
     */
    public const val FD_FILESTAT_GET: Long = 0x200000L

    /**
     * The right to invoke `fd_filestat_set_size`.
     */
    public const val FD_FILESTAT_SET_SIZE: Long = 0x400000L

    /**
     * The right to invoke `fd_filestat_set_times`.
     */
    public const val FD_FILESTAT_SET_TIMES: Long = 0x800000L

    /**
     * The right to invoke `path_symlink`.
     */
    public const val PATH_SYMLINK: Long = 0x1000000L

    /**
     * The right to invoke `path_remove_directory`.
     */
    public const val PATH_REMOVE_DIRECTORY: Long = 0x2000000L

    /**
     * The right to invoke `path_unlink_file`.
     */
    public const val PATH_UNLINK_FILE: Long = 0x4000000L

    /**
     * If `rights::fd_read` is set, includes the right to invoke `poll_oneoff` to subscribe to
     * `eventtype::fd_read`. If `rights::fd_write` is set, includes the right to invoke `poll_oneoff` to
     * subscribe to `eventtype::fd_write`.
     */
    public const val POLL_FD_READWRITE: Long = 0x8000000L

    /**
     * The right to invoke `sock_shutdown`.
     */
    public const val SOCK_SHUTDOWN: Long = 0x10000000L

    /**
     * The right to invoke `sock_accept`.
     */
    public const val SOCK_ACCEPT: Long = 0x20000000L
    internal const val DIRECTORY_BASE_RIGHTS: Long =
        PATH_CREATE_DIRECTORY or
                PATH_CREATE_FILE or
                PATH_LINK_SOURCE or
                PATH_LINK_TARGET or
                PATH_OPEN or
                FD_READDIR or
                PATH_READLINK or
                PATH_RENAME_SOURCE or
                PATH_RENAME_TARGET or
                PATH_SYMLINK or
                PATH_REMOVE_DIRECTORY or
                PATH_UNLINK_FILE or
                PATH_FILESTAT_GET or
                PATH_FILESTAT_SET_TIMES or
                FD_FILESTAT_GET or
                FD_FILESTAT_SET_TIMES
    internal const val FILE_BASE_RIGHTS: Long = FD_DATASYNC or
            FD_READ or
            FD_SEEK or
            FD_FDSTAT_SET_FLAGS or
            FD_SYNC or
            FD_TELL or
            FD_WRITE or
            FD_ADVISE or
            FD_ALLOCATE or
            FD_FILESTAT_GET or
            FD_FILESTAT_SET_SIZE or
            FD_FILESTAT_SET_TIMES or
            POLL_FD_READWRITE
    internal const val DIRECTORY_INHERITING_RIGHTS: Long = DIRECTORY_BASE_RIGHTS or FILE_BASE_RIGHTS
}

/**
 * Specifies that the annotated element of [Long] type represents the value of type [FdRights].
 */
@Retention(SOURCE)
@Target(
    FIELD,
    FUNCTION,
    LOCAL_VARIABLE,
    PROPERTY,
    PROPERTY_GETTER,
    PROPERTY_SETTER,
    VALUE_PARAMETER,
)
@LongDef(
    flag = true,
    value = [
        FD_DATASYNC,
        FD_READ,
        FD_SEEK,
        FD_FDSTAT_SET_FLAGS,
        FD_SYNC,
        FD_TELL,
        FD_WRITE,
        FD_ADVISE,
        FD_ALLOCATE,
        PATH_CREATE_DIRECTORY,
        PATH_CREATE_FILE,
        PATH_LINK_SOURCE,
        PATH_LINK_TARGET,
        PATH_OPEN,
        FD_READDIR,
        PATH_READLINK,
        PATH_RENAME_SOURCE,
        PATH_RENAME_TARGET,
        PATH_FILESTAT_GET,
        PATH_FILESTAT_SET_SIZE,
        PATH_FILESTAT_SET_TIMES,
        FD_FILESTAT_GET,
        FD_FILESTAT_SET_SIZE,
        FD_FILESTAT_SET_TIMES,
        PATH_SYMLINK,
        PATH_REMOVE_DIRECTORY,
        PATH_UNLINK_FILE,
        POLL_FD_READWRITE,
        SOCK_SHUTDOWN,
        SOCK_ACCEPT,
    ],
)
public annotation class FdRightsType
