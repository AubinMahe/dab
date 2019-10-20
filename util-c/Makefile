SRCS =\
 src/io/byte_buffer.c\
 src/io/console.c\
 src/io/datagram_socket.c\
 src/io/selector.c\
 src/io/sockets.c\
 src/os/errors.c\
 src/os/event.c\
 src/os/mutex.c\
 src/os/thread.c\
 src/util/args.c\
 src/util/automaton.c\
 src/util/error_codes.c\
 src/util/map.c\
 src/util/timeout.c

CFLAGS    = -I inc -O3 -g0 -pedantic -W -Wall -Wextra -Wconversion -std=c11
WINVER    = 0x06010000
CW32FLAGS = $(CFLAGS) -DWIN32_WINDOWS=$(WINVER) -D_WIN32_WINNT=$(WINVER) -DWINVER=$(WINVER)
LIB       = util-c
LIBW32    = $(LIB)-mingw32
OBJS      = $(SRCS:src/%c=BUILD/%o)
OBJSW32   = $(SRCS:src/%c=BUILD-mingw32/%o)

all: lib/lib$(LIB).a lib/lib$(LIBW32).a

clean:
	rm -f  deps
	rm -f  compile.log
	rm -fr lib
	rm -fr BUILD
	rm -fr BUILD-mingw32

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
	i686-w64-mingw32-gcc $(CW32FLAGS) -c -o $@ $<

deps: $(SRCS)
	gcc $(CFLAGS) -MM $(SRCS) | awk '/.*\.o:/ { print "BUILD/"$$0         ; next } { print }'  > $@
	gcc $(CFLAGS) -MM $(SRCS) | awk '/.*\.o:/ { print "BUILD-mingw32/"$$0 ; next } { print }' >> $@

-include deps
