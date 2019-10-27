EXEC = SiteCentral
NAME = Site-Central
XTERM_SIZE_POSX= 45x30-0
SRCS =\
 src/sc/Banque.cpp\
 src/sc/BanqueUI.cpp\
 src/sc/Repository.cpp\
 src/main.cpp

-include generated-files.mk

CPPFLAGS    = -I ../util-cpp/inc -I ../dabtypes-cpp/inc-gen -I inc -I src-gen -O3 -g0 -pedantic -W -Wall -Wextra -Wconversion -c -std=c++17
WINVER      = 0x06010000
CPPW32FLAGS = $(CPPFLAGS) -DWIN32_WINDOWS=$(WINVER) -D_WIN32_WINNT=$(WINVER) -DWINVER=$(WINVER)

OBJS    = $(SRCS:src/%cpp=BUILD/%o) $(SRCSGEN:src-gen/%cpp=BUILD/%o)
OBJSW32 = $(SRCS:src/%cpp=BUILD-mingw32/%o) $(SRCSGEN:src-gen/%cpp=BUILD-mingw32/%o)

all: ../$(EXEC) ../$(EXEC)-mingw32

clean:
	rm -f  deps
	rm -f  compile.log
	rm -fr BUILD
	rm -fr BUILD-mingw32

../$(EXEC): ../util-cpp/lib/libutil-cpp.a ../dabtypes-cpp/lib/libdabtypes-cpp.a $(OBJS)
	g++ $(OBJS) -L../util-cpp/lib -lutil-cpp -L../dabtypes-cpp/lib -ldabtypes-cpp -lpthread -o $@

../$(EXEC)-mingw32: ../util-cpp/lib/libutil-cpp-mingw32.a $(OBJSW32)
	i686-w64-mingw32-g++ $(OBJSW32) -L../util-cpp/lib -lutil-cpp-mingw32 -L../dabtypes-cpp/lib -ldabtypes-cpp-mingw32 -lws2_32 -o $@

BUILD/%.o: src/%.cpp
	@mkdir -p $$(dirname $@)
	g++ $(CPPFLAGS) -o $@ $<

BUILD/%.o: src-gen/%.cpp
	@mkdir -p $$(dirname $@)
	g++ $(CPPFLAGS) -o $@ $<

BUILD-mingw32/%.o: src/%.cpp
	@mkdir -p $$(dirname $@)
	i686-w64-mingw32-g++ $(CPPW32FLAGS) -o $@ $<

BUILD-mingw32/%.o: src-gen/%.cpp
	@mkdir -p $$(dirname $@)
	i686-w64-mingw32-g++ $(CPPW32FLAGS) -o $@ $<

run: ../$(EXEC)
	xterm -title "$(NAME)-1 C++" -geometry $(XTERM_SIZE_POSX)-16 -e '(../$(EXEC) --name=sc 2>/dev/pts/2)' &

run-win32: ../$(EXEC)-mingw32
	@export WINEPATH=/usr/lib/gcc/i686-w64-mingw32/7.3-win32
	xterm -title "$(NAME)-1 C++ Win32" -geometry $(XTERM_SIZE_POSX)-16 -hold -e wine ../$(EXEC)-mingw32 --name=sc

deps: $(SRCS) $(SRCSGEN)
	g++ $(CPPFLAGS) -MM $(SRCS) $(SRCSGEN) | awk '/.*\.o:/ {print "BUILD/"$$0         ; next } {print}'  > $@
	g++ $(CPPFLAGS) -MM $(SRCS) $(SRCSGEN) | awk '/.*\.o:/ {print "BUILD-mingw32/"$$0 ; next } {print}' >> $@

-include deps
