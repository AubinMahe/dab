#pragma once

#include <types.hpp>

namespace io {

   enum ByteOrder {

      ByteOrder_BIG_ENDIAN,
      ByteOrder_LITTLE_ENDIAN
   };

   /**
    * This class raises util::NullArg, std::Runtime (OS calls errors),
    * util::Overflow (put operation),
    * util::Underflow (get operation)
    */
   class ByteBuffer {
   public:

      /**
       * Wraps a byte buffer.
       * Raises util::NullArg when array is null.
       */
      ByteBuffer( byte * array, size_t capacity );

      ~ ByteBuffer( void ) = default;

   public:

      byte * array( void );

      ByteBuffer & clear( void );

      ByteBuffer & mark( void );

      ByteBuffer & reset( void );

      ByteBuffer & rewind( void );

      ByteBuffer & flip( void );

      size_t position( void ) const;

      ByteBuffer & position( size_t position );

      size_t limit( void ) const;

      size_t remaining( void ) const;

      ByteBuffer & put( const byte * src, size_t from, size_t to );

      ByteBuffer & get( byte * target, size_t from, size_t to );

      ByteBuffer & putByte( byte value );

      ByteBuffer & putByte( size_t index, byte value );

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

      const char * getString( char * dest, size_t dest_size );

      ByteBuffer & put( ByteBuffer & source );

   private:

      ByteOrder _order;
      size_t    _position;
      size_t    _limit;
      size_t    _capacity;
      size_t    _mark;
      byte *    _bytes;

   private:

      ByteBuffer( const ByteBuffer & right ) = delete;
      ByteBuffer & operator = ( const ByteBuffer & ) = delete;
   };
}
