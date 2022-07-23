#!make

.PHONY: bin

default:
	make clear && \
	make jar && \
	make bin && \
	make install

clear:
	rm -r classes/ target/
jar:
	mkdir -p bin/ classes/
	clj -T:build uber

bin:
	src/bash/acid/compile.sh

install:
	cp bin/acid ~/.local/bin/acid
