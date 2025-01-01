/*
 * Copyright 2024-2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.errorcode

import at.released.weh.filesystem.windows.win32api.errorcode.NtStatus.Severity

/**
 * Windows NTSTATUS code.
 *
 * For more details, see [[MS-ERREF]].
 *
 * @property raw The raw NTSTATUS code.
 */
internal value class NtStatus(
    val raw: UInt,
) {
    val severity: Severity
        get() = when (raw and 0xC0_00_00_00_U) {
            0x0U -> Severity.SUCCESS
            0x40_00_00_00_U -> Severity.INFORMATIONAL
            0x80_00_00_00_U -> Severity.WARNING
            0xC0_00_00_00_U -> Severity.ERROR
            else -> error("Shouldn't be reachable")
        }

    val isCustomerDefined: Boolean get() = raw and 0x20_00_00_00_U != 0U

    val facilityCode: UInt get() = raw.shr(16) and 0x0fffU

    val code: UInt get() = raw and 0xffffU

    override fun toString(): String {
        return "NTSTATUS(0x${raw.toString(16).padStart(8, '0')})"
    }

    public enum class Severity {
        SUCCESS,
        INFORMATIONAL,
        WARNING,
        ERROR,
    }

    /**
     * Windows common NTSTATUS Values
     *
     * See [MS-ERREF] for a complete list of all possible error codes.
     */
    @Suppress("LONG_NUMERICAL_VALUES_SEPARATED")
    internal companion object NtStatusCode {
        const val STATUS_UNSUCCESSFUL: UInt = 0xC0000001U
        const val STATUS_NOT_IMPLEMENTED: UInt = 0xC0000002U
        const val STATUS_INVALID_HANDLE: UInt = 0xC0000008U
        const val STATUS_INVALID_PARAMETER: UInt = 0xC000000DU
        const val STATUS_NO_SUCH_DEVICE: UInt = 0xC000000EU
        const val STATUS_NO_SUCH_FILE: UInt = 0xC000000FU
        const val STATUS_INVALID_DEVICE_REQUEST: UInt = 0xC0000010U
        const val STATUS_END_OF_FILE: UInt = 0xC0000011U
        const val STATUS_WRONG_VOLUME: UInt = 0xC0000012U
        const val STATUS_NO_MEDIA_IN_DEVICE: UInt = 0xC0000013U
        const val STATUS_UNRECOGNIZED_MEDIA: UInt = 0xC0000014U
        const val STATUS_NONEXISTENT_SECTOR: UInt = 0xC0000015U
        const val STATUS_ALREADY_COMMITTED: UInt = 0xC0000021U
        const val STATUS_ACCESS_DENIED: UInt = 0xC0000022U
        const val STATUS_DISK_CORRUPT_ERROR: UInt = 0xC0000032U
        const val STATUS_OBJECT_NAME_INVALID: UInt = 0xC0000033U
        const val STATUS_OBJECT_NAME_NOT_FOUND: UInt = 0xC0000034U
        const val STATUS_OBJECT_NAME_COLLISION: UInt = 0xC0000035U
        const val STATUS_OBJECT_PATH_INVALID: UInt = 0xC0000039U
        const val STATUS_OBJECT_PATH_NOT_FOUND: UInt = 0xC000003AU
        const val STATUS_OBJECT_PATH_SYNTAX_BAD: UInt = 0xC000003BU
        const val STATUS_DELETE_PENDING: UInt = 0xC0000056U
        const val STATUS_DISK_FULL: UInt = 0xC000007FU
        const val STATUS_FILE_IS_A_DIRECTORY: UInt = 0xC00000BAU
        const val STATUS_NOT_SUPPORTED: UInt = 0xC00000BBU
        const val STATUS_BAD_FUNCTION_TABLE: UInt = 0xC00000FFU
        const val STATUS_VARIABLE_NOT_FOUND: UInt = 0xC0000100U
        const val STATUS_DIRECTORY_NOT_EMPTY: UInt = 0xC0000101U
        const val STATUS_FILE_CORRUPT_ERROR: UInt = 0xC0000102U
        const val STATUS_NOT_A_DIRECTORY: UInt = 0xC0000103U
        const val STATUS_NAME_TOO_LONG: UInt = 0xC0000106U
        const val STATUS_FILES_OPEN: UInt = 0xC0000107U
    }
}

internal val NtStatus.isSuccess: Boolean get() = severity != Severity.ERROR
