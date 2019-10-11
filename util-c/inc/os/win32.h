#ifdef _WIN32
#  define WIN32_LEAN_AND_MEAN
#  define _WIN32_WINDOWS 0x06010000
#  define _WIN32_WINNT   0x06010000
#  define WINVER         0x06010000

#  include <windows.h>
#  include <winsock2.h>
#  include <ws2tcpip.h>
#  include <mswsock.h>

#  define SHUT_RD       SD_RECEIVE
#  define SHUT_WR       SD_SEND
#  define SHUT_RDWR     SD_BOTH
#endif
