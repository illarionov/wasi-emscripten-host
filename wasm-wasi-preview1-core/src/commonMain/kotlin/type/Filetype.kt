/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MagicNumber")

package at.released.weh.wasi.preview1.type

import at.released.weh.wasi.preview1.WasiTypename
import at.released.weh.wasi.preview1.type.WasiValueTypes.U8
import at.released.weh.wasm.core.WasmValueType

/**
 * The type of a file descriptor or file.
 */
public enum class Filetype(
    public val id: Int,
) {
    /**
     * The type of the file descriptor or file is unknown or is different from any of the other types specified.
     */
    UNKNOWN(0),

    /**
     * The file descriptor or file refers to a block device inode.
     */
    BLOCK_DEVICE(1),

    /**
     * The file descriptor or file refers to a character device inode.
     */
    CHARACTER_DEVICE(2),

    /**
     * The file descriptor or file refers to a directory inode.
     */
    DIRECTORY(3),

    /**
     * The file descriptor or file refers to a regular file inode.
     */
    REGULAR_FILE(4),

    /**
     * The file descriptor or file refers to a datagram socket.
     */
    SOCKET_DGRAM(5),

    /**
     * The file descriptor or file refers to a byte-stream socket.
     */
    SOCKET_STREAM(6),

    /**
     * The file refers to a symbolic link inode.
     */
    SYMBOLIC_LINK(7),

    ;

    public companion object : WasiTypename {
        @WasmValueType
        override val wasmValueType: Int = U8
    }
}
