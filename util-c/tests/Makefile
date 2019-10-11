LIB         = util-c
LIBW32      = $(LIB)-mingw32
UTILC       = ../../$(LIB)
CFLAGS      = -I $(UTILC)/inc -O3 -g0 -W -pedantic -Wall -Wextra -Wconversion -std=c11
LIBFLAGS    = -L $(UTILC)/lib -l $(LIB) -lpthread -lefence
LIBW32FLAGS = -L $(UTILC)/lib -l $(LIBW32) -lws2_32
SRCS     =\
 src/args.c\
 src/main.c\
 src/map.c\
 src/timeout.c

OBJS    = $(SRCS:src/%c=BUILD/%o)
OBJSW32 = $(SRCS:src/%c=BUILD-mingw32/%o)

.PHONY: all
all:
	cd .. && make
	make tests tests-win32
	@echo "'make run[-win32]' pour les tests fonctionnels avec 'Valgrind'"
	@echo "'make perf[-win32]' pour les tests de performance"

.PHONY: clean
clean:
	rm -f  tests
	rm -fr bin
	rm -fr Debug
	rm -fr Release

.PHONY: run
run: tests
	valgrind --track-origins=yes --leak-check=full ./tests --perf=false

.PHONY: run-win32
run-win32: tests-win32
	WINEPATH=/usr/lib/gcc/i686-w64-mingw32/7.3-win32 wine ./tests-win32 --perf=false

.PHONY: perf
perf: tests
	./tests --perf=true

.PHONY: perf-win32
perf-win32: tests-win32
	WINEPATH=/usr/lib/gcc/i686-w64-mingw32/7.3-win32 wine ./tests-win32 --perf=true

$(UTILC)/lib/lib$(LIB).a $(UTILC)/lib/lib$(LIBW32).a:
	cd .. && make

tests: $(OBJS) $(UTILC)/lib/lib$(LIB).a
	gcc $(CFLAGS) -o $@ $(OBJS) $(LIBFLAGS)

tests-win32: $(OBJSW32) $(UTILC)/lib/lib$(LIBW32).a
	i686-w64-mingw32-gcc $(CFLAGS) -o $@ $(OBJSW32) $(LIBW32FLAGS)

BUILD/%.o: src/%.c
	@mkdir -p $$(dirname $@)
	gcc $(CFLAGS) -c -o $@ $<

BUILD-mingw32/%.o: src/%.c
	@mkdir -p $$(dirname $@)
	i686-w64-mingw32-gcc $(CFLAGS) -c -o $@ $<
