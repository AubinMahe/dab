dispatcherHeader(
   namespace, ifacesNamespace, component, requires, offered,
   hasResponse, respRawSize, data, offRequests, reqRequests
) ::= <<
#pragma once
// dispatcherInterfaceHeader

< if( component.timeout )>#include \<<ifacesNamespace; format="NamespaceToPath">/TimeoutInterface.hpp>
< endif>

#include \<da/FacetMessage.hpp>
#include \<io/DatagramSocket.hpp>
#include \<io/ByteBuffer.hpp>

#include \<< ifacesNamespace; format="NamespaceToPath">/Interfaces.hpp>
<offered:{ifaceName|
#include \<< ifacesNamespace; format="NamespaceToPath">/<ifaceName>Interface.hpp>
}><reqRequests:{ifaceName|
#include \<< ifacesNamespace; format="NamespaceToPath">/<ifaceName>Interface.hpp>
}><data:{iface|
#include \<< ifacesNamespace; format="NamespaceToPath">/<iface.name>Interface.hpp>
}>< if( component.timeout )
>#include \<<ifacesNamespace; format="NamespaceToPath">/TimeoutInterface.hpp>
< endif>

namespace <namespace> {

   class <component.name>Component;
   class <component.name>Dispatcher {
   public:

      <component.name>Dispatcher(
         bool &                 running,
         const da::InstanceID & instance,
         io::DatagramSocket &   socket,
         <component.name>Component & listener );

   public:

      bool isRunning( void ) const { return _running; }

      void terminate( void );

   public:

      virtual bool hasDispatched( const da::BaseFacetMessage\<<ifacesNamespace>::Interfaces> & message );
      virtual void afterDispatch( void );

< if( component.timeout )>
      void timeout( <ifacesNamespace>::TimeoutInterface::Facets event );

< endif>
   private:

<offered:{ifaceName|
      bool dispatch<ifaceName ; format="cap">( const <ifacesNamespace>::<ifaceName>Interface::Message & message );
}><reqRequests:{ifaceName|
      bool dispatch<ifaceName ; format="cap">( const <ifacesNamespace>::<ifaceName>Interface::Message & message );
}><data:{iface           |
      bool dispatch<iface.name; format="cap">( const <ifacesNamespace>::<iface.name>Interface::Message & message );
}>< if( component.timeout )
>      bool dispatchTimeout( <ifacesNamespace>::TimeoutInterface::Facets event );
< endif>

   private:

      bool &                 _running;
< if( component.timeout || hasResponse )>
      const da::InstanceID & _instance;
< endif>
      io::DatagramSocket &   _socket;
< if( hasResponse )>
      io::ByteBuffer         _out;
      byte                   _outRaw[<respRawSize>];
< endif>
      <component.name>Component & _listener;
   };
}

>>
