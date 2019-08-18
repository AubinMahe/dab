SRCS =\
 src/io/ByteBuffer.cpp\
 src/io/DatagramSocket.cpp\
 src/io/Selector.cpp\
 src/io/sockets.cpp\
 src/os/Event.cpp\
 src/os/StdApiException.cpp\
 src/util/Args.cpp

CPPFLAGS = -I inc -O3 -g0 -W -Wall -pedantic -c -std=c++1z
LIB      = cpputil
LIBW32   = cpputil-mingw32
OBJS     = $(SRCS:src/%cpp=BUILD/%o)
OBJSW32  = $(SRCS:src/%cpp=BUILD-mingw32/%o)

all: lib$(LIB).a lib$(LIB)-mingw32.a

clean:
	rm -f  lib$(LIB).a
	rm -f  lib$(LIBW32).a
	rm -f  deps
	rm -fr BUILD
	rm -fr BUILD-mingw32
	rm -fr bin
	rm -fr Debug
	rm -fr Release

lib$(LIB).a: $(OBJS)
	ar crv $@ $(OBJS)

lib$(LIBW32).a: $(OBJSW32)
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
