SRCS =\
 src/os/errors.c\
 src/os/event.c\
 src/os/mutex.c\
 src/util/args.c\
 src/util/error_codes.c\
 src/util/map.c

CFLAGS       = -I inc -O3 -g0 -W -Wall -pedantic -std=c11
LIB          = cutil
LIB_HEAP     = cutil-heap
LIBW32       = cutil-mingw32
LIBW32_HEAP  = cutil-mingw32-heap
OBJS         = $(SRCS:src/%c=BUILD/%o)
OBJSW32      = $(SRCS:src/%c=BUILD-mingw32/%o)
OBJS_HEAP    = $(SRCS:src/%c=BUILD-heap/%o)
OBJSW32_HEAP = $(SRCS:src/%c=BUILD-mingw32-heap/%o)

all: lib$(LIB).a lib$(LIBW32).a lib$(LIB_HEAP).a lib$(LIBW32_HEAP).a

clean:
	rm -f  lib$(LIB).a
	rm -f  lib$(LIBW32).a
	rm -f  lib$(LIB_HEAP).a
	rm -f  lib$(LIBW32_HEAP).a
	rm -f  deps
	rm -fr BUILD
	rm -fr BUILD-heap
	rm -fr BUILD-mingw32
	rm -fr BUILD-mingw32-heap
#	rm -fr bin
#	rm -fr Debug
#	rm -fr Release

lib$(LIB).a: $(OBJS)
	ar crv $@ $(OBJS)

lib$(LIBW32).a: $(OBJSW32)
	ar crv $@ $(OBJSW32)

lib$(LIB_HEAP).a: $(OBJS_HEAP)
	ar crv $@ $(OBJS_HEAP)

lib$(LIBW32_HEAP).a: $(OBJSW32_HEAP)
	ar crv $@ $(OBJSW32_HEAP)

BUILD/%.o: src/%.c
	@mkdir -p $$(dirname $@)
	gcc $(CFLAGS) -c -o $@ $<

BUILD-heap/%.o: src/%.c
	@mkdir -p $$(dirname $@)
	gcc $(CFLAGS) -DUTIL_USE_HEAP -c -o $@ $<

BUILD-mingw32/%.o: src/%.c
	@mkdir -p $$(dirname $@)
	i686-w64-mingw32-gcc $(CFLAGS) -c -o $@ $<

BUILD-mingw32-heap/%.o: src/%.c
	@mkdir -p $$(dirname $@)
	i686-w64-mingw32-gcc $(CFLAGS) -DUTIL_USE_HEAP -c -o $@ $<

deps: $(SRCS)
	gcc $(CFLAGS)                -MM $(SRCS) | awk '/.*\.o:/ { print "BUILD/"$$0              ; next } { print }'  > $@
	gcc $(CFLAGS) -DUTIL_USE_HEAP -MM $(SRCS) | awk '/.*\.o:/ { print "BUILD-heap/"$$0         ; next } { print }'  > $@
	gcc $(CFLAGS)                -MM $(SRCS) | awk '/.*\.o:/ { print "BUILD-mingw32/"$$0      ; next } { print }' >> $@
	gcc $(CFLAGS) -DUTIL_USE_HEAP -MM $(SRCS) | awk '/.*\.o:/ { print "BUILD-mingw32-heap/"$$0 ; next } { print }' >> $@

-include deps
