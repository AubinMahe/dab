#include <util/Exceptions.hpp>

#include <stdarg.h>
#include <stdio.h>

#ifdef _WIN32
#  define WIN32_LEAN_AND_MEAN
#  include <windows.h>
#  include <winsock2.h>
#  include <iostream>
#elif __linux__
#  include <errno.h>
#  include <string.h>
#endif

using namespace util;

Exception::Exception( const char * file, int line, const char * func, const char * fmt ... ) {
   va_list args;
   va_start( args, fmt );
   vsnprintf( _message, sizeof( _message ), fmt, args );
   va_end( args );
   snprintf( _prefix, sizeof( _prefix ), "%s:%d:%s", file, line, func );
}

const char * Exception::what( void ) const noexcept {
   static char buffer[2048];
   snprintf( buffer, sizeof( buffer ), "%s|%s exception:%s", _prefix, getName(), _message );
   return buffer;
}

static const char * getSystemErrorMessage() {
#ifdef WIN32
   static char systMsg[1000] = "";
   DWORD err = GetLastError();
   if( err == 0 ) {
      err = (DWORD)WSAGetLastError();
   }
   if( 0 == ::FormatMessage( FORMAT_MESSAGE_FROM_SYSTEM, 0, err, 0, systMsg, sizeof( systMsg ), 0 )) {
      fprintf( stderr, "Unable to format error #%lu\n", err );
   }
   return systMsg;
#elif __linux__
   return ::strerror( errno );
#endif
}

Runtime::Runtime( const char * file, int line, const char * func, const char * fmt ... ) :
   Exception( file, line, func, "" )
{
   va_list args;
   va_start( args, fmt );
   char buffer[1024];
   vsnprintf( buffer, sizeof( buffer ), fmt, args );
   snprintf( _message, sizeof( _message ), "%s: %s", buffer, getSystemErrorMessage());
   va_end( args );
}
