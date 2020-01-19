#pragma once

#include <io/ByteBuffer.hpp>

namespace da {

   class InstanceID {
   public:

      InstanceID( const char * name = nullptr, byte value = 0 ) :
         _name ( name ),
         _value( value )
      {}

      InstanceID( const InstanceID & ) = default;

      InstanceID & operator = ( const InstanceID & ) = default;

   public:

      void put( io::ByteBuffer & target ) const { target.putByte( _value ); }

      void get( io::ByteBuffer & target ) { _value = target.getByte(); }

      const char * toString( void ) const { return _name; }

      operator byte( void ) const { return _value; }

   public:

      bool operator == ( const InstanceID & right ) const {
         return _value == right._value;
      }

      bool operator != ( const InstanceID & right ) const {
         return _value != right._value;
      }

   private:

      const char * _name;
      byte         _value;
   };
}
