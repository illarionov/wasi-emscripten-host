/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.fileinfo

import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import kotlinx.cinterop.CStructVar
import kotlinx.cinterop.NativePointed
import kotlinx.cinterop.interpretPointed
import platform.windows.ERROR_ACCESS_DENIED
import platform.windows.ERROR_FILE_NOT_FOUND
import platform.windows.ERROR_INVALID_HANDLE
import platform.windows.ERROR_INVALID_PARAMETER
import platform.windows.ERROR_PATH_NOT_FOUND
import platform.windows.ERROR_SHARING_VIOLATION
import platform.windows._FILE_INFO_BY_HANDLE_CLASS

internal val _FILE_INFO_BY_HANDLE_CLASS.Companion.FileStorageInfo: Int get() = 0x10
internal val _FILE_INFO_BY_HANDLE_CLASS.Companion.FileAlignmentInfo: Int get() = 0x11
internal val _FILE_INFO_BY_HANDLE_CLASS.Companion.FileIdInfo: Int get() = 0x12
internal val _FILE_INFO_BY_HANDLE_CLASS.Companion.FileIdExtdDirectoryInfo: Int get() = 0x13
internal val _FILE_INFO_BY_HANDLE_CLASS.Companion.FileIdExtdDirectoryRestartInfo: Int get() = 0x14
internal val _FILE_INFO_BY_HANDLE_CLASS.Companion.FileDispositionInfoEx: Int get() = 0x15

internal inline fun <R : Any, reified T : CStructVar> readListOfItemsByNextEntryOffset(
    buffer: NativePointed,
    bufferSize: Int,
    itemFactory: (T, maxBytes: Int) -> R?,
    nextEntryOffset: (R) -> UInt,
): List<R> {
    val resultList: MutableList<R> = mutableListOf()
    var ptr = 0
    do {
        val itemPtr: T = interpretPointed<T>(buffer.rawPtr + ptr.toLong())
        val maxBytes = bufferSize - ptr
        val item = itemFactory(itemPtr, maxBytes) ?: break
        resultList.add(item)
        ptr = nextEntryOffset(item).toInt()
    } while (ptr < bufferSize && ptr != 0)
    return resultList
}

internal fun Win32ErrorCode.getFileInfoErrorToStatError(): StatError = when (this.code.toInt()) {
    // XXX: error codes
    ERROR_ACCESS_DENIED -> AccessDenied("Can not read attributes: access denied")
    ERROR_FILE_NOT_FOUND -> NoEntry("File not found")
    ERROR_INVALID_HANDLE -> BadFileDescriptor("Bad file hande")
    ERROR_INVALID_PARAMETER -> InvalidArgument("Invalid argument")
    ERROR_PATH_NOT_FOUND -> NoEntry("Path not found")
    ERROR_SHARING_VIOLATION -> AccessDenied("Sharing violation")
    else -> IoError("Other error, code: $this")
}
