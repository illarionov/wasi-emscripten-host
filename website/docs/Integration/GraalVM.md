---
sidebar_label: 'GraalWasm'
sidebar_position: 3
description: 'Implementation of Emscripten host functions for GraalWasm'
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# GraalWasm Integration

This integration allows you to run WebAssembly binaries that use either WASI Preview 1 or Emscripten functions 
on the JVM with [GraalWasm].

## Requirements

* JVM JDK 23+ (it may also work on JDK21+)
* GraalVM SDK Polyglot API 24.1.X

The current implementation heavily relies on internal GraalWasm APIs, making it compatible only with the
[GraalVM SDK Polyglot API 24.1.X][Polyglot API 24] for JDK23.

## WASI Preview 1 Bindings Integration

Check [WASI Preview 1](../WASIP1) to see the limitations of the WASI P1 implementation.

### Installation

Add the required dependencies:

<Tabs>
    <TabItem value="gradle" label="Gradle" default>

```kotlin
dependencies {
    implementation("at.released.weh:bindings-graalvm241-wasip1:0.2")
    implementation("org.graalvm.polyglot:polyglot:24.1.1")
    implementation("org.graalvm.polyglot:wasm:24.1.1")
}
```
    </TabItem>

    <TabItem value="maven" label="Maven">
```xml
<dependencies>
    <dependency>
        <groupId>org.graalvm.polyglot</groupId>
        <artifactId>polyglot</artifactId>
        <version>24.1.1</version>
        <type>jar</type>
    </dependency>
    <dependency>
        <groupId>org.graalvm.polyglot</groupId>
        <artifactId>wasm</artifactId>
        <version>24.1.1</version>
        <type>pom</type>
    </dependency>
    <dependency>
        <groupId>at.released.weh</groupId>
        <artifactId>bindings-graalvm241-emscripten-wasip1</artifactId>
        <version>0.1</version>
    </dependency>
</dependencies>
```
    </TabItem>
</Tabs>

### Usage

Below is an example demonstrating the execution of **helloworld.wasm**, build using Emscripten with the `STANDALONE_WASM` flag.

<Tabs>
    <TabItem value="kotlin" label="Kotlin" default>

```kotlin
import at.released.weh.bindings.graalvm241.wasip1.GraalvmWasiPreview1Builder
import at.released.weh.host.EmbedderHost
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.PolyglotException
import org.graalvm.polyglot.Source

internal object App

const val HELLO_WORLD_MODULE_NAME: String = "helloworld"

fun main() {
    // Create Host and run code
    EmbedderHost {
        fileSystem {
            addPreopenedDirectory(".", "/data")
        }
    }.use(::executeCode)
}

private fun executeCode(embedderHost: EmbedderHost) {
    // Prepare Source
    val source = Source.newBuilder("wasm", App::class.java.getResource("helloworld_wasi.wasm"))
        .name(HELLO_WORLD_MODULE_NAME)
        .build()

    // Setup Polyglot Context
    val context: Context = Context.newBuilder().build()
    context.use {
        // Context must be initialized before installing modules
        context.initialize("wasm")

        // Setup WASI Preview 1 module
        GraalvmWasiPreview1Builder {
            host = embedderHost
        }.build(context)

        // Evaluate the WebAssembly module
        context.eval(source)

        // Run code
        val startFunction = context.getBindings("wasm").getMember(HELLO_WORLD_MODULE_NAME).getMember("_start")

        try {
            startFunction.execute()
        } catch (re: PolyglotException) {
            if (re.message?.startsWith("Program exited with status code") == false) {
                throw re
            } else {
                // Handle exit code
            }
            Unit
        }
    }
}
```

    </TabItem>

    <TabItem value="java" label="Java">

```java
import at.released.weh.bindings.graalvm241.wasip1.GraalvmWasiPreview1Builder;
import at.released.weh.host.EmbedderHost;
import at.released.weh.host.EmbedderHostBuilder;
import java.io.IOException;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;

public class App {
    private static final String HELLO_WORLD_MODULE_NAME = "helloworld";

    public static void main(String[] args) throws Exception {
        new App().start();
    }

    public void start() throws Exception {
        // Prepare source
        var source = Source.newBuilder("wasm", App.class.getResource("helloworld_wasi.wasm"))
                .name(HELLO_WORLD_MODULE_NAME)
                .build();

        // Prepare Host
        var hostBuilder = new EmbedderHostBuilder();
        hostBuilder.fileSystem().addPreopenedDirectory(".", "/data");
        try (var embedderHost = hostBuilder.build()) {
            executeWasmCode(embedderHost, source);
        }
    }

    private void executeWasmCode(EmbedderHost embedderHost, Source wasmSource) throws IOException {
        // Setup Polyglot Context
        try (var context = Context.newBuilder("wasm").build()) {
            // Context must be initialized before installing modules
            context.initialize("wasm");

            // Setup WASI Preview 1 module
            new GraalvmWasiPreview1Builder().setHost(embedderHost).build(context);

            // Evaluate the WebAssembly module
            context.eval(wasmSource);

            // Execute code
            var startFunction = context.getBindings("wasm").getMember(HELLO_WORLD_MODULE_NAME).getMember("_start");
            try {
                startFunction.execute();
            } catch (PolyglotException re) {
                if (re.getMessage() == null || !re.getMessage().startsWith("Program exited with status code")) {
                    throw re;
                }
            }
        }
    }
}
```

    </TabItem>
