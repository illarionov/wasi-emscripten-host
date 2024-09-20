/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1

import at.released.weh.wasi.preview1.type.Advice
import at.released.weh.wasi.preview1.type.CiovecArray
import at.released.weh.wasi.preview1.type.ClockId
import at.released.weh.wasi.preview1.type.Dircookie
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Event
import at.released.weh.wasi.preview1.type.ExitCode
import at.released.weh.wasi.preview1.type.FdFlags
import at.released.weh.wasi.preview1.type.FdStat
import at.released.weh.wasi.preview1.type.FdWasmValueType
import at.released.weh.wasi.preview1.type.FileDelta
import at.released.weh.wasi.preview1.type.FileSize
import at.released.weh.wasi.preview1.type.Filestat
import at.released.weh.wasi.preview1.type.Fstflags
import at.released.weh.wasi.preview1.type.IovecArray
import at.released.weh.wasi.preview1.type.LookupFlags
import at.released.weh.wasi.preview1.type.Oflags
import at.released.weh.wasi.preview1.type.Prestat
import at.released.weh.wasi.preview1.type.Riflags
import at.released.weh.wasi.preview1.type.Rights
import at.released.weh.wasi.preview1.type.Roflags
import at.released.weh.wasi.preview1.type.Sdflags
import at.released.weh.wasi.preview1.type.SiFlags
import at.released.weh.wasi.preview1.type.Signal
import at.released.weh.wasi.preview1.type.Size
import at.released.weh.wasi.preview1.type.Subscription
import at.released.weh.wasi.preview1.type.Timestamp
import at.released.weh.wasi.preview1.type.WasiValueTypes
import at.released.weh.wasi.preview1.type.WasiValueTypes.U8
import at.released.weh.wasi.preview1.type.Whence
import at.released.weh.wasi.preview1.type.wasmValueType
import at.released.weh.wasm.core.HostFunction
import at.released.weh.wasm.core.WasmValueType
import at.released.weh.wasm.core.WasmValueTypes.I32
import at.released.weh.wasm.core.witxPointer

/**
 * WASI Preview1 function descriptors
 *
 * https://github.com/WebAssembly/WASI/blob/main/legacy/preview1/witx/wasi_snapshot_preview1.witx
 */
