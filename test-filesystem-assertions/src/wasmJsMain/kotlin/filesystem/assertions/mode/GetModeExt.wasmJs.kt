/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.filesystem.assertions.mode

import kotlinx.io.files.Path

public actual val Path.isPosixFileModeSupported: Boolean get() = true

internal actual fun Path.getFileMode(): Set<PosixFileModeBit> {
    TODO("Not yet implemented")
}
