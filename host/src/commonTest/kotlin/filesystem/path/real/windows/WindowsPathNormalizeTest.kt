/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real.windows

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.tableOf
import at.released.weh.filesystem.path.PathError
import kotlin.test.Test

class WindowsPathNormalizeTest {
    @Test
    fun normalizeWindowsPath_test_success_case__drive_absolute_path() {
        tableOf("path", "expectedNormalized")
            .row("""c:\""", """c:\""")
            .row("""c:\.""", """c:\""")
            .row("""c:\f.txt""", """c:\f.txt""")
            .row("""c:/f.txt\""", """c:\f.txt\""")
            .row("""c:\f.txt\""", """c:\f.txt\""")
            .row("""c:\.txt""", """c:\.txt""")
            .row("""c:\txt. ..""", """c:\txt""")
            .row("""e:\ """, """e:\ """)
            .row("""c:\.\.\a\b\df\..\..\..\e\..\\\\.\""", """c:\""")
            .forAll { path, expectedNormalized ->
                val normalized = normalizeWindowsPath(path).getOrNull()
                assertThat(normalized).isEqualTo(expectedNormalized)
            }
    }

    @Test
    fun normalizeWindowsPath_test_path_outsize_root__driver_absolute_path() {
        tableOf("path")
            .row("""c:\..""")
            .row("""d:\../""")
            .row("""e:\a\../..\""")
            .row("""f:\../a\..\..""")
            .row("""g:/..\aa\..\..\""")
            .forAll { path ->
                val normalized = normalizeWindowsPath(path).leftOrNull()
                assertThat(normalized).isNotNull().isInstanceOf<PathError.PathOutsideOfRootPath>()
            }
    }

    @Test
    fun normalizeWindowsPath_test_success_cased__relative_path() {
        tableOf("path", "expectedNormalized")
            .row("""""", """""")
            .row(""".""", """""")
            .row("""./""", """\""")
            .row("""./.""", """""")
            .row(""".///.\""", """\""")
            .row("a/././", "a\\")
            .row("""ab1\ab2\ab3\../ab4\../../ab5\..\..\ab6\..\.\ab7\""", """ab7\""")
            .forAll { path, expectedNormalized ->
                val normalized = normalizeWindowsPath(path).getOrNull()
                assertThat(normalized).isEqualTo(expectedNormalized)
            }
    }

    @Test
    fun normalizeWindowsPath_test_path_outsize_root__relative_path() {
        tableOf("path")
            .row("""..""")
            .row("""../""")
            .row("""../.""")
            .row(""".././/""")
            .row(""".././..""")
            .row("""ab/..//.//../""")
            .forAll { path ->
                val normalized = normalizeWindowsPath(path).leftOrNull()
                assertThat(normalized).isNotNull().isInstanceOf<PathError.PathOutsideOfRootPath>()
            }
    }

    @Test
    fun normalizeWindowsPath_test_success_case__drive_current_directory_relative() {
        tableOf("path", "expectedNormalized")
            .row("""C:""", """C:""")
            .row("""C:""", """C:""")
            .row("""C:fd\\///ffd\gh""", """C:fd\ffd\gh""")
            .row("""c:a/b/c/d/e\..\..\..\f/""", """c:a\b\f\""")
            .row("""c:ab\..\""", """c:\""") // XX: type of path changed. Should be handled?
            .forAll { path, expectedNormalized ->
                val normalized = normalizeWindowsPath(path).getOrNull()
                assertThat(normalized).isEqualTo(expectedNormalized)
            }
    }

    @Test
    fun normalizeWindowsPath_test_path_outsize_root__drive_current_directory_relative() {
        tableOf("path")
            .row("""..""")
            .row("""../""")
            .row("""../.""")
            .row(""".././/""")
            .row(""".././..""")
            .row("""ab/..//.//../""")
            .forAll { path ->
                val normalized = normalizeWindowsPath(path).leftOrNull()
                assertThat(normalized).isNotNull().isInstanceOf<PathError.PathOutsideOfRootPath>()
            }
    }

    @Test
    fun normalizeWindowsPath_test_success_case__current_drive_relative() {
        tableOf("path", "expectedNormalized")
            .row("""\""", """\""")
            .row("""\.""", """\""")
            .row("""\.\""", """\""")
            .row("""\.\.\""", """\""")
            .row("""\ab\cf\""", """\ab\cf\""")
            .row("""\ab\.\..\cf\\f.txt""", """\cf\f.txt""")
            .forAll { path, expectedNormalized ->
                val normalized = normalizeWindowsPath(path).getOrNull()
                assertThat(normalized).isEqualTo(expectedNormalized)
            }
    }

    @Test
    fun normalizeWindowsPath_test_path_outsize_root__current_drive_relative() {
        tableOf("path")
            .row("""\..""")
            .forAll { path ->
                val normalized = normalizeWindowsPath(path).leftOrNull()
                assertThat(normalized).isNotNull().isInstanceOf<PathError.PathOutsideOfRootPath>()
            }
    }

    @Test
    fun normalizeWindowsPath_test_success_case__unc() {
        tableOf("path", "expectedNormalized")
            .row("""\\server\share""", """\\server\share""")
            .row("""\\server\share\..\..\""", """\\""")
            .forAll { path, expectedNormalized ->
                val normalized = normalizeWindowsPath(path).getOrNull()
                assertThat(normalized).isEqualTo(expectedNormalized)
            }
    }

    @Test
    fun normalizeWindowsPath_test_path_outsize_root__unc() {
        tableOf("path")
            .row("""\\server\share\..\..\..\""")
            .forAll { path ->
                val normalized = normalizeWindowsPath(path).leftOrNull()
                assertThat(normalized).isNotNull().isInstanceOf<PathError.PathOutsideOfRootPath>()
            }
    }

    @Test
    fun normalizeWindowsPath_test_success_case__local_device_normalized() {
        tableOf("path", "expectedNormalized")
            .row("""\\.\c:\Windows""", """\\.\c:\Windows""")
            .row("""\\.\c:\Windows\..\..\""", """\\.\""")
            .forAll { path, expectedNormalized ->
                val normalized = normalizeWindowsPath(path).getOrNull()
                assertThat(normalized).isEqualTo(expectedNormalized)
            }
    }

    @Test
    fun normalizeWindowsPath_test_path_outsize_root__local_device_normalized() {
        tableOf("path")
            .row("""\\.\c:\Windows\..\..\..\""")
            .forAll { path ->
                val normalized = normalizeWindowsPath(path).leftOrNull()
                assertThat(normalized).isNotNull().isInstanceOf<PathError.PathOutsideOfRootPath>()
            }
    }

    @Test
    fun normalizeWindowsPath_test_success_case__local_device_literal() {
        // XXX this type of path should not be normalized?
        tableOf("path", "expectedNormalized")
            .row("""\\?\c:\Windows""", """\\?\c:\Windows""")
            .row("""\\?\c:\Windows\..\..\""", """\\?\""")
            .forAll { path, expectedNormalized ->
                val normalized = normalizeWindowsPath(path).getOrNull()
                assertThat(normalized).isEqualTo(expectedNormalized)
            }
    }

    @Test
    fun normalizeWindowsPath_test_path_outsize_root__local_device_literal() {
        // XXX this type of path should not be normalized?
        tableOf("path")
            .row("""\\?\c:\Windows\..\..\..\""")
            .forAll { path ->
                val normalized = normalizeWindowsPath(path).leftOrNull()
                assertThat(normalized).isNotNull().isInstanceOf<PathError.PathOutsideOfRootPath>()
            }
    }
}
