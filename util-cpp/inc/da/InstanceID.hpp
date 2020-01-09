#pragma once

namespace da {

   class InstanceID {
   public:

      InstanceID( byte value );

      InstanceID( const InstanceID & ) = default;

      InstanceID & operator = ( const InstanceID & ) = default;

   public:

      void put( io::ByteBuffer & target ) const;

      void get( io::ByteBuffer & target );

   private:

      byte _value;
   };
}
