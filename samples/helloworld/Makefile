.PHONY: all clean

all: helloworld.wasm helloworld_nodebug.wasm helloworld_nodebug.wasm helloworld_wasi.wasm helloworld_wasi_nodebug.wasm

helloworld.wasm:
	emcc helloworld.cpp -O3 -g -mbulk-memory -o helloworld.mjs

helloworld_nodebug.wasm:
	emcc helloworld.cpp -O3 -mbulk-memory -o helloworld_nodebug.mjs

helloworld_wasi.wasm:
	emcc helloworld.cpp -O3 -g -mbulk-memory -o helloworld_wasi.wasm

helloworld_wasi_nodebug.wasm:
	emcc helloworld.cpp -O3 -mbulk-memory -o helloworld_wasi_modebug.wasm

clean:
	@rm -f *.wasm *.mjs

