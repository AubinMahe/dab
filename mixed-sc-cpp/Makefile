C_SRCS    = src/main.cpp
C_SRCSGEN = src-gen/mixed_sc/factory.cpp
CINC      = -I ../Banque-cpp/inc -I ../Banque-cpp/src-gen

.PHONY: all
all: ../dab-bin/mixed-sc-cpp ../dab-bin/mixed-sc-cpp-win32 ../dab-bin/mixed-sc-cpp-o64

include ../common.mk

../dab-bin/mixed-sc-cpp: $(OBJS)
	@mkdir -p ../dab-bin
	g++ -o $@ $(OBJS) -L ../dab-lib -lBanque-cpp -ldabtypes-cpp -lutil-cpp -lpthread

../dab-bin/mixed-sc-cpp-win32: $(OBJSW32)
	@mkdir -p ../dab-bin
	i686-w64-mingw32-g++ -o $@ $(OBJSW32) -L ../dab-lib -lBanque-cpp -ldabtypes-cpp -lutil-cpp -lws2_32

../dab-bin/mixed-sc-cpp-o64: $(OBJSO64)
	@mkdir -p ../dab-bin
	o64-clang++ -o $@ $(OBJSO64) -L ../dab-lib -lBanque-cpp -ldabtypes-cpp -lutil-cpp
