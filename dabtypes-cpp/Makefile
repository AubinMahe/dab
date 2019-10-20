-include generated-files.mk

CPPFLAGS    = -I ../util-cpp/inc -I inc-gen -O3 -g0 -pedantic -W -Wall -Wextra -Wconversion -c -std=c++17
WINVER      = 0x06010000
CPPW32FLAGS = $(CPPFLAGS) -DWIN32_WINDOWS=$(WINVER) -D_WIN32_WINNT=$(WINVER) -DWINVER=$(WINVER)

OBJS    = $(SRCSGEN:src-gen/%cpp=BUILD/%o)
OBJSW32 = $(SRCSGEN:src-gen/%cpp=BUILD-mingw32/%o)

all: lib/libdabtypes-cpp.a lib/libdabtypes-cpp-mingw32.a

clean:
	rm -f  deps
	rm -fr lib/
	rm -fr BUILD
	rm -fr BUILD-mingw32

lib/libdabtypes-cpp.a: $(OBJS)
	@mkdir -p $$(dirname $@)
	ar crv $@ $(OBJS)

lib/libdabtypes-cpp-mingw32.a: $(OBJSW32)
	@mkdir -p $$(dirname $@)
	ar crv $@ $(OBJSW32)

BUILD/%.o: src-gen/%.cpp
	@mkdir -p $$(dirname $@)
	g++ $(CPPFLAGS) -o $@ $<

BUILD-mingw32/%.o: src-gen/%.cpp
	@mkdir -p $$(dirname $@)
	i686-w64-mingw32-g++ $(CPPW32FLAGS) -o $@ $<

deps: $(SRCSGEN)
	g++ $(CPPFLAGS) -MM $(SRCSGEN) | awk '/.*\.o:/ {print "BUILD/"$$0         ; next } {print}'  > $@
	g++ $(CPPFLAGS) -MM $(SRCSGEN) | awk '/.*\.o:/ {print "BUILD-mingw32/"$$0 ; next } {print}' >> $@

-include deps
