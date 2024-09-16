/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MagicNumber")

package at.released.weh.host.wasi.preview1.type

import at.released.weh.host.base.WasmValueType
import kotlin.jvm.JvmInline

@JvmInline
public value class Rights(
    public val mask: ULong,
) {
    public constructor(
        vararg flags: Flags,
    ) : this(
        flags.fold(0UL) { acc, flag -> acc.or(flag.mask) },
    )

    public enum class Flags(
        public val mask: ULong,
    ) {
        /**
         * The right to invoke `fd_datasync`.
         *
         * If `path_open` is set, includes the right to invoke
         *  `path_open` with `fdflags::dsync`.
         */
        FD_DATASYNC(0),

        /**
         * The right to invoke `fd_read` and `sock_recv`.
         *
         *  If `rights::fd_seek` is set, includes the right to invoke `fd_pread`.
         */
        FD_READ(1),

        /**
         * The right to invoke `fd_seek`. This flag implies `rights::fd_tell`.
         */
        FD_SEEK(2),

        /**
         * The right to invoke `fd_fdstat_set_flags`
         */
        FD_FDSTAT_SET_FLAGS(3),

        /**
         * The right to invoke `fd_sync`.
         *
         * If `path_open` is set, includes the right to invoke
         * `path_open` with `fdflags::rsync` and `fdflags::dsync`.
         */
        FD_SYNC(4),

        /**
         * The right to invoke `fd_seek` in such a way that the file offset
         * remains unaltered (i.e., `whence::cur` with offset zero), or to
         * invoke `fd_tell`.
         */
        FD_TELL(5),

        /**
         * The right to invoke `fd_write` and `sock_send`.
         *
         * If `rights::fd_seek` is set, includes the right to invoke `fd_pwrite`.
         */
        FD_WRITE(6),

        /**
         * The right to invoke `fd_advise`.
         */
        FD_ADVISE(7),

        /**
         * The right to invoke `fd_allocate`.
         */
        FD_ALLOCATE(8),

        /**
         * The right to invoke `path_create_directory`.
         */
        PATH_CREATE_DIRECTORY(9),

        /**
         * If `path_open` is set, the right to invoke `path_open` with `oflags::creat`.
         */
        PATH_CREATE_FILE(10),

        /**
         * The right to invoke `path_link` with the file descriptor as the
         * source directory.
         */
        PATH_LINK_SOURCE(11),

        /**
         * The right to invoke `path_link` with the file descriptor as the
         * target directory.
         */
        PATH_LINK_TARGET(12),

        /**
         * The right to invoke `path_open`.
         */
        PATH_OPEN(13),

        /**
         * The right to invoke `fd_readdir`.
         */
        FD_READDIR(14),

        /**
         * The right to invoke `path_readlink`.
         */
        PATH_READLINK(15),

        /**
         * The right to invoke `path_rename` with the file descriptor as the source directory.
         */
        PATH_RENAME_SOURCE(16),

        /**
         * The right to invoke `path_rename` with the file descriptor as the target directory.
         */
        PATH_RENAME_TARGET(17),

        /**
         * The right to invoke `path_filestat_get`.
         */
        PATH_FILESTAT_GET(18),

        /**
         * The right to change a file's size.
         * If `path_open` is set, includes the right to invoke `path_open` with `oflags::trunc`.
         * Note: there is no function named `path_filestat_set_size`. This follows POSIX design,
         * which only has `ftruncate` and does not provide `ftruncateat`.
         * While such function would be desirable from the API design perspective, there are virtually
         * no use cases for it since no code written for POSIX systems would use it.
         * Moreover, implementing it would require multiple syscalls, leading to inferior performance.
         */
        PATH_FILESTAT_SET_SIZE(19),

        /**
         * The right to invoke `path_filestat_set_times`.
         */
        PATH_FILESTAT_SET_TIMES(20),

        /**
         * The right to invoke `fd_filestat_get`.
         */
        FD_FILESTAT_GET(21),

        /**
         * The right to invoke `fd_filestat_set_size`.
         */
        FD_FILESTAT_SET_SIZE(22),

        /**
         * The right to invoke `fd_filestat_set_times`.
         */
        FD_FILESTAT_SET_TIMES(23),

        /**
         * The right to invoke `path_symlink`.
         */
        PATH_SYMLINK(24),

        /**
         * The right to invoke `path_remove_directory`.
         */
        PATH_REMOVE_DIRECTORY(25),

        /**
         * The right to invoke `path_unlink_file`.
         */
        PATH_UNLINK_FILE(26),

        /**
         * If `rights::fd_read` is set, includes the right to invoke `poll_oneoff` to subscribe
         * to `eventtype::fd_read`.
         * If `rights::fd_write` is set, includes the right to invoke `poll_oneoff` to subscribe
         * to `eventtype::fd_write`.
         */
        POLL_FD_READWRITE(27),

        /**
         * The right to invoke `sock_shutdown`.
         */
        SOCK_SHUTDOWN(28),

        /**
         * The right to invoke `sock_accept`.
         */
        SOCK_ACCEPT(29),

        ;

        constructor(bit: Int) : this(1UL.shl(bit))
    }

    public companion object : WasiTypename {
        @WasmValueType
        override val wasmValueType: Int = WasiValueTypes.U16
    }
}
