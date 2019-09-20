#pragma once

#include <map>
#include <string>

namespace util {

   class Args {
   public:

      Args( int argc, char * argv[] );

   public:

      bool getChar  ( const std::string & key, char &           target ) const;
      bool getByte  ( const std::string & key, unsigned char &  target ) const;
      bool getShort ( const std::string & key, short &          target ) const;
      bool getUShort( const std::string & key, unsigned short & target ) const;
      bool getInt   ( const std::string & key, int &            target ) const;
      bool getUInt  ( const std::string & key, unsigned int &   target ) const;
      bool getLong  ( const std::string & key, long &           target ) const;
      bool getULong ( const std::string & key, unsigned long &  target ) const;
      bool getInt64 ( const std::string & key, int64_t &        target ) const;
      bool getUInt64( const std::string & key, uint64_t &       target ) const;
      bool getFloat ( const std::string & key, float &          target ) const;
      bool getDouble( const std::string & key, double &         target ) const;
      bool getString( const std::string & key, std::string &    target ) const;

   private:

      std::map<std::string, std::string> _named;

      Args( const Args & ) = delete;
      Args & operator = ( const Args & ) = delete;
   };
}
