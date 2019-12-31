C_SRCS    = src/main.c
C_SRCSGEN = src-gen/ISOUDT1/factory.c
CINC      = -I ../Controleur-c/inc -I ../Controleur-c/src-gen

.PHONY: all
all: ../dab-bin/isolated-udt1-c ../dab-bin/isolated-udt1-c-win32 ../dab-bin/isolated-udt1-c-o64

include ../common.mk

../dab-bin/isolated-udt1-c: $(OBJS)
	@mkdir -p ../dab-bin
	gcc                  -o $@ $(OBJS)    -L ../dab-lib -lControleur-c -ldabtypes-c -lutil-c -lpthread

../dab-bin/isolated-udt1-c-win32: $(OBJSW32)
	@mkdir -p ../dab-bin
	i686-w64-mingw32-gcc -o $@ $(OBJSW32) -L ../dab-lib -lControleur-c -ldabtypes-c -lutil-c -lws2_32

../dab-bin/isolated-udt1-c-o64: $(OBJSO64)
	@mkdir -p ../dab-bin
	o64-clang            -o $@ $(OBJSO64) -L ../dab-lib -lControleur-c -ldabtypes-c -lutil-c
