SRCS =\
 src/main.cpp\
 src/dab/udt/UniteDeTraitement.cpp

SRCSGEN =\
 src-gen/dab/Etat.cpp\
 src-gen/dab/Carte.cpp\
 src-gen/dab/Compte.cpp\
 src-gen/dab/net/IHM.cpp\
 src-gen/dab/net/SiteCentral.cpp\
 src-gen/dab/net/UniteDeTraitementDispatcher.cpp

HEADERSGEN=\
 src-gen/dab/Etat.hpp\
 src-gen/dab/IIHM.hpp\
 src-gen/dab/ISiteCentral.hpp\
 src-gen/dab/IUniteDeTraitement.hpp\
 src-gen/dab/IUniteDeTraitementDispatcher.hpp

CPPFLAGS = -I ../util-cpp/inc -I inc -I src-gen -O3 -g0 -W -Wall -pedantic -c -std=c++17

DISAPPGEN_CLASSPATH=\
 lib/antlr-runtime-3.3.jar:\
 lib/javax.activation-1.2.0.jar:\
 lib/javax.activation-api-1.2.0.jar:\
 lib/jaxb-api-2.3.1.jar:\
 lib/jaxb-core-2.3.0.jar:\
 lib/jaxb-impl-2.3.2.jar:\
 lib/ST4-4.0.4.jar:\
 dis-app-gen.jar

OBJS    = $(SRCS:src/%cpp=BUILD/%o) $(SRCSGEN:src-gen/%cpp=BUILD/%o)
OBJSW32 = $(SRCS:src/%cpp=BUILD-mingw32/%o) $(SRCSGEN:src-gen/%cpp=BUILD-mingw32/%o)

all: ../UniteDeTraitement ../UniteDeTraitement-mingw32 ../Site-Central.jar ../DAB-UI.jar

clean-all:
	cd ../disappgen && ant  clean
	cd ../util-java && ant  clean
	cd ../sc        && ant  clean
	cd ../dab       && ant  clean
	cd ../util-cpp  && make clean
	rm -f  ../UniteDeTraitement
	rm -f  ../UniteDeTraitement-mingw32
	rm -f  deps
	rm -fr BUILD
	rm -fr BUILD-mingw32
	rm -fr bin
	rm -fr Debug
	rm -fr Release
	rm -fr src-gen

clean:
	rm -f  deps
	rm -fr BUILD
	rm -fr BUILD-mingw32
	rm -fr bin
	rm -fr Debug
	rm -fr Release
	rm -fr src-gen

../util-cpp/lib/libutil-cpp.a:
	cd ../util-cpp && make

../util-cpp/lib/libutil-cpp-mingw32.a:
	cd ../util-cpp && make

../UniteDeTraitement: ../util-cpp/lib/libutil-cpp.a $(OBJS)
	cd ../util-cpp && make
	g++ $(OBJS) -L../util-cpp/lib -lutil-cpp -o $@

../UniteDeTraitement-mingw32: ../util-cpp/lib/libutil-cpp-mingw32.a $(OBJSW32)
	cd ../util-cpp && make
	i686-w64-mingw32-g++ $(OBJSW32) -L../util-cpp/lib -lutil-cpp-mingw32 -lws2_32 -o $@

BUILD/%.o: src/%.cpp
	@mkdir -p $$(dirname $@)
	g++ $(CPPFLAGS) -o $@ $<

BUILD/%.o: src-gen/%.cpp
	@mkdir -p $$(dirname $@)
	g++ $(CPPFLAGS) -o $@ $<

BUILD-mingw32/%.o: src/%.cpp
	@mkdir -p $$(dirname $@)
	i686-w64-mingw32-g++ $(CPPFLAGS) -o $@ $<

BUILD-mingw32/%.o: src-gen/%.cpp
	@mkdir -p $$(dirname $@)
	i686-w64-mingw32-g++ $(CPPFLAGS) -o $@ $<

../dis-app-gen.jar:
	cd ../disappgen && ant

../Site-Central.jar:
	cd ../util-java && ant
	cd ../sc        && ant

../DAB-UI.jar:
	cd ../util-java && ant
	cd ../dab       && ant

run:
	cd ../util-java && ant
	cd ../sc        && ant
	cd ../dab       && ant
	cd ../util-cpp  && make
	make ../UniteDeTraitement
	cd ../sc        && xterm -geometry 132x30-650+0 -e 'ant run' &
	cd ../dab       && xterm -geometry 132x30-800-0 -e 'ant run' &
	../UniteDeTraitement --iface=enp3s0 --udt-port=2417 --sc-address=localhost --sc-port=2416 --ui-address=localhost --ui-port=2418

run-debug:
	cd ../util-java && ant
	cd ../sc        && ant
	cd ../dab       && ant
	cd ../util-cpp  && make
	cd ../sc        && xterm -geometry 132x30-650+0 -e 'ant run' &
	cd ../dab       && xterm -geometry 132x30-800-0 -e 'ant run' &
	@echo "Allez, c'est parti pour le debug du bordel en C++ sous Eclipse..."

run-mingw32:
	cd ../util-java && ant
	cd ../sc        && ant
	cd ../dab       && ant
	cd ../util-cpp  && make
	make ../UniteDeTraitement-mingw32
	cd ../sc        && xterm -geometry 132x30-650+0 -e 'ant run' &
	cd ../dab       && xterm -geometry 132x30-800-0 -e 'ant run' &
	WINEPATH=/usr/lib/gcc/i686-w64-mingw32/7.3-win32 wine ../UniteDeTraitement-mingw32 --iface=enp3s0 --udt-port=2417 --sc-address=localhost --sc-port=2416 --ui-address=localhost --ui-port=2418

deps: $(SRCS) $(SRCSGEN) $(HEADERSGEN)
	g++ $(CPPFLAGS) -MM $(SRCS) $(SRCSGEN) | awk '/.*\.o:/ {print "BUILD/"$$0         ; next } {print}'  > $@
	g++ $(CPPFLAGS) -MM $(SRCS) $(SRCSGEN) | awk '/.*\.o:/ {print "BUILD-mingw32/"$$0 ; next } {print}' >> $@

-include deps
