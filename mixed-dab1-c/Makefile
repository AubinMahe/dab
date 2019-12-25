C_SRCS    = src/main.c
C_SRCSGEN = src-gen/mixed_dab1/factory.c
CINC      = -I ../Controleur-c/inc -I ../Controleur-c/src-gen -I ../Distributeur-c/inc -I ../Distributeur-c/src-gen

.PHONY: all
all: ../dab-bin/mixed-dab1-c ../dab-bin/mixed-dab1-c-win32 ../dab-bin/mixed-dab1-c-o64

include ../common.mk

../dab-bin/mixed-dab1-c: $(OBJS)
	@mkdir -p ../dab-bin
	g++                  -o $@ $(OBJS)    -L ../dab-lib -lControleur-c -lDistributeur-c -ldabtypes-c -lutil-c -lpthread

../dab-bin/mixed-dab1-c-win32: $(OBJSW32)
	@mkdir -p ../dab-bin
	i686-w64-mingw32-g++ -o $@ $(OBJSW32) -L ../dab-lib -lControleur-c -lDistributeur-c -ldabtypes-c -lutil-c -lws2_32

../dab-bin/mixed-dab1-c-o64: $(OBJSO64)
	@mkdir -p ../dab-bin
	o64-clang++          -o $@ $(OBJSO64) -L ../dab-lib -lControleur-c -lDistributeur-c -ldabtypes-c -lutil-c
