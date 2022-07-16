#!make

default:
	mkdir -p bin/ classes/
	clj -T:build uber

bin:
	src/bash/acid/compile.sh

install:
	cp bin/acid ~/.local/bin/acid