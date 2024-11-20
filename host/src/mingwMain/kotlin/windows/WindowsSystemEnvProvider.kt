/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.windows

import at.released.weh.filesystem.windows.win32api.ext.readNullTerminatedCharArray
import at.released.weh.host.SystemEnvProvider
import at.released.weh.host.native.parsePosixEnvironToEnvMap
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.UShortVar
import kotlinx.cinterop.UShortVarOf
import kotlinx.cinterop.get
import kotlinx.cinterop.plus
import platform.windows.FreeEnvironmentStringsW
import platform.windows.GetEnvironmentStringsW

internal object WindowsSystemEnvProvider : SystemEnvProvider {
    override fun getSystemEnv(): Map<String, String> {
        val envStrings: CPointer<UShortVarOf<UShort>> = GetEnvironmentStringsW() ?: return emptyMap()
        val params = try {
            readEnvStrings(envStrings)
        } finally {
            FreeEnvironmentStringsW(envStrings)
        }
        return parsePosixEnvironToEnvMap(params)
    }

    private fun readEnvStrings(
        head: CPointer<UShortVar>,
    ): List<String> {
        val envStrings: MutableList<String> = mutableListOf()
        var ptr = head
        while (ptr.get(0) != 0.toUShort()) {
            val chars = ptr.readNullTerminatedCharArray()
            envStrings += chars.concatToString()
            ptr = (ptr + chars.size + 1)!!
        }
        return envStrings
    }
}
