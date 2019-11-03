EXEC_CPP = SiteCentral
NAME     = Site-Central
XTERM_SIZE_POSX= 45x30-0
SRCS =\
 src/sc/Banque.cpp\
 src/sc/BanqueUI.cpp\
 src/sc/Repository.cpp\
 src/main.cpp

.PHONY: all
all: ../$(EXEC_CPP) ../$(EXEC_CPP)-mingw32 ../$(EXEC_CPP)-o64

-include generated-files.mk
include ../common.mk

run: ../$(EXEC_CPP)
	xterm -title "$(NAME)-1 C++" -geometry $(XTERM_SIZE_POSX)-16 -e '(../$(EXEC_CPP) --name=sc 2>/dev/pts/2)' &

run-win32: ../$(EXEC_CPP)-mingw32
	@export WINEPATH=/usr/lib/gcc/i686-w64-mingw32/7.3-win32
	xterm -title "$(NAME)-1 C++ Win32" -geometry $(XTERM_SIZE_POSX)-16 -hold -e wine ../$(EXEC_CPP)-mingw32 --name=sc

run-darwin: ../$(EXEC_CPP)-o64
	xterm -title "$(NAME) C++ Darwin" -geometry $(XTERM_SIZE_POSX)-16 -e darling shell ./run-$(EXEC_CPP)-o64.sh &

-include deps
