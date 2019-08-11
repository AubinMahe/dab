SRCS =\
 src/io/ByteBuffer.cpp\
 src/io/DatagramSocket.cpp\
 src/io/Selector.cpp\
 src/os/Event.cpp\
 src/os/StdApiException.cpp\
 src/util/Args.cpp

CPPFLAGS = -I inc -O3 -g0 -W -Wall -pedantic -c -std=c++1z
LIB      = cpputil
OBJS     = $(SRCS:src/%cpp=BUILD/%o)

all: lib$(LIB).a

clean:
	rm -f lib$(LIB).a
	rm -fr BUILD

lib$(LIB).a: $(OBJS)
	ar crv $@ $(OBJS)

BUILD/%.o: src/%.cpp
	@mkdir -p $$(dirname $@)
	g++ $(CPPFLAGS) -o $@ $<

deps: $(SRCS)
	g++ $(CPPFLAGS) -MM $(SRCS) | awk '/.*\.o:/ {print "BUILD/"$$0;next} {print}' > $@

-include deps
