/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.windows

import at.released.weh.host.EntropySource
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.windows.BCRYPT_USE_SYSTEM_PREFERRED_RNG
import platform.windows.BCryptGenRandom

internal object WindowsEntropySource : EntropySource {
    override fun generateEntropy(size: Int): ByteArray {
        if (size == 0) {
            return ByteArray(0)
        }
        val buf = ByteArray(size)
        val status = buf.usePinned { pinnedBytes ->
             BCryptGenRandom(
                null,
                pinnedBytes.addressOf(0).reinterpret(),
                buf.size.toUInt(),
                BCRYPT_USE_SYSTEM_PREFERRED_RNG.toUInt(),
            )
        }
        if (status != 0) {
            error("BCryptGenRandom() failed")
        }
        return buf
    }
}
