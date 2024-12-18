/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.path.real.windows

import assertk.Assert
import assertk.assertThat
import assertk.assertions.support.expected
import assertk.assertions.support.show
import assertk.tableOf
import at.released.weh.filesystem.path.real.windows.WindowsPathFixtures.WINDOWS_TEST_PATH_TYPES
import at.released.weh.filesystem.path.real.windows.WindowsPathType
import at.released.weh.filesystem.path.real.windows.WindowsPathType.CURRENT_DRIVE_RELATIVE
import at.released.weh.filesystem.path.real.windows.WindowsPathType.DRIVE_ABSOLUTE
import at.released.weh.filesystem.path.real.windows.WindowsPathType.DRIVE_CURRENT_DIRECTORY_RELATIVE
import at.released.weh.filesystem.path.real.windows.WindowsPathType.LOCAL_DEVICE_LITERAL
import at.released.weh.filesystem.path.real.windows.WindowsPathType.LOCAL_DEVICE_NORMALIZED
import at.released.weh.filesystem.path.real.windows.WindowsPathType.RELATIVE
import at.released.weh.filesystem.path.real.windows.WindowsPathType.ROOT_LOCAL_DEVICE
import at.released.weh.filesystem.path.real.windows.WindowsPathType.UNC
import at.released.weh.filesystem.path.real.windows.getWindowsPathType
import at.released.weh.host.platform.windows.test.RTL_PATH_TYPE
import at.released.weh.host.platform.windows.test.RtlDetermineDosPathNameType_U
import io.kotest.common.runBlocking
import io.kotest.property.Arb
import io.kotest.property.arbitrary.char
import io.kotest.property.arbitrary.charArray
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.placeTo
import kotlinx.cinterop.utf16
import kotlin.test.Test

class WindowsPathTypeTest {
    @Test
    fun windows_test_patch_types_should_match_RtlDetermineDosPathNameType() {
        val testPathTypes: List<Pair<String, WindowsPathType>> = WINDOWS_TEST_PATH_TYPES.flatMap { (type, patches) ->
            patches.map { it to type }
        }
        tableOf("path", "expectedType")
            .row(testPathTypes[0].first, testPathTypes[0].second)
            .apply {
                testPathTypes.drop(1).forEach { row(it.first, it.second) }
            }.forAll { path, expectedType: WindowsPathType ->
                val rtlPathType: RTL_PATH_TYPE = determineDosPathType(path)
                assertThat(expectedType).matchingRtlPathType(rtlPathType, path)
            }
    }

    @Test
    fun getWindowsPathType_should_match_RtlDetermineDosPathNameType() = runBlocking {
        val checkGen = Arb.charArray(
            Arb.int(1..TEST_PATH_MAX_LENGTH),
            Arb.char(TEST_PATH_CHARACTERS.map { it..it }),
        )

        checkAll<CharArray>(checkGen) { pathArray ->
            val path = pathArray.concatToString()
            val windowsPathType = getWindowsPathType(path)
            val rtlPathType = determineDosPathType(path)
            assertThat(windowsPathType).matchingRtlPathType(rtlPathType, path)
        }
        Unit
    }

    internal companion object {
        private const val TEST_PATH_MAX_LENGTH = 12
        val TEST_PATH_CHARACTERS: List<Char> = buildList {
            add(0.toChar())
            repeat(4) {
                addAll(listOf('\\', '/'))
            }
            addAll("""!"#${'$'}%&'()*+,-./01:;<=>?@ABCDEZ[]/^`abcdez{|}~""".toCharArray().toList())
            addAll("""ЭЫъЯ""".toCharArray().toList())
            addAll("""😊👌⛷️💅""".toCharArray().toList())
            addAll("""²é€""".toCharArray().toList())
        }

        fun Assert<WindowsPathType>.matchingRtlPathType(pathType: RTL_PATH_TYPE, path: String) = given { actual ->
            if (windowsTypeMatchingRtlPathType(actual, pathType)) {
                return
            }
            expected("for path ${show(path)} to be rtlPathType: ${show(pathType)} but was win ${show(actual)} ")
        }

        fun windowsTypeMatchingRtlPathType(
            pathType: WindowsPathType,
            rtlPathType: RTL_PATH_TYPE,
        ): Boolean {
            return rtlPathType == when (pathType) {
                DRIVE_ABSOLUTE -> RTL_PATH_TYPE.RtlPathTypeDriveAbsolute
                RELATIVE -> RTL_PATH_TYPE.RtlPathTypeRelative
                DRIVE_CURRENT_DIRECTORY_RELATIVE -> RTL_PATH_TYPE.RtlPathTypeDriveRelative
                CURRENT_DRIVE_RELATIVE -> RTL_PATH_TYPE.RtlPathTypeRooted
                UNC -> RTL_PATH_TYPE.RtlPathTypeUncAbsolute
                LOCAL_DEVICE_NORMALIZED -> RTL_PATH_TYPE.RtlPathTypeLocalDevice
                LOCAL_DEVICE_LITERAL -> RTL_PATH_TYPE.RtlPathTypeLocalDevice
                ROOT_LOCAL_DEVICE -> RTL_PATH_TYPE.RtlPathTypeRootLocalDevice
            }
        }

        fun determineDosPathType(path: String): RTL_PATH_TYPE = memScoped {
            val str = path.utf16.placeTo(this)
            RtlDetermineDosPathNameType_U(str)
        }
    }
}
