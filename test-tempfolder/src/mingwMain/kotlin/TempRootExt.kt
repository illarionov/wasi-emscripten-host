/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.utils

import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.io.IOException
import platform.windows.GetLastError
import platform.windows.GetTempPathW
import platform.windows.MAX_PATH
import platform.windows.WCHARVar

private const val MAX_ATTEMPTS = 100

@Throws(IOException::class)
internal fun resolveTempRoot(): String {
    val path = getTempRoot()
    createDirectory(path)
    return path
}

@Throws(IOException::class)
private fun getTempRoot(): String {
    var length = MAX_PATH
    repeat(MAX_ATTEMPTS) {
        when (val result = getTempPath(length)) {
            is GetTempPathResult.Success -> return result.path
            is GetTempPathResult.Error -> throw getTempPathToWindowsIoException(result.lastError)
            is GetTempPathResult.BufferToSmall -> length = result.requiredSize + 1
        }
    }
    throw IOException("Can not create temp path, max attempts reached")
}

private fun getTempPath(length: Int): GetTempPathResult = memScoped {
    val buffer: CArrayPointer<WCHARVar> = allocArray(length)
    val charsCopied: Int = GetTempPathW(length.toUInt(), buffer).toInt()
    when {
        charsCopied == 0 -> GetTempPathResult.Error(GetLastError())
        charsCopied <= length -> {
            val pathCharArray = CharArray(charsCopied) { index ->
                buffer[index].toInt().toChar()
            }
            val path = pathCharArray.concatToString()
            GetTempPathResult.Success(path)
        }

        else -> GetTempPathResult.BufferToSmall(charsCopied)
    }
}

private fun getTempPathToWindowsIoException(lastError: UInt): WindowsIoException = WindowsIoException(
    "Windows error. Code: 0x${lastError.toString(16).padStart(8, '0')}`",
    lastError,
)

private sealed class GetTempPathResult {
    class Error(val lastError: UInt) : GetTempPathResult()
    class BufferToSmall(val requiredSize: Int) : GetTempPathResult()
    class Success(val path: String) : GetTempPathResult()
}
