#ifdef _WIN32
#  include <os/win32.h>
#elif __linux__
#  include <errno.h>
#  include <string.h>
#endif
#include <stdio.h>

#include <os/errors.h>

util_error os_get_error_message( const char * func, const char * file, unsigned line, char *target, unsigned sizeof_target ) {
   if( !func || ! file || ! target) {
      return UTIL_NULL_ARG;
   }
   char systMsg[1000] = "";
#ifdef _WIN32
   DWORD err = GetLastError();
   if( err == 0 ) {
      err = (DWORD)WSAGetLastError();
   }
   if( 0 == FormatMessage( FORMAT_MESSAGE_FROM_SYSTEM, 0, err, 0, systMsg, sizeof( systMsg ), 0 )) {
      return UTIL_OS_ERROR;
   }
#elif __linux__
   strncpy( systMsg, strerror( errno ), sizeof( systMsg ));
#endif
   snprintf( target, sizeof_target, "%s:%d: %s:%s\n", file, line, func, systMsg );
   return UTIL_NO_ERROR;
}