</Tabs>

### GraalVM's Built-in WASI Preview 1 Functions

It is worth noting that GraalWasm provides its own implementation of the WASI Preview 1 interfaces.  
In many cases, it may be a more suitable choice rather than this library.

To use it, you should add the `wasm.Builtins` option with the value `wasi_snapshot_preview1`.

Below is an example of running **helloworld.wasm** using the built-in implementation:

```kotlin
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Source

const val HELLO_WORLD_MODULE_NAME: String = "helloworld"

fun main() {
    val source = Source.newBuilder("wasm", App::class.java.getResource("helloworld.wasm"))
        .name(HELLO_WORLD_MODULE_NAME)
        .build()
    val context: Context = Context.newBuilder()
        .option("wasm.Builtins", "wasi_snapshot_preview1")
        .build()
    context.use {
        context.eval(source)
        run(context)
    }
}
```

See also example in documentation: [GraalVM: Running WebAssembly Embedded in Java][graalvm-running-webassembly-embedded-in-java]

## Emscripten bindings integration

### Installation

Add the required dependencies:

<Tabs>
    <TabItem value="gradle" label="Gradle" default>

```kotlin
dependencies {
    implementation("at.released.weh:bindings-graalvm241-emscripten:0.2")
    implementation("org.graalvm.polyglot:polyglot:24.1.1")
    implementation("org.graalvm.polyglot:wasm:24.1.1")
}
```
    </TabItem>

    <TabItem value="maven" label="Maven">
```xml
<dependencies>
    <dependency>
        <groupId>org.graalvm.polyglot</groupId>
        <artifactId>polyglot</artifactId>
        <version>24.1.1</version>
        <type>jar</type>
    </dependency>
    <dependency>
        <groupId>org.graalvm.polyglot</groupId>
        <artifactId>wasm</artifactId>
        <version>24.1.1</version>
        <type>pom</type>
    </dependency>
    <dependency>
        <groupId>at.released.weh</groupId>
        <artifactId>bindings-graalvm241-emscripten-jvm</artifactId>
        <version>0.1</version>
    </dependency>
</dependencies>
```
    </TabItem>
</Tabs>

### Usage

Below is an example demonstrating the execution of **helloworld.wasm**, prepared 
in the "[Emscripten Example](../Emscripten#example)".

<Tabs>
    <TabItem value="kotlin" label="Kotlin" default>

```kotlin
import at.released.weh.bindings.graalvm241.GraalvmHostFunctionInstaller
import at.released.weh.host.EmbedderHost
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Source

internal object App

const val HELLO_WORLD_MODULE_NAME: String = "helloworld"

fun main() {
    // Create Host and run code
    EmbedderHost {
        fileSystem {
            unrestricted = true
        }
    }.use(::executeCode)
}

private fun executeCode(embedderHost: EmbedderHost) {
    // Prepare Source
    val source = Source.newBuilder("wasm", App::class.java.getResource("helloworld.wasm"))
        .name(HELLO_WORLD_MODULE_NAME)
        .build()

    // Setup Polyglot Context
    val context: Context = Context.newBuilder().build()
    context.use {
        // Context must be initialized before installing modules
        context.initialize("wasm")

        // Setup modules
        val installer = GraalvmHostFunctionInstaller(context) {
            host = embedderHost
        }
        installer.setupWasiPreview1Module()
        val emscriptenInstaller = installer.setupEmscriptenFunctions()

        // Evaluate the WebAssembly module
        context.eval(source)

        // Finish initialization after module instantiation
        emscriptenInstaller.finalize(HELLO_WORLD_MODULE_NAME).use { emscriptenEnv ->
            // Initialize Emscripten runtime environment
            emscriptenEnv.emscriptenRuntime.initMainThread()
            run(context)
        }
    }
}

private fun run(
    context: Context,
) {
    val mainFunction = context.getBindings("wasm").getMember(HELLO_WORLD_MODULE_NAME).getMember("main")
    mainFunction.execute(
        /* argc */ 0,
        /* argv */ 0,
    ).asInt()
}
```
    </TabItem>

    <TabItem value="java" label="Java">

