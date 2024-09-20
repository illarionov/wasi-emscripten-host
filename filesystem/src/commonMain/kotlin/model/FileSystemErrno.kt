/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.model

/**
 * File system error identifiers
 */
public enum class FileSystemErrno {
    /**
     * No error occurred. System call completed successfully.
     */
    SUCCESS,

    /**
     * Argument list too long.
     */
    TOO_BIG,

    /**
     * Permission denied.
     */
    ACCES,

    /**
     * Address in use.
     */
    ADDRINUSE,

    /**
     * Address not available.
     */
    ADDRNOTAVAIL,

    /**
     * Address family not supported.
     */
    AFNOSUPPORT,

    /**
     * Resource unavailable, or operation would block.
     */
    AGAIN,

    /**
     * Connection already in progress.
     */
    ALREADY,

    /**
     * Bad file descriptor.
     */
    BADF,

    /**
     * Bad message.
     */
    BADMSG,

    /**
     * Device or resource busy.
     */
    BUSY,

    /**
     * Operation canceled.
     */
    CANCELED,

    /**
     * Disk Quota Exceeded
     */
    DQUOT,

    /**
     * File exists.
     */
    EXIST,

    /**
     * File too large.
     */
    FBIG,

    /**
     * Interrupted function.
     */
    INTR,

    /**
     * Invalid argument.
     */
    INVAL,

    /**
     * I/O error.
     */
    IO,

    /**
     * Is a directory.
     */
    ISDIR,

    /**
     * Too many levels of symbolic links.
     */
    LOOP,

    /**
     * File descriptor value too large.
     */
    MFILE,

    /**
     * Too many links.
     */
    MLINK,

    /**
     * Filename too long.
     */
    NAMETOOLONG,

    /**
     * No buffer space available.
     */
    NOBUFS,

    /**
     * No such file or directory.
     */
    NOENT,

    /**
     * Too many files open in system.
     */
    NFILE,

    /**
     * No locks available.
     */
    NOLCK,

    /**
     * No space left on device.
     */
    NOSPC,

    /**
     * Not a directory or a symbolic link to a directory.
     */
    NOTDIR,

    /**
     * Directory not empty.
     */
    NOTEMPTY,

    /**
     * Not supported, or operation not supported on socket.
     */
    NOTSUP,

    /**
     * Inappropriate I/O control operation.
     */
    NOTTY,

    /**
     * No such device or address.
     */
    NXIO,

    /**
     * Value too large to be stored in data type.
     */
    OVERFLOW,

    /**
     * Operation not permitted.
     */
    PERM,

    /**
     * Broken pipe.
     */
    PIPE,

    /**
     * Read-only file system.
     */
    ROFS,

    /**
     * Text file busy.
     */
    TXTBSY,

    /**
     * Extension: Capabilities insufficient.
     */
    NOTCAPABLE,

    ;

    public companion object {
        public val FileSystemErrno.wasiPreview1Code: Int
            get() = when (this) {
                SUCCESS -> 0
                TOO_BIG -> 1
                ACCES -> 2
                ADDRINUSE -> 3
                ADDRNOTAVAIL -> 4
                AFNOSUPPORT -> 5
                AGAIN -> 6
                ALREADY -> 7
                BADF -> 8
                BADMSG -> 9
                BUSY -> 10
                CANCELED -> 11
                DQUOT -> 19
                EXIST -> 20
                FBIG -> 22
                INTR -> 27
                INVAL -> 28
                IO -> 29
                ISDIR -> 31
                LOOP -> 32
                MFILE -> 33
                MLINK -> 34
                NAMETOOLONG -> 37
                NFILE -> 41
                NOBUFS -> 42
                NOENT -> 44
                NOLCK -> 46
                NOSPC -> 51
                NOTDIR -> 54
                NOTEMPTY -> 55
                NOTSUP -> 58
                NOTTY -> 59
                NXIO -> 60
                OVERFLOW -> 61
                PERM -> 63
                PIPE -> 64
                ROFS -> 69
                TXTBSY -> 74
                NOTCAPABLE -> 76
            }
    }
}
