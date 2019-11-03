LIB_CPP = util-cpp
SRCS    =\
 src/io/ByteBuffer.cpp\
 src/io/Console.cpp\
 src/io/DatagramSocket.cpp\
 src/io/Selector.cpp\
 src/io/sockets.cpp\
 src/os/Event.cpp\
 src/os/Mutex.cpp\
 src/os/sleep.cpp\
 src/os/Thread.cpp\
 src/util/Args.cpp\
 src/util/Exceptions.cpp\
 src/util/Time.cpp\
 src/util/Timeout.cpp

.PHONY: all
all: lib/lib$(LIB_CPP).a lib/lib$(LIB_CPP)-mingw32.a lib/lib$(LIB_CPP)-o64.a

include ../common.mk
-include deps
