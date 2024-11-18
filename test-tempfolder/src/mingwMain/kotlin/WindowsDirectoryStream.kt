/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.utils

import at.released.weh.test.utils.WindowsDirectoryStream.DirectoryStreamItem.Error
import at.released.weh.test.utils.WindowsDirectoryStream.DirectoryStreamItem.FileItem
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKStringFromUtf16
import platform.windows.ERROR_NO_MORE_FILES
import platform.windows.FILE_ATTRIBUTE_DIRECTORY
import platform.windows.FILE_ATTRIBUTE_REPARSE_POINT
import platform.windows.FindClose
import platform.windows.FindFirstFileExW
import platform.windows.FindNextFileW
import platform.windows.GetLastError
import platform.windows.HANDLE
import platform.windows.INVALID_HANDLE_VALUE
import platform.windows.IO_REPARSE_TAG_SYMLINK
import platform.windows.WIN32_FIND_DATAW
import platform.windows._FINDEX_INFO_LEVELS
import platform.windows._FINDEX_SEARCH_OPS

internal class WindowsDirectoryStream(
    private val path: String,
) : AutoCloseable {
    private var childItemsHandle: HANDLE? = null
    private var findData: WIN32_FIND_DATAW = nativeHeap.alloc()
    private var isClosed: Boolean = false

    fun next(): DirectoryStreamItem {
        check(!isClosed)
        val lastError: UInt
        if (childItemsHandle == null) {
            val childPattern = combinePath(path, "*")
            childItemsHandle = FindFirstFileExW(
                lpFileName = childPattern,
                fInfoLevelId = _FINDEX_INFO_LEVELS.FindExInfoBasic,
                lpFindFileData = findData.ptr,
                fSearchOp = _FINDEX_SEARCH_OPS.FindExSearchNameMatch,
                lpSearchFilter = null,
                dwAdditionalFlags = 0U,
            )
            lastError = if (childItemsHandle == INVALID_HANDLE_VALUE) {
                GetLastError()
            } else {
                0U
            }
        } else {
            lastError = if (FindNextFileW(childItemsHandle, findData.ptr) == 0) {
                GetLastError()
            } else {
                0U
            }
        }
        return when (lastError) {
            0U -> findData.toFileItem(path)
            ERROR_NO_MORE_FILES.toUInt() -> {
                close()
                DirectoryStreamItem.EndOfStream
            }

            else -> {
                close()
                Error(path, lastError)
            }
        }
    }

    private fun WIN32_FIND_DATAW.toFileItem(rootPath: String): FileItem {
        val fileName = cFileName.toKStringFromUtf16()
        val attributesInt = dwFileAttributes.toInt()
        val fileType = when {
            attributesInt and FILE_ATTRIBUTE_DIRECTORY == FILE_ATTRIBUTE_DIRECTORY -> Filetype.DIRECTORY
            attributesInt and FILE_ATTRIBUTE_REPARSE_POINT == FILE_ATTRIBUTE_REPARSE_POINT &&
                    (dwReserved0 != IO_REPARSE_TAG_SYMLINK) -> Filetype.OTHER

            else -> Filetype.FILE
        }
        val isSymlink = attributesInt and FILE_ATTRIBUTE_REPARSE_POINT == FILE_ATTRIBUTE_REPARSE_POINT &&
                (dwReserved0 == IO_REPARSE_TAG_SYMLINK)

        return FileItem(rootPath, fileName, fileType, isSymlink)
    }

    override fun close() {
        if (isClosed) {
            return
        }
        isClosed = true

        nativeHeap.free(findData.rawPtr)

        if (childItemsHandle != null) {
            FindClose(childItemsHandle) // Ignore error
        }
    }

    enum class Filetype {
        FILE,
        DIRECTORY,
        OTHER,
    }

    sealed class DirectoryStreamItem {
        data object EndOfStream : DirectoryStreamItem()

        data class FileItem(
            val rootDir: String,
            val name: String,
            val type: Filetype,
            val isSymlink: Boolean,
        ) : DirectoryStreamItem() {
            val absolutePath: String get() = combinePath(rootDir, name)
        }

        data class Error(
            val rootDir: String,
            val lastError: UInt,
        ) : DirectoryStreamItem()
    }
}
