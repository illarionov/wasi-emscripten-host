/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.utils

import at.released.weh.test.utils.WindowsDirectoryStream.DirectoryStreamItem
import at.released.weh.test.utils.WindowsDirectoryStream.DirectoryStreamItem.EndOfStream
import at.released.weh.test.utils.WindowsDirectoryStream.DirectoryStreamItem.Error
import at.released.weh.test.utils.WindowsDirectoryStream.DirectoryStreamItem.FileItem
import at.released.weh.test.utils.WindowsDirectoryStream.Filetype.DIRECTORY
import at.released.weh.test.utils.WindowsDirectoryStream.Filetype.FILE
import at.released.weh.test.utils.WindowsDirectoryStream.Filetype.OTHER
import platform.windows.DeleteFileW
import platform.windows.ERROR_ACCESS_DENIED
import platform.windows.FILE_ATTRIBUTE_NORMAL
import platform.windows.GetLastError
import platform.windows.RemoveDirectoryW
import platform.windows.SetFileAttributesW

@Throws(WindowsIoException::class)
internal fun deleteDirectoryRecursively(
    path: String,
): Unit = BottomUpFileTreeWalker(path).use { walker ->
    while (true) {
        when (val item = walker.next()) {
            EndOfStream -> break
            is Error -> throw WindowsIoException("Failed to delete file or directory", item.lastError)
            is FileItem -> item.delete()
        }
    }
}

private fun FileItem.delete() {
    when (type) {
        FILE, OTHER -> if (DeleteFileW(absolutePath) == 0) {
            val lastErr = GetLastError()
            if (lastErr == ERROR_ACCESS_DENIED.toUInt()) {
                try {
                    stripReadOnlyAttribute(absolutePath)
                } catch (@Suppress("SwallowedException") _: WindowsIoException) {
                    // Ignore
                }
                if (DeleteFileW(absolutePath) == 0) {
                    val newLastErr = GetLastError()
                    throw WindowsIoException("Failed to delete file `$absolutePath`", newLastErr)
                }
            }
        }

        DIRECTORY -> if (RemoveDirectoryW(absolutePath) == 0) {
            val lastErr = GetLastError()
            throw WindowsIoException("Failed to delete directory `$absolutePath`", lastErr)
        }
    }
}

private fun stripReadOnlyAttribute(path: String) {
    if (SetFileAttributesW(path, FILE_ATTRIBUTE_NORMAL.toUInt()) != 0) {
        val lastErr = GetLastError()
        throw WindowsIoException("Failed to set file attributes to normal on`$path`", lastErr)
    }
}

private class BottomUpFileTreeWalker(
    val path: String,
) : AutoCloseable {
    private val stack: ArrayDeque<Pair<WindowsDirectoryStream, FileItem>> = ArrayDeque()

    init {
        val rootItem = FileItem(path, "", DIRECTORY, false)
        stack.addLast(WindowsDirectoryStream(path) to rootItem)
    }

    @Suppress("LoopWithTooManyJumpStatements")
    fun next(): DirectoryStreamItem {
        var item: DirectoryStreamItem = EndOfStream
        while (stack.isNotEmpty()) {
            val (topStream, topItemInfo) = stack.last()
            item = topStream.next()
            when (item) {
                is Error -> {
                    close()
                    break
                }

                is FileItem -> when (item.type) {
                    FILE, OTHER -> break
                    DIRECTORY -> {
                        if (item.isSymlink) {
                            break
                        }
                        if (item.name != "." && item.name != "..") {
                            val newStream = WindowsDirectoryStream(item.absolutePath)
                            stack.addLast(newStream to item)
                        }
                    }
                }

                EndOfStream -> {
                    stack.removeLast()
                    item = topItemInfo
                    break
                }
            }
        }
        return item
    }

    override fun close() {
        stack.forEach { it.first.close() }
    }
}
