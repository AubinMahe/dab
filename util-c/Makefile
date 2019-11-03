LIB_C = util-c
SRCS  =\
 src/io/byte_buffer.c\
 src/io/console.c\
 src/io/datagram_socket.c\
 src/io/selector.c\
 src/io/sockets.c\
 src/os/errors.c\
 src/os/event.c\
 src/os/mutex.c\
 src/os/sleep.c\
 src/os/thread.c\
 src/util/args.c\
 src/util/automaton.c\
 src/util/error_codes.c\
 src/util/map.c\
 src/util/timeout.c\
 src/util/timestamp.c

.PHONY: all
all: lib/lib$(LIB_C).a lib/lib$(LIB_C)-mingw32.a lib/lib$(LIB_C)-o64.a

include ../common.mk
-include deps
