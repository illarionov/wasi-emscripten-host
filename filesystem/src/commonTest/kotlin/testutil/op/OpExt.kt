/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.testutil.op

import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_FIRST_PREOPEN_FD
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.op.opencreate.OpenFileFlags
import at.released.weh.filesystem.testutil.TEST_DIRECTORY_NAME
import kotlinx.io.files.Path

internal fun Open.Companion.createForTestFile(
    testfilePath: Path,
    openFlags: OpenFileFlags = OpenFileFlag.O_RDWR,
    fdflags: Fdflags = 0,
) = createTest(
    path = testfilePath.name,
    openFlags = openFlags,
    fdFlags = fdflags,
)

internal fun Open.Companion.createForTestDirectory(
    testfilePath: Path = Path(TEST_DIRECTORY_NAME),
    openFlags: OpenFileFlags = OpenFileFlag.O_DIRECTORY,
    fdflags: Fdflags = 0,
) = createTest(
    path = testfilePath.name,
    openFlags = openFlags,
    fdFlags = fdflags,
)

internal fun Open.Companion.createForTestFileOrDirectory(
    testPath: Path,
    openFlags: OpenFileFlags = OpenFileFlag.O_RDONLY,
    fdflags: Fdflags = 0,
) = createTest(
    path = testPath.name,
    openFlags = openFlags,
    fdFlags = fdflags,
)

private fun Open.Companion.createTest(
    path: String,
    baseDirectory: BaseDirectory = BaseDirectory.DirectoryFd(WASI_FIRST_PREOPEN_FD),
    openFlags: OpenFileFlags = OpenFileFlag.O_RDONLY,
    fdFlags: Fdflags = 0,
) = Open(
    path = path,
    baseDirectory = baseDirectory,
    openFlags = openFlags,
    fdFlags = fdFlags,
)
