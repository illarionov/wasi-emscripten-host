/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.linux

import at.released.weh.host.native.NativeEnvironBasedEnvProvider
import kotlinx.cinterop.ByteVarOf
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVarOf
import platform.posix.__environ

internal object LinuxSystemEnvProvider : NativeEnvironBasedEnvProvider() {
    override fun getEnviron(): CPointer<CPointerVarOf<CPointer<ByteVarOf<Byte>>>>? = __environ
}
