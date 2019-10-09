#pragma once

#include <types.hpp>

namespace util {

   class Args {
   public:

      Args( int argc, char * argv[] );

   public:

      bool getChar  ( const char * key, char &           target ) const;
      bool getByte  ( const char * key, byte &           target ) const;
      bool getShort ( const char * key, short &          target ) const;
      bool getUShort( const char * key, unsigned short & target ) const;
      bool getInt   ( const char * key, int &            target ) const;
      bool getUInt  ( const char * key, unsigned int &   target ) const;
      bool getLong  ( const char * key, long &           target ) const;
      bool getULong ( const char * key, unsigned long &  target ) const;
      bool getInt64 ( const char * key, int64_t &        target ) const;
      bool getUInt64( const char * key, uint64_t &       target ) const;
      bool getFloat ( const char * key, float &          target ) const;
      bool getDouble( const char * key, double &         target ) const;
      bool getString( const char * key, const char * &   target ) const;

   private:

      struct NVP {
         const char * name;
         const char * value;
         static int comparator( const NVP * left, const NVP * right );
      };

      unsigned _count;
      NVP      _named[100];

      Args( const Args & ) = delete;
      Args & operator = ( const Args & ) = delete;
   };
}
