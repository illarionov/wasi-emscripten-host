---
sidebar_label: 'Chicory'
sidebar_position: 2
description: 'Implementation of WASI Preview 1 and Emscripten host functions for Chicory'
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Chicory Integration

[Chicory] is a zero-dependency, pure Java WebAssembly runtime.

Key Features:

- Compatible with Android API 28+ and JVM JDK 17+.
- Simple JVM-only runtime with minimal dependencies.
- Supports single-threaded execution only.
  
This integration allows you to run WebAssembly binaries that use either WASI Preview 1 or Emscripten functions on the JVM with Chicory.

Compatible with version **[1.2.1][Chicory_version]** of Chicory.

## WASI Preview 1 Bindings Integration

Check [WASI Preview 1](../WASIP1) to see the limitations of the WASI P1 implementation.

### Installation

Add the required dependencies:

<Tabs>
    <TabItem value="gradle" label="Gradle" default>

```kotlin
dependencies {
    implementation("at.released.weh:bindings-chicory-wasip1:0.5")
    implementation("com.dylibso.chicory:runtime:1.2.1")
}
```
    </TabItem>

    <TabItem value="maven" label="Maven">
```xml
<dependencies>
    <dependency>
        <groupId>at.released.weh</groupId>
        <artifactId>bindings-chicory-wasip1-jvm</artifactId>
        <version>0.5</version>
    </dependency>
    <dependency>
        <groupId>com.dylibso.chicory</groupId>
        <artifactId>runtime</artifactId>
        <version>1.2.1</version>
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
import at.released.weh.bindings.chicory.exception.ProcExitException
import at.released.weh.bindings.chicory.wasip1.ChicoryWasiPreview1Builder
import at.released.weh.host.EmbedderHost
import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.ImportValues
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasm.WasmModule
import kotlin.system.exitProcess

fun main() {
    // Create Host and run code
    EmbedderHost {
        fileSystem {
            addPreopenedDirectory(".", "/data")
        }
    }.use { executeCode(it) }
}

private fun executeCode(embedderHost: EmbedderHost) {
    // Prepare WASI host imports
    val wasiImports: List<HostFunction> = ChicoryWasiPreview1Builder {
        host = embedderHost
    }.build()

    val hostImports = ImportValues.builder().withFunctions(wasiImports).build()

    // Instantiate the WebAssembly module
    val instance = Instance
        .builder(File("helloworld_wasi.wasm"))
        .withImportValues(hostImports)
        .withInitialize(true)
        .withStart(false)
        .build()

    // Execute code
    try {
        instance.export("_start").apply()
    } catch (pre: ProcExitException) {
        if (pre.exitCode != 0) {
            exitProcess(pre.exitCode)
        }
    }
}
```

    </TabItem>

    <TabItem value="java" label="Java">

```java
import at.released.weh.bindings.chicory.exception.ProcExitException;
import at.released.weh.bindings.chicory.wasip1.ChicoryWasiPreview1Builder;
import at.released.weh.host.EmbedderHost;
import at.released.weh.host.EmbedderHostBuilder;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import java.util.Collection;
import java.util.List;

public class App {
    public static void main(String[] args) throws Exception {
        // Prepare Host
        var hostBuilder = new EmbedderHostBuilder();
        hostBuilder.fileSystem().addPreopenedDirectory(".", "/data");

        try (var embedderHost = hostBuilder.build()) {
            executeWasmCode(embedderHost, wasmModule);
        }
    }

    private static void executeWasmCode(EmbedderHost embedderHost) {
        // Prepare WASI host imports
        Collection<HostFunction> wasiImports = new ChicoryWasiPreview1Builder().setHost(embedderHost).build();

        var hostImports = ImportValues.builder().withFunctions(List.copyOf(wasiImports)).build();

        // Instantiate the WebAssembly module
        var instance = Instance.builder(new File("helloworld_wasi.wasm"))
                .withImportValues(hostImports)
                .withInitialize(true)
                .withStart(false)
                .build();

        // Execute code
        try {
            instance.export("_start").apply();
        } catch (ProcExitException pre) {
            if (pre.getExitCode() != 0) {
                System.exit(pre.getExitCode());
            }
        }
    }
}
```

    </TabItem>
</Tabs>


## Chicory WASI Preview 1 implementation

Chicory includes its own implementation of the WASI Preview 1 interfaces.
You can find documentation on how to use it here: https://chicory.dev/docs/usage/wasi/.

## Emscripten bindings integration

### Installation

Add the required dependencies:

<Tabs>
    <TabItem value="gradle" label="Gradle" default>

```kotlin
dependencies {
    implementation("at.released.weh:bindings-chicory-emscripten:0.5")
    implementation("com.dylibso.chicory:runtime:1.2.1")
}
```
    </TabItem>

    <TabItem value="maven" label="Maven">
