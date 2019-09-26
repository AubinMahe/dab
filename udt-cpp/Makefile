SRCS =\
 src/main.cpp

SRCSGEN =\
 src-gen/dab/Automaton.cpp\
 src-gen/dab/Carte.cpp\
 src-gen/dab/Compte.cpp\
 src-gen/dab/Etat.cpp\
 src-gen/dab/Evenement.cpp\
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

OBJS    = $(SRCS:src/%cpp=BUILD/%o) $(SRCSGEN:src-gen/%cpp=BUILD/%o)
OBJSW32 = $(SRCS:src/%cpp=BUILD-mingw32/%o) $(SRCSGEN:src-gen/%cpp=BUILD-mingw32/%o)

all: ../UniteDeTraitement ../UniteDeTraitement-mingw32

clean:
	rm -f  deps
	rm -fr BUILD
	rm -fr BUILD-mingw32
	rm -fr bin
	rm -fr Debug
	rm -fr Release
	rm -fr src-gen

../disappgen.jar:
	cd ../disappgen && ant

$(SRCSGEN) $(HEADERSGEN): ../dab.xml ../disappgen.jar
	cd .. && ant generate-all-sources

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

run:
	make ../UniteDeTraitement
	cd ../sc        && xterm -geometry 132x30-650+0 -e 'ant run' &
	cd ../dab       && xterm -geometry 132x30-800-0 -e 'ant run' &
	../UniteDeTraitement\
 --iface=$(IFACE)\
 --udt-port=$(UDT_PORT)\
 --sc-address=$(SC_ADDRESS)\
 --sc-port=$(SC_PORT)\
 --dab-address=$(DAB_ADDRESS)\
 --dab-port=$(DAB_PORT)

run-debug:
	cd ../sc        && xterm -geometry 132x30-650+0 -e 'ant run' &
	cd ../dab       && xterm -geometry 132x30-800-0 -e 'ant run' &
	@echo "Allez, c'est parti pour le debug du bordel en C++ sous Eclipse..."

run-mingw32:
	make ../UniteDeTraitement-mingw32
	cd ../sc        && xterm -geometry 132x30-650+0 -e 'ant run' &
	cd ../dab       && xterm -geometry 132x30-800-0 -e 'ant run' &
	WINEPATH=/usr/lib/gcc/i686-w64-mingw32/7.3-win32 wine\
 ../UniteDeTraitement-mingw32\
 --iface=$(IFACE)\
 --udt-port=$(UDT_PORT)\
 --sc-address=$(SC_ADDRESS)\
 --sc-port=$(SC_PORT)\
 --dab-address=$(DAB_ADDRESS)\
 --dab-port=$(DAB_PORT)

deps: $(SRCS) $(SRCSGEN)
	g++ $(CPPFLAGS) -MM $(SRCS) $(SRCSGEN) | awk '/.*\.o:/ {print "BUILD/"$$0         ; next } {print}'  > $@
	g++ $(CPPFLAGS) -MM $(SRCS) $(SRCSGEN) | awk '/.*\.o:/ {print "BUILD-mingw32/"$$0 ; next } {print}' >> $@

-include deps

