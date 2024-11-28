/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.createfile

import at.released.weh.common.ext.maskToString
import at.released.weh.filesystem.platform.windows.OBJECT_ATTRIBUTES
import at.released.weh.filesystem.platform.windows.OBJ_CASE_INSENSITIVE
import at.released.weh.filesystem.platform.windows.OBJ_INHERIT
import platform.windows.DELETE
import platform.windows.FILE_APPEND_DATA
import platform.windows.FILE_ATTRIBUTE_ARCHIVE
import platform.windows.FILE_ATTRIBUTE_COMPRESSED
import platform.windows.FILE_ATTRIBUTE_DEVICE
import platform.windows.FILE_ATTRIBUTE_DIRECTORY
import platform.windows.FILE_ATTRIBUTE_ENCRYPTED
import platform.windows.FILE_ATTRIBUTE_HIDDEN
import platform.windows.FILE_ATTRIBUTE_NORMAL
import platform.windows.FILE_ATTRIBUTE_NOT_CONTENT_INDEXED
import platform.windows.FILE_ATTRIBUTE_OFFLINE
import platform.windows.FILE_ATTRIBUTE_READONLY
import platform.windows.FILE_ATTRIBUTE_REPARSE_POINT
import platform.windows.FILE_ATTRIBUTE_SPARSE_FILE
import platform.windows.FILE_ATTRIBUTE_SYSTEM
import platform.windows.FILE_ATTRIBUTE_TEMPORARY
import platform.windows.FILE_ATTRIBUTE_VIRTUAL
import platform.windows.FILE_COMPLETE_IF_OPLOCKED
import platform.windows.FILE_CREATE
import platform.windows.FILE_CREATE_TREE_CONNECTION
import platform.windows.FILE_DELETE_ON_CLOSE
import platform.windows.FILE_DIRECTORY_FILE
import platform.windows.FILE_EXECUTE
import platform.windows.FILE_LIST_DIRECTORY
import platform.windows.FILE_NON_DIRECTORY_FILE
import platform.windows.FILE_NO_EA_KNOWLEDGE
import platform.windows.FILE_NO_INTERMEDIATE_BUFFERING
import platform.windows.FILE_OPEN
import platform.windows.FILE_OPEN_BY_FILE_ID
import platform.windows.FILE_OPEN_FOR_BACKUP_INTENT
import platform.windows.FILE_OPEN_IF
import platform.windows.FILE_OPEN_REPARSE_POINT
import platform.windows.FILE_OPEN_REQUIRING_OPLOCK
import platform.windows.FILE_OVERWRITE
import platform.windows.FILE_OVERWRITE_IF
import platform.windows.FILE_RANDOM_ACCESS
import platform.windows.FILE_READ_ATTRIBUTES
import platform.windows.FILE_READ_DATA
import platform.windows.FILE_READ_EA
import platform.windows.FILE_RESERVE_OPFILTER
import platform.windows.FILE_SEQUENTIAL_ONLY
import platform.windows.FILE_SHARE_DELETE
import platform.windows.FILE_SHARE_READ
import platform.windows.FILE_SHARE_WRITE
import platform.windows.FILE_SUPERSEDE
import platform.windows.FILE_SYNCHRONOUS_IO_ALERT
import platform.windows.FILE_SYNCHRONOUS_IO_NONALERT
import platform.windows.FILE_TRAVERSE
import platform.windows.FILE_WRITE_ATTRIBUTES
import platform.windows.FILE_WRITE_DATA
import platform.windows.FILE_WRITE_EA
import platform.windows.FILE_WRITE_THROUGH
import platform.windows.READ_CONTROL
import platform.windows.SYNCHRONIZE
import platform.windows.WRITE_DAC
import platform.windows.WRITE_OWNER

