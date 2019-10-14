SRCS = src/main.c

-include generated-files.mk

CFLAGS    = -I ../util-c/inc -I inc -I src-gen -O3 -g0 -pedantic -W -Wall -Wextra -Wconversion -c -std=c11
WINVER    = 0x06010000
CW32FLAGS = $(CFLAGS) -DWIN32_WINDOWS=$(WINVER) -D_WIN32_WINNT=$(WINVER) -DWINVER=$(WINVER)

OBJS    = $(SRCS:src/%c=BUILD/%o) $(SRCSGEN:src-gen/%c=BUILD/%o)
OBJSW32 = $(SRCS:src/%c=BUILD-mingw32/%o) $(SRCSGEN:src-gen/%c=BUILD-mingw32/%o)

all: ../unite_de_traitement ../unite_de_traitement-mingw32

clean:
	rm -f  deps
	rm -fr BUILD
	rm -fr BUILD-mingw32
	rm -fr bin
	rm -fr Debug
	rm -fr Release
	rm -fr src-gen

../disappgen.jar:
	cd ../disappgen && ant

generated-files.mk $(SRCSGEN) $(HEADERSGEN): ../dab.xml ../disappgen.jar
	cd .. && ant generate-all-sources

../util-c/lib/libutil-c.a:
	cd ../util-c && make

../util-c/lib/libutil-c-mingw32.a:
	cd ../util-c && make

../unite_de_traitement: ../util-c/lib/libutil-c.a $(OBJS)
	cd ../util-c && make
	gcc $(OBJS) -L../util-c/lib -lutil-c -lpthread -o $@

../unite_de_traitement-mingw32: ../util-c/lib/libutil-c-mingw32.a $(OBJSW32)
	cd ../util-c && make
	i686-w64-mingw32-gcc $(OBJSW32) -L../util-c/lib -lutil-c-mingw32 -lws2_32 -o $@

BUILD/%.o: src/%.c
	@mkdir -p $$(dirname $@)
	gcc $(CFLAGS) -o $@ $<

BUILD/%.o: src-gen/%.c
	@mkdir -p $$(dirname $@)
	gcc $(CFLAGS) -o $@ $<

BUILD-mingw32/%.o: src/%.c
	@mkdir -p $$(dirname $@)
	i686-w64-mingw32-gcc $(CW32FLAGS) -o $@ $<

BUILD-mingw32/%.o: src-gen/%.c
	@mkdir -p $$(dirname $@)
	i686-w64-mingw32-gcc $(CW32FLAGS) -o $@ $<

run-1: ../unite_de_traitement
	xterm -title "UDT-1 C" -geometry 150x30+810-16 -hold -e ../unite_de_traitement --name=udt1 &

run-2: ../unite_de_traitement
	xterm -title "UDT-2 C" -geometry 150x30+0+0 -hold -e ../unite_de_traitement --name=udt2 &

run-win32-1: ../unite_de_traitement-mingw32
	@export WINEPATH=/usr/lib/gcc/i686-w64-mingw32/7.3-win32
	xterm -title "UDT-1 C Win32" -geometry 150x30+810-16 -hold -e wine ../unite_de_traitement-mingw32 --name=udt1

run-win32-2: ../unite_de_traitement-mingw32
	@export WINEPATH=/usr/lib/gcc/i686-w64-mingw32/7.3-win32
	xterm -title "UDT-2 C Win32" -geometry 150x30+0+0 -hold -e wine ../unite_de_traitement-mingw32 --name=udt2

deps: $(SRCS) $(SRCSGEN)
	gcc $(CFLAGS) -MM $(SRCS) $(SRCSGEN) | awk '/.*\.o:/ {print "BUILD/"$$0         ; next } {print}'  > $@
	gcc $(CFLAGS) -MM $(SRCS) $(SRCSGEN) | awk '/.*\.o:/ {print "BUILD-mingw32/"$$0 ; next } {print}' >> $@

-include deps

