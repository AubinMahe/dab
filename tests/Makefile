all: tests tests-heap
	valgrind --leak-check=full ./tests
	valgrind --leak-check=full ./tests-heap

clean:
	rm -f tests
	rm -f tests-heap

tests: src/main.c
	cd ../cutil && make
	gcc -I ../cutil/inc -O3 -g0 -W -Wall -pedantic -std=c11 -o $@ $< -L ../cutil -l cutil -lefence

tests-heap: src/main.c
	cd ../cutil && make
	gcc -I ../cutil/inc -O3 -g0 -W -Wall -pedantic -std=c11 -DUTIL_USE_HEAP -o $@ $< -L ../cutil -l cutil-heap -lefence

