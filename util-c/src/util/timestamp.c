#include <util/timestamp.h>

struct tm *localtime_r(const time_t *timep, struct tm *result);

const char * util_timestamp_now_tz( void ) {
   time_t now = time( NULL );
   struct tm local;
#ifdef _WIN32
   if( localtime_s( &local, &now ))
#else
   if( NULL == localtime_r( &now, &local ))
#endif
   {
      return NULL;
   }
   return util_timestamp_to_string_tz( &local );

}

const char * util_timestamp_now( void ) {
   time_t now = time( NULL );
   struct tm  local;
#ifdef _WIN32
   if( localtime_s( &local, &now ))
#else
   if( NULL == localtime_r( &now, &local ))
#endif
   {
      return NULL;
   }
   return util_timestamp_to_string( &local );

}

const char * util_timestamp_to_string_tz( const struct tm * time ) {
   static char buffer[25]; // ISO8601 : 2019-10-27T15:04:59+0100
   const char * format = "%Y-%m-%dT%H:%M:%S%z";
   const size_t expectedCount = 24;
   const size_t count = strftime( buffer, sizeof( buffer ), format, time );
   if( count != expectedCount ) {
      return NULL;
   }
   buffer[expectedCount] = '\0';
   return buffer;
}

const char * util_timestamp_to_string( const struct tm * time ) {
   static char buffer[25]; // ISO8601 : 2019-10-27T15:04:59+0100
   const char * format = "%Y-%m-%dT%H:%M:%S";
   const size_t expectedCount = 19;
   const size_t count = strftime( buffer, sizeof( buffer ), format, time );
   if( count != expectedCount ) {
      return NULL;
   }
   buffer[expectedCount] = '\0';
   return buffer;
}
