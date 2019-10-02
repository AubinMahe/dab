SRCS =\
 src/io/ByteBuffer.cpp\
 src/io/DatagramSocket.cpp\
 src/io/Selector.cpp\
 src/io/sockets.cpp\
 src/os/Event.cpp\
 src/os/Mutex.cpp\
 src/os/StdApiException.cpp\
 src/util/Args.cpp\
 src/util/Timeout.cpp

CPPFLAGS = -I inc -O3 -g0 -W -Wall -pedantic -c -std=c++1z
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
