/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.utils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

internal actual fun createPlatformTempFolder(namePrefix: String): TempFolder {
    return JvmTempFolder.create(namePrefix)
}

public class JvmTempFolder private constructor(
    private val jvmPath: Path,
) : TempFolder {
    override val path: String
        get() = jvmPath.toString()

    override fun resolve(name: String): String {
        return jvmPath.resolve(name).toString()
    }

    @OptIn(ExperimentalPathApi::class)
    override fun delete() {
        jvmPath.deleteRecursively()
    }

    public companion object {
        public fun create(
            namePrefix: String,
        ): JvmTempFolder {
            val folder = Files.createTempDirectory(
                namePrefix,
                PosixFilePermissions.asFileAttribute(
                    setOf(
                        PosixFilePermission.OWNER_EXECUTE,
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE,
                    ),
                ),
            )
            return JvmTempFolder(folder)
        }
    }
}
