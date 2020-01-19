WINVER           = 0x06010000
WINDOWS_VERSION  = -DWIN32_WINDOWS=$(WINVER) -D_WIN32_WINNT=$(WINVER) -DWINVER=$(WINVER)
CINC            += -I inc -I src-gen -I ../util-c/inc   -I ../dabtypes-c/src-gen
CPPINC          += -I inc -I src-gen -I ../util-cpp/inc -I ../dabtypes-cpp/src-gen -I ../interfaces-cpp/src-gen
RELFLAGS         = -O3 -g0
DBGFLAGS         = -O0 -g3
CFLAGS           = $(CINC)   $(RELFLAGS) -fPIC -pedantic -W -Wall -Wextra -Wconversion -c -std=c11
CPPFLAGS         = $(CPPINC) $(DBGFLAGS) -fPIC -pedantic -W -Wall -Wextra -Wconversion -c -std=c++17
CW32FLAGS        = $(CFLAGS) $(WINDOWS_VERSION)
CO64FLAGS        = $(CFLAGS)
CPPW32FLAGS      = $(CPPFLAGS) $(WINDOWS_VERSION)
CPPO64FLAGS      = $(CPPFLAGS)
DARWIN_AR        = $(HOME)/cctools/bin/ar

-include generated-files.mk

OBJS     = $(C_SRCS:src/%c=BUILD/%o)                $(CPP_SRCS:src/%cpp=BUILD/%o)
OBJSW32  = $(C_SRCS:src/%c=BUILD-mingw32/%o)        $(CPP_SRCS:src/%cpp=BUILD-mingw32/%o)
OBJSO64  = $(C_SRCS:src/%c=BUILD-o64/%o)            $(CPP_SRCS:src/%cpp=BUILD-o64/%o)
OBJS    += $(C_SRCSGEN:src-gen/%c=BUILD/%o)         $(CPP_SRCSGEN:src-gen/%cpp=BUILD/%o)
OBJSW32 += $(C_SRCSGEN:src-gen/%c=BUILD-mingw32/%o) $(CPP_SRCSGEN:src-gen/%cpp=BUILD-mingw32/%o)
OBJSO64 += $(C_SRCSGEN:src-gen/%c=BUILD-o64/%o)     $(CPP_SRCSGEN:src-gen/%cpp=BUILD-o64/%o)

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

BUILD-o64/%.o: src/%.c
	@mkdir -p $$(dirname $@)
	o64-clang $(CO64FLAGS) -c -o $@ $<

BUILD-o64/%.o: src-gen/%.c
	@mkdir -p $$(dirname $@)
	o64-clang $(CO64FLAGS) -c -o $@ $<

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

BUILD-o64/%.o: src/%.cpp
	@mkdir -p $$(dirname $@)
	o64-clang++ $(CPPO64FLAGS) -o $@ $<

BUILD-o64/%.o: src-gen/%.cpp
	@mkdir -p $$(dirname $@)
	o64-clang++ $(CPPO64FLAGS) -o $@ $<
