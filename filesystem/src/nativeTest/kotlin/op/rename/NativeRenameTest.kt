/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.rename

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.model.FdFlag.FD_APPEND
import at.released.weh.filesystem.op.fdattributes.FdAttributes
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadFd
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.filesystem.testutil.createTestFile
import at.released.weh.filesystem.testutil.tempFolderDirectoryFd
import at.released.weh.test.filesystem.assertions.isNotExists
import at.released.weh.test.ignore.annotations.IgnoreMingw
import kotlin.test.Test
import kotlin.test.fail

@IgnoreMingw
public class NativeRenameTest : BaseFileSystemIntegrationTest() {
    @Test
    public fun overwritten_opened_file_should_be_readable() {
        val srcFile = tempFolder.createTestFile()
        val dstFile = tempFolder.createTestFile("dstfile.txt", "dstContent")

        createTestFileSystem().use { fileSystem ->
            fileSystem.execute(
                Open,
                Open(srcFile.name, tempFolderDirectoryFd, OpenFileFlag.O_RDONLY, 0),
            ).getOrElse { fail("Can not open source file") }
            val openedDstFile = fileSystem.execute(
                Open,
                Open(dstFile.name, tempFolderDirectoryFd, OpenFileFlag.O_RDONLY, FD_APPEND),
            ).getOrElse { fail("Can not open destination file") }

            fileSystem.execute(
                Rename,
                Rename(tempFolderDirectoryFd, srcFile.name, tempFolderDirectoryFd, dstFile.name),
            ).onLeft { fail("Can not rename file: $it") }

            @Suppress("MagicNumber")
            val bbuf = FileSystemByteBuffer(ByteArray(100))

            val readBytes: ULong = fileSystem.execute(
                ReadFd,
                ReadFd(openedDstFile, listOf(bbuf), ReadWriteStrategy.CurrentPosition),
            ).getOrElse { fail("Can not read renamed file: $it") }

            assertThat(srcFile).isNotExists()
            assertThat(readBytes.toInt()).isEqualTo("dstContent".encodeToByteArray().size)

            val fdAttributes = fileSystem.execute(
                FdAttributes,
                FdAttributes(openedDstFile),
            ).getOrElse { fail("Can not get attributes of renamed file: $it") }
            assertThat(fdAttributes.flags).isEqualTo(FD_APPEND)
        }
    }
}
