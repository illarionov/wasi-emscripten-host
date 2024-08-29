/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.utils

import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import platform.posix.FTW_DEPTH
import platform.posix.FTW_PHYS
import platform.posix.errno
import platform.posix.getenv
import platform.posix.mkdtemp
import platform.posix.nftw
import platform.posix.remove

internal actual fun createPlatformTempFolder(namePrefix: String): TempFolder = LinuxTempFolder.create(namePrefix)

public class LinuxTempFolder private constructor(
    override val path: String,
) : TempFolder {
    override fun delete() {
        val code = nftw(
            path,
            @Suppress("UNUSED_ANONYMOUS_PARAMETER")
            staticCFunction { path, stat, typeFlag, ftw ->
                memScoped {
                    remove(path!!.toKString())
                }
            },
            64,
            FTW_DEPTH.or(FTW_PHYS),
        )
        if (code != 0) {
            error("Can not remove $path. Error $errno")
        }
    }

    // TODO: escape name
    override fun resolve(name: String): String = "$path/$name"

    public companion object {
        public fun create(
            namePrefix: String,
        ): LinuxTempFolder {
            val tmpdir = getenv("TMPDIR")?.toKString() ?: "/tmp"
            val template = "$tmpdir/${namePrefix}XXXXXX"
            val path = memScoped {
                val nativeTemplate = template.cstr.getPointer(this)
                val newPath = mkdtemp(nativeTemplate) ?: error("Can not create temp dir: error $errno")
                newPath.toKString()
            }
            return LinuxTempFolder(path)
        }
    }
}
