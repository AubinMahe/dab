#include <util/Exceptions.hpp>

#include <stdarg.h>
#include <stdio.h>
#include <typeinfo>
#include <ctype.h>

#ifdef _WIN32
#  define WIN32_LEAN_AND_MEAN
#  include <windows.h>
#  include <winsock2.h>
#  include <iostream>
#  include <util/Log.hpp>
#else
#  include <errno.h>
#  include <string.h>
#  include <execinfo.h>
#endif

using namespace util;

Exception::message_t Exception::_message;

Exception::Exception( const char * file, int line, const char * func, const char * fmt ... ) :
   _stackIndex( 0 )
{
   va_list args;
   va_start( args, fmt );
   vsnprintf( _message, sizeof( message_t ), fmt, args );
   va_end( args );
   snprintf( _stack[_stackIndex++], sizeof( stackItem_t ), "%s:%d:%s:%s", file, line, func, _message );
}

void Exception::push_backtrace( const char * file, int line, const char * func, const char * fmt ... ) {
   if( _stackIndex < sizeof( _stack ) / sizeof( stackItem_t )) {
      va_list args;
      va_start( args, fmt );
      vsnprintf( _message, sizeof( _message ), fmt, args );
      va_end( args );
      snprintf( _stack[_stackIndex++], sizeof( stackItem_t ), "%s:%d:%s:%s", file, line, func, _message );
   }
}

const char * demangleClassname( const char * name ) {
   static char demangled[200];
   memset( demangled, 0, sizeof( demangled ));
   size_t len   = strlen( name );
   size_t index = 1;
   while( index < len ) {
      size_t size = 0;
      while( isdigit( name[index] )) {
         size *= 10U;
         size += (size_t)( name[index++] - '0');
      }
      strncat( demangled, name+index, size );
      index += size;
      if( name[index] != 'E' ) {
         strncat( demangled, "::", 3 );
      }
      else {
         ++index;
      }
   }
   return demangled;
}

const char * Exception::what( void ) const noexcept {
   static char buffer[ 100 + sizeof( _stack )];
   buffer[0] = '\0';
   strncat( buffer, demangleClassname( typeid(*this).name()), sizeof( buffer )-1);
   strncat( buffer, ":\n", 3 );
   for( size_t i = 0; i < _stackIndex; ++i ) {
      strncat( buffer, "\t"     , 3 );
      strncat( buffer, _stack[i], sizeof( buffer )-1);
      strncat( buffer, "\n"     , 2 );
   }
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
      UTIL_LOG_ARGS( "Unable to format error #%lu\n", err );
   }
   return systMsg;
#else
   return ::strerror( errno );
#endif
}

Runtime::Runtime( const char * file, int line, const char * func, const char * fmt ... ) :
   Exception( file, line, func, "" )
{
   va_list args;
   va_start( args, fmt );
   message_t buffer;
   vsnprintf( buffer, sizeof( buffer ), fmt, args );
   va_end( args );
   snprintf( _message, sizeof( _message ), "%s: %s", buffer, getSystemErrorMessage());
   snprintf( _stack[_stackIndex++], sizeof( stackItem_t ), "%s:%d:%s:%s", file, line, func, _message );
}
