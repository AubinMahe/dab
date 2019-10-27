#include <util/Time.hpp>
#include <util/Exceptions.hpp>

#include <time.h>
#include <sys/time.h>

using namespace util;

const char * Time::now( void ) {
   time_t now = ::time( nullptr );
   tm     local;
#ifdef _WIN32
   if( ::localtime_s( &local, &now ))
#else
   if( nullptr == ::localtime_r( &now, &local ))
#endif
   {
      throw util::Runtime( UTIL_CTXT, "localtime" );
   }
   return Time::toString( local );
}

const char * Time::toString( const tm & time ) {
   static char buffer[25]; // ISO8601 : 2019-10-27T15:04:59+0100
   size_t count = ::strftime( buffer, sizeof( buffer ), "%Y-%m-%dT%H:%M:%S%z", &time );
   if( count < 24 ) {
      throw util::Runtime( UTIL_CTXT, "strftime returns only %d characters", count );
   }
   buffer[24] = '\0';
   return buffer;
}
