/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.windows.ext

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.host.platform.windows.MultiByteToWideChar
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKStringFromUtf16
import platform.windows.CP_ACP
import platform.windows.CP_MACCP
import platform.windows.CP_OEMCP
import platform.windows.CP_THREAD_ACP
import platform.windows.CP_UTF8
import platform.windows.ERROR_INSUFFICIENT_BUFFER
import platform.windows.ERROR_INVALID_FLAGS
import platform.windows.ERROR_INVALID_PARAMETER
import platform.windows.ERROR_NO_UNICODE_TRANSLATION
import platform.windows.GetLastError
import platform.windows.MB_ERR_INVALID_CHARS
import platform.windows.WCHARVar

internal fun CPointer<ByteVar>.toKStringFromLocalCodepage(
    codepage: SourceCodepage = SourceCodepage.ANSI,
): Either<NonUnicodeReadError, String> {
    val requiredChars = MultiByteToWideChar(
        codepage.windowsCode,
        MB_ERR_INVALID_CHARS.toUInt(),
        this,
        -1,
        null,
        0,
    )
    if (requiredChars == 0) {
        return NonUnicodeReadError.fromLastError().left()
    }

    return memScoped {
        val buf: CPointer<WCHARVar> = allocArray(requiredChars)
        val writtenChars = MultiByteToWideChar(
            codepage.windowsCode,
            MB_ERR_INVALID_CHARS.toUInt(),
            this@toKStringFromLocalCodepage,
            -1,
            buf,
            requiredChars,
        )
        if (writtenChars != 0) {
            buf.toKStringFromUtf16().right()
        } else {
            NonUnicodeReadError.fromLastError().left()
        }
    }
}

internal enum class SourceCodepage(val windowsCode: UInt) {
    ANSI(CP_ACP.toUInt()),
    ANSI_CURRENT_THREAD(CP_THREAD_ACP.toUInt()),
    MAC(CP_MACCP.toUInt()),
    OEM(CP_OEMCP.toUInt()),
    UTF8(CP_UTF8.toUInt()),
}

internal enum class NonUnicodeReadError {
    INSUFFICIENT_BUFFER,
    INVALID_FLAGS,
    INVALID_PARAMETER,
    NO_UNICODE_TRANSLATION,
    OTHER,
    ;

    companion object {
        fun fromLastError(lastError: UInt = GetLastError()): NonUnicodeReadError = when (lastError.toInt()) {
            ERROR_INSUFFICIENT_BUFFER -> INSUFFICIENT_BUFFER
            ERROR_INVALID_FLAGS -> INVALID_FLAGS
            ERROR_INVALID_PARAMETER -> INVALID_PARAMETER
            ERROR_NO_UNICODE_TRANSLATION -> NO_UNICODE_TRANSLATION
            else -> OTHER
        }
    }
}
