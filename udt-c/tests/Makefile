LIB      = util-c
UTILC    = ../../$(LIB)
CFLAGS   = -I $(UTILC)/inc -O3 -g0 -W -Wall -pedantic -std=c11
LIBFLAGS = -L $(UTILC)/lib -l $(LIB) -lefence

all: tests run

clean:
	rm -f tests

run:
	valgrind --leak-check=full ./tests

tests: src/main.c
	cd .. && make
	gcc $(CFLAGS) -o $@ $< $(LIBFLAGS)

