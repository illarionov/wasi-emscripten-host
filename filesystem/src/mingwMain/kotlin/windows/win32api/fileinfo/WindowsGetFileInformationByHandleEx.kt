/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.fileinfo

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.platform.windows.FILE_ALIGNMENT_INFO
import at.released.weh.filesystem.platform.windows.FILE_ID_EXTD_DIR_INFO
import at.released.weh.filesystem.platform.windows.FILE_ID_INFO
import at.released.weh.filesystem.platform.windows.FILE_STORAGE_INFO
import at.released.weh.filesystem.windows.win32api.ext.readChars
import at.released.weh.filesystem.windows.win32api.model.FileAttributes
import at.released.weh.filesystem.windows.win32api.model.ReparseTag
import at.released.weh.filesystem.windows.win32api.model.errorcode.Win32ErrorCode
import kotlinx.cinterop.CStructVar
import kotlinx.cinterop.NativePointed
import kotlinx.cinterop.alignOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.interpretPointed
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import platform.windows.ERROR_ACCESS_DENIED
import platform.windows.ERROR_FILE_NOT_FOUND
import platform.windows.ERROR_INVALID_HANDLE
import platform.windows.ERROR_INVALID_PARAMETER
import platform.windows.ERROR_MORE_DATA
import platform.windows.ERROR_PATH_NOT_FOUND
import platform.windows.ERROR_SHARING_PAUSED
import platform.windows.ERROR_SHARING_VIOLATION
import platform.windows.FILE_ATTRIBUTE_TAG_INFO
import platform.windows.FILE_BASIC_INFO
import platform.windows.FILE_FULL_DIR_INFO
import platform.windows.FILE_ID_BOTH_DIR_INFO
import platform.windows.FILE_NAME_INFO
import platform.windows.FILE_STANDARD_INFO
import platform.windows.GetFileInformationByHandleEx
import platform.windows.HANDLE
import platform.windows.MAX_PATH
import platform.windows.WCHARVar
import platform.windows._FILE_INFO_BY_HANDLE_CLASS
import platform.windows._FILE_INFO_BY_HANDLE_CLASS.FileFullDirectoryInfo
import platform.windows._FILE_INFO_BY_HANDLE_CLASS.FileFullDirectoryRestartInfo
import platform.windows._FILE_INFO_BY_HANDLE_CLASS.FileIdBothDirectoryInfo
import platform.windows._FILE_INFO_BY_HANDLE_CLASS.FileIdBothDirectoryRestartInfo
import at.released.weh.filesystem.platform.windows.GetFileInformationByHandleEx as GetFileInformationByHandleMy

private val _FILE_INFO_BY_HANDLE_CLASS.Companion.FileStorageInfo: Int get() = 0x10
private val _FILE_INFO_BY_HANDLE_CLASS.Companion.FileAlignmentInfo: Int get() = 0x11
private val _FILE_INFO_BY_HANDLE_CLASS.Companion.FileIdInfo: Int get() = 0x12
private val _FILE_INFO_BY_HANDLE_CLASS.Companion.FileIdExtdDirectoryInfo: Int get() = 0x13
private val _FILE_INFO_BY_HANDLE_CLASS.Companion.FileIdExtdDirectoryRestartInfo: Int get() = 0x14

internal fun windowsGetFileBasicInfo(handle: HANDLE): Either<StatError, FileBasicInfo> = memScoped {
    val basicInfo: FILE_BASIC_INFO = alloc()
    val result = GetFileInformationByHandleEx(
        handle,
        _FILE_INFO_BY_HANDLE_CLASS.FileBasicInfo,
        basicInfo.ptr,
        sizeOf<FILE_BASIC_INFO>().toUInt(),
    )
    return if (result != 0) {
        FileBasicInfo.create(basicInfo).right()
    } else {
        Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
    }
}

