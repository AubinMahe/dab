C_SRCS    = src/main.c
C_SRCSGEN = src-gen/ISOSC/factory.c
CINC      = -I ../Banque-c/inc -I ../Banque-c/src-gen

.PHONY: all
all: ../dab-bin/isolated-sc-c ../dab-bin/isolated-sc-c-win32 ../dab-bin/isolated-sc-c-o64

include ../common.mk

../dab-bin/isolated-sc-c: $(OBJS)
	@mkdir -p ../dab-bin
	gcc                  -o $@ $(OBJS)    -L ../dab-lib -lBanque-c -ldabtypes-c -lutil-c -lpthread

../dab-bin/isolated-sc-c-win32: $(OBJSW32)
	@mkdir -p ../dab-bin
	i686-w64-mingw32-gcc -o $@ $(OBJSW32) -L ../dab-lib -lBanque-c -ldabtypes-c -lutil-c -lws2_32

../dab-bin/isolated-sc-c-o64: $(OBJSO64)
	@mkdir -p ../dab-bin
	o64-clang            -o $@ $(OBJSO64) -L ../dab-lib -lBanque-c -ldabtypes-c -lutil-c
