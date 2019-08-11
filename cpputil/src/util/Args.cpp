#include <util/Args.hpp>

#include <limits.h>
#include <string.h>

using namespace util;

Args::Args( int argc, char * argv[] ) {
   for( int i = 1; i < argc; ++i ) {
      const char * arg = argv[i];
      if(( arg[0] == '-' )&&( arg[1] == '-' )) {
         const char * eok = strchr( arg, '=' );
         if( ! eok ) {
            throw std::runtime_error( std::string( "Unexpected argument: " ) + arg );
         }
         std::string key   = std::string( arg + 2, eok );
         std::string value = ( eok + 1 );
         _named[key] = value;
      }
      else {
         throw std::runtime_error( std::string( "Unexpected argument: " ) + arg );
      }
   }
}

bool Args::getChar( const std::string & key, char & target ) const {
   std::string value;
   if( getString( key, value )) {
      target = value[0];
      return true;
   }
   return false;
}

bool Args::getByte( const std::string & key, unsigned char & target ) const {
   uint64_t value;
   if( getUInt64( key, value )) {
      if( value > UCHAR_MAX ) {
         return false;
      }
      target = (unsigned char)value;
      return true;
   }
   return false;
}

bool Args::getShort( const std::string & key, short & target ) const {
   int64_t value;
   if( getInt64( key, value )) {
      if( value > SHRT_MAX ) {
         return false;
      }
      target = (short)value;
   }
   return false;
}

bool Args::getUShort( const std::string & key, unsigned short & target ) const {
   uint64_t value;
   if( getUInt64( key, value )) {
      if( value > USHRT_MAX ) {
         return false;
      }
      target = (unsigned short)value;
      return true;
   }
   return false;
}

bool Args::getInt( const std::string & key, int & target ) const {
   int64_t value;
   if( getInt64( key, value )) {
      if( value > INT_MAX ) {
         return false;
      }
      target = (int)value;
   }
   return false;
}

bool Args::getUInt( const std::string & key, unsigned int & target ) const {
   uint64_t value;
   if( getUInt64( key, value )) {
      if( value > UINT_MAX ) {
         return false;
      }
      target = (unsigned int)value;
      return true;
   }
   return false;
}

bool Args::getLong( const std::string & key, long & target ) const {
   int64_t value;
   if( getInt64( key, value )) {
      if( value > LONG_MAX ) {
         return false;
      }
      target = (long)value;
   }
   return false;
}

bool Args::getULong( const std::string & key, unsigned long & target ) const {
   uint64_t value;
   if( getUInt64( key, value )) {
      if( value > ULONG_MAX ) {
         return false;
      }
      target = (unsigned long)value;
      return true;
   }
   return false;
}

bool Args::getInt64( const std::string & key, int64_t & target ) const {
   std::string number;
   if( getString( key, number )) {
      char * error = 0;
      int64_t value = strtoll( number.c_str(), &error, 10 );
      if( error || *error ) {
         return false;
      }
      target = value;
      return true;
   }
   return false;
}

bool Args::getUInt64( const std::string & key, uint64_t & target ) const {
   std::string number;
   if( getString( key, number )) {
      char * error = 0;
      uint64_t value = strtoull( number.c_str(), &error, 10 );
      if( error && *error ) {
         return false;
      }
      target = value;
      return true;
   }
   return false;
}

bool Args::getFloat( const std::string & key, float & target ) const {
   std::string number;
   if( getString( key, number )) {
      char * error = 0;
      float value = strtof( number.c_str(), &error );
      if( error || *error ) {
         return false;
      }
      target = value;
      return true;
   }
   return false;
}

bool Args::getDouble( const std::string & key, double & target ) const {
   std::string number;
   if( getString( key, number )) {
      char * error = 0;
      double value = strtod( number.c_str(), &error );
      if( error || *error ) {
         return false;
      }
      target = value;
      return true;
   }
   return false;
}

bool Args::getString( const std::string & key, std::string & target ) const {
   auto it = _named.find( key );
   if( it == _named.end()) {
      return false;
   }
   target = it->second;
   return true;
}
