CPP_SRCS    = src/main.cpp
CPP_SRCSGEN = src-gen/isolated/udt1/ComponentFactory.cpp
CPPINC      = -I ../Controleur-cpp/inc -I ../Controleur-cpp/src-gen

.PHONY: all
all: ../dab-bin/isolated-udt1-cpp ../dab-bin/isolated-udt1-cpp-win32 ../dab-bin/isolated-udt1-cpp-o64

include ../common.mk

../dab-bin/isolated-udt1-cpp: $(OBJS)
	@mkdir -p ../dab-bin
	g++ -o $@ $(OBJS) -L ../dab-lib -lControleur-cpp -ldabtypes-cpp -lutil-cpp -lpthread

../dab-bin/isolated-udt1-cpp-win32: $(OBJSW32)
	@mkdir -p ../dab-bin
	i686-w64-mingw32-g++ -o $@ $(OBJSW32) -L ../dab-lib -lControleur-cpp -ldabtypes-cpp -lutil-cpp -lws2_32

../dab-bin/isolated-udt1-cpp-o64: $(OBJSO64)
	@mkdir -p ../dab-bin
	o64-clang++ -o $@ $(OBJSO64) -L ../dab-lib -lControleur-cpp -ldabtypes-cpp -lutil-cpp
