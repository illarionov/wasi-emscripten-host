/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path

import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import assertk.fail
import assertk.tableOf
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.path.SymlinkResolver.Subcomponent
import at.released.weh.filesystem.test.fixtures.toVirtualPath
import at.released.weh.filesystem.testutil.TestFs
import at.released.weh.filesystem.testutil.TestFs.FsNode
import at.released.weh.filesystem.testutil.TestFs.HandleType
import at.released.weh.filesystem.testutil.TestFs.HandleType.DIRECTORY
import at.released.weh.filesystem.testutil.TestFs.HandleType.FILE
import at.released.weh.filesystem.testutil.TestFs.HandleType.SYMLINK
import kotlin.test.Test

class SymlinkResolverTest {
    @Test
    fun symlinkResolver_follow_basename_success_cases() {
        val fs = TestFs().apply {
            mkFile("rootFile1.txt")
            mkSymlink("symlinkToRootFile.txt", "rootFile1.txt")
            mkSymlink("symlinkToRootDir10", "dir10")
            mkSymlink("symlinkToDir5", "dir0/dir1/dir2/dir3/dir4/dir5")
            mkSymlink("symlinkToSymlink5", "symlinkToDir5")

            mkDir("dir0/dir1") {
                mkFile("dir1File1.txt")
                mkFile("dir1File2.txt")
                mkSymlink("symlinkToRootDir10", "../../dir10")
                mkDir("dir2/dir3/dir4") {
                    mkFile("dir5/dir6/dir7/file.txt")
                    mkSymlink("symlinkTo_Root_symlinkToSymlink5", "../../../../../symlinkToSymlink5")
                }
            }
            mkDir("dir10") {
                mkFile("dir10file1.txt")
                mkSymlink("dir10SymlinkToRootFile1", "../rootFile1.txt")
            }
        }

        tableOf("path", "expectedType", "expectedPath")
            .row(".", DIRECTORY, "")
            .row("dir0", DIRECTORY, "dir0")
            .row("./dir0", DIRECTORY, "dir0")
            .row("dir0/dir1", DIRECTORY, "dir0/dir1")
            .row("dir0/dir1/", DIRECTORY, "dir0/dir1")
            .row("dir0/dir1//", DIRECTORY, "dir0/dir1")
            .row("dir0/dir1/.", DIRECTORY, "dir0/dir1")
            .row("dir0/dir1/./", DIRECTORY, "dir0/dir1")
            .row("dir0/dir1/./../", DIRECTORY, "dir0")
            .row("dir10", DIRECTORY, "dir10")
            .row("rootFile1.txt", FILE, "rootFile1.txt")
            .row(
                "dir0/dir1/dir2/dir3/dir4/dir5/dir6/dir7/file.txt",
                FILE,
                "dir0/dir1/dir2/dir3/dir4/dir5/dir6/dir7/file.txt",
            )
            .row("dir0/dir1/dir1File1.txt", FILE, "dir0/dir1/dir1File1.txt")
            .row("symlinkToRootFile.txt", FILE, "rootFile1.txt")
            .row("symlinkToRootDir10", DIRECTORY, "dir10")
            .row("symlinkToRootDir10/", DIRECTORY, "dir10")
            .row("symlinkToRootDir10/dir10file1.txt", FILE, "dir10/dir10file1.txt")
            .row("symlinkToRootDir10/dir10SymlinkToRootFile1", FILE, "rootFile1.txt")
            .row("symlinkToSymlink5", DIRECTORY, "dir0/dir1/dir2/dir3/dir4/dir5")
            .row("symlinkToSymlink5/", DIRECTORY, "dir0/dir1/dir2/dir3/dir4/dir5")
            .row("symlinkToSymlink5/../../../.././/dir1File1.txt", FILE, "dir0/dir1/dir1File1.txt")
            .row(
                "dir0/dir1/dir2/dir3/dir4/symlinkTo_Root_symlinkToSymlink5//./../../dir4/",
                DIRECTORY,
                "dir0/dir1/dir2/dir3/dir4",
            )
            .forAll { pathString: String, expectedType: HandleType, expectedPath: String ->
                val resolver: SymlinkResolver<TestFs.Handle> = SymlinkResolver(
                    base = Subcomponent.Directory(fs.rootHandle),
                    path = pathString.toVirtualPath(),
                    followBasenameSymlink = true,
                    openFunction = { base, component, _ -> testOpen(fs, base, component) },
                    closeFunction = ::testClose,
                )
                val handle: TestFs.Handle = resolver.resolve().fold(
                    ifLeft = { fail("resolve() failed for `$pathString`: $it") },
                    ifRight = { it.handle },
                )
                assertThat(handle.type).isEqualTo(expectedType)
                assertThat(handle.fullpath).isEqualTo(expectedPath)

                fs.close(handle)

                assertThat(fs.allHandlesAreClosed()).isTrue()
            }
    }