```xml
<dependencies>
    <dependency>
        <groupId>at.released.weh</groupId>
        <artifactId>bindings-chicory-emscripten-jvm</artifactId>
        <version>0.1</version>
    </dependency>
    <dependency>
        <groupId>com.dylibso.chicory</groupId>
        <artifactId>runtime</artifactId>
        <version>1.1.1</version>
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
import at.released.weh.bindings.chicory.ChicoryEmscriptenHostInstaller
import at.released.weh.bindings.chicory.ChicoryEmscriptenHostInstaller.ChicoryEmscriptenSetupFinalizer
import at.released.weh.host.EmbedderHost
import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.ImportValues
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.Parser

fun main() {
    // Create Host and run code
    EmbedderHost {
        fileSystem {
            unrestricted = true
        }
    }.use(::executeCode)
}

private fun executeCode(embedderHost: EmbedderHost) {
    // Prepare WASI and Emscripten host imports
    val installer = ChicoryEmscriptenHostInstaller {
        host = embedderHost
    }

    val wasiFunctions: List<HostFunction> = installer.setupWasiPreview1HostFunctions()
    val emscriptenFinalizer: ChicoryEmscriptenSetupFinalizer = installer.setupEmscriptenFunctions()

    val hostImports = ImportValues.builder()
        .withFunctions(emscriptenFinalizer.emscriptenFunctions + wasiFunctions)
        .build()

    // Instantiate the WebAssembly module
    val instance = Instance
        .builder(File("helloworld.wasm"))
        .withImportValues(hostImports)
        .withInitialize(true)
        .withStart(false)
        .build()

    // Finalize initialization after module instantiation
    val emscriptenRuntime = emscriptenFinalizer.finalize(instance)

    // Initialize Emscripten runtime environment
    emscriptenRuntime.initMainThread()

    // Execute code
    instance.export("main").apply(
        /* argc */ 0,
        /* argv */ 0,
    )[0]
}
```

    </TabItem>

    <TabItem value="java" label="Java">

```java
import at.released.weh.bindings.chicory.ChicoryEmscriptenHostInstaller;
import at.released.weh.host.EmbedderHost;
import at.released.weh.host.EmbedderHostBuilder;
import com.dylibso.chicory.runtime.ImportFunction;
import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import java.util.ArrayList;

public class App {
    public static void main(String[] args) throws Exception {
        // Prepare Host
        var hostBuilder = new EmbedderHostBuilder();
        hostBuilder.fileSystem().setUnrestricted(true);
        try (var embedderHost = hostBuilder.build()) {
            executeWasmCode(embedderHost);
        }
    }

    private static void executeWasmCode(EmbedderHost embedderHost) throws Exception {
        // Prepare WASI and Emscripten host imports
        var installer = new ChicoryEmscriptenHostInstaller.Builder()
                .setHost(embedderHost)
                .build();

        ArrayList<ImportFunction> hostFunctions = new ArrayList<>(installer.setupWasiPreview1HostFunctions());
        var emscriptenFinalizer = installer.setupEmscriptenFunctions();
        hostFunctions.addAll(emscriptenFinalizer.getEmscriptenFunctions());

        var hostImports = ImportValues.builder().withFunctions(hostFunctions).build();

        // Instantiate the WebAssembly module
        var instance = Instance.builder(new File("helloworld.wasm"))
                .withImportValues(hostImports)
                .withInitialize(true)
                .withStart(false)
                .build();

        // Finalize initialization after module instantiation
        var emscriptenRuntime = emscriptenFinalizer.finalize(instance);

        // Initialize Emscripten runtime environment
        emscriptenRuntime.initMainThread();

        // Execute code
        long exitCode = instance.export("main").apply(
                /* argc */ 0,
                /* argv */ 0
        )[0];
    }
}
```

    </TabItem>
</Tabs>


## Other samples

You can also check out samples in the repository:

* Gradle project with Kotlin code: [samples/wasm-gradle/app-chicory]
* Maven project with Java code: [samples/wasm-maven/chicory-maven]

## Optimizations

Chicory also supports Ahead-Of-Time (AOT) optimization of executable WebAssembly code, 
both at runtime and at build time.
For the latest updates, visit this link: [chicory/aot]

[Chicory]: https://chicory.dev/
[Chicory_version]: https://github.com/dylibso/chicory/releases/tag/1.2.1
[samples/wasm-gradle/app-chicory]: https://github.com/illarionov/wasi-emscripten-host/tree/main/samples/wasm-gradle
[samples/wasm-maven/chicory-maven]: https://github.com/illarionov/wasi-emscripten-host/tree/main/samples/wasm-maven
[chicory/aot]: https://chicory.dev/docs/experimental/aot
