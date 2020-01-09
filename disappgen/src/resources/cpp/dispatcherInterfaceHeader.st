dispatcherInterfaceHeader(
   namespace, component, ifaces, ifacesIDs, requires, events, hasResponse, respRawSize, data, offRequests, reqRequests
) ::= <<
#pragma once
// dispatcherInterface

#include \<io/DatagramSocket.hpp>
#include \<io/ByteBuffer.hpp>

namespace <namespace> {

   class <component.name>Component;
   class <component.name>Dispatcher {
   public:

      <component.name>Dispatcher( bool & running, byte instance, io::DatagramSocket & socket, <component.name>Component & listener );

   public:

      bool isRunning( void ) const { return _running; }

      void terminate( void );

   public:

      virtual void beforeDispatch( void );
      
      virtual bool hasDispatched( byte intrfc, byte event, byte instance, sockaddr_in & from, io::ByteBuffer & in );
      
      virtual void afterDispatch( bool dispatched );
< if( component.timeout )>

      enum class LoopbackEvent : byte {
<  first( component.timeout):{to|         <to.name; format="ID"> = 1,
}>< rest( component.timeout):{to|         <to.name; format="ID">,
}>      \};

      void loopback( const LoopbackEvent & event );
< endif>

   private:

      enum class Interface : byte {
         < if( component.timeout )>LOOPBACK = 0,< endif>
<ifaces.keys:{ifaceName|
         <ifaceName; format="ID,width"> = <ifaces.(ifaceName)>,
< if( offRequests.(ifaceName))>
         <ifaceName; format="ID">_RESPONSES = <ifacesIDs.( common/concat( ifaceName, "Response" ))>,
< endif>
<data:{iface|
         <iface.name; format="ID,width"> = <requires.(iface.name)>,
}>}><reqRequests:{ifaceName|
         <ifaceName; format="ID">_RESPONSES = <ifacesIDs.( common/concat( ifaceName, "Response" ))>,
}>      };
<ifaces.keys:{ifaceName|


      enum class <ifaceName>Event : byte {
<  first( events.(ifaceName)):{event|         <event.name; format="ID"> = 1,
}>< rest( events.(ifaceName)):{event|         <event.name; format="ID">,
}>      \};

      bool dispatch( < if( offRequests.(ifaceName))>sockaddr_in & from, < endif><ifaceName>Event event, io::ByteBuffer & in );
}><data:{iface|


      enum class <iface.name>Data : byte {
<  first( data.(iface)):{dataType|         <dataType.name; format="ID"> = 1,
}>< rest( data.(iface)):{dataType|         <dataType.name; format="ID">,
}>      \};

      bool dispatch( <iface.name>Data event, io::ByteBuffer & in );
}><reqRequests:{ifaceName|

      enum class <ifaceName>Response : byte {
<  first( reqRequests.(ifaceName)):{req|         <req.name; format="ID"> = 1,
}>< rest( reqRequests.(ifaceName)):{req|         <req.name; format="ID">,
}>      \};

      bool dispatch( <ifaceName>Response event, io::ByteBuffer & in );
}>< if( component.timeout )>
      bool dispatch( LoopbackEvent event );
< endif>

   private:

      bool &               _running;
      byte                 _instance;
      io::DatagramSocket & _socket;
< if( hasResponse )>
      byte                 _outRaw[<respRawSize>];
      io::ByteBuffer       _out;
< endif>
      <component.name>Component & _listener;
   };
}

>>
