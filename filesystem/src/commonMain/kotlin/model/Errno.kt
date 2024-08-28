/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MagicNumber")

package at.released.weh.filesystem.model

/**
 * Errno from Wasi Preview 1
 */
public enum class Errno(
    public val code: Int,
) {
    /**
     * No error occurred. System call completed successfully.
     */
    SUCCESS(0),

    /**
     * Argument list too long.
     */
    TOO_BIG(1),

    /**
     * Permission denied.
     */
    ACCES(2),

    /**
     * Address in use.
     */
    ADDRINUSE(3),

    /**
     * Address not available.
     */
    ADDRNOTAVAIL(4),

    /**
     * Address family not supported.
     */
    AFNOSUPPORT(5),

    /**
     * Resource unavailable, or operation would block.
     */
    AGAIN(6),

    /**
     * Connection already in progress.
     */
    ALREADY(7),

    /**
     * Bad file descriptor.
     */
    BADF(8),

    /**
     * Bad message.
     */
    BADMSG(9),

    /**
     * Device or resource busy.
     */
    BUSY(10),

    /**
     * Operation canceled.
     */
    CANCELED(11),

    /**
     * No child processes.
     */
    CHILD(12),

    /**
     * Connection aborted.
     */
    CONNABORTED(13),

    /**
     * Connection refused.
     */
    CONNREFUSED(14),

    /**
     * Connection reset.
     */
    CONNRESET(15),

    /**
     * Resource deadlock would occur.
     */
    DEADLK(16),

    /**
     * Destination address required.
     */
    DESTADDRREQ(17),

    /**
     * Mathematics argument out of domain of function.
     */
    DOM(18),

    /**
     * Reserved.
     */
    DQUOT(19),

    /**
     * File exists.
     */
    EXIST(20),

    /**
     * Bad address.
     */
    FAULT(21),

    /**
     * File too large.
     */
    FBIG(22),

    /**
     * Host is unreachable.
     */
    HOSTUNREACH(23),

    /**
     * Identifier removed.
     */
    IDRM(24),

    /**
     * Illegal byte sequence.
     */
    ILSEQ(25),

    /**
     * Operation in progress.
     */
    INPROGRESS(26),

    /**
     * Interrupted function.
     */
    INTR(27),

    /**
     * Invalid argument.
     */
    INVAL(28),

    /**
     * I/O error.
     */
    IO(29),

    /**
     * Socket is connected.
     */
    ISCONN(30),

    /**
     * Is a directory.
     */
    ISDIR(31),

    /**
     * Too many levels of symbolic links.
     */
    LOOP(32),

    /**
     * File descriptor value too large.
     */
    MFILE(33),

    /**
     * Too many links.
     */
    MLINK(34),

    /**
     * Message too large.
     */
    MSGSIZE(35),

    /**
     * Reserved.
     */
    MULTIHOP(36),

    /**
     * Filename too long.
     */
    NAMETOOLONG(37),

    /**
     * Network is down.
     */
    NETDOWN(38),

    /**
     * Connection aborted by network.
     */
    NETRESET(39),

    /**
     * Network unreachable.
     */
    NETUNREACH(40),

    /**
     * Too many files open in system.
     */
    NFILE(41),

    /**
     * No buffer space available.
     */
    NOBUFS(42),

    /**
     * No such device.
     */
    NODEV(43),

    /**
     * No such file or directory.
     */
    NOENT(44),

    /**
     * Executable file format error.
     */
    NOEXEC(45),

    /**
     * No locks available.
     */
    NOLCK(46),

    /**
     * Reserved.
     */
    NOLINK(47),

    /**
     * Not enough space.
     */
    NOMEM(48),

    /**
     * No message of the desired type.
     */
    NOMSG(49),

    /**
     * Protocol not available.
     */
    NOPROTOOPT(50),

    /**
     * No space left on device.
     */
    NOSPC(51),

    /**
     * Function not supported.
     */
    NOSYS(52),

    /**
     * The socket is not connected.
     */
    NOTCONN(53),

    /**
     * Not a directory or a symbolic link to a directory.
     */
    NOTDIR(54),

    /**
     * Directory not empty.
     */
    NOTEMPTY(55),

    /**
     * State not recoverable.
     */
    NOTRECOVERABLE(56),

    /**
     * Not a socket.
     */
    NOTSOCK(57),

    /**
     * Not supported, or operation not supported on socket.
     */
    NOTSUP(58),

    /**
     * Inappropriate I/O control operation.
     */
    NOTTY(59),

    /**
     * No such device or address.
     */
    NXIO(60),

    /**
     * Value too large to be stored in data type.
     */
    OVERFLOW(61),

    /**
     * Previous owner died.
     */
    OWNERDEAD(62),

    /**
     * Operation not permitted.
     */
    PERM(63),

    /**
     * Broken pipe.
     */
    PIPE(64),

    /**
     * Protocol error.
     */
    PROTO(65),

    /**
     * Protocol not supported.
     */
    PROTONOSUPPORT(66),

    /**
     * Protocol wrong type for socket.
     */
    PROTOTYPE(67),

    /**
     * Result too large.
     */
    RANGE(68),

    /**
     * Read-only file system.
     */
    ROFS(69),

    /**
     * Invalid seek.
     */
    SPIPE(70),

    /**
     * No such process.
     */
    SRCH(71),

    /**
     * Reserved.
     */
    STALE(72),

    /**
     * Connection timed out.
     */
    TIMEDOUT(73),

    /**
     * Text file busy.
     */
    TXTBSY(74),

    /**
     * Cross-device link.
     */
    XDEV(75),

    /**
     * Extension: Capabilities insufficient.
     */
    NOTCAPABLE(76),

    ;

    public companion object {
        public fun fromErrNoCode(code: Int): Errno? = entries.firstNotNullOfOrNull { if (it.code == code) it else null }
    }
}
