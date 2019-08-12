SRCS =\
 src/main.cpp\
 src/dab/udt/UniteDeTraitement.cpp

SRCSGEN =\
 src-gen/dab/Etat.cpp\
 src-gen/dab/net/IHM.cpp\
 src-gen/dab/net/SiteCentral.cpp\
 src-gen/dab/net/UniteDeTraitementDispatcher.cpp

HEADERSGEN=\
 src-gen/dab/Etat.hpp\
 src-gen/dab/IIHM.hpp\
 src-gen/dab/ISiteCentral.hpp\
 src-gen/dab/IUniteDeTraitement.hpp\
 src-gen/dab/IUniteDeTraitementDispatcher.hpp

CPPFLAGS = -I ../cpputil/inc -I inc -I src-gen -O3 -g0 -W -Wall -pedantic -c -std=c++17

OBJS    = $(SRCS:src/%cpp=BUILD/%o) $(SRCSGEN:src-gen/%cpp=BUILD/%o)
OBJSW32 = $(SRCS:src/%cpp=BUILD-mingw32/%o) $(SRCSGEN:src-gen/%cpp=BUILD-mingw32/%o)

all: ../UniteDeTraitement ../UniteDeTraitement-mingw32 ../Site-Central.jar ../DAB-UI.jar

clean:
	cd ../disappgen && ant  clean
	cd ../jutil     && ant  clean
	cd ../sc        && ant  clean
	cd ../dab       && ant  clean
	cd ../cpputil   && make clean
	rm -f  ../UniteDeTraitement
	rm -f  ../UniteDeTraitement-mingw32
	rm -f  deps
	rm -fr BUILD
	rm -fr BUILD-mingw32
	rm -fr bin
	rm -fr Debug
	rm -fr Release
	rm -fr src-gen

$(SRCSGEN) $(HEADERSGEN): ../dab.xml ../dis-app-gen.jar
	cd .. && java --module-path=dis-app-gen.jar --module=disappgen/disappgen.Main dab.xml

../cpputil/libcpputil.a:
	cd ../cpputil && make

../cpputil/libcpputil-mingw32.a:
	cd ../cpputil && make

../UniteDeTraitement: ../cpputil/libcpputil.a $(OBJS)
	cd ../cpputil && make
	g++ $(OBJS) -L../cpputil -lcpputil -o $@

../UniteDeTraitement-mingw32: ../cpputil/libcpputil-mingw32.a $(OBJSW32)
	cd ../cpputil && make
	i686-w64-mingw32-g++ $(OBJSW32) -L../cpputil -lcpputil-mingw32 -lws2_32 -o $@

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
	cd ../jutil && ant
	cd ../sc    && ant

../DAB-UI.jar:
	cd ../jutil && ant
	cd ../dab   && ant

run:
	cd ../jutil   && ant
	cd ../sc      && ant
	cd ../dab     && ant
	cd ../cpputil && make
	make ../UniteDeTraitement
	cd ../sc      && xterm -e 'ant run' &
	cd ../dab     && xterm -e 'ant run' &
	../UniteDeTraitement --iface=enp3s0 --udt-port=2417 --sc-address=localhost --sc-port=2416 --ui-address=localhost --ui-port=2418

run-mingw32:
	cd ../jutil   && ant
	cd ../sc      && ant
	cd ../dab     && ant
	cd ../cpputil && make
	make ../UniteDeTraitement-mingw32
	cd ../sc      && xterm -e 'ant run' &
	cd ../dab     && xterm -e 'ant run' &
	WINEPATH=/usr/lib/gcc/i686-w64-mingw32/7.3-win32 wine ../UniteDeTraitement-mingw32 --iface=enp3s0 --udt-port=2417 --sc-address=localhost --sc-port=2416 --ui-address=localhost --ui-port=2418

deps: $(SRCS) $(SRCSGEN) $(HEADERSGEN)
	g++ $(CPPFLAGS) -MM $(SRCS) $(SRCSGEN) | awk '/.*\.o:/ {print "BUILD/"$$0"         ; next } {print}'  > $@
	g++ $(CPPFLAGS) -MM $(SRCS) $(SRCSGEN) | awk '/.*\.o:/ {print "BUILD-mingw32/"$$0" ; next } {print}' >> $@

-include deps
