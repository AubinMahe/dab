#ifdef _WIN32
#  include <os/win32.h>
#elif __linux__
#  include <errno.h>
#  include <string.h>
#endif

#include <os/errors.h>

void os_error_print( const char * call, const char * file, unsigned line, const char * func ) {
   if( ! call ) {
      call = "???";
   }
   if( ! file ) {
      file = "???";
   }
   if( ! func ) {
      func = "???";
   }
   char systMsg[1000] = "";
#ifdef _WIN32
   DWORD err = GetLastError();
   if( err == 0 ) {
      err = (DWORD)WSAGetLastError();
   }
   if( 0 == FormatMessage( FORMAT_MESSAGE_FROM_SYSTEM, 0, err, 0, systMsg, sizeof( systMsg ), 0 )) {
      fprintf( stderr, "%s:%d:%s:unable to format message for error %lu\n", file, line, call, err );
      return;
   }
#elif __linux__
   strncpy( systMsg, strerror( errno ), sizeof( systMsg ));
#endif
   fprintf( stderr, "%s:%d:%s:%s:%s\n", file, line, func, call, systMsg );
}
