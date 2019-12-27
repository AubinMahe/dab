CPP_SRCS    = src/main.cpp
CPP_SRCSGEN = src-gen/allin/one/ComponentFactory.cpp
CPPINC      =\
 -I ../Banque-cpp/inc\
 -I ../Banque-cpp/src-gen\
 -I ../Controleur-cpp/src-gen\
 -I ../Controleur-cpp/inc\
 -I ../Distributeur-cpp/inc\
 -I ../Distributeur-cpp/src-gen

.PHONY: all
all: ../dab-bin/allin-one-cpp ../dab-bin/allin-one-cpp-win32 ../dab-bin/allin-one-cpp-o64

include ../common.mk

../dab-bin/allin-one-cpp: $(OBJS)
	@mkdir -p ../dab-bin
	g++                  -o $@ $(OBJS)    -L ../dab-lib -lBanque-cpp -lControleur-cpp -lDistributeur-cpp -ldabtypes-cpp -lutil-cpp -lpthread

../dab-bin/allin-one-cpp-win32: $(OBJSW32)
	@mkdir -p ../dab-bin
	i686-w64-mingw32-g++ -o $@ $(OBJSW32) -L ../dab-lib -lBanque-cpp -lControleur-cpp -lDistributeur-cpp -ldabtypes-cpp -lutil-cpp -lws2_32

../dab-bin/allin-one-cpp-o64: $(OBJSO64)
	@mkdir -p ../dab-bin
	o64-clang++          -o $@ $(OBJSO64) -L ../dab-lib -lBanque-cpp -lControleur-cpp -lDistributeur-cpp -ldabtypes-cpp -lutil-cpp
