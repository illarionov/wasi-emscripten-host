---
sidebar_label: 'GraalWasm'
sidebar_position: 1
description: 'Implementation of Emscripten host functions for GraalWasm'
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# GraalWasm Integration

This integration enables the execution of WebAssembly binaries using the Emscripten and WASI Preview 1 APIs 
on the JVM through [GraalWasm].

## Requirements

* JVM JDK 22+ (it may also work on JDK21+) 
* GraalVM SDK Polyglot API 24.0.X 

The current implementation heavily relies on internal GraalWasm APIs, making it compatible only with the
[GraalVM SDK Polyglot API 24.0.X][Polyglot API 24] for JDK22.

## Installation

Add the required dependencies:

<Tabs>
    <TabItem value="gradle" label="Gradle" default>

```kotlin
dependencies {
    implementation("at.released.weh:bindings-graalvm240:0.1-alpha01")
    implementation("org.graalvm.polyglot:polyglot:24.0.2")
    implementation("org.graalvm.polyglot:wasm:24.0.2")
}
```
    </TabItem>

    <TabItem value="maven" label="Maven">
```xml
<repositories>
    <repository>
        <id>Google</id>
        <url>https://maven.google.com</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.graalvm.polyglot</groupId>
        <artifactId>polyglot</artifactId>
        <version>24.0.2</version>
        <type>jar</type>
    </dependency>
    <dependency>
        <groupId>org.graalvm.polyglot</groupId>
        <artifactId>wasm</artifactId>
        <version>24.0.2</version>
        <type>pom</type>
    </dependency>
    <dependency>
        <groupId>at.released.weh</groupId>
        <artifactId>bindings-graalvm240-jvm</artifactId>
        <version>0.1-alpha01</version>
    </dependency>
</dependencies>
```
    </TabItem>
</Tabs>

## Usage

Below is an example demonstrating the execution of **helloworld.wasm**, prepared 
in the "[Emscripten Example](../Emscripten#example)".

<Tabs>
    <TabItem value="kotlin" label="Kotlin" default>

```kotlin
import at.released.weh.bindings.graalvm240.GraalvmHostFunctionInstaller
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Source

private object App

const val HELLO_WORLD_MODULE_NAME: String = "helloworld"

fun main() {
    // Prepare source
    val source = Source.newBuilder("wasm", App::class.java.getResource("helloworld.wasm"))
        .name(HELLO_WORLD_MODULE_NAME)
        .build()

    // Setup Polyglot Context
    val context: Context = Context.newBuilder().build()
    context.use {
        // Context must be initialized before installing modules
        context.initialize("wasm")

        // Setup modules
        val installer = GraalvmHostFunctionInstaller(context)
        installer.setupWasiPreview1Module()
        val emscriptenInstaller = installer.setupEmscriptenFunctions()

        // Evaluate the WebAssembly module
        context.eval(source)

        // Finish initialization after module instantiation
        emscriptenInstaller.finalize(HELLO_WORLD_MODULE_NAME).use { emscriptenEnv ->
            // Initialize Emscripten runtime environment
            emscriptenEnv.emscriptenRuntime.initMainThread()

            // Execute code
            run(context)
        }
    }
}

private fun run(
    context: Context,
) {
    val main = context.getBindings("wasm").getMember(HELLO_WORLD_MODULE_NAME).getMember("main")
    main.execute(/* argc */ 0, /* argv */ 0).asInt()
}
```
    </TabItem>

    <TabItem value="java" label="Java">

```java
import at.released.weh.bindings.graalvm240.GraalvmHostFunctionInstaller;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
    
public class App {
    private static final String HELLO_WORLD_MODULE_NAME = "helloworld";

    public static void main(String[] args) throws IOException {
        new App().start();
    }

    public void start() throws IOException {
        // Prepare source
        var source = Source.newBuilder("wasm", App.class.getResource("helloworld.wasm"))
                .name(HELLO_WORLD_MODULE_NAME)
                .build();

        // Setup Polyglot Context
        try (var context = Context.newBuilder("wasm").build()) {
            // Context must be initialized before installing modules
            context.initialize("wasm");

            // Setup modules
            var installer = new GraalvmHostFunctionInstaller.Builder(context).build();
            installer.setupWasiPreview1Module();
            var emscriptenInstaller = installer.setupEmscriptenFunctions();

            // Evaluate the WebAssembly module
            context.eval(source);

            // Finish initialization after module instantiation
            try (var emscriptenEnvironment = emscriptenInstaller.finalize(HELLO_WORLD_MODULE_NAME)) {
                // Initialize Emscripten runtime environment
                emscriptenEnvironment.getEmscriptenRuntime().initMainThread();

                // Execute code
                executeWasmCode(context);
            }
        }
    }

    private void executeWasmCode(Context context) {
        var main = context.getBindings("wasm").getMember(HELLO_WORLD_MODULE_NAME).getMember("main");
        main.execute(/* argc */ 0, /* argv */ 0).asInt();
    }
}
```
    </TabItem>
</Tabs>

You can also check out the example in the repository:

* Gradle project with Kotlin code: [samples/wasm-gradle/app-graalvm]
* Maven project with Java code: [samples/wasm-maven/graalvm-maven]

## GraalVM's Built-in Emscripten Functions

It is worth noting that GraalWasm provides its own implementation of the Emscripten JS and WASI Preview 1 interfaces.  
This implementation varies in the set of implemented functions and the version of Emscripten.
In many cases, it may be a more suitable choice rather than this library.

To use it, you should add the `wasm.Builtins` option with the value `emscripten,wasi_snapshot_preview1`.

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
        .option("wasm.Builtins", "emscripten,wasi_snapshot_preview1")
        .build()
    context.use {
        context.eval(source)
        run(context)
    }
}
```

See also example in documentation: [GraalVM: Running WebAssembly Embedded in Java][graalvm-running-webassembly-embedded-in-java]

## Runtime optimizations

By default, GraalVM executes code in interpreter mode, which can be slow, However, it offers runtime optimizations
to improve performance. For more details, check this link: [GraalVM: Enable Optimization on OpenJDK and Oracle JDK][graalvm-runtime-optimization-support].

You can use Gradle toolchains to run your application on the GraalVM JVM with optimizations enabled:

```kotlin
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
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

[GraalWasm]: https://www.graalvm.org/jdk22/reference-manual/wasm/
[Polyglot API 24]: https://central.sonatype.com/artifact/org.graalvm.sdk/graal-sdk/24.0.2
[samples/wasm-gradle/app-graalvm]: https://github.com/illarionov/wasi-emscripten-host/tree/main/samples/wasm-gradle/app-graalvm
[samples/wasm-maven/graalvm-maven]: https://github.com/illarionov/wasi-emscripten-host/tree/main/samples/wasm-maven/graalvm-maven
[graalvm-running-webassembly-embedded-in-java]: https://www.graalvm.org/latest/reference-manual/wasm/#running-webassembly-embedded-in-java
[graalvm-runtime-optimization-support]: https://www.graalvm.org/latest/reference-manual/embed-languages/#runtime-optimization-support
[jvmci-gradle]: https://gist.github.com/illarionov/9ce560f95366649876133c1634a03b88
[graalvm-managing-the-code-cache]: https://www.graalvm.org/latest/reference-manual/embed-languages/#managing-the-code-cache
