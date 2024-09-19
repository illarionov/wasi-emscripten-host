/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.linux

import at.released.weh.host.SystemEnvProvider
import at.released.weh.host.native.parsePosixEnvironToEnvMap
import kotlinx.cinterop.ByteVarOf
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.get
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.__environ

internal object LinuxSystemEnvProvider : SystemEnvProvider {
    override fun getSystemEnv(): Map<String, String> {
        val envVariables: MutableList<String> = mutableListOf()
        val env: CPointer<CPointerVarOf<CPointer<ByteVarOf<Byte>>>> = __environ ?: return emptyMap()
        var index = 0
        while (true) {
            val current = env[index] ?: break
            envVariables += current.toKStringFromUtf8()
            index += 1
        }
        return parsePosixEnvironToEnvMap(envVariables)
    }
}
