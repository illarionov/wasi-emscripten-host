/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.test.fixtures.stdio

import at.released.weh.filesystem.stdio.SinkProvider
import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlinx.io.readString

public class TestSinkProvider : SinkProvider {
    public val sink: TestSink = TestSink()

    override fun open(): RawSink = sink

    public fun readContent(): String = sink.buffer.readString()

    public class TestSink : RawSink {
        public val buffer: Buffer = Buffer()

        override fun close(): Unit = Unit

        override fun flush(): Unit = Unit

        override fun write(source: Buffer, byteCount: Long) {
            buffer.write(source, byteCount)
        }
    }
}
