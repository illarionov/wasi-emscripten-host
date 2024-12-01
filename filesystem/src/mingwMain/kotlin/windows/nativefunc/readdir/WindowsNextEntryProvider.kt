/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.nativefunc.readdir

import arrow.core.getOrElse
import at.released.weh.filesystem.op.readdir.DirEntry
import at.released.weh.filesystem.posix.readdir.ReadDirResult
import at.released.weh.filesystem.posix.readdir.ReadDirResult.Companion.readDirResult
import at.released.weh.filesystem.windows.win32api.close
import at.released.weh.filesystem.windows.win32api.createfile.windowsNtOpenDirectory
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import at.released.weh.filesystem.windows.win32api.ext.combinePath
import at.released.weh.filesystem.windows.win32api.filepath.getFinalPath
import kotlinx.cinterop.alloc
import kotlinx.cinterop.free
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import platform.windows.ERROR_NO_MORE_FILES
import platform.windows.FindFirstFileExW
import platform.windows.FindNextFileW
import platform.windows.HANDLE
import platform.windows.INVALID_HANDLE_VALUE
import platform.windows.WIN32_FIND_DATAW
import platform.windows._FINDEX_INFO_LEVELS.FindExInfoBasic
import platform.windows._FINDEX_SEARCH_OPS.FindExSearchNameMatch

internal interface WindowsNextEntryProvider : AutoCloseable {
    fun readNextDir(): ReadDirResult

    companion object {
        @Suppress("ReturnCount")
        fun create(
            rootHandle: HANDLE,
        ): FirstFileResult {
            val dirHandle: HANDLE = windowsNtOpenDirectory("", rootHandle).getOrElse { openError ->
                val readDirError = openError.toReadDirError()
                return FirstFileResult.error(readDirError)
            }

            val rootPath = dirHandle.getFinalPath().getOrElse { getFinalPathError ->
                dirHandle.close().onLeft { /* ignore error */ }
                val readDirError = getFinalPathError.toReadDirError()
                return FirstFileResult.error(readDirError)
            }

            val childPattern = combinePath(rootPath, "*")

            return memScoped {
                val firstItemData: WIN32_FIND_DATAW = alloc()

                val childItemsHandle: HANDLE = FindFirstFileExW(
                    lpFileName = childPattern,
                    fInfoLevelId = FindExInfoBasic,
                    lpFindFileData = firstItemData.ptr,
                    fSearchOp = FindExSearchNameMatch,
                    lpSearchFilter = null,
                    dwAdditionalFlags = 0U,
                ) ?: INVALID_HANDLE_VALUE!!
                if (childItemsHandle == INVALID_HANDLE_VALUE) {
                    val error = Win32ErrorCode.getLast().toReadDirError()
                    dirHandle.close().onLeft { /* ignore error */ }
                    return FirstFileResult.error(error)
                }

                val firstEntry: DirEntry = readDirEntry(dirHandle, rootPath, firstItemData).getOrElse { readDirError ->
                    dirHandle.close().onLeft { /* ignore error */ }
                    return FirstFileResult.error(readDirError)
                }

                val provider = RealWindowsNextFileProvider(dirHandle, rootPath, childItemsHandle)
                FirstFileResult(firstEntry.readDirResult(), provider)
            }
        }
    }
}

private class RealWindowsNextFileProvider(
    private val rootPath: HANDLE,
    private val rootAbsolutePath: String,
    private val childItemsHandle: HANDLE,
) : WindowsNextEntryProvider {
    private var isClosed: Boolean = false
    private val findData: WIN32_FIND_DATAW = nativeHeap.alloc()

    override fun readNextDir(): ReadDirResult {
        check(!isClosed)

        val lastError = if (FindNextFileW(childItemsHandle, findData.ptr) == 0) {
            Win32ErrorCode.getLast()
        } else {
            Win32ErrorCode(0U)
        }

        return when (lastError.code) {
            0U -> readDirEntry(rootPath, rootAbsolutePath, findData).fold(
                ifLeft = {
                    close()
                    it.readDirResult()
                },
                ifRight = {
                    it.readDirResult()
                },
            )

            ERROR_NO_MORE_FILES.toUInt() -> {
                close()
                ReadDirResult.EndOfStream
            }

            else -> {
                close()
                ReadDirResult.Error(lastError.toReadDirError())
            }
        }
    }

    override fun close() {
        if (!isClosed) {
            isClosed = true
            nativeHeap.free(findData.ptr)
            childItemsHandle.close().onLeft { /* ignore error */ }
            rootPath.close().onLeft { /* ignore error */ }
        }
    }
}

internal object ClosedNextDirProvider : WindowsNextEntryProvider {
    override fun readNextDir(): ReadDirResult {
        error("Provider Closed")
    }
    override fun close() = Unit
}
