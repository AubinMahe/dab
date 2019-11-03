WINVER          = 0x06010000
WINDOWS_VERSION = -DWIN32_WINDOWS=$(WINVER) -D_WIN32_WINNT=$(WINVER) -DWINVER=$(WINVER)
CINC            = -I inc -I src-gen -I ../util-c/inc   -I ../dabtypes-c/inc-gen
CPPINC          = -I inc -I src-gen -I ../util-cpp/inc -I ../dabtypes-cpp/inc-gen
RELFLAGS        = -O3 -g0
DBGFLAGS        = -O0 -g3
CFLAGS          = $(CINC)   $(RELFLAGS) -pedantic -W -Wall -Wextra -Wconversion -c -std=c11
CPPFLAGS        = $(CPPINC) $(RELFLAGS) -pedantic -W -Wall -Wextra -Wconversion -c -std=c++17
CW32FLAGS       = $(CFLAGS) $(WINDOWS_VERSION)
CO64FLAGS       = $(CFLAGS)
CPPW32FLAGS     = $(CPPFLAGS) $(WINDOWS_VERSION)
CPPO64FLAGS     = $(CPPFLAGS)
OBJS            = $(SRCS:src/%cpp=BUILD/%o)
OBJSW32         = $(SRCS:src/%cpp=BUILD-mingw32/%o)
OBJSO64         = $(SRCS:src/%cpp=BUILD-o64/%o)
DARWIN_AR       = $(HOME)/cctools/bin/ar
ifdef EXEC_C
   TARGET_IS_C = 1
else
   ifdef LIB_C
      TARGET_IS_C = 1
   endif
endif
ifdef TARGET_IS_C
   OBJS    = $(SRCS:src/%c=BUILD/%o)
   OBJSW32 = $(SRCS:src/%c=BUILD-mingw32/%o)
   OBJSO64 = $(SRCS:src/%c=BUILD-o64/%o)
   ifdef SRCSGEN
      OBJS    += $(SRCSGEN:src-gen/%c=BUILD/%o)
      OBJSW32 += $(SRCSGEN:src-gen/%c=BUILD-mingw32/%o)
      OBJSO64 += $(SRCSGEN:src-gen/%c=BUILD-o64/%o)
   endif
   DEPS_CC       = gcc
   DEPS_CC_FLAGS = $(CINC)
else
   OBJS    = $(SRCS:src/%cpp=BUILD/%o)
   OBJSW32 = $(SRCS:src/%cpp=BUILD-mingw32/%o)
   OBJSO64 = $(SRCS:src/%cpp=BUILD-o64/%o)
   ifdef SRCSGEN
      OBJS    += $(SRCSGEN:src-gen/%cpp=BUILD/%o)
      OBJSW32 += $(SRCSGEN:src-gen/%cpp=BUILD-mingw32/%o)
      OBJSO64 += $(SRCSGEN:src-gen/%cpp=BUILD-o64/%o)
   endif
   DEPS_CC       = g++
   DEPS_CC_FLAGS = $(CPPINC)
endif

.PHONY: clean
clean:
	rm -f  deps
	rm -f  compile.log
	rm -fr lib
	rm -fr BUILD
	rm -fr BUILD-mingw32
	rm -fr BUILD-o64

ifdef LIB_C

lib/lib$(LIB_C).a: $(OBJS)
	@mkdir -p $$(dirname $@)
	ar crv $@ $(OBJS)

lib/lib$(LIB_C)-mingw32.a: $(OBJSW32)
	@mkdir -p $$(dirname $@)
	ar crv $@ $(OBJSW32)

lib/lib$(LIB_C)-o64.a: $(OBJSO64)
	@mkdir -p $$(dirname $@)
	$(DARWIN_AR) crv $@ $(OBJSO64)

else
ifdef LIB_CPP

lib/lib$(LIB_CPP).a: $(OBJS)
	@mkdir -p $$(dirname $@)
	ar crv $@ $(OBJS)

lib/lib$(LIB_CPP)-mingw32.a: $(OBJSW32)
	@mkdir -p $$(dirname $@)
	ar crv $@ $(OBJSW32)

