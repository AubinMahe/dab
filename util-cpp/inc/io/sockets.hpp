#pragma once

#ifdef _WIN32
#  include <os/win32.hpp>
#else
#  include <sys/socket.h>
#  include <netinet/in.h>
#  include <netinet/ip.h>
#  include <arpa/inet.h>
#  include <netdb.h>
#  include <unistd.h>
   typedef int SOCKET;
#  define INVALID_SOCKET (-1)
#  define closesocket close
#endif
