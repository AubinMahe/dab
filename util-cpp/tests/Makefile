SRCS =\
 src/main.cpp\
 src/exceptionsTests.cpp\
 src/timeoutTests.cpp

CPPFLAGS    = -I ../inc -O3 -g0 -pedantic -W -Wall -Wextra -Wconversion -c -std=c++17
LIB         = util-cpp
LIBW32      = $(LIB)-mingw32
LIBFLAGS    = -L ../lib -l $(LIB) -lpthread -lefence
LIBW32FLAGS = -L ../lib -l $(LIBW32) -lws2_32
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
	rm -f  tests
	rm -f  tests-win32
	rm -fr BUILD
	rm -fr BUILD-mingw32

$(UTILC)/lib/lib$(LIB).a $(UTILC)/lib/lib$(LIBW32).a:
	cd .. && make

tests: $(OBJS) $(UTILC)/lib/lib$(LIB).a
	cd .. && make
	g++ $(CFLAGS) -o $@ $(OBJS) $(LIBFLAGS)

tests-win32: $(OBJSW32) $(UTILC)/lib/lib$(LIBW32).a
	cd .. && make
	i686-w64-mingw32-g++ $(CFLAGS) -o $@ $(OBJSW32) $(LIBW32FLAGS)

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

BUILD/%.o: src/%.cpp
	@mkdir -p $$(dirname $@)
	g++ $(CPPFLAGS) -o $@ $<

BUILD-mingw32/%.o: src/%.cpp
	@mkdir -p $$(dirname $@)
	i686-w64-mingw32-g++ $(CPPFLAGS) -o $@ $<

deps: $(SRCS)
	g++ $(CPPFLAGS) -MM $(SRCS) | awk '/.*\.o:/ {print "BUILD/"$$0        ; next } {print}'  > $@
	g++ $(CPPFLAGS) -MM $(SRCS) | awk '/.*\.o:/ {print "BUILD-mingw32/"$$0; next } {print}' >> $@

-include deps
