CPP_SRCS    = src/main.cpp
CPP_SRCSGEN = src-gen/isolated/sc/ComponentFactory.cpp
CPPINC      = -I ../Banque-cpp/inc -I ../Banque-cpp/src-gen

.PHONY: all
all: ../dab-bin/isolated-sc-cpp ../dab-bin/isolated-sc-cpp-win32 ../dab-bin/isolated-sc-cpp-o64

include ../common.mk

../dab-bin/isolated-sc-cpp: $(OBJS)
	@mkdir -p ../dab-bin
	g++                  -o $@ $(OBJS)    -L ../dab-lib -linterfaces-cpp -lBanque-cpp -ldabtypes-cpp -lutil-cpp -lpthread

../dab-bin/isolated-sc-cpp-win32: $(OBJSW32)
	@mkdir -p ../dab-bin
	i686-w64-mingw32-g++ -o $@ $(OBJSW32) -L ../dab-lib -linterfaces-cpp -lBanque-cpp -ldabtypes-cpp -lutil-cpp -lws2_32

../dab-bin/isolated-sc-cpp-o64: $(OBJSO64)
	@mkdir -p ../dab-bin
	o64-clang++          -o $@ $(OBJSO64) -L ../dab-lib -linterfaces-cpp -lBanque-cpp -ldabtypes-cpp -lutil-cpp
