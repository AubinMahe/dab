requiredInterfaceHeader( namespace, usedTypes, rawSize, iface, ifaceID, types ) ::= <<
#pragma once
//requiredInterface

#include \<da/Targets.hpp>
#include \<io/DatagramSocket.hpp>

<usedTypes:{type|#include \<<types.(type); format="NamespaceToPath">.hpp>
}>
namespace <namespace> {

   class <iface.name> {
   public:

      <iface.name>( io::DatagramSocket & socket, byte instance, unsigned targetsCount, da::Targets * targets );

   public:

<iface.eventOrRequestOrData:{facet|< if( facet.isData )>< else>
      void <facet.name>( <
if( facet.isRequest )><
         common/signature( facet.arguments.field )><
else><
         common/signature( facet.field )><
endif> );
< endif>}>
   private:

      enum EventID {
< first(iface.eventOrRequestOrData):{facet|
         <facet.name; format="ID"> = 1,}>
< rest(iface.eventOrRequestOrData) :{facet|
         <facet.name; format="ID">,
}>      };

      enum Interface {
         <iface.name; format="ID">_ID = <ifaceID>,
      };

   private:

      io::DatagramSocket & _socket;
      byte                 _instance;
      unsigned             _targetsCount;
      io::ByteBuffer       _out;
      da::Targets *        _targets;
      byte                 _raw[<rawSize>];

   private:
      <iface.name>( const <iface.name> & ) = delete;
      <iface.name> & operator = ( const <iface.name> & ) = delete;
   };
}

>>
