/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.testutil

import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.dsl.CurrentWorkingDirectoryConfig
import at.released.weh.test.logger.TestLogger
import at.released.weh.test.utils.TempFolder
import at.released.weh.test.utils.absolutePath
import kotlinx.io.files.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

public abstract class BaseFileSystemIntegrationTest {
    protected val logger = TestLogger()
    internal lateinit var tempFolder: TempFolder

    @BeforeTest
    fun setup() {
        tempFolder = TempFolder.create()
    }

    @AfterTest
    fun cleanup() {
        tempFolder.delete()
    }

    open fun createTestFileSystem(
        root: Path = tempFolder.absolutePath(),
    ): FileSystem = DefaultTestFileSystem(
        engine = getDefaultTestEngine(),
    ) {
        currentWorkingDirectory = CurrentWorkingDirectoryConfig.Path(root.toString())
    }
}
