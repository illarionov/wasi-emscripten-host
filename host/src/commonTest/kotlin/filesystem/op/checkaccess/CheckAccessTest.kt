/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.checkaccess

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import at.released.weh.filesystem.error.CheckAccessError
import at.released.weh.filesystem.model.FileSystemErrno.ACCES
import at.released.weh.filesystem.model.FileSystemErrno.NOENT
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck.EXECUTABLE
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck.READABLE
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck.WRITEABLE
import at.released.weh.filesystem.test.fixtures.toVirtualPath
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.filesystem.testutil.createTestFile
import at.released.weh.filesystem.testutil.tempFolderDirectoryFd
import at.released.weh.test.ignore.annotations.IgnoreMingw
import at.released.weh.test.ignore.annotations.dynamic.DynamicIgnoreTarget.JVM_ON_WINDOWS
import at.released.weh.test.ignore.annotations.dynamic.checkIfShouldBeIgnored
import kotlin.test.Test
import kotlin.test.fail

class CheckAccessTest : BaseFileSystemIntegrationTest() {
    @Test
    fun checkaccess_success_case() {
        val testFile = tempFolder.createTestFile(size = 100)
        tableOf("mode", "useEffectiveUserId")
            .row<Set<FileAccessibilityCheck>, Boolean>(setOf(), true)
            .row(setOf(), false)
            .row(setOf(READABLE), true)
            .row(setOf(WRITEABLE), false)
            .row(setOf(READABLE, WRITEABLE), true)
            .forAll { mode, useEffectiveUserId ->
                createTestFileSystem().use { fs ->
                    val checkaccessRequest = CheckAccess(
                        path = testFile.name.toVirtualPath(),
                        baseDirectory = tempFolderDirectoryFd,
                        mode = mode,
                        useEffectiveUserId = useEffectiveUserId,
                    )
                    fs.execute(CheckAccess, checkaccessRequest).getOrElse { fail("CheckAccess() failed: $it") }
                }
            }
    }

    @Test
    fun checkaccess_on_nonexistent_should_fail() {
        createTestFileSystem().use { fs ->
            val checkaccessRequest = CheckAccess(
                path = "nonexistent.txt".toVirtualPath(),
                baseDirectory = tempFolderDirectoryFd,
                mode = setOf(),
            )
            val error: CheckAccessError? = fs.execute(CheckAccess, checkaccessRequest).leftOrNull()
            assertThat(error?.errno).isEqualTo(NOENT)
        }
    }

    @Test
    @IgnoreMingw
    fun checkaccess_executable_should_fail() {
        if (checkIfShouldBeIgnored(setOf(JVM_ON_WINDOWS))) {
            return
        }

        val testFile = tempFolder.createTestFile(size = 100)
        createTestFileSystem().use { fs ->
            val checkaccessRequest = CheckAccess(
                path = testFile.name.toVirtualPath(),
                baseDirectory = tempFolderDirectoryFd,
                mode = setOf(EXECUTABLE),
                useEffectiveUserId = true,
            )
            val error: CheckAccessError? = fs.execute(CheckAccess, checkaccessRequest).leftOrNull()
            assertThat(error?.errno).isEqualTo(ACCES)
        }
    }
}
