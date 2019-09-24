#pragma once

#include <types.hpp>
#include <string>

namespace io {

   enum ByteOrder {

      ByteOrder_BIG_ENDIAN,
      ByteOrder_LITTLE_ENDIAN
   };

   class ByteBuffer {
   public:

      ByteBuffer( size_t capacity, byte * array = 0 );

      ByteBuffer( const ByteBuffer & right );

      ByteBuffer & operator = ( const ByteBuffer & );

      ~ ByteBuffer( void );

   public:

      byte * array( void );

      ByteBuffer & clear( void );

      ByteBuffer & mark( void );

      ByteBuffer & reset( void );

      ByteBuffer & flip( void );

      size_t position( void ) const;

      ByteBuffer & position( size_t position );

      size_t limit( void ) const;

      size_t remaining( void ) const;

      ByteBuffer & put( const byte * src, size_t from, size_t to );

      ByteBuffer & get( byte * target, size_t from, size_t to );

      ByteBuffer & putByte( byte value );

      byte getByte( void );

      ByteBuffer & putBool( bool value );

      bool getBool( void );

      ByteBuffer & putShort( short value );

      short getShort( void );

      ByteBuffer & putUShort( unsigned short value );

      unsigned short getUShort( void );

      ByteBuffer & putInt( int value );

      int getInt( void );

      ByteBuffer & putUInt( unsigned int value );

      unsigned int getUInt( void );

      ByteBuffer & putUInt( size_t index, unsigned int value );

      ByteBuffer & putLong( int64_t value );

      int64_t getLong( void );

      ByteBuffer & putULong( uint64_t value );

      uint64_t getULong( void );

      ByteBuffer & putFloat( float value );

      float getFloat( void );

      ByteBuffer & putDouble( double value );

      double getDouble( void );

      ByteBuffer & putString( const char * value );

      ByteBuffer & putString( const std::string & value ) {
         return putString( value.c_str());
      }

      std::string getString( void );

      ByteBuffer & put( ByteBuffer & source );

   private:

      ByteOrder _order;
      size_t    _position;
      size_t    _limit;
      size_t    _capacity;
      size_t    _mark;
      byte *    _bytes;
   };
}
