#pragma once

#include <inttypes.h>
#include <time.h>

namespace util {

   class Time {
   public:

      static const char * now( void );

      /**
       * Return a string conformant to ISO8601 : 2019-10-27T15:04:59[+0100].
       * The last part is optional, see boolean withTimeZone argument.
       */
      static const char * toString( const tm & time, bool withTimeZone = false );

   private:
      Time( void ) = delete;
      Time( const Time & ) = delete;
      Time & operator = ( const Time & ) = delete;
   };
}