lib/lib$(LIB_CPP)-o64.a: $(OBJSO64)
	@mkdir -p $$(dirname $@)
	$(DARWIN_AR) crv $@ $(OBJSO64)

else

../util-cpp/lib/libutil-cpp.a\
 ../util-cpp/lib/libutil-cpp-mingw32.a\
 ../util-cpp/lib/libutil-cpp-o64.a:
	cd ../util-cpp && make

../dabtypes-cpp/lib/libdabtypes-cpp.a\
 ../dabtypes-cpp/lib/libdabtypes-cpp-mingw32.a\
 ../dabtypes-cpp/lib/libdabtypes-cpp-o64.a:
	cd ../dabtypes-cpp && make

ifdef EXEC_C

../$(EXEC_C): ../util-c/lib/libutil-c.a ../dabtypes-c/lib/libdabtypes-c.a $(OBJS)
	cd ../util-c && make
	cd ../dabtypes-c && make
	gcc $(OBJS) -L../util-c/lib -lutil-c -L../dabtypes-c/lib -ldabtypes-c -lpthread -o $@

../$(EXEC_C)-mingw32: ../util-c/lib/libutil-c-mingw32.a ../dabtypes-c/lib/libdabtypes-c-mingw32.a $(OBJSW32)
	cd ../util-c && make
	cd ../dabtypes-c && make
	i686-w64-mingw32-gcc $(OBJSW32) -L../util-c/lib -lutil-c-mingw32 -L../dabtypes-c/lib -ldabtypes-c-mingw32 -lws2_32 -o $@

../$(EXEC_C)-o64: ../util-c/lib/libutil-c-o64.a ../dabtypes-c/lib/libdabtypes-c-o64.a $(OBJSO64)
	cd ../util-c && make
	cd ../dabtypes-c && make
	o64-clang $(OBJSO64) -L../util-c/lib -lutil-c-o64 -L../dabtypes-c/lib -ldabtypes-c-o64 -o $@

else

../$(EXEC_CPP): ../util-cpp/lib/libutil-cpp.a ../dabtypes-cpp/lib/libdabtypes-cpp.a $(OBJS)
	cd ../util-cpp && make
	cd ../dabtypes-cpp && make
	g++ -o $@ $(OBJS) -L../dabtypes-cpp/lib -ldabtypes-cpp -L../util-cpp/lib -lutil-cpp -lpthread

../$(EXEC_CPP)-mingw32: ../util-cpp/lib/libutil-cpp-mingw32.a ../dabtypes-cpp/lib/libdabtypes-cpp-mingw32.a $(OBJSW32)
	cd ../util-cpp && make
	cd ../dabtypes-cpp && make
	i686-w64-mingw32-g++ -o $@ $(OBJSW32) -L../dabtypes-cpp/lib -ldabtypes-cpp-mingw32 -L../util-cpp/lib -lutil-cpp-mingw32 -lws2_32

../$(EXEC_CPP)-o64: ../util-cpp/lib/libutil-cpp-o64.a ../dabtypes-cpp/lib/libdabtypes-cpp-o64.a $(OBJSO64)
	cd ../util-cpp && make
	cd ../dabtypes-cpp && make
	o64-clang++ -o $@ $(OBJSO64) -L../dabtypes-cpp/lib -ldabtypes-cpp-o64 -L../util-cpp/lib -lutil-cpp-o64 -lpthread

endif
endif
endif

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

deps: $(SRCS) $(SRCSGEN)
	$(DEPS_CC) $(DEPS_CC_FLAGS) -MM $(SRCS) $(SRCSGEN) | awk '/.*\.o:/ {print "BUILD/"$$0         ; next } {print}'  > $@
	$(DEPS_CC) $(DEPS_CC_FLAGS) -MM $(SRCS) $(SRCSGEN) | awk '/.*\.o:/ {print "BUILD-mingw32/"$$0 ; next } {print}' >> $@
	$(DEPS_CC) $(DEPS_CC_FLAGS) -MM $(SRCS) $(SRCSGEN) | awk '/.*\.o:/ {print "BUILD-o64/"$$0     ; next } {print}' >> $@
