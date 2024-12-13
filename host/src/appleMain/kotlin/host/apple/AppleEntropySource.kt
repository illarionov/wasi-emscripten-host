/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.apple

import at.released.weh.host.EntropySource
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault

internal object AppleEntropySource : EntropySource {
    override fun generateEntropy(size: Int): ByteArray {
        val bytes = ByteArray(size)

        if (size == 0) {
            return bytes
        }

        val status = bytes.usePinned {
            SecRandomCopyBytes(kSecRandomDefault, size.toULong(), it.addressOf(0))
        }
        if (status != errSecSuccess) {
            error("Can not generate entropy. Status: $status")
        }
        return bytes
    }
}