internal object WindowsNtCreateFileDebug {
    private val desiredAccessFileFlagsProperties = listOf(
        ::DELETE,
        ::FILE_READ_DATA,
        ::FILE_READ_ATTRIBUTES,
        ::FILE_READ_EA,
        ::READ_CONTROL,
        ::FILE_WRITE_DATA,
        ::FILE_WRITE_ATTRIBUTES,
        ::FILE_WRITE_EA,
        ::FILE_APPEND_DATA,
        ::WRITE_DAC,
        ::WRITE_OWNER,
        ::SYNCHRONIZE,
        ::FILE_EXECUTE,
    )
    private val desiredAccessDirectoryFlagsProperties = listOf(
        ::FILE_LIST_DIRECTORY,
        ::FILE_TRAVERSE,
        ::DELETE,
        ::FILE_READ_ATTRIBUTES,
        ::FILE_READ_EA,
        ::READ_CONTROL,
        ::FILE_WRITE_ATTRIBUTES,
        ::FILE_WRITE_EA,
        ::WRITE_DAC,
        ::WRITE_OWNER,
        ::SYNCHRONIZE,
    )
    private val fileAttributesProperties = listOf(
        ::FILE_ATTRIBUTE_READONLY,
        ::FILE_ATTRIBUTE_HIDDEN,
        ::FILE_ATTRIBUTE_SYSTEM,
        ::FILE_ATTRIBUTE_DIRECTORY,
        ::FILE_ATTRIBUTE_ARCHIVE,
        ::FILE_ATTRIBUTE_DEVICE,
        ::FILE_ATTRIBUTE_NORMAL,
        ::FILE_ATTRIBUTE_TEMPORARY,
        ::FILE_ATTRIBUTE_SPARSE_FILE,
        ::FILE_ATTRIBUTE_REPARSE_POINT,
        ::FILE_ATTRIBUTE_COMPRESSED,
        ::FILE_ATTRIBUTE_OFFLINE,
        ::FILE_ATTRIBUTE_NOT_CONTENT_INDEXED,
        ::FILE_ATTRIBUTE_ENCRYPTED,
        ::FILE_ATTRIBUTE_VIRTUAL,
    )
    private val shareAccessProperties = listOf(
        ::FILE_SHARE_READ,
        ::FILE_SHARE_WRITE,
        ::FILE_SHARE_DELETE,
    )
    private val createDispositionProperties = listOf(
        ::FILE_SUPERSEDE,
        ::FILE_CREATE,
        ::FILE_OPEN,
        ::FILE_OPEN_IF,
        ::FILE_OVERWRITE,
        ::FILE_OVERWRITE_IF,
    )
    private val createOptionsProperties = listOf(
        ::FILE_DIRECTORY_FILE,
        ::FILE_NON_DIRECTORY_FILE,
        ::FILE_WRITE_THROUGH,
        ::FILE_SEQUENTIAL_ONLY,
        ::FILE_RANDOM_ACCESS,
        ::FILE_NO_INTERMEDIATE_BUFFERING,
        ::FILE_SYNCHRONOUS_IO_ALERT,
        ::FILE_SYNCHRONOUS_IO_NONALERT,
        ::FILE_CREATE_TREE_CONNECTION,
        ::FILE_NO_EA_KNOWLEDGE,
        ::FILE_OPEN_REPARSE_POINT,
        ::FILE_DELETE_ON_CLOSE,
        ::FILE_OPEN_BY_FILE_ID,
        ::FILE_OPEN_FOR_BACKUP_INTENT,
        ::FILE_RESERVE_OPFILTER,
        ::FILE_OPEN_REQUIRING_OPLOCK,
        ::FILE_COMPLETE_IF_OPLOCKED,
    )
    private val objectAttributesProperties = listOf(
        ::OBJ_CASE_INSENSITIVE,
        ::OBJ_INHERIT,
    )

    fun ntCreateFileArgsToString(
        path: String,
        objectAttributes: OBJECT_ATTRIBUTES,
        desiredAccess: Int,
        fileAttributes: Int,
        shareAccess: Int,
        createDisposition: Int,
        createOptions: Int,
    ): String {
        val isDirectory = createOptions and FILE_DIRECTORY_FILE == FILE_DIRECTORY_FILE ||
                fileAttributes and FILE_ATTRIBUTE_DIRECTORY == FILE_ATTRIBUTE_DIRECTORY
        return "path: $path, desiredAccess: ${desiredAccessToString(desiredAccess, isDirectory)}, " +
                "objectAttributes: ${objectAttributesToString(objectAttributes.Attributes.toInt())}. " +
                "fileAttributes: ${fileAttributesToString(fileAttributes)}, " +
                "shareAccess: ${shareAccessToString(shareAccess)}, " +
                "createDisposition: ${createDispositionToString(createDisposition)}, " +
                "createOptions: ${createOptionsToString(createOptions)}"
    }

    fun desiredAccessToString(
        mask: Int,
        isDirectoryAttributes: Boolean = false,
    ): String {
        val properties = if (isDirectoryAttributes) {
            desiredAccessDirectoryFlagsProperties
        } else {
            desiredAccessFileFlagsProperties
        }
        return maskToString(mask, properties)
    }

    fun fileAttributesToString(mask: Int): String = maskToString(mask, fileAttributesProperties)

    fun shareAccessToString(mask: Int) = maskToString(mask, shareAccessProperties)

    fun createDispositionToString(mask: Int) = maskToString(mask, createDispositionProperties)

    fun createOptionsToString(mask: Int) = maskToString(mask, createOptionsProperties)

    fun objectAttributesToString(mask: Int) = maskToString(mask, objectAttributesProperties)
}
