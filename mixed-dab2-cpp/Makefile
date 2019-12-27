CPP_SRCS    = src/main.cpp
CPP_SRCSGEN = src-gen/mixed/dab2/ComponentFactory.cpp
CPPINC      = -I ../Controleur-cpp/inc -I ../Controleur-cpp/src-gen -I ../Distributeur-cpp/inc -I ../Distributeur-cpp/src-gen

.PHONY: all
all: ../dab-bin/mixed-dab2-cpp ../dab-bin/mixed-dab2-cpp-win32 ../dab-bin/mixed-dab2-cpp-o64

include ../common.mk

../dab-bin/mixed-dab2-cpp: $(OBJS)
	@mkdir -p ../dab-bin
	g++                  -o $@ $(OBJS)    -L ../dab-lib -lControleur-cpp -lDistributeur-cpp -ldabtypes-cpp -lutil-cpp -lpthread

../dab-bin/mixed-dab2-cpp-win32: $(OBJSW32)
	@mkdir -p ../dab-bin
	i686-w64-mingw32-g++ -o $@ $(OBJSW32) -L ../dab-lib -lControleur-cpp -lDistributeur-cpp -ldabtypes-cpp -lutil-cpp -lws2_32

../dab-bin/mixed-dab2-cpp-o64: $(OBJSO64)
	@mkdir -p ../dab-bin
	o64-clang++          -o $@ $(OBJSO64) -L ../dab-lib -lControleur-cpp -lDistributeur-cpp -ldabtypes-cpp -lutil-cpp
