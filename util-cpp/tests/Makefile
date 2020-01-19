SRCS =\
 src/main.cpp\
 src/exceptionsTests.cpp\
 src/timeoutTests.cpp\
 src/facetMessagesQueueTests.cpp

CPPFLAGS    = -I ../inc -O3 -g0 -pedantic -W -Wall -Wextra -Wconversion -std=c++17
LIB         = util-cpp
LIBW32      = $(LIB).dll
LIBFLAGS    = -L ../../dab-lib -l $(LIB) -lpthread -lefence
LIBW32FLAGS = -L ../../dab-lib -l $(LIBW32) -lws2_32
OBJS        = $(SRCS:src/%cpp=BUILD/%o)
OBJSW32     = $(SRCS:src/%cpp=BUILD-mingw32/%o)

.PHONY: all
all:
	cd .. && make
	make tests tests-win32
	@echo "'make run[-win32]' pour les tests fonctionnels avec 'Valgrind'"
	@echo "'make perf[-win32]' pour les tests de performance"

.PHONY: clean
clean:
	rm -f  deps
	rm -f  tests
	rm -f  tests-win32
	rm -fr BUILD
	rm -fr BUILD-mingw32

tests: $(OBJS)
	cd .. && make
	g++ $(CPPFLAGS) -o $@ $(OBJS) $(LIBFLAGS)

tests-win32: $(OBJSW32)
	cd .. && make
	i686-w64-mingw32-g++ $(CPPFLAGS) -o $@ $(OBJSW32) $(LIBW32FLAGS)

.PHONY: run
run: tests
	valgrind --track-origins=yes --leak-check=full ./tests --perf=false

.PHONY: run-win32
run-win32: tests-win32
	WINEPATH=/usr/lib/gcc/i686-w64-mingw32/7.3-win32\
 WINEDLLPATH=/home/aubin/Dev/git/dab/dab-lib/\
 wine ./tests-win32 --perf=false

.PHONY: perf
perf: tests
	./tests --perf=true

.PHONY: perf-win32
perf-win32: tests-win32
	WINEPATH=/usr/lib/gcc/i686-w64-mingw32/7.3-win32 wine ./tests-win32 --perf=true

BUILD/%.o: src/%.cpp
	@mkdir -p $$(dirname $@)
	g++ $(CPPFLAGS) -c -o $@ $<

BUILD-mingw32/%.o: src/%.cpp
	@mkdir -p $$(dirname $@)
	i686-w64-mingw32-g++ $(CPPFLAGS) -c -o $@ $<

deps: $(SRCS)
	g++ $(CPPFLAGS) -c -MM $(SRCS) | awk '/.*\.o:/ {print "BUILD/"$$0        ; next } {print}'  > $@
	g++ $(CPPFLAGS) -c -MM $(SRCS) | awk '/.*\.o:/ {print "BUILD-mingw32/"$$0; next } {print}' >> $@

-include deps
