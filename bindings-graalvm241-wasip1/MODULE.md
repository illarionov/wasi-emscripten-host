# Module bindings-graalvm241-wasip1

Wasi Preview 1 host functions for [GraalWasm] WebAssembly runtime.

[<img alt="Maven Central Version" src="https://img.shields.io/maven-central/v/at.released.weh/bindings-graalvm241-wasip1?style=flat-square">](https://central.sonatype.com/artifact/at.released.weh/bindings-graalvm240/overview)

* Targets: *JVM*

Use [GraalvmWasiPreview1Builder] to install host functions into the GraalVM context. 

## Usage

* Usage example:
*
 ```kotlin
const val HELLO_WORLD_MODULE_NAME: String = "helloworld"
val source = Source.newBuilder("wasm", App::class.java.getResource("helloworld.wasm"))
    .name(HELLO_WORLD_MODULE_NAME)
    .build()

// Setup Polyglot Context
val context: Context = Context.newBuilder().build()
context.use {
    // Context must be initialized before installing modules
    context.initialize("wasm")

    // Setup module with Wasi Preview host functions 
    GraalvmWasiPreview1Builder {
        this.host = host // setup host
    }.build(context)

    // Evaluate the WebAssembly module
    context.eval(source)

    // Execute start function
    val startFunc = context.getBindings("wasm").getMember(HELLO_WORLD_MODULE_NAME).getMember("_start")
    startFunc.execute()
}
 ```

[GraalWasm]: https://www.graalvm.org/latest/reference-manual/wasm/
