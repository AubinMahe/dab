SRCS =\
 src/main.c\
 src/dab/udt/unite_de_traitement.c

SRCSGEN =\
 src-gen/dab/etat.c\
 src-gen/dab/carte.c\
 src-gen/dab/compte.c\
 src-gen/dab/net/ihm.c\
 src-gen/dab/net/site_central.c\
 src-gen/dab/net/unite_de_traitement_dispatcher.c

HEADERSGEN=\
 src-gen/dab/etat.h\
 src-gen/dab/ihm.h\
 src-gen/dab/site_central.h\
 src-gen/dab/unite_de_traitement.h\
 src-gen/dab/unite_de_traitement_dispatcher.h

CFLAGS = -I ../util-c/inc -I inc -I src-gen -O3 -g0 -W -Wall -pedantic -c -std=c11

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

$(SRCSGEN) $(HEADERSGEN): ../dab.xml ../disappgen.jar
	cd .. && ant generate-all-sources

../util-c/lib/libutil-c.a:
	cd ../util-c && make

../util-c/lib/libutil-c-mingw32.a:
	cd ../util-c && make

../unite_de_traitement: ../util-c/lib/libutil-c.a $(OBJS)
	cd ../util-c && make
	gcc $(OBJS) -L../util-c/lib -lutil-c -o $@

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
	i686-w64-mingw32-gcc $(CFLAGS) -o $@ $<

BUILD-mingw32/%.o: src-gen/%.c
	@mkdir -p $$(dirname $@)
	i686-w64-mingw32-gcc $(CFLAGS) -o $@ $<

run:
	make ../unite_de_traitement
	cd ../sc        && xterm -geometry 132x30-650+0 -e 'ant run' &
	cd ../dab       && xterm -geometry 132x30-800-0 -e 'ant run' &
	../unite_de_traitement\
 --iface=$(IFACE)\
 --udt-port=$(UDT_PORT)\
 --sc-address=$(SC_ADDRESS)\
 --sc-port=$(SC_PORT)\
 --dab-address=$(DAB_ADDRESS)\
 --dab-port=$(DAB_PORT)

run-debug:
	cd ../sc        && xterm -geometry 132x30-650+0 -e 'ant run' &
	cd ../dab       && xterm -geometry 132x30-800-0 -e 'ant run' &
	@echo "Allez, c'est parti pour le debug du bordel en C sous Eclipse..."

run-mingw32:
	make ../unite_de_traitement-mingw32
	cd ../sc        && xterm -geometry 132x30-650-0 -e 'ant run' &
	cd ../dab       && xterm -geometry 132x30-800-0 -e 'ant run' &
	WINEPATH=/usr/lib/gcc/i686-w64-mingw32/7.3-win32 wine\
 ../unite_de_traitement-mingw32\
 --iface=$(IFACE)\
 --udt-port=$(UDT_PORT)\
 --sc-address=$(SC_ADDRESS)\
 --sc-port=$(SC_PORT)\
 --dab-address=$(DAB_ADDRESS)\
 --dab-port=$(DAB_PORT)

deps: $(SRCS) $(SRCSGEN)
	gcc $(CFLAGS) -MM $(SRCS) $(SRCSGEN) | awk '/.*\.o:/ {print "BUILD/"$$0         ; next } {print}'  > $@
	gcc $(CFLAGS) -MM $(SRCS) $(SRCSGEN) | awk '/.*\.o:/ {print "BUILD-mingw32/"$$0 ; next } {print}' >> $@

-include deps

