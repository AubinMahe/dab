dataWriterHeader( namespace, interface, ifaceID, data, rawSize, types ) ::= <<
#pragma once
// dataWriterHeader

#include \<da/Targets.hpp>
#include \<io/DatagramSocket.hpp>

<data:{dataType|
#include \<<types.(dataType.type); format="NamespaceToPath">.hpp>
}>
namespace <namespace> {

   class <interface.name>Data {
   public:

      <interface.name>Data( io::DatagramSocket & socket, byte instance, unsigned targetsCount, da::Targets * targets );

   public:

      <data:{dataType|void publish<dataType.name; format="cap">( const <types.(dataType.type)> & <dataType.name; format="argument"> );
}>

   private:

      io::DatagramSocket & _socket;
      byte                 _instance;
      unsigned             _targetsCount;
      io::ByteBuffer       _out;
      da::Targets *        _targets;
      byte                 _raw[<rawSize>];
   };
}

>>
