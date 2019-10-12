#ifdef _WIN32
#  define WIN32_LEAN_AND_MEAN

#  include <windows.h>
#  include <winsock2.h>
#  include <ws2tcpip.h>
#  include <mswsock.h>

#  define SHUT_RD       SD_RECEIVE
#  define SHUT_WR       SD_SEND
#  define SHUT_RDWR     SD_BOTH
#endif
