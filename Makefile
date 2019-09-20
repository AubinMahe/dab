all:
	cd dab          && ant
	cd sc           && ant
#	cd udt-c        && make             # pour activer cette ligne changer le type d'implémentation dans dab.xml
	cd udt-cpp      && make             # pour activer cette ligne changer le type d'implémentation dans dab.xml
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
#	cd udt-c        && make clean       # pour activer cette ligne changer le type d'implémentation dans dab.xml
	cd udt-cpp      && make clean       # pour activer cette ligne changer le type d'implémentation dans dab.xml
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

