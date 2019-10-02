LIB      = util-c
UTILC    = ../../$(LIB)
CFLAGS   = -I $(UTILC)/inc -O3 -g0 -W -Wall -pedantic -std=c11
LIBFLAGS = -L $(UTILC)/lib -l $(LIB) -lpthread -lefence
SRCS     =\
 src/args.c\
 src/main.c\
 src/map.c\
 src/timeout.c

OBJS = $(SRCS:src/%c=BUILD/%o)

.PHONY: all
all:
	cd .. && make
	make tests
	@echo "'make run' pour les tests fonctionnels avec 'Valgrind'"
	@echo "'make perf' pour les tests de performance"

.PHONY: clean
clean:
	rm -f  tests
	rm -fr bin
	rm -fr Debug
	rm -fr Release

.PHONY: run
run: tests
	valgrind --track-origins=yes --leak-check=full ./tests --perf=false

.PHONY: perf
perf: tests
	./tests --perf=true

$(UTILC)/lib/lib$(LIB).a:
	cd .. && make

tests: $(OBJS) $(UTILC)/lib/lib$(LIB).a
	gcc $(CFLAGS) -o $@ $(OBJS) $(LIBFLAGS)

BUILD/%.o: src/%.c
	@mkdir -p $$(dirname $@)
	gcc $(CFLAGS) -c -o $@ $<
