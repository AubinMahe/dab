publisherHeader( namespace, iface, data, types ) ::= <<
#pragma once
// publisherHeader

#include \<da/Targets.hpp>
#include \<io/DatagramSocket.hpp>

<data:{dataType|
#include \<<types.(dataType.type); format="NamespaceToPath">.hpp>
}>
namespace <namespace> {

   class <iface.name>Publisher {
   public:

      <iface.name>Publisher(
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
      da::Targets *          _targets;
   };
}

>>
