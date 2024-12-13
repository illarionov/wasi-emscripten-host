/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.model

@Suppress("LONG_NUMERICAL_VALUES_SEPARATED")
internal object IoStatusBlockInformation {
    internal const val FILE_SUPERSEDED: UInt = 0x00000000U
    internal const val FILE_OPENED: UInt = 0x00000001U
    internal const val FILE_CREATED: UInt = 0x00000002U
    internal const val FILE_OVERWRITTEN: UInt = 0x00000003U
    internal const val FILE_EXISTS: UInt = 0x00000004U
    internal const val FILE_DOES_NOT_EXIST: UInt = 0x00000005U
}
