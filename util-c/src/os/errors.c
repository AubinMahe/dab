#ifdef _WIN32
#  include <os/win32.h>
#elif __linux__
#  include <errno.h>
#  include <string.h>
#endif

#include <os/errors.h>

util_error os_error_print( const char * call, const char * file, unsigned line ) {
   if( ! call ) {
      fprintf( stderr, "os_get_error_message: call is null!" );
      return UTIL_NULL_ARG;
   }
   if( ! file ) {
      fprintf( stderr, "os_get_error_message: file is null!" );
      return UTIL_NULL_ARG;
   }
   char systMsg[1000] = "";
#ifdef _WIN32
   DWORD err = GetLastError();
   if( err == 0 ) {
      err = (DWORD)WSAGetLastError();
   }
   if( 0 == FormatMessage( FORMAT_MESSAGE_FROM_SYSTEM, 0, err, 0, systMsg, sizeof( systMsg ), 0 )) {
      fprintf( stderr, "%s:%d:%s:unable to format message for error %lu\n", file, line, call, err );
      return UTIL_OS_ERROR;
   }
#elif __linux__
   strncpy( systMsg, strerror( errno ), sizeof( systMsg ));
#endif
   fprintf( stderr, "%s:%d:%s:%s\n", file, line, call, systMsg );
   return UTIL_OS_ERROR;
}
