EXEC_CPP = UniteDeTraitement
NAME = UDT
XTERM_SIZE_POSX = 227x30+276
SRCS = src/main.cpp

.PHONY: all
all: ../$(EXEC_CPP) ../$(EXEC_CPP)-mingw32 ../$(EXEC_CPP)-o64

-include generated-files.mk
include ../common.mk

run-1: ../$(EXEC_CPP)
	xterm -title "$(NAME)-1 C++" -geometry $(XTERM_SIZE_POSX)-16  -hold -e ../$(EXEC_CPP) --name=udt1 &

run-2: ../$(EXEC_CPP)
	xterm -title "$(NAME)-2 C++" -geometry $(XTERM_SIZE_POSX)-200 -hold -e ../$(EXEC_CPP) --name=udt2 &

run-win32-1: ../$(EXEC_CPP)-mingw32
	@export WINEPATH=/usr/lib/gcc/i686-w64-mingw32/7.3-win32
	xterm -title "$(NAME)-1 C++ Win32" -geometry $(XTERM_SIZE_POSX)-16  -hold -e wine ../$(EXEC_CPP)-mingw32 --name=udt1 &

run-win32-2: ../$(EXEC_CPP)-mingw32
	@export WINEPATH=/usr/lib/gcc/i686-w64-mingw32/7.3-win32
	xterm -title "$(NAME)-2 C++ Win32" -geometry $(XTERM_SIZE_POSX)-200 -hold -e wine ../$(EXEC_CPP)-mingw32 --name=udt2 &

run-darwin-1: ../$(EXEC_CPP)-o64
	xterm -title "$(NAME) C Darwin" -geometry $(XTERM_SIZE_POSX)-16 -hold -e darling shell ../$(EXEC_CPP)-o64 --name=udt1 &

run-darwin-2: ../$(EXEC_CPP)-o64
	xterm -title "$(NAME) C Darwin" -geometry $(XTERM_SIZE_POSX)-16 -hold -e darling shell ../$(EXEC_CPP)-o64 --name=udt2 &

-include deps
