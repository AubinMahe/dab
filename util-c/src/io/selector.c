#include <io/selector.h>

#include <stdarg.h>
#include <sys/time.h>

util_error io_selector_init( fd_set * set, ... ) {
   va_list args;
   va_start( args, set );
   FD_ZERO( set );
   SOCKET sckt = 0;
   while(( sckt = va_arg( args, SOCKET )) != 0 ) {
      FD_SET( sckt, set );
   }
   va_end( args );
   return UTIL_NO_ERROR;
}

util_error io_selector_select( fd_set * set, unsigned timeoutValue ) {
   struct timeval timeout;
   if( timeoutValue > 0 ) {
#ifdef _WIN32
      timeout.tv_sec  = (long)(timeoutValue / 1000);
      timeout.tv_usec = (long)(1000 * ( timeoutValue % 1000 ));
#else
      timeout.tv_sec  = timeoutValue / 1000;
      timeout.tv_usec = 1000 * ( timeoutValue % 1000 );
#endif
   }
   int count = select( FD_SETSIZE, set, 0, 0, ( timeoutValue > 0 ) ? &timeout : 0 );
   if( count < 0 ) {
      return UTIL_OS_ERROR;
   }
   return count > 0;
}

util_error io_selector_is_set( fd_set * set, SOCKET sckt ) {
   return FD_ISSET( sckt, set );
}
