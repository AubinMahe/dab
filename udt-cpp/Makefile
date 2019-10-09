SRCS = src/main.cpp

-include generated-files.mk

CPPFLAGS = -I ../util-cpp/inc -I inc -I src-gen -O3 -g0 -W -Wall -pedantic -c -std=c++17

OBJS    = $(SRCS:src/%cpp=BUILD/%o) $(SRCSGEN:src-gen/%cpp=BUILD/%o)
OBJSW32 = $(SRCS:src/%cpp=BUILD-mingw32/%o) $(SRCSGEN:src-gen/%cpp=BUILD-mingw32/%o)

all: ../UniteDeTraitement ../UniteDeTraitement-mingw32

clean:
	rm -f  deps
	rm -f  generated-files.mk
	rm -fr BUILD
	rm -fr BUILD-mingw32
	rm -fr bin
	rm -fr Debug
	rm -fr Release
	rm -fr src-gen

../disappgen.jar:
	cd ../disappgen && ant

generated-files.mk $(SRCSGEN) $(HEADERSGEN): ../dab.xml ../disappgen.jar
	cd .. && ant generate-all-sources

../util-cpp/lib/libutil-cpp.a:
	cd ../util-cpp && make

../util-cpp/lib/libutil-cpp-mingw32.a:
	cd ../util-cpp && make

../UniteDeTraitement: ../util-cpp/lib/libutil-cpp.a $(OBJS)
	cd ../util-cpp && make
	g++ $(OBJS) -L../util-cpp/lib -lutil-cpp -lpthread -o $@

../UniteDeTraitement-mingw32: ../util-cpp/lib/libutil-cpp-mingw32.a $(OBJSW32)
	cd ../util-cpp && make
	i686-w64-mingw32-g++ $(OBJSW32) -L../util-cpp/lib -lutil-cpp-mingw32 -lws2_32 -lpthread -o $@

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

run-1: ../UniteDeTraitement
	xterm -title UDT-1 -geometry 90x30+810-16 -hold -e ../UniteDeTraitement --name=udt1 &

run-2: ../UniteDeTraitement
	xterm -title UDT-2 -geometry 90x30-0+0 -hold -e ../UniteDeTraitement --name=udt2 &

run-mingw32: ../UniteDeTraitement-mingw32
	WINEPATH=/usr/lib/gcc/i686-w64-mingw32/7.3-win32 wine ../UniteDeTraitement-mingw32 --name=udt-1

deps: $(SRCS) $(SRCSGEN)
	g++ $(CPPFLAGS) -MM $(SRCS) $(SRCSGEN) | awk '/.*\.o:/ {print "BUILD/"$$0         ; next } {print}'  > $@
	g++ $(CPPFLAGS) -MM $(SRCS) $(SRCSGEN) | awk '/.*\.o:/ {print "BUILD-mingw32/"$$0 ; next } {print}' >> $@

-include deps

