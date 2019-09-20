SRCS =\
 src/io/byte_buffer.c\
 src/io/datagram_socket.c\
 src/io/selector.c\
 src/io/sockets.c\
 src/os/errors.c\
 src/os/event.c\
 src/os/mutex.c\
 src/util/args.c\
 src/util/automaton.c\
 src/util/error_codes.c\
 src/util/map.c

CFLAGS  = -I inc -O3 -g0 -W -Wall -pedantic -std=c11
LIB     = util-c
LIBW32  = $(LIB)-mingw32
OBJS    = $(SRCS:src/%c=BUILD/%o)
OBJSW32 = $(SRCS:src/%c=BUILD-mingw32/%o)

all: lib/lib$(LIB).a lib/lib$(LIBW32).a

clean:
	rm -f  lib/lib$(LIB).a
	rm -f  lib/lib$(LIBW32).a
	rm -f  deps
	rm -fr BUILD
	rm -fr BUILD-mingw32
#	rm -fr bin
#	rm -fr Debug
#	rm -fr Release

lib/lib$(LIB).a: $(OBJS)
	@mkdir -p $$(dirname $@)
	ar crv $@ $(OBJS)

lib/lib$(LIBW32).a: $(OBJSW32)
	@mkdir -p $$(dirname $@)
	ar crv $@ $(OBJSW32)

BUILD/%.o: src/%.c
	@mkdir -p $$(dirname $@)
	gcc $(CFLAGS) -c -o $@ $<

BUILD-mingw32/%.o: src/%.c
	@mkdir -p $$(dirname $@)
	i686-w64-mingw32-gcc $(CFLAGS) -c -o $@ $<

deps: $(SRCS)
	gcc $(CFLAGS) -MM $(SRCS) | awk '/.*\.o:/ { print "BUILD/"$$0         ; next } { print }'  > $@
	gcc $(CFLAGS) -MM $(SRCS) | awk '/.*\.o:/ { print "BUILD-mingw32/"$$0 ; next } { print }' >> $@

-include deps
