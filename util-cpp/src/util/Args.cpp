#include <util/Args.hpp>
#include <util/Exceptions.hpp>

#include <limits.h>
#include <stdlib.h>
#include <string.h>

using namespace util;

Args::Args( int argc, char * argv[] ) {
   const int max = (int)(sizeof( _named )/sizeof( _named[0]));
   if( argc > max ) {
      throw Overflow( UTIL_CTXT, "Too many arguments %d > %d", argc, max );
   }
   _count = argc - 1;
   for( int i = 1; i < argc; ++i ) {
      const char * arg = argv[i];
      if(( arg[0] == '-' )&&( arg[1] == '-' )) {
         const char * eok = strchr( arg, '=' );
         if( ! eok ) {
            throw Unexpected( UTIL_CTXT, "%s", arg );
         }
         _named[i-1].name  = arg + 2;
         _named[i-1].value = eok + 1;
      }
      else {
         throw Unexpected( UTIL_CTXT, "%s", arg );
      }
   }
}

bool Args::getChar( const char * key, char & target ) const {
   const char * value = 0;
   if( getString( key, value )) {
      target = value[0];
      return true;
   }
   return false;
}

bool Args::getByte( const char * key, unsigned char & target ) const {
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

bool Args::getShort( const char * key, short & target ) const {
   int64_t value;
   if( getInt64( key, value )) {
      if( value > SHRT_MAX ) {
         return false;
      }
      target = (short)value;
   }
   return false;
}

bool Args::getUShort( const char * key, unsigned short & target ) const {
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

bool Args::getInt( const char * key, int & target ) const {
   int64_t value;
   if( getInt64( key, value )) {
      if( value > INT_MAX ) {
         return false;
      }
      target = (int)value;
   }
   return false;
}

bool Args::getUInt( const char * key, unsigned int & target ) const {
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

bool Args::getLong( const char * key, long & target ) const {
   int64_t value;
   if( getInt64( key, value )) {
      if( value > LONG_MAX ) {
         return false;
      }
      target = (long)value;
   }
   return false;
}

bool Args::getULong( const char * key, unsigned long & target ) const {
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

bool Args::getInt64( const char * key, int64_t & target ) const {
   const char * strval = 0;
   if( getString( key, strval )) {
      char * error = 0;
      int64_t value = strtoll( strval, &error, 10 );
      if( error || *error ) {
         return false;
      }
      target = value;
      return true;
   }
   return false;
}

bool Args::getUInt64( const char * key, uint64_t & target ) const {
   const char * strval = 0;
   if( getString( key, strval )) {
      char * error = 0;
      uint64_t value = strtoull( strval, &error, 10 );
      if( error && *error ) {
         return false;
      }
      target = value;
      return true;
   }
   return false;
}

bool Args::getFloat( const char * key, float & target ) const {
   const char * strval = 0;
   if( getString( key, strval )) {
      char * error = 0;
      float value = strtof( strval, &error );
      if( error || *error ) {
         return false;
      }
      target = value;
      return true;
   }
   return false;
}

bool Args::getDouble( const char * key, double & target ) const {
   const char * strval = 0;
   if( getString( key, strval )) {
      char * error = 0;
      double value = strtod( strval, &error );
      if( error || *error ) {
         return false;
      }
      target = value;
      return true;
   }
   return false;
}

int Args::NVP::comparator( const Args::NVP * left, const Args::NVP * right ) {
   const char * eq = strchr( left ->name, '=' );
   long         le = eq ? ( eq - left ->name ) : strlen( left ->name );
   /*         */eq = strchr( right->name, '=' );
   long         re = eq ? ( eq - right->name ) : strlen( right->name );
   return strncmp( left->name, right->name, (le < re) ? le : re );
}

typedef int ( * comparator_t )( const void *, const void * );

bool Args::getString( const char * key, const char * & target ) const {
   NVP   k   = { key, 0 };
   NVP * ka   = &k;
   NVP * nvp = (NVP *)::bsearch( ka, _named, _count, sizeof( NVP ), (comparator_t)NVP::comparator );
   if( ! nvp ) {
      return false;
   }
   target = nvp->value;
   return true;
}
