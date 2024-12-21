/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.common

import arrow.core.Either
import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_FIRST_PREOPEN_FD
import at.released.weh.filesystem.model.BaseDirectory.DirectoryFd
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.test.fixtures.toVirtualPath
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.filesystem.testutil.SymlinkType
import at.released.weh.filesystem.testutil.createTestDirectory
import at.released.weh.filesystem.testutil.createTestFile
import at.released.weh.filesystem.testutil.createTestSymlink
import at.released.weh.test.ignore.annotations.IgnoreApple
import at.released.weh.test.ignore.annotations.IgnoreJvm
import at.released.weh.test.ignore.annotations.IgnoreMingw
import kotlinx.io.files.Path
import kotlin.test.Test

class PathTraversalTest : BaseFileSystemIntegrationTest() {
    @Test
    @IgnoreApple // TODO: implement
    fun relative_path_should_not_escape_sandbox() {
        val fsRoot = tempFolder.createTestDirectory("fs")
        tempFolder.apply {
            createTestDirectory("fs/dir1")
            createTestFile("fs/dir1/testfile", "testfile")
            createTestFile("unreachable.txt")
        }

        val error = tryOpenFile(fsRoot, "dir1/../../unreachable.txt".toVirtualPath()).leftOrNull()
        assertThat(error?.errno).isEqualTo(FileSystemErrno.NOTCAPABLE)
    }

    @Test
    @IgnoreJvm // TODO
    @IgnoreApple // TODO: implement
    @IgnoreMingw // TODO: implement
    fun symlink_to_file_should_not_escape_sandbox() {
        val fsRoot = tempFolder.createTestDirectory("fs")
        tempFolder.apply {
            createTestFile("unreachable.txt")
            createTestDirectory("""fs/dir1""")
            createTestSymlink(
                """../../unreachable.txt""",
                """fs/dir1/escape.link""",
                SymlinkType.SYMLINK_TO_FILE,
            )
        }
        val error = tryOpenFile(fsRoot, "dir1/escape.link".toVirtualPath()).leftOrNull()
        assertThat(error?.errno).isEqualTo(FileSystemErrno.NOTCAPABLE)
    }

    @Test
    @IgnoreJvm // TODO: implement
    @IgnoreApple // TODO: implement
    @IgnoreMingw // TODO: implement
    fun symlink_to_directory_should_not_escape_sandbox() {
        val fsRoot = tempFolder.createTestDirectory("fs")
        tempFolder.apply {
            createTestDirectory("""unreachableDir""")
            createTestFile("unreachableDir/unreachable.txt")
            createTestSymlink(
                """../unreachableDir""",
                """fs/escapeSymlink""",
                SymlinkType.SYMLINK_TO_DIRECTORY,
            )
        }
        val error = tryOpenFile(fsRoot, "escapeSymlink/unreachable.txt".toVirtualPath()).leftOrNull()
        assertThat(error?.errno).isEqualTo(FileSystemErrno.NOTCAPABLE)
    }

    @Test
    @IgnoreJvm // TODO: implement
    @IgnoreApple // TODO: implement
    @IgnoreMingw // TODO: implement
    fun symlink_to_middle_directory_should_not_escape_sandbox() {
        val fsRoot = tempFolder.createTestDirectory("fs")
        tempFolder.apply {
            createTestDirectory("""unreachableDir""")
            createTestDirectory("""unreachableDir/dir2""")
            createTestFile("""unreachableDir/dir2/unreachable.txt""")
            createTestDirectory("""fs/dir1""")
            createTestSymlink(
                """../../unreachableDir""",
                """fs/dir1/escapeSymlink""",
                SymlinkType.SYMLINK_TO_DIRECTORY,
            )
        }
        val error = tryOpenFile(fsRoot, "dir1/escapeSymlink/dir2/unreachable.txt".toVirtualPath()).leftOrNull()
        assertThat(error?.errno).isEqualTo(FileSystemErrno.NOTCAPABLE)
    }

    private fun tryOpenFile(
        fsRoot: Path,
        openPath: VirtualPath,
    ): Either<OpenError, Unit> = createTestFileSystem(
        root = fsRoot,
    ).use { fileSystem ->
        val request = Open(
            path = openPath,
            baseDirectory = DirectoryFd(WASI_FIRST_PREOPEN_FD),
            openFlags = OpenFileFlag.O_RDONLY,
            fdFlags = 0,
            mode = 0b111_000_000,
        )
        fileSystem.execute(Open, request).map { }
    }
}
