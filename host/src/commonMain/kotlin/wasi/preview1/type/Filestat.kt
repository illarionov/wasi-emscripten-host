/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.wasi.preview1.type

import at.released.weh.host.base.WasmValueType

/**
 * File attributes.
 */
@Suppress("KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT")
public data class Filestat(
    /**
     * Device ID of device containing the file.
     */
    val dev: Device, // (field $dev $device)

    /**
     * File serial number.
     */
    val ino: Inode, // (field $ino $inode)

    /**
     * File type.
     */
    val fileType: Filetype, // (field $filetype $filetype)

    /**
     * Number of hard links to the file.
     */
    val nlink: Linkcount, // (field $nlink $linkcount)

    /**
     * For regular files, the file size in bytes. For symbolic links, the length in bytes of the pathname contained
     * in the symbolic link.
     */
    val size: FileSize, // (field $size $filesize)

    /**
     * Last data access timestamp.
     *
     * This can be 0 if the underlying platform doesn't provide suitable
     * timestamp for this file.
     */
    val atim: Timestamp, // (field $atim $timestamp)

    /**
     * Last data modification timestamp.
     *
     * This can be 0 if the underlying platform doesn't provide suitable
     * timestamp for this file.
     */
    val mtim: Timestamp, // (field $mtim $timestamp)

    /**
     * Last file status change timestamp.
     *
     * This can be 0 if the underlying platform doesn't provide suitable
     * timestamp for this file.
     */
    val ctim: Timestamp, // (field $ctim $timestamp)
) {
    public companion object : WasiTypename {
        public override val wasmValueType: WasmValueType = WasmValueType.I32
    }
}
