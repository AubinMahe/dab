EXEC_C = site_central
NAME   = Site-Central
XTERM_SIZE_POSX= 45x30-0
SRCS =\
 src/sc/banque_ui.c\
 src/sc/repository.c\
 src/main.c

.PHONY: all
all: ../$(EXEC_C) ../$(EXEC_C)-mingw32 ../$(EXEC_C)-o64

-include generated-files.mk
include ../common.mk

run: ../$(EXEC_C)
	xterm -title "$(NAME) C" -geometry $(XTERM_SIZE_POSX)-16 -e '(../$(EXEC_C) --name=sc 2>/dev/pts/2)' &

run-win32: ../$(EXEC_C)-mingw32
	@export WINEPATH=/usr/lib/gcc/i686-w64-mingw32/7.3-win32
	xterm -title "$(NAME) C Win32" -geometry $(XTERM_SIZE_POSX)-16 -hold -e wine ../$(EXEC_C)-mingw32 --name=sc &

run-darwin: ../$(EXEC_C)-o64
	xterm -title "$(NAME) C Darwin" -geometry $(XTERM_SIZE_POSX)-16 -e darling shell ./run-$(EXEC_C)-o64.sh &

-include deps
