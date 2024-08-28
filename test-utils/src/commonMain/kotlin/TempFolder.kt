/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.utils

internal expect fun createPlatformTempFolder(
    namePrefix: String = "wsohTest",
): TempFolder

public interface TempFolder {
    public val path: String
    public fun delete()

    public fun resolve(name: String): String

    public companion object {
        public fun create(
            namePrefix: String = "wsohTest",
        ): TempFolder = createPlatformTempFolder(namePrefix)
    }
}