```java
import at.released.weh.bindings.graalvm241.GraalvmHostFunctionInstaller;
import at.released.weh.host.EmbedderHost;
import at.released.weh.host.EmbedderHostBuilder;
import java.io.IOException;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

public class App {
    private static final String HELLO_WORLD_MODULE_NAME = "helloworld";

    public static void main(String[] args) throws Exception {
        new App().start();
    }

    public void start() throws Exception {
        // Prepare source
        var source = Source.newBuilder("wasm", App.class.getResource("helloworld.wasm"))
                .name(HELLO_WORLD_MODULE_NAME)
                .build();

        // Prepare Host
        var hostBuilder = new EmbedderHostBuilder();
        hostBuilder.fileSystem().setUnrestricted(true);
        try (var embedderHost = hostBuilder.build()) {
            executeWasmCode(embedderHost, source);
        }
    }

    private void executeWasmCode(EmbedderHost embedderHost, Source wasmSource) throws IOException {
        // Setup Polyglot Context
        try (var context = Context.newBuilder("wasm").build()) {
            // Context must be initialized before installing modules
            context.initialize("wasm");

            // Setup modules
            var installer = new GraalvmHostFunctionInstaller.Builder(context).setHost(embedderHost).build();
            installer.setupWasiPreview1Module();

            var emscriptenInstaller = installer.setupEmscriptenFunctions();

            // Evaluate the WebAssembly module
            context.eval(wasmSource);

            // Finish initialization after module instantiation
            try (var emscriptenEnvironment = emscriptenInstaller.finalize(HELLO_WORLD_MODULE_NAME)) {
                // Initialize Emscripten runtime environment
                emscriptenEnvironment.getEmscriptenRuntime().initMainThread();

                // Execute code
                var main = context.getBindings("wasm").getMember(HELLO_WORLD_MODULE_NAME).getMember("main");
                main.execute(/* argc */ 0, /* argv */ 0).asInt();
            }
        }
    }
}
```
    </TabItem>
</Tabs>

## Other samples

You can also check out samples in the repository:

* Gradle project with Kotlin code: [samples/wasm-gradle/app-graalvm]
* Maven project with Java code: [samples/wasm-maven/graalvm-maven]

## Runtime optimizations

By default, GraalVM executes code in interpreter mode, which can be slow, However, it offers runtime optimizations
to improve performance. For more details, check this link: [GraalVM: Enable Optimization on OpenJDK and Oracle JDK][graalvm-runtime-optimization-support].

You can use Gradle toolchains to run your application on the GraalVM JVM with optimizations enabled:

```kotlin
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
        vendor = JvmVendorSpec.GRAAL_VM
    }
}
```

If you need to run optimized code on OpenJDK or other non-GraalVM JDKs, you'll need to activate JVM Compiler Interface 
(JVMCI) using the `-XX:+EnableJVMCI` option and add the GraalVM compiler to the `--upgrade-module-path` classpath.

This can be tricky to set up with Gradle. Additionally, the GraalVM version used in the project requires JDK 22 or
later to run GraalVM Compiler. For an example of how to enable the GraalVM compiler, take a look
at [this gist][jvmci-gradle].

### Other optimizations

To speed up initialization, you can reuse a single instance of the GraalVM Engine across multiple instances of Context.
Check this link for more information: [GraalVM: Managing the Code Cache][graalvm-managing-the-code-cache] 

[GraalWasm]: https://www.graalvm.org/latest/reference-manual/wasm/
[Polyglot API 24]: https://central.sonatype.com/artifact/org.graalvm.sdk/graal-sdk/24.1.1
[samples/wasm-gradle/app-graalvm]: https://github.com/illarionov/wasi-emscripten-host/tree/main/samples/wasm-gradle
[samples/wasm-maven/graalvm-maven]: https://github.com/illarionov/wasi-emscripten-host/tree/main/samples/wasm-maven
[graalvm-running-webassembly-embedded-in-java]: https://www.graalvm.org/latest/reference-manual/wasm/#options
[graalvm-runtime-optimization-support]: https://www.graalvm.org/latest/reference-manual/embed-languages/#runtime-optimization-support
[jvmci-gradle]: https://gist.github.com/illarionov/9ce560f95366649876133c1634a03b88
[graalvm-managing-the-code-cache]: https://www.graalvm.org/latest/reference-manual/embed-languages/#managing-the-code-cache
