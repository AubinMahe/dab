C_SRCS    = src/main.c
C_SRCSGEN = src-gen/allin_one/factory.c
CINC      =\
 -I ../Banque-c/inc\
 -I ../Banque-c/src-gen\
 -I ../Controleur-c/src-gen\
 -I ../Distributeur-c/inc\
 -I ../Distributeur-c/src-gen

.PHONY: all
all: ../dab-bin/allin-one-c ../dab-bin/allin-one-c-win32 ../dab-bin/allin-one-c-o64

include ../common.mk

../dab-bin/allin-one-c: $(OBJS)
	@mkdir -p ../dab-bin
	gcc                  -o $@ $(OBJS)    -L ../dab-lib -lBanque-c -lControleur-c -lDistributeur-c -ldabtypes-c -lutil-c -lpthread

../dab-bin/allin-one-c-win32: $(OBJSW32)
	@mkdir -p ../dab-bin
	i686-w64-mingw32-gcc -o $@ $(OBJSW32) -L ../dab-lib -lBanque-c -lControleur-c -lDistributeur-c -ldabtypes-c -lutil-c -lws2_32

../dab-bin/allin-one-c-o64: $(OBJSO64)
	@mkdir -p ../dab-bin
	o64-clang            -o $@ $(OBJSO64) -L ../dab-lib -lBanque-c -lControleur-c -lDistributeur-c -ldabtypes-c -lutil-c
