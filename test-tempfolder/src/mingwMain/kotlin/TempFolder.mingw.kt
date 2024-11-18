/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.utils

import kotlinx.io.IOException

internal actual fun createPlatformTempFolder(namePrefix: String): TempFolder {
    return WindowsTempFolder.create(namePrefix)
}

public class WindowsTempFolder private constructor(
    override val path: String,
) : TempFolder {
    override fun delete() {
        deleteDirectoryRecursively(path)
    }

    // TODO: escape name?
    override fun resolve(name: String): String = combinePath(path, name)

    public companion object {
        private const val MAX_ATTEMPTS = 100

        public fun create(
            namePrefix: String,
        ): WindowsTempFolder {
            val tempPath = resolveTempRoot()
            repeat(MAX_ATTEMPTS) {
                val tempDirectoryPath = tempPath + generateTempDirectoryName(namePrefix)
                val directoryCreated = createDirectory(tempDirectoryPath)
                if (directoryCreated) {
                    return WindowsTempFolder(tempDirectoryPath)
                }
            }
            throw IOException("Can not create directory: max attempts reached")
        }
    }
}