public enum class WasiHostFunction(
    public override val wasmName: String,
    public override val type: HostFunction.HostFunctionType,
) : HostFunction {
    /**
     * Read command-line argument data.
     *
     * ```
     * (@interface func (export "args_get")
     *   (param $argv (@witx pointer (@witx pointer u8)))
     *   (param $argv_buf (@witx pointer u8))
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    ARGS_GET(
        wasmName = "args_get",
        paramTypes = listOf(
            witxPointer(witxPointer(U8)), // argv
            witxPointer(U8), // argv_buf
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Return command-line argument data sizes.
     *
     * ```
     * (@interface func (export "args_sizes_get")
     *   ;;; Returns the number of arguments and the size of the argument string
     *   ;;; data, or an error.
     *   (result $error (expected (tuple $size $size) (error $errno)))
     * )
     * ```
     */
    ARGS_SIZES_GET(
        wasmName = "args_sizes_get",
        paramTypes = listOf(
            pointerToType(Size),
            pointerToType(Size),
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Read environment variable data.
     * The sizes of the buffers should match that returned by `environ_sizes_get`.
     * Key/value pairs are expected to be joined with `=`s, and terminated with `\0`s.
     *
     * ```
     * (@interface func (export "environ_get")
     *     (param $environ (@witx pointer (@witx pointer u8)))
     *     (param $environ_buf (@witx pointer u8))
     *     (result $error (expected (error $errno)))
     *   )
     * ```
     */
    ENVIRON_GET(
        wasmName = "environ_get",
        paramTypes = listOf(
            witxPointer(witxPointer(U8)),
            witxPointer(U8),
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Return environment variable data sizes.
     *
     * ```
     * (@interface func (export "environ_sizes_get")
     *   ;;; Returns the number of environment variable arguments and the size of the
     *   ;;; environment variable data.
     *   (result $error (expected (tuple $size $size) (error $errno)))
     * )
     * ```
     */
    ENVIRON_SIZES_GET(
        wasmName = "environ_sizes_get",
        paramTypes = listOf(
            pointerToType(Size), // *environ_count
            pointerToType(Size), // *environ_buf_size
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Return the resolution of a clock.
     * Implementations are required to provide a non-zero value for supported clocks. For unsupported clocks,
     * return `errno::inval`.
     *
     * Note: This is similar to `clock_getres` in POSIX.
     *
     * ```
     * (@interface func (export "clock_res_get")
     *   ;;; The clock for which to return the resolution.
     *   (param $id $clockid)
     *   ;;; The resolution of the clock, or an error if one happened.
     *   (result $error (expected $timestamp (error $errno)))
     * )
     * ```
     */
    CLOCK_RES_GET(
        wasmName = "clock_res_get",
        paramTypes = listOf(
            ClockId.wasmValueType, // id
            pointerToType(Timestamp), // expected timestamp
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     *  Return the time value of a clock.
     *  Note: This is similar to `clock_gettime` in POSIX.
     *
     * ```
     * (@interface func (export "clock_time_get")
     *   ;;; The clock for which to return the time.
     *   (param $id $clockid)
     *   ;;; The maximum lag (exclusive) that the returned time value may have, compared to its actual value.
     *   (param $precision $timestamp)
     *   ;;; The time value of the clock.
     *   (result $error (expected $timestamp (error $errno)))
     * )
     * ```
     */
    CLOCK_TIME_GET(
        wasmName = "clock_time_get",
        paramTypes = listOf(
            ClockId.wasmValueType, // id
            Timestamp.wasmValueType, // precision
            pointerToType(Timestamp), // expected $timestamp
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Provide file advisory information on a file descriptor.
     * Note: This is similar to `posix_fadvise` in POSIX.
     *
     * ```
     * (@interface func (export "fd_advise")
     *   (param $fd $fd)
     *   ;;; The offset within the file to which the advisory applies.
     *   (param $offset $filesize)
     *   ;;; The length of the region to which the advisory applies.
     *   (param $len $filesize)
     *   ;;; The advice.
     *   (param $advice $advice)
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    FD_ADVISE(
        wasmName = "fd_advise",
        paramTypes = listOf(
            FdWasmValueType, // fd
            FileSize.wasmValueType, // offset
            FileSize.wasmValueType, // len
            Advice.wasmValueType, // advice
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Force the allocation of space in a file.
     * Note: This is similar to `posix_fallocate` in POSIX.
     *
     * ```
     * (@interface func (export "fd_allocate")
     *   (param $fd $fd)
     *   ;;; The offset at which to start the allocation.
     *   (param $offset $filesize)
     *   ;;; The length of the area that is allocated.
     *   (param $len $filesize)
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    FD_ALLOCATE(
        wasmName = "fd_allocate",
        paramTypes = listOf(
            FdWasmValueType, // fd
            FileSize.wasmValueType, // offset
            FileSize.wasmValueType, // len
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Close a file descriptor.
     * Note: This is similar to `close` in POSIX.
     *
     * ```
     * (@interface func (export "fd_close")
     *    (param $fd $fd)
     *    (result $error (expected (error $errno)))
     *  )
     * ```
     */
    FD_CLOSE(
        wasmName = "fd_close",
        paramTypes = listOf(
            FdWasmValueType, // Fd
        ),
        retType = Errno.wasmValueType,
    ),

    FD_DATASYNC(
        wasmName = "fd_datasync",
        paramTypes = listOf(
            FdWasmValueType, // Fd
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Get the attributes of a file descriptor.
     *
     * Note: This returns similar flags to `fcntl(fd, F_GETFL)` in POSIX, as well as additional fields.
     *
     * ```
     * (@interface func (export "fd_fdstat_get")
     *   (param $fd $fd)
     *   ;;; The buffer where the file descriptor's attributes are stored.
     *   (result $error (expected $fdstat (error $errno)))
     * )
     * ```
     */
    FD_FDSTAT_GET(
        wasmName = "fd_fdstat_get",
        paramTypes = listOf(
            FdWasmValueType, // Fd
            pointerToType(FdStat), // expected $fdstat
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Adjust the flags associated with a file descriptor.
     *
     * Note: This is similar to `fcntl(fd, F_SETFL, flags)` in POSIX.
     *
     * ```
     * (@interface func (export "fd_fdstat_set_flags")
     *   (param $fd $fd)
     *   ;;; The desired values of the file descriptor flags.
     *   (param $flags $fdflags)
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    FD_FDSTAT_SET_FLAGS(
        wasmName = "fd_fdstat_set_flags",
        paramTypes = listOf(
            FdWasmValueType, // Fd
            pointerToType(FdFlags), // flags
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     *  Adjust the rights associated with a file descriptor.
     *  This can only be used to remove rights, and returns `errno::notcapable` if called in a way that would attempt
     *  to add rights
     *
     * ```
     * (@interface func (export "fd_fdstat_set_rights")
     *    (param $fd $fd)
     *    ;;; The desired rights of the file descriptor.
     *    (param $fs_rights_base $rights)
     *    (param $fs_rights_inheriting $rights)
     *    (result $error (expected (error $errno)))
     *  )
     * ```
     */
    FD_FDSTAT_SET_RIGHTS(
        wasmName = "fd_fdstat_set_rights",
        paramTypes = listOf(
            FdWasmValueType, // Fd
            Rights.wasmValueType, // fs_rights_base
            Rights.wasmValueType, // fs_rights_inheriting
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Return the attributes of an open file.
     *
     * ```
     * (@interface func (export "fd_filestat_get")
     *   (param $fd $fd)
     *   ;;; The buffer where the file's attributes are stored.
     *   (result $error (expected $filestat (error $errno)))
     * )
     * ```
     */
    FD_FILESTAT_GET(
        wasmName = "fd_filestat_get",
        paramTypes = listOf(
            FdWasmValueType, // Fd
            pointerToType(Filestat), // expected $filestat
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Adjust the size of an open file. If this increases the file's size, the extra bytes are filled with zeros.
     * Note: This is similar to `ftruncate` in POSIX.
     *
     * ```
     * (@interface func (export "fd_filestat_set_size")
     *   (param $fd $fd)
     *   ;;; The desired file size.
     *   (param $size $filesize)
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    FD_FILESTAT_SET_SIZE(
        wasmName = "fd_filestat_set_size",
        paramTypes = listOf(
            FdWasmValueType, // Fd
            Size.wasmValueType, // size
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Adjust the timestamps of an open file or directory.
     * Note: This is similar to `futimens` in POSIX.
     *
     * ```
     * (@interface func (export "fd_filestat_set_times")
     *   (param $fd $fd)
     *   ;;; The desired values of the data access timestamp.
     *   (param $atim $timestamp)
     *   ;;; The desired values of the data modification timestamp.
     *   (param $mtim $timestamp)
     *   ;;; A bitmask indicating which timestamps to adjust.
     *   (param $fst_flags $fstflags)
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    FD_FILESTAT_SET_TIMES(
        wasmName = "fd_filestat_set_times",
        paramTypes = listOf(
            FdWasmValueType, // Fd
            Timestamp.wasmValueType, // atim
            Timestamp.wasmValueType, // mtim
            Fstflags.wasmValueType, // fst_flags
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Read from a file descriptor, without using and updating the file descriptor's offset.
     * Note: This is similar to `preadv` in Linux (and other Unix-es).
     *
     * ```
     * (@interface func (export "fd_pread")
     *   (param $fd $fd)
     *   ;;; List of scatter/gather vectors in which to store data.
     *   (param $iovs $iovec_array)
     *   ;;; The offset within the file at which to read.
     *   (param $offset $filesize)
     *   ;;; The number of bytes read.
     *   (result $error (expected $size (error $errno)))
     * )
     * ```
     */
    FD_PREAD(
        wasmName = "fd_pread",
        paramTypes = listOf(
            FdWasmValueType, // Fd
            pointerToType(IovecArray), // $iovs
            I32, // iov_cnt or offset?. XXX: should be I64?
            pointerToType(Size), // expected $size
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Return a description of the given preopened file descriptor.
     *
     * ```
     * (@interface func (export "fd_prestat_get")
     *   (param $fd $fd)
     *   ;;; The buffer where the description is stored.
     *   (result $error (expected $prestat (error $errno)))
     * )
     * ```
     */
    FD_PRESTAT_GET(
        wasmName = "fd_prestat_get",
        paramTypes = listOf(
            FdWasmValueType, // fd
            pointerToType(Prestat), // expected $prestat
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Return a description of the given preopened file descriptor.
     *
     * ```
     * (@interface func (export "fd_prestat_dir_name")
     *   (param $fd $fd)
     *   ;;; A buffer into which to write the preopened directory name.
     *   (param $path (@witx pointer u8))
     *   (param $path_len $size)
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    FD_PRESTAT_DIR_NAME(
        wasmName = "fd_prestat_dir_name",
        paramTypes = listOf(
            FdWasmValueType, // fd,
            witxPointer(U8), // path
            Size.wasmValueType, // path_len
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Write to a file descriptor, without using and updating the file descriptor's offset.
     * Note: This is similar to `pwritev` in Linux (and other Unix-es).
     *
     * Like Linux (and other Unix-es), any calls of `pwrite` (and other
     * functions to read or write) for a regular file by other threads in the
     * WASI process should not be interleaved while `pwrite` is executed.
     *
     * ```
     * (@interface func (export "fd_pwrite")
     *   (param $fd $fd)
     *   ;;; List of scatter/gather vectors from which to retrieve data.
     *   (param $iovs $ciovec_array)
     *   ;;; The offset within the file at which to write.
     *   (param $offset $filesize)
     *   ;;; The number of bytes written.
     *   (result $error (expected $size (error $errno)))
     * )
     * ```
     */
    FD_PWRITE(
        wasmName = "fd_pwrite",
        paramTypes = listOf(
            FdWasmValueType, // fd
            pointerToType(CiovecArray), //  $iovs
            I32, // iovs_cnt XXX
            pointerToType(Size), // expected $size
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Read from a file descriptor.
     * Note: This is similar to `readv` in POSIX.
     *
     * ```
     * (@interface func (export "fd_read")
     *   (param $fd $fd)
     *   ;;; List of scatter/gather vectors to which to store data.
     *   (param $iovs $iovec_array)
     *   ;;; The number of bytes read.
     *   (result $error (expected $size (error $errno)))
     * )
     * ```
     */
    FD_READ(
        wasmName = "fd_read",
        paramTypes = listOf(
            FdWasmValueType, // Fd
            pointerToType(IovecArray), // iov
            I32, // iov_cnt
            witxPointer(I32), // pNum
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Read directory entries from a directory.
     * When successful, the contents of the output buffer consist of a sequence of
     * directory entries. Each directory entry consists of a `dirent` object,
     * followed by `dirent::d_namlen` bytes holding the name of the directory
     *  entry.
     *
     * This function fills the output buffer as much as possible, potentially
     * truncating the last directory entry. This allows the caller to grow its
     * read buffer size in case it's too small to fit a single large directory
     * entry, or skip the oversized directory entry.
     *
     * Entries for the special `.` and `..` directory entries are included in the
     * sequence.
     *
     * ```
     * (@interface func (export "fd_readdir")
     *   (param $fd $fd)
     *   ;;; The buffer where directory entries are stored
     *   (param $buf (@witx pointer u8))
     *   (param $buf_len $size)
     *   ;;; The location within the directory to start reading
     *   (param $cookie $dircookie)
     *   ;;; The number of bytes stored in the read buffer. If less than the size of the read buffer, the end of the
     *    ;;; directory has been reached.
     *   (result $error (expected $size (error $errno)))
     * )
     * ```
     */
    FD_READDIR(
        wasmName = "fd_readdir",
        paramTypes = listOf(
            FdWasmValueType, // Fd
            witxPointer(U8), // buf
            Size.wasmValueType, // $buf_len
            Dircookie.wasmValueType, // cookie
            pointerToType(Size), // expected $size
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Atomically replace a file descriptor by renumbering another file descriptor.
     *
     * Due to the strong focus on thread safety, this environment does not provide
     * a mechanism to duplicate or renumber a file descriptor to an arbitrary
     * number, like `dup2()`. This would be prone to race conditions, as an actual
     * file descriptor with the same number could be allocated by a different
     * thread at the same time.
     *
     * This function provides a way to atomically renumber file descriptors, which
     * would disappear if `dup2()` were to be removed entirely.
     *
     * ```
     * (@interface func (export "fd_renumber")
     *   (param $fd $fd)
     *   ;;; The file descriptor to overwrite.
     *   (param $to $fd)
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    FD_RENUMBER(
        wasmName = "fd_renumber",
        paramTypes = listOf(
            FdWasmValueType, // fd
            FdWasmValueType, // to
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Move the offset of a file descriptor.
     * Note: This is similar to `lseek` in POSIX.
     *
     * ```
     * (@interface func (export "fd_seek")
     *   (param $fd $fd)
     *   ;;; The number of bytes to move.
     *   (param $offset $filedelta)
     *   ;;; The base from which the offset is relative.
     *   (param $whence $whence)
     *   ;;; The new offset of the file descriptor, relative to the start of the file.
     *   (result $error (expected $filesize (error $errno)))
     * )
     * ```
     */
    FD_SEEK(
        wasmName = "fd_seek",
        paramTypes = listOf(
            FdWasmValueType, // fd
            FileDelta.wasmValueType, // offset
            Whence.wasmValueType, // whence
            pointerToType(FileSize), // expected newOffset
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Synchronize the data and metadata of a file to disk.
     * Note: This is similar to `fsync` in POSIX.
     *
     * ```
     * (@interface func (export "fd_sync")
     *   (param $fd $fd)
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    FD_SYNC(
        wasmName = "fd_sync",
        paramTypes = listOf(
            FdWasmValueType, // fd
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Return the current offset of a file descriptor.
     * Note: This is similar to `lseek(fd, 0, SEEK_CUR)` in POSIX.
     *
     * ```
     * (@interface func (export "fd_tell")
     *   (param $fd $fd)
     *   ;;; The current offset of the file descriptor, relative to the start of the file.
     *   (result $error (expected $filesize (error $errno)))
     * )
     * ```
     */
    FD_TELL(
        wasmName = "fd_tell",
        paramTypes = listOf(
            FdWasmValueType, // fd
            pointerToType(FileSize), // expected $filesize
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Write to a file descriptor.
     * Note: This is similar to `writev` in POSIX.
     *
     * Like POSIX, any calls of `write` (and other functions to read or write)
     * for a regular file by other threads in the WASI process should not be
     * interleaved while `write` is executed.
     *
     * ```
     * (@interface func (export "fd_write")
     *   (param $fd $fd)
     *   ;;; List of scatter/gather vectors from which to retrieve data.
     *   (param $iovs $ciovec_array)
     *   (result $error (expected $size (error $errno)))
     * )
     * ```
     */
    FD_WRITE(
        wasmName = "fd_write",
        paramTypes = listOf(
            FdWasmValueType, // fd
            CiovecArray.wasmValueType, // iovs
            I32, // iovs_cnt
            pointerToType(Size), // expected $size
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Create a directory.
     * Note: This is similar to `mkdirat` in POSIX.
     *
     * ```
     * (@interface func (export "path_create_directory")
     *   (param $fd $fd)
     *   ;;; The path at which to create the directory.
     *   (param $path string)
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    PATH_CREATE_DIRECTORY(
        wasmName = "path_create_directory",
        paramTypes = listOf(
            FdWasmValueType, // fd
            witxPointer(U8), // path
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Return the attributes of a file or directory.
     * Note: This is similar to `stat` in POSIX.
     *
     * ```
     * (@interface func (export "path_filestat_get")
     *   (param $fd $fd)
     *   ;;; Flags determining the method of how the path is resolved.
     *   (param $flags $lookupflags)
     *   ;;; The path of the file or directory to inspect.
     *   (param $path string)
     *   ;;; The buffer where the file's attributes are stored.
     *   (result $error (expected $filestat (error $errno)))
     * )
     * ```
     */
    PATH_FILESTAT_GET(
        wasmName = "path_filestat_get",
        paramTypes = listOf(
            FdWasmValueType, // fd
            witxPointer(U8), // path
            pointerToType(Filestat), // expected $filestat
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Adjust the timestamps of a file or directory.
     * Note: This is similar to `utimensat` in POSIX.
     *
     * ```
     * (@interface func (export "path_filestat_set_times")
     *   (param $fd $fd)
     *   ;;; Flags determining the method of how the path is resolved.
     *   (param $flags $lookupflags)
     *   ;;; The path of the file or directory to operate on.
     *   (param $path string)
     *   ;;; The desired values of the data access timestamp.
     *   (param $atim $timestamp)
     *   ;;; The desired values of the data modification timestamp.
     *   (param $mtim $timestamp)
     *   ;;; A bitmask indicating which timestamps to adjust.
     *   (param $fst_flags $fstflags)
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    PATH_FILESTAT_SET_TIMES(
        wasmName = "path_filestat_set_times",
        paramTypes = listOf(
            FdWasmValueType, // fd
            LookupFlags.wasmValueType, // $flags
            witxPointer(U8), // $path
            Timestamp.wasmValueType, // atim
            Timestamp.wasmValueType, // mtim
            Fstflags.wasmValueType, // fst_flags
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Create a hard link.
     * Note: This is similar to `linkat` in POSIX.
     *
     * ```
     * (@interface func (export "path_link")
     *   (param $old_fd $fd)
     *   ;;; Flags determining the method of how the path is resolved.
     *   (param $old_flags $lookupflags)
     *   ;;; The source path from which to link.
     *   (param $old_path string)
     *   ;;; The working directory at which the resolution of the new path starts.
     *   (param $new_fd $fd)
     *   ;;; The destination path at which to create the hard link.
     *   (param $new_path string)
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    PATH_LINK(
        wasmName = "path_link",
        paramTypes = listOf(
            FdWasmValueType, // old_fd
            LookupFlags.wasmValueType, // old_flags
            witxPointer(U8), // old_path
            FdWasmValueType, // new_fd
            witxPointer(U8), // new_path
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Open a file or directory.
     *
     * The returned file descriptor is not guaranteed to be the lowest-numbered file descriptor not currently open;
     * Note: This is similar to `openat` in POSIX.
     *
     * ```
     * (@interface func (export "path_open")
     *   (param $fd $fd)
     *   ;;; Flags determining the method of how the path is resolved.
     *   (param $dirflags $lookupflags)
     *   ;;; The relative path of the file or directory to open, relative to the
     *   ;;; `path_open::fd` directory.
     *   (param $path string)
     *   ;;; The method by which to open the file.
     *   (param $oflags $oflags)
     *   ;;; The initial rights of the newly created file descriptor. The
     *   ;;; implementation is allowed to return a file descriptor with fewer rights
     *   ;;; than specified, if and only if those rights do not apply to the type of
     *   ;;; file being opened.
     *   ;;
     *   ;;; The *base* rights are rights that will apply to operations using the file
     *   ;;; descriptor itself, while the *inheriting* rights are rights that apply to
     *   ;;; file descriptors derived from it.
     *   (param $fs_rights_base $rights)
     *   (param $fs_rights_inheriting $rights)
     *   (param $fdflags $fdflags)
     *   ;;; The file descriptor of the file that has been opened.
     *   (result $error (expected $fd (error $errno)))
     * )
     * ```
     */
    PATH_OPEN(
        wasmName = "path_open",
        paramTypes = listOf(
            FdWasmValueType, // fd
            LookupFlags.wasmValueType, // dirflags
            witxPointer(U8), // path
            Oflags.wasmValueType, // oflags
            Rights.wasmValueType, // fs_rights_base
            Rights.wasmValueType, // fs_rights_inheriting
            FdFlags.wasmValueType, // fdflags
            witxPointer(FdWasmValueType), // expected $fd
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Read the contents of a symbolic link.
     *
     * Note: This is similar to `readlinkat` in POSIX. If `buf` is not large
     * enough to contain the contents of the link, the first `buf_len` bytes will be
     * written to `buf`.
     *
     * ```
     * (@interface func (export "path_readlink")
     *   (param $fd $fd)
     *   ;;; The path of the symbolic link from which to read.
     *   (param $path string)
     *   ;;; The buffer to which to write the contents of the symbolic link.
     *   (param $buf (@witx pointer u8))
     *   (param $buf_len $size)
     *   ;;; The number of bytes placed in the buffer.
     *   (result $error (expected $size (error $errno)))
     * )
     * ```
     */
    PATH_READLINK(
        wasmName = "path_readlink",
        paramTypes = listOf(
            FdWasmValueType, // fd
            witxPointer(U8), // path
            witxPointer(U8), // buf
            Size.wasmValueType, // buf_size
            pointerToType(Size), // expected $size
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Remove a directory.
     *
     * Return `errno::notempty` if the directory is not empty.
     * Note: This is similar to `unlinkat(fd, path, AT_REMOVEDIR)` in POSIX.
     *
     * ```
     * (@interface func (export "path_remove_directory")
     *   (param $fd $fd)
     *   ;;; The path to a directory to remove.
     *   (param $path string)
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    PATH_REMOVE_DIRECTORY(
        wasmName = "path_remove_directory",
        paramTypes = listOf(
            FdWasmValueType, // fd
            witxPointer(U8), // path
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Rename a file or directory.
     * Note: This is similar to `renameat` in POSIX.
     *
     * ```
     * (@interface func (export "path_rename")
     *   (param $fd $fd)
     *   ;;; The source path of the file or directory to rename.
     *   (param $old_path string)
     *   ;;; The working directory at which the resolution of the new path starts.
     *   (param $new_fd $fd)
     *   ;;; The destination path to which to rename the file or directory.
     *   (param $new_path string)
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    PATH_RENAME(
        wasmName = "path_rename",
        paramTypes = listOf(
            FdWasmValueType, // fd
            witxPointer(U8), // old_path
            FdWasmValueType, // new_fd
            witxPointer(U8), // new_path
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     *  Create a symbolic link.
     *  Note: This is similar to `symlinkat` in POSIX.
     *
     * ```
     * (@interface func (export "path_symlink")
     *   ;;; The contents of the symbolic link.
     *   (param $old_path string)
     *   (param $fd $fd)
     *   ;;; The destination path at which to create the symbolic link.
     *   (param $new_path string)
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    PATH_SYMLINK(
        wasmName = "path_symlink",
        paramTypes = listOf(
            witxPointer(U8), // old_path
            FdWasmValueType, // fd
            witxPointer(U8), // new_path
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Unlink a file.
     *
     * Return `errno::isdir` if the path refers to a directory.
     * Note: This is similar to `unlinkat(fd, path, 0)` in POSIX.
     *
     * ```
     * (@interface func (export "path_unlink_file")
     *   (param $fd $fd)
     *   ;;; The path to a file to unlink.
     *   (param $path string)
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    PATH_UNLINK_FILE(
        wasmName = "path_unlink_file",
        paramTypes = listOf(
            FdWasmValueType, // fd
            witxPointer(U8), // path
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Concurrently poll for the occurrence of a set of events.
     *
     * If `nsubscriptions` is 0, returns `errno::inval`.
     *
     * ```
     * (@interface func (export "poll_oneoff")
     *   ;;; The events to which to subscribe.
     *   (param $in (@witx const_pointer $subscription))
     *   ;;; The events that have occurred.
     *   (param $out (@witx pointer $event))
     *   ;;; Both the number of subscriptions and events.
     *   (param $nsubscriptions $size)
     *   ;;; The number of events stored.
     *   (result $error (expected $size (error $errno)))
     * )
     * ```
     */
    POLL_ONEOFF(
        wasmName = "poll_oneoff",
        paramTypes = listOf(
            pointerToType(Subscription), // in
            pointerToType(Event), // out
            Size.wasmValueType, // $nsubscriptions
            pointerToType(Size), // expected $size
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Terminate the process normally. An exit code of 0 indicates successful
     * termination of the program. The meanings of other values is dependent on
     * the environment.
     *
     * ```
     * (@interface func (export "proc_exit")
     *   ;;; The exit code returned by the process.
     *   (param $rval $exitcode)
     *   (@witx noreturn)
     * )
     * ```
     */
    PROC_EXIT(
        wasmName = "proc_exit",
        paramTypes = listOf(
            ExitCode.wasmValueType, // exitcode
        ),
        retType = null,
    ),

    /**
     * Send a signal to the process of the calling thread.
     * Note: This is similar to `raise` in POSIX.
     *
     * ```
     * (@interface func (export "proc_raise")
     *   ;;; The signal condition to trigger.
     *   (param $sig $signal)
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    PROC_RAISE(
        wasmName = "proc_raise",
        paramTypes = listOf(
            Signal.wasmValueType,
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Temporarily yield execution of the calling thread.
     * Note: This is similar to `sched_yield` in POSIX.
     *
     * ```
     * (@interface func (export "sched_yield")
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    SCHED_YIELD(
        wasmName = "sched_yield",
        paramTypes = listOf(),
        retType = Errno.wasmValueType,
    ),

    /**
     * Write high-quality random data into a buffer.
     * This function blocks when the implementation is unable to immediately
     * provide sufficient high-quality random data.
     * This function may execute slowly, so when large mounts of random data are
     * required, it's advisable to use this function to seed a pseudo-random
     * number generator, rather than to provide the random data directly.
     *
     * ```
     * (@interface func (export "random_get")
     *   ;;; The buffer to fill with random data.
     *   (param $buf (@witx pointer u8))
     *   (param $buf_len $size)
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    RANDOM_GET(
        wasmName = "random_get",
        paramTypes = listOf(
            witxPointer(U8), // buf
            Size.wasmValueType, // size
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Accept a new incoming connection.
     * Note: This is similar to `accept` in POSIX.
     *
     * ```
     * (@interface func (export "sock_accept")
     *   ;;; The listening socket.
     *   (param $fd $fd)
     *   ;;; The desired values of the file descriptor flags.
     *   (param $flags $fdflags)
     *   ;;; New socket connection
     *   (result $error (expected $fd (error $errno)))
     * )
     * ```
     */
    SOCK_ACCEPT(
        wasmName = "sock_accept",
        paramTypes = listOf(
            FdWasmValueType, // fd
            FdFlags.wasmValueType, // flags
            witxPointer(FdWasmValueType), // expected fd
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Receive a message from a socket.
     *
     * Note: This is similar to `recv` in POSIX, though it also supports reading
     * the data into multiple buffers in the manner of `readv`.
     *
     * ```
     * (@interface func (export "sock_recv")
     *   (param $fd $fd)
     *   ;;; List of scatter/gather vectors to which to store data.
     *   (param $ri_data $iovec_array)
     *   ;;; Message flags.
     *   (param $ri_flags $riflags)
     *   ;;; Number of bytes stored in ri_data and message flags.
     *   (result $error (expected (tuple $size $roflags) (error $errno)))
     * )
     * ```
     */
    SOCK_RECV(
        wasmName = "sock_recv",
        paramTypes = listOf(
            FdWasmValueType, // fd
            IovecArray.wasmValueType, // ri_data
            WasiValueTypes.U32, // ri_data_len
            Riflags.wasmValueType,
            pointerToType(Size), // expected size
            Roflags.wasmValueType, // expected roflags
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Send a message on a socket.
     * Note: This is similar to `send` in POSIX, though it also supports writing
     * the data from multiple buffers in the manner of `writev`.
     *
     * ```
     * (@interface func (export "sock_send")
     *   (param $fd $fd)
     *   ;;; List of scatter/gather vectors to which to retrieve data
     *   (param $si_data $ciovec_array)
     *   ;;; Message flags.
     *   (param $si_flags $siflags)
     *   ;;; Number of bytes transmitted.
     *   (result $error (expected $size (error $errno)))
     * )
     * ```
     */
    SOCK_SEND(
        wasmName = "sock_send",
        paramTypes = listOf(
            FdWasmValueType, // fd
            CiovecArray.wasmValueType, // si_data
            WasiValueTypes.U32, // si_data size
            SiFlags.wasmValueType, // si_flags
            pointerToType(Size), // expected $size
        ),
        retType = Errno.wasmValueType,
    ),

    /**
     * Shut down socket send and receive channels.
     * Note: This is similar to `shutdown` in POSIX.
     *
     * ```
     * (@interface func (export "sock_shutdown")
     *   (param $fd $fd)
     *   ;;; Which channels on the socket to shut down.
     *   (param $how $sdflags)
     *   (result $error (expected (error $errno)))
     * )
     * ```
     */
    SOCK_SHUTDOWN(
        wasmName = "sock_shutdown",
        paramTypes = listOf(
            FdWasmValueType, // fd
            Sdflags.wasmValueType, // how
        ),
        retType = Errno.wasmValueType,
    ),
    ;

    constructor(
        wasmName: String,
        @WasmValueType paramTypes: List<Int>,
        @WasmValueType retType: Int? = null,
    ) : this(
        wasmName = wasmName,
        type = HostFunction.HostFunctionType(
            params = paramTypes,
            returnTypes = if (retType != null) {
                listOf(retType)
            } else {
                emptyList()
            },
        ),
    )

    public companion object {
        public val byWasmName: Map<String, WasiHostFunction> = entries.associateBy(WasiHostFunction::wasmName)
    }
}
