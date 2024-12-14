/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real.windows

import at.released.weh.filesystem.path.real.windows.WindowsPathType.CURRENT_DRIVE_RELATIVE
import at.released.weh.filesystem.path.real.windows.WindowsPathType.DRIVE_ABSOLUTE
import at.released.weh.filesystem.path.real.windows.WindowsPathType.DRIVE_CURRENT_DIRECTORY_RELATIVE
import at.released.weh.filesystem.path.real.windows.WindowsPathType.LOCAL_DEVICE_LITERAL
import at.released.weh.filesystem.path.real.windows.WindowsPathType.LOCAL_DEVICE_NORMALIZED
import at.released.weh.filesystem.path.real.windows.WindowsPathType.RELATIVE
import at.released.weh.filesystem.path.real.windows.WindowsPathType.ROOT_LOCAL_DEVICE
import at.released.weh.filesystem.path.real.windows.WindowsPathType.UNC

internal object WindowsPathFixtures {
    val WINDOWS_TEST_PATH_TYPES: Map<WindowsPathType, List<String>> = mapOf(
        DRIVE_ABSOLUTE to listOf(
            """f:\""",
            """@:/""",
            """Ы:\Disk""",
            """C:\Program Files (x86)""",
            """D:/${'$'}RECYCLE.BIN\""",
            """E:\Documents And Settings\desktop.ini""",
        ),
        RELATIVE to listOf(
            "",
            " ",
            ".",
            "..",
            "..\\",
            ":",
            "\u0000",
            "\u0000:",
            "\u0000:\\",
            "c",
            """.\Public""",
            """../Users""",
            """Windows\System32/LogFiles""",
            """@t\emp""",
            """ c:\""",
            "🤔:readme.txt",
        ),
        DRIVE_CURRENT_DIRECTORY_RELATIVE to listOf(
            """d:""",
            """C:Public""",
            """^:Users/""",
        ),
        CURRENT_DRIVE_RELATIVE to listOf(
            """/""",
            """/:""",
            """/:/""",
            """/:E""",
            """/:\""",
            """\""",
            """\:""",
            """\:-)""",
            """\:/""",
            """\:\""",
            """/dev/null""",
            """\Documents And Settings""",
            """\??\""",
            """\??\C:\Windows\System32""",
        ),
        UNC to listOf(
            """\\server\share\ABC\DEF""",
            """//server/share\ABC\DEF""",
            """//..""",
            """\\""",
            """\\\""",
            """//""",
            """///""",
            """\/\/\ \/\/\""",
            "//? /Эz",
            "//? \\Эz",
        ),
        LOCAL_DEVICE_NORMALIZED to listOf(
            """\\.\""",
            """\\.\C:\Windows/System32/../""",
            """\\.\\Windows/""",
        ),
        LOCAL_DEVICE_LITERAL to listOf(
            """\\?\""",
            """\\?\C:\Windows\System32/""",
            """\\?\UNC\server\${'$'}are""",
            """\\?\Volume{2811f533-b467-44f5-be1e-d45bad174114}\Users\Work\AppData\Local\Temp\wehTestiawi2z\test.txt""",
        ),
        ROOT_LOCAL_DEVICE to listOf(
            """\\?""",
            "//.\u0000",
            "//.\u0000\\server\\share",
            "//?\u0000/Эz",
            "//?\u0000 /Эz",
            "//.\u0000/Эz",
            """\\.""",
            """//.""",
            """/\.""",
        ),
    )
}
