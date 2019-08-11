#include <os/StdApiException.hpp>
#ifdef _WIN32
#  define WIN32_LEAN_AND_MEAN
#  include <windows.h>
#  include <winsock2.h>
#elif __linux__
#  include <errno.h>
#  include <string.h>
#endif

using namespace os;

static const char * getSystemErrorMessage( const char * classMethod, const char * file, unsigned line ) {
   char systMsg[1000] = "";
#ifdef WIN32
   DWORD err = GetLastError();
   if( err == 0 ) {
      err = (DWORD)WSAGetLastError();
   }
   ::FormatMessage( FORMAT_MESSAGE_FROM_SYSTEM, NULL, err, 0, systMsg, sizeof( systMsg ), NULL );
#elif __linux__
   strncpy( systMsg, ::strerror( errno ), sizeof( systMsg ));
#endif
   static char what[1000] = "";
   ::sprintf( what, "%s:%d: %s:%s\n", file, line, classMethod, systMsg );
   return what;
}

StdApiException::StdApiException( const char * classMethod, const char * file, unsigned line ) :
   std::runtime_error( getSystemErrorMessage( classMethod, file, line ))
{}