internal fun windowsGetFileStandardInfo(handle: HANDLE): Either<StatError, FileStandardInfo> = memScoped {
    val standardInfo: FILE_STANDARD_INFO = alloc()
    val result = GetFileInformationByHandleEx(
        handle,
        _FILE_INFO_BY_HANDLE_CLASS.FileStandardInfo,
        standardInfo.ptr,
        sizeOf<FILE_BASIC_INFO>().toUInt(),
    )
    return if (result != 0) {
        FileStandardInfo.create(standardInfo).right()
    } else {
        Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
    }
}

// TODO: check
internal fun windowsGetFileFilename(handle: HANDLE): Either<StatError, String> {
    var maxLength = MAX_PATH * sizeOf<WCHARVar>()
    val fnSize = sizeOf<FILE_NAME_INFO>()
    val fnAlign = alignOf<FILE_NAME_INFO>()
    repeat(3) {
        memScoped {
            val totalSize = fnSize + maxLength
            val fileNameInfoBuf = alloc(totalSize, fnAlign)
            val filenameInfo: FILE_NAME_INFO = fileNameInfoBuf.reinterpret()

            val result = GetFileInformationByHandleEx(
                handle,
                _FILE_INFO_BY_HANDLE_CLASS.FileNameInfo,
                filenameInfo.ptr,
                totalSize.toUInt(),
            )

            if (result != 0) {
                // Name is not null-terminated according to MS-FSCC
                val buf =
                    filenameInfo.FileName.readChars((filenameInfo.FileNameLength / sizeOf<WCHARVar>().toULong()).toInt())
                return buf.concatToString().right()
            } else {
                val errCode = Win32ErrorCode.getLast()
                if (errCode.code != ERROR_MORE_DATA.toUInt()) {
                    return Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
                } else {
                    maxLength = filenameInfo.FileNameLength.toLong()
                }
            }
        }
    }
    return IoError("Can not get file name: max attempts reached").left()
}

internal fun windowsGetFileAttributeTagInfo(handle: HANDLE): Either<StatError, FileAttributeTagInfo> = memScoped {
    val fileAttributeTagInfo: FILE_ATTRIBUTE_TAG_INFO = alloc()
    val result = GetFileInformationByHandleEx(
        handle,
        _FILE_INFO_BY_HANDLE_CLASS.FileAttributeTagInfo,
        fileAttributeTagInfo.ptr,
        sizeOf<FILE_ATTRIBUTE_TAG_INFO>().toUInt(),
    )
    return if (result != 0) {
        FileAttributeTagInfo(
            FileAttributes(fileAttributeTagInfo.FileAttributes),
            ReparseTag(fileAttributeTagInfo.ReparseTag),
        ).right()
    } else {
        Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
    }
}

internal fun windowsGetFileIdBothDirectoryInfo(
    handle: HANDLE,
    restart: Boolean = false,
    bufferSize: Int = 64 * 1024 * 1024,
): Either<StatError, List<FileIdBothDirInfo>> = memScoped {
    val buffer = alloc(bufferSize, alignOf<FILE_ID_BOTH_DIR_INFO>())
    val firstId: FILE_ID_BOTH_DIR_INFO = buffer.reinterpret()

    val infoClass = if (restart) {
        FileIdBothDirectoryRestartInfo
    } else {
        FileIdBothDirectoryInfo
    }

    val result = GetFileInformationByHandleEx(handle, infoClass, firstId.ptr, bufferSize.toUInt())
    return if (result != 0) {
        readListOfItemsByNextEntryOffset(
            buffer,
            bufferSize,
            FileIdBothDirInfo::create,
            FileIdBothDirInfo::nextEntryOffset,
        ).right()
    } else {
        Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
    }
}

