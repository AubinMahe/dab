publisherHeader( namespace, interface, data, rawSize, types ) ::= <<
#pragma once
// publisherHeader

#include \<da/Targets.hpp>
#include \<io/DatagramSocket.hpp>

<data:{dataType|
#include \<<types.(dataType.type); format="NamespaceToPath">.hpp>
}>
namespace <namespace> {

   class <interface.name>Publisher {
   public:

      <interface.name>Publisher(
         io::DatagramSocket &   socket,
         const da::InstanceID & instance,
         unsigned               targetsCount,
         da::Targets *          targets      );

   public:

      <data:{dataType|void publish<dataType.name; format="cap">( const <types.(dataType.type)> & <dataType.name; format="argument"> );
}>
   private:

      io::DatagramSocket &   _socket;
      const da::InstanceID & _instance;
      unsigned               _targetsCount;
      io::ByteBuffer         _out;
      da::Targets *          _targets;
      byte                   _raw[<rawSize>];
   };
}

>>
