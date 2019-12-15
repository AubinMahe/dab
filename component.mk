include ../common.mk
-include generated-files.mk

.PHONY: all
ifdef COMP_C

all: ../dab-lib/lib$(COMP_C)-c.so     ../dab-lib/$(COMP_C)-c.dll     ../dab-lib/lib$(COMP_C)-c.dylib

else
ifdef COMP_CPP

all: ../dab-lib/lib$(COMP_CPP)-cpp.so ../dab-lib/$(COMP_CPP)-cpp.dll ../dab-lib/lib$(COMP_CPP)-cpp.dylib

endif
endif

.PHONY: clean
clean:
	rm -f  deps.mk
	rm -f  compile.log
	rm -fr BUILD
	rm -fr BUILD-mingw32
	rm -fr BUILD-o64
	rm -f ../dab-lib/lib$(COMP_C)-c.so
	rm -f ../dab-lib/$(COMP_C)-c.dll
	rm -f ../dab-lib/lib$(COMP_C)-c.dll.a
	rm -f ../dab-lib/lib$(COMP_C)-c.dylib
	rm -f ../dab-lib/lib$(COMP_CPP)-cpp.so
	rm -f ../dab-lib/$(COMP_CPP)-cpp.dll
	rm -f ../dab-lib/lib$(COMP_CPP)-cpp.dll.a
	rm -f ../dab-lib/lib$(COMP_CPP)-cpp.dylib

ifdef COMP_C

../dab-lib/lib$(COMP_C)-c.so: $(OBJS)
	@mkdir -p ../dab-lib
	gcc -shared -o $@ $(OBJS)

../dab-lib/$(COMP_C)-c.dll: $(OBJSW32)
	@mkdir -p ../dab-lib
	i686-w64-mingw32-gcc -shared -o $@ $(OBJSW32) $(WIN_DEPS) $(DLL_DEPS) -Wl,--out-implib,../dab-lib/lib$(COMP_C)-c.dll.a

../dab-lib/lib$(COMP_C)-c.dylib: $(OBJSO64)
	@mkdir -p ../dab-lib
	o64-clang -dynamiclib -current_version 1.0 -compatibility_version 1.0 -o $@ $(OBJSO64) $(MAC_DEPS) $(DLL_DEPS)

   DEPS_CC    = gcc
   DEPS_FLAGS = $(CINC)
else
ifdef COMP_CPP

../dab-lib/lib$(COMP_CPP)-cpp.so: $(OBJS)
	@mkdir -p ../dab-lib
	g++ -shared -o $@ $(OBJS)

../dab-lib/$(COMP_CPP)-cpp.dll: $(OBJSW32)
	@mkdir -p ../dab-lib
	i686-w64-mingw32-g++ -shared -o $@ $(OBJSW32) $(WIN_DEPS) $(DLL_DEPS) -Wl,--out-implib,../dab-lib/lib$(COMP_CPP)-cpp.dll.a

../dab-lib/lib$(COMP_CPP)-cpp.dylib: $(OBJSO64)
	@mkdir -p ../dab-lib
	o64-clang++ -dynamiclib -current_version 1.0 -compatibility_version 1.0 -o $@ $(OBJSO64) $(MAC_DEPS) $(DLL_DEPS)

   DEPS_CC    = g++
   DEPS_FLAGS = $(CPPINC)
endif
endif

deps.mk: $(CPP_SRCS) $(CPP_SRCSGEN)
	$(DEPS_CC) $(DEPS_FLAGS) -MM $(CPP_SRCS) $(CPP_SRCSGEN) | awk '/.*\.o:/ {print "BUILD/"$$0         ; next } {print}'  > $@
	$(DEPS_CC) $(DEPS_FLAGS) -MM $(CPP_SRCS) $(CPP_SRCSGEN) | awk '/.*\.o:/ {print "BUILD-mingw32/"$$0 ; next } {print}' >> $@
	$(DEPS_CC) $(DEPS_FLAGS) -MM $(CPP_SRCS) $(CPP_SRCSGEN) | awk '/.*\.o:/ {print "BUILD-o64/"$$0     ; next } {print}' >> $@

-include deps.mk
