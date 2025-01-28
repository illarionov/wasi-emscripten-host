# Module bindings-graalvm241-wasip1

Implementation of WASI Preview 1 host functions for [GraalWasm] WebAssembly runtime.

[<img alt="Maven Central Version" src="https://img.shields.io/maven-central/v/at.released.weh/bindings-graalvm241-wasip1?style=flat-square">](https://central.sonatype.com/artifact/at.released.weh/bindings-graalvm240-wasip1/overview)

* Targets: *JVM*

Use [GraalvmWasiPreview1Builder](https://weh.released.at/api/bindings-graalvm241-wasip1/at.released.weh.bindings.graalvm241.wasip1/-graalvm-wasi-preview1-builder/index.html) to install host functions into the GraalVM context. 

## Usage

Usage example:

 ```kotlin
const val HELLO_WORLD_MODULE_NAME: String = "helloworld"

// Create Host and run code
EmbedderHost {
    fileSystem {
        addPreopenedDirectory(".", "/data")
    }
}.use(::executeCode)

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
            }
            Unit
        }
    }
}
 ```

[GraalWasm]: https://www.graalvm.org/latest/reference-manual/wasm/