internal fun windowsGetFileIdFullDirectoryInfo(
    handle: HANDLE,
    restart: Boolean = false,
    bufferSize: Int = 64 * 1024 * 1024,
): Either<StatError, List<FileFullDirInfo>> = memScoped {
    val buffer = alloc(bufferSize, alignOf<FILE_FULL_DIR_INFO>())
    val firstId: FILE_FULL_DIR_INFO = buffer.reinterpret()

    val infoClass = if (restart) {
        FileFullDirectoryRestartInfo
    } else {
        FileFullDirectoryInfo
    }

    val result = GetFileInformationByHandleEx(handle, infoClass, firstId.ptr, bufferSize.toUInt())
    return if (result != 0) {
        readListOfItemsByNextEntryOffset(
            buffer,
            bufferSize,
            FileFullDirInfo::create,
            FileFullDirInfo::nextEntryOffset,
        ).right()
    } else {
        Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
    }
}

internal fun windowsGetFileStorageInfo(handle: HANDLE): Either<StatError, FileStorageInfo> = memScoped {
    val info: FILE_STORAGE_INFO = alloc()
    val result = GetFileInformationByHandleMy(
        handle,
        _FILE_INFO_BY_HANDLE_CLASS.FileStorageInfo,
        info.ptr,
        sizeOf<FILE_STORAGE_INFO>().toUInt(),
    )
    return if (result != 0) {
        FileStorageInfo.create(info).right()
    } else {
        Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
    }
}

internal fun windowsGetFileIdInfo(handle: HANDLE): Either<StatError, FileIdInfo> = memScoped {
    val info: FILE_ID_INFO = alloc()
    val result = GetFileInformationByHandleMy(
        handle,
        _FILE_INFO_BY_HANDLE_CLASS.FileIdInfo,
        info.ptr,
        sizeOf<FILE_ID_INFO>().toUInt(),
    )
    return if (result != 0) {
        FileIdInfo.create(info).right()
    } else {
        Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
    }
}

internal fun windowsGetAlignmentInfo(handle: HANDLE): Either<StatError, UInt> = memScoped {
    val info: FILE_ALIGNMENT_INFO = alloc()
    val result = GetFileInformationByHandleMy(
        handle,
        _FILE_INFO_BY_HANDLE_CLASS.FileAlignmentInfo,
        info.ptr,
        sizeOf<FILE_ALIGNMENT_INFO>().toUInt(),
    )
    return if (result != 0) {
        info.AlignmentRequirement.right()
    } else {
        Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
    }
}

internal fun windowsGetExtDirInfo(
    handle: HANDLE,
    restart: Boolean = false,
    bufferSize: Int = 64 * 1024 * 1024,
): Either<StatError, List<FileIdExtdDirInfo>> = memScoped {
    val buffer: NativePointed = alloc(bufferSize, alignOf<FILE_ID_EXTD_DIR_INFO>())
    val firstId: FILE_ID_EXTD_DIR_INFO = buffer.reinterpret()

    val infoClass = if (restart) {
        _FILE_INFO_BY_HANDLE_CLASS.FileIdExtdDirectoryRestartInfo
    } else {
        _FILE_INFO_BY_HANDLE_CLASS.FileIdExtdDirectoryInfo
    }

    val result = GetFileInformationByHandleMy(handle, infoClass, firstId.ptr, bufferSize.toUInt())
    return if (result != 0) {
        readListOfItemsByNextEntryOffset(
            buffer,
            bufferSize,
            FileIdExtdDirInfo::create,
            FileIdExtdDirInfo::nextEntryOffset,
        ).right()
    } else {
        Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
    }
}

private inline fun <R : Any, reified T : CStructVar> readListOfItemsByNextEntryOffset(
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

private fun Win32ErrorCode.getFileInfoErrorToStatError(): StatError = when (this.code.toInt()) {
    // TODO: error codes
    ERROR_ACCESS_DENIED -> AccessDenied("Can not read attributes: access denied")
    ERROR_FILE_NOT_FOUND -> NoEntry("File not found")
    ERROR_INVALID_HANDLE -> BadFileDescriptor("Bad file hande")
    ERROR_INVALID_PARAMETER -> InvalidArgument("Invalid argument")
    ERROR_PATH_NOT_FOUND -> NoEntry("Path not found")
    ERROR_SHARING_VIOLATION -> AccessDenied("Sharing violation")
    else -> IoError("Other error, code: $this")
}
