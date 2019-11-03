EXEC_C = distributeur
NAME   = DAB-C
XTERM_SIZE_POSX = 45x30+0
SRCS =\
 src/dab/distributeur_ui.c\
 src/main.c

.PHONY: all
all: ../$(EXEC_C) ../$(EXEC_C)-mingw32 ../$(EXEC_C)-o64

-include generated-files.mk
include ../common.mk

run-1: ../$(EXEC_C)
	xterm -title "$(NAME)-1 C" -geometry $(XTERM_SIZE_POSX)-16 -e '(../$(EXEC_C) --name=ihm1 2>/dev/pts/1)' &

run-2: ../$(EXEC_C)
	xterm -title "$(NAME)-2 C" -geometry $(XTERM_SIZE_POSX)-200 -hold -e ../$(EXEC_C) --name=ihm2 &

run-win32-1: ../$(EXEC_C)-mingw32
	@export WINEPATH=/usr/lib/gcc/i686-w64-mingw32/7.3-win32
	xterm -title "$(NAME)-1 C Win32" -geometry $(XTERM_SIZE_POSX)-16 -hold -e wine ../$(EXEC_C)-mingw32 --name=ihm1 &

run-win32-2: ../$(EXEC_C)-mingw32
	@export WINEPATH=/usr/lib/gcc/i686-w64-mingw32/7.3-win32
	xterm -title "$(NAME)-2 C Win32" -geometry $(XTERM_SIZE_POSX)-200 -hold -e wine ../$(EXEC_C)-mingw32 --name=ihm2 &

run-darwin-1: ../$(EXEC_C)-o64
	xterm -title "$(NAME)-1 C Darwin" -geometry $(XTERM_SIZE_POSX)-16 -hold -e darling shell ./run-$(EXEC_C)-o64.sh &

run-darwin-2: ../$(EXEC_C)-o64
	xterm -title "$(NAME)-2 C Darwin" -geometry $(XTERM_SIZE_POSX)-16 -e darling shell ../$(EXEC_C)-o64 --name=ihm2 &

-include deps
