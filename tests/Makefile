CUTIL    = ../cutil
CFLAGS   = -I $(CUTIL)/inc -O3 -g0 -W -Wall -pedantic -std=c11 
LIBFLAGS = -L $(CUTIL)/lib -l cutil -lefence

all: tests
	valgrind --leak-check=full ./tests

clean:
	rm -f tests

tests: src/main.c
	cd $(CUTIL) && make
	gcc $(CFLAGS) -o $@ $< $(LIBFLAGS)
