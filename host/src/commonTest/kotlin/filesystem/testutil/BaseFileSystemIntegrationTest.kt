/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.testutil

import at.released.tempfolder.sync.TempDirectory
import at.released.tempfolder.sync.createTempDirectory
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.dsl.CurrentWorkingDirectoryConfig
import at.released.weh.test.logger.TestLogger
import kotlinx.io.files.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

public abstract class BaseFileSystemIntegrationTest {
    protected val logger = TestLogger()
    internal lateinit var tempFolder: TempDirectory<*>

    @BeforeTest
    fun setup() {
        tempFolder = createTempDirectory { prefix = "weh-" }
    }

    @AfterTest
    fun cleanup() {
        tempFolder.delete()
    }

    open fun createTestFileSystem(
        root: Path = Path(tempFolder.absolutePath().asString()),
    ): FileSystem = DefaultTestFileSystem(
        engine = getDefaultTestEngine(),
    ) {
        currentWorkingDirectory = CurrentWorkingDirectoryConfig.Path(root.toString())
    }
}
