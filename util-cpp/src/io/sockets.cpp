#include <io/sockets.hpp>
#include <io/DatagramSocket.hpp>
#include <util/Exceptions.hpp>

#include <string.h>

#ifdef _WIN32
#  define NS_INADDRSZ  4
#  define NS_IN6ADDRSZ 16
#  define NS_INT16SZ   2

static struct WinsockInit {
   WinsockInit( void ) {
      WORD    wVersionRequested = MAKEWORD(2, 2);
      WSADATA wsaData;
      int     err = WSAStartup(wVersionRequested, &wsaData);
      if( err ) {
         throw util::Runtime( UTIL_CTXT, "WinsockInit" );
      }
   }
} initWinsock;

#endif
