all:
	cd dab          && ant
	cd sc           && ant
	cd udt-c        && make
	cd udt-cpp      && make
	cd util-c       && make
	cd util-cpp     && make
	cd disappgen    && ant
	cd util-java    && ant
	cd util-c/tests && make

run-c:
	cd udt-c        && make run

run-cpp:
	cd udt-cpp      && make run

clean:
	cd dab          && ant  clean
	cd sc           && ant  clean
	cd udt-c        && make clean
	cd udt-cpp      && make clean
	cd util-c       && make clean
	cd util-cpp     && make clean
	cd disappgen    && ant  clean
	cd util-java    && ant  clean
	cd util-c/tests && make clean
	rm -f  dis-app-gen.jar
	rm -f  unite_de_traitement
	rm -f  unite_de_traitement-mingw32
	rm -f  uniteDeTraitement
	rm -f  uniteDeTraitement-mingw32

