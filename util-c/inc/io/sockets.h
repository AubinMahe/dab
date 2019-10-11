#pragma once

#ifdef _WIN32
#  include <os/win32.h>
#else
#  include <sys/socket.h>
#  include <netinet/in.h>
#  include <netinet/ip.h>
#  include <arpa/inet.h>
#  include <netdb.h>
#  include <unistd.h>
#  include <sys/select.h>
   typedef int SOCKET;
#  define INVALID_SOCKET (-1)
#  define closesocket close
#endif

int io_winsock_init( void );