    @Test
    fun symlinkResolver_should_fail_on_symlink_loop() {
        val fs = TestFs().apply {
            mkFile("rootFile1.txt")
            mkDir("dir1") {
                mkFile("testfile1")
                mkSymlink("symlinkToParent1", "../")
                mkSymlink("symlinkToParent2", "..")
                mkSymlink("loop1_element2", "../loop1_element1")
            }
            mkSymlink("symlinkToSelf", "symlinkToSelf")
            mkSymlink("symlinkToSel2", "dir1/../symlinkToSelf2")
            mkSymlink("loop1_element1", "dir1/loop1_element2")
            mkSymlink("symlinkToDir1", "dir1")
        }

        tableOf("path")
            .row("symlinkToSelf")
            .row("symlinkToSelf/")
            .row("symlinkToSelf/..//")
            .row("loop1_element1")
            .row("symlinkToDir1/loop1_element2")
            .row("symlinkToDir1/loop1_element2/")
            .row("symlinkToDir1/loop1_element2/file.txt")
            .row("dir1/symlinkToParent1/loop1_element1")
            .row("dir1/symlinkToParent2/loop1_element1")
            .forAll { pathString ->
                val resolver: SymlinkResolver<TestFs.Handle> = SymlinkResolver(
                    base = Subcomponent.Directory(fs.rootHandle),
                    path = pathString.toVirtualPath(),
                    followBasenameSymlink = true,
                    openFunction = { base, component, _ -> testOpen(fs, base, component) },
                    closeFunction = ::testClose,
                )
                val error: FileSystemErrno? = resolver.resolve().leftOrNull()?.errno
                assertThat(error).isNotNull().isEqualTo(FileSystemErrno.LOOP)
                assertThat(fs.allHandlesAreClosed()).isTrue()
            }
    }

    @Test
    fun symlinkResolver_should_fail_on_non_directory_in_path() {
        val fs = TestFs().apply {
            mkDir("dir1") {
                mkFile("testfile1")
                mkDir("dir2")
            }
            mkSymlink("link1", "dir1/testfile1/")
            mkSymlink("link2", "dir1/testfile1/dir2")
        }

        tableOf("path")
            .row("link1")
            .row("link1/.")
            .row("link1/file2.txt")
            .row("link2")
            .row("link2/")
            .row("link2/./.././/")
            .forAll { pathString ->
                val resolver: SymlinkResolver<TestFs.Handle> = SymlinkResolver(
                    base = Subcomponent.Directory(fs.rootHandle),
                    path = pathString.toVirtualPath(),
                    followBasenameSymlink = true,
                    openFunction = { base, component, _ -> testOpen(fs, base, component) },
                    closeFunction = ::testClose,
                )
                val error: FileSystemErrno? = resolver.resolve().leftOrNull()?.errno
                assertThat(error).isNotNull().isEqualTo(FileSystemErrno.NOTDIR)
                assertThat(fs.allHandlesAreClosed()).isTrue()
            }
    }

    @Test
    fun symlinkResolver_should_fail_escaping_sandbox() {
        val fs = TestFs().apply {
            mkDir("dir1") {
                mkDir("dir2")
                mkSymlink("escapelinkdir11", "../../file1.txt")
            }
            mkSymlink("escapelink1", "../file1.txt")
            mkSymlink("escapelink2", "dir1/dir2/../.././../file1.txt")
            mkSymlink("escapelink3", "../preopen/dir1/")
            mkSymlink("escapelink4", "../")
        }

        tableOf("path")
            .row("escapelink1")
            .row("escapelink2")
            .row("escapelink3")
            .row("escapelink4")
            .row("dir1/escapelinkdir11")
            .forAll { pathString ->
                val resolver: SymlinkResolver<TestFs.Handle> = SymlinkResolver(
                    base = Subcomponent.Directory(fs.rootHandle),
                    path = pathString.toVirtualPath(),
                    followBasenameSymlink = true,
                    openFunction = { base, component, _ -> testOpen(fs, base, component) },
                    closeFunction = ::testClose,
                )
                val error: FileSystemErrno? = resolver.resolve().leftOrNull()?.errno
                assertThat(error).isNotNull().isEqualTo(FileSystemErrno.NOTCAPABLE)
                assertThat(fs.allHandlesAreClosed()).isTrue()
            }
    }

