requiredInterfaceHeader( namespace, usedTypes, rawSize, iface, types ) ::= <<
#pragma once
// requiredInterface

#include \<da/Targets.hpp>
#include \<io/DatagramSocket.hpp>

<usedTypes:{type|#include \<<types.(type); format="NamespaceToPath">.hpp>
}>
namespace <namespace> {

   class <iface.name> {
   public:

      <iface.name>(
         io::DatagramSocket &   socket,
         const da::InstanceID & instance,
         unsigned               targetsCount,
         da::Targets *          targets      );

   public:

<iface.eventOrRequestOrData:{facet|< if( facet.isData )>< else>
      void <facet.name>( <
if( facet.isRequest )><
         signature( facet.arguments.field )><
else><
         signature( facet.field )><
endif> );
< endif>}>
   private:

      io::DatagramSocket &   _socket;
      const da::InstanceID & _instance;
      unsigned               _targetsCount;
      io::ByteBuffer         _out;
      da::Targets *          _targets;
      byte                   _raw[<rawSize>];

   private:
      <iface.name>( const <iface.name> & ) = delete;
      <iface.name> & operator = ( const <iface.name> & ) = delete;
   };
}

>>
