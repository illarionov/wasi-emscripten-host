package at.released.weh.sample.graalvm.wasip1.maven;

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
