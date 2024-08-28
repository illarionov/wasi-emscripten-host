/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.wasi.preview1.type

import at.released.weh.filesystem.model.Errno
import at.released.weh.host.base.WasmValueType

/**
 * An event that occurred.
 *
 * @param userdata User-provided value that got attached to `subscription::userdata`.
 * @param error (field $error $errno)
 * @param type The type of event that occured
 * @param fdReadwrite The contents of the event, if it is an `eventtype::fd_read` or `eventtype::fd_write`.
 * `eventtype::clock` events ignore this field.
 */
@Suppress("KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT")
public data class Event(
    val userdata: Userdata, // (field $userdata $userdata)
    val error: Errno, // (field $error $errno)
    val type: Eventtype, // (field $type $eventtype)
    val fdReadwrite: EventFdReadwrite, // (field $fd_readwrite $event_fd_readwrite)
) {
    public companion object : WasiTypename {
        override val wasmValueType: WasmValueType = WasiValueTypes.U32
    }
}
