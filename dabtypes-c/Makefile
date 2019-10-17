-include generated-files.mk

CFLAGS    = -I inc-gen -I ../util-c/inc -O3 -g0 -pedantic -W -Wall -Wextra -Wconversion -std=c11
WINVER    = 0x06010000
CW32FLAGS = $(CFLAGS) -DWIN32_WINDOWS=$(WINVER) -D_WIN32_WINNT=$(WINVER) -DWINVER=$(WINVER)
LIB       = dabtypes-c
LIBW32    = $(LIB)-mingw32
OBJS      = $(SRCSGEN:src-gen/%c=BUILD/%o)
OBJSW32   = $(SRCSGEN:src-gen/%c=BUILD-mingw32/%o)

all: lib/lib$(LIB).a lib/lib$(LIBW32).a

clean:
	rm -f  deps
	rm -fr lib
	rm -fr BUILD
	rm -fr BUILD-mingw32

lib/lib$(LIB).a: $(OBJS)
	@mkdir -p $$(dirname $@)
	ar crv $@ $(OBJS)

lib/lib$(LIBW32).a: $(OBJSW32)
	@mkdir -p $$(dirname $@)
	ar crv $@ $(OBJSW32)

BUILD/%.o: src-gen/%.c
	@mkdir -p $$(dirname $@)
	gcc $(CFLAGS) -c -o $@ $<

BUILD-mingw32/%.o: src-gen/%.c
	@mkdir -p $$(dirname $@)
	i686-w64-mingw32-gcc $(CW32FLAGS) -c -o $@ $<

deps: $(SRCSGEN)
	gcc $(CFLAGS) -MM $(SRCSGEN) | awk '/.*\.o:/ { print "BUILD/"$$0         ; next } { print }'  > $@
	gcc $(CFLAGS) -MM $(SRCSGEN) | awk '/.*\.o:/ { print "BUILD-mingw32/"$$0 ; next } { print }' >> $@

-include deps