    @Test
    fun symlinkResolver_should_follow_basename() {
        val fs = TestFs().apply {
            mkDir("dir1") {
                mkDir("dir2") {
                    mkFile("testfile2.txt")
                    mkSymlink("testfile3", "dir3/testfile3.txt")
                    mkDir("dir3") {
                        mkFile("testfile3.txt")
                    }
                }
                mkSymlink("testfile2", "dir2/testfile2.txt")
                mkSymlink("testfile3", "dir2/testfile3")
            }
            mkSymlink("dir2link", "dir1/dir2")
            mkSymlink("testfile3", "dir1/testfile3")
        }

        tableOf("path", "expectedType", "expectedPath")
            .row("dir1/testfile2", FILE, "dir1/dir2/testfile2.txt")
            .row("dir1/testfile2/", FILE, "dir1/dir2/testfile2.txt") // should this success ?
            .row("dir2link", DIRECTORY, "dir1/dir2")
            .row("dir2link/", DIRECTORY, "dir1/dir2")
            .row("testfile3", FILE, "dir1/dir2/dir3/testfile3.txt")
            .forAll { pathString: String, expectedType: HandleType, expectedPath: String ->
                val resolver: SymlinkResolver<TestFs.Handle> = SymlinkResolver(
                    base = Subcomponent.Directory(fs.rootHandle),
                    path = pathString.toVirtualPath(),
                    followBasenameSymlink = true,
                    openFunction = { base, component, _ -> testOpen(fs, base, component) },
                    closeFunction = ::testClose,
                )
                val handle: TestFs.Handle = resolver.resolve().fold(
                    ifLeft = { fail("resolve() failed for `$pathString`: $it") },
                    ifRight = { it.handle },
                )
                assertThat(handle.type).isEqualTo(expectedType)
                assertThat(handle.fullpath).isEqualTo(expectedPath)

                fs.close(handle)

                assertThat(fs.allHandlesAreClosed()).isTrue()
            }
    }

    @Test
    fun symlinkResolver_should_not_follow_basename_if_not_requested() {
        val fs = TestFs().apply {
            mkDir("dir1") {
                mkDir("dir2") {
                    mkFile("testfile2.txt")
                    mkSymlink("testfile3", "dir3/testfile3.txt")
                    mkDir("dir3") {
                        mkFile("testfile3.txt")
                    }
                }
                mkSymlink("testfile2", "dir2/testfile2.txt")
                mkSymlink("testfile3", "dir2/testfile3")
            }
            mkSymlink("dir2link", "dir1/dir2")
            mkSymlink("testfile3", "dir1/testfile3")
        }

        tableOf("path", "expectedType", "expectedPath")
            .row("dir1/testfile2", SYMLINK, "dir1/testfile2")
            .row("dir1/testfile2/", SYMLINK, "dir1/testfile2")
            .row("dir2link", SYMLINK, "dir2link")
            .row("dir2link/", SYMLINK, "dir2link")
            .row("testfile3", SYMLINK, "testfile3")
            .forAll { pathString: String, expectedType: HandleType, expectedPath: String ->
                val resolver: SymlinkResolver<TestFs.Handle> = SymlinkResolver(
                    base = Subcomponent.Directory(fs.rootHandle),
                    path = pathString.toVirtualPath(),
                    followBasenameSymlink = false,
                    openFunction = { base, component, _ -> testOpen(fs, base, component) },
                    closeFunction = ::testClose,
                )
                val handle: TestFs.Handle = resolver.resolve().fold(
                    ifLeft = { fail("resolve() failed for `$pathString`: $it") },
                    ifRight = { it.handle },
                )
                assertThat(handle.type).isEqualTo(expectedType)
                assertThat(handle.fullpath).isEqualTo(expectedPath)

                fs.close(handle)

                assertThat(fs.allHandlesAreClosed()).isTrue()
            }
    }

    private fun testOpen(
        testFs: TestFs,
        base: Subcomponent.Directory<TestFs.Handle>,
        component: String,
    ): Either<OpenError, Subcomponent<TestFs.Handle>> {
        return testFs.openComponent(base.handle, component)
            .map { newHandle ->
                when (newHandle.type) {
                    DIRECTORY -> Subcomponent.Directory(newHandle)
                    FILE -> Subcomponent.Other(newHandle)
                    SYMLINK -> Subcomponent.Symlink(
                        newHandle,
                        (newHandle.node as FsNode.Symlink).target.toVirtualPath(),
                    )
                }
            }
    }

    private fun testClose(
        subcomponent: Subcomponent<TestFs.Handle>,
    ): Either<CloseError, Unit> {
        subcomponent.handle.close()
        return Unit.right()
    }
}
