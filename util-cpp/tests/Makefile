SRCS =\
 src/main.cpp

CPPFLAGS = -I inc -O3 -g0 -pedantic -W -Wall -Wextra -Wconversion -c -std=c++17
LIB      = util-cpp
LIBW32   = $(LIB)-mingw32
OBJS     = $(SRCS:src/%cpp=BUILD/%o)
OBJSW32  = $(SRCS:src/%cpp=BUILD-mingw32/%o)

all: lib/lib$(LIB).a lib/lib$(LIB)-mingw32.a

clean:
	rm -f  deps
	rm -fr lib
	rm -fr BUILD
	rm -fr BUILD-mingw32
	rm -fr bin
	rm -fr Debug
	rm -fr Release

lib/lib$(LIB).a: $(OBJS)
	@mkdir -p $$(dirname $@)
	ar crv $@ $(OBJS)

lib/lib$(LIBW32).a: $(OBJSW32)
	@mkdir -p $$(dirname $@)
	ar crv $@ $(OBJSW32)

BUILD/%.o: src/%.cpp
	@mkdir -p $$(dirname $@)
	g++ $(CPPFLAGS) -o $@ $<

BUILD-mingw32/%.o: src/%.cpp
	@mkdir -p $$(dirname $@)
	i686-w64-mingw32-g++ $(CPPFLAGS) -o $@ $<

deps: $(SRCS)
	g++ $(CPPFLAGS) -MM $(SRCS) | awk '/.*\.o:/ {print "BUILD/"$$0        ; next } {print}'  > $@
	g++ $(CPPFLAGS) -MM $(SRCS) | awk '/.*\.o:/ {print "BUILD-mingw32/"$$0; next } {print}' >> $@

-include deps
