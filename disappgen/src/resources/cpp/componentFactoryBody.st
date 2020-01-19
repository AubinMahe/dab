componentFactoryBody(
   namespace, ifacesNamespace, deployment, deploymentImpl, process, processImpl, processes, processesImpl,
   queues, hasTimeout, proxies, dataPublishers, consumedData, types, modules, ids
) ::= <<
// componentFactoryBody

#include "ComponentFactory.hpp"

#include \<io/sockets.hpp>
#include \<os/sleep.hpp>
#include \<util/Exceptions.hpp>
#include \<util/Log.hpp>

#include \<stdio.h>

namespace <namespace> {

   enum class Instances : byte {

      <ids:{instance|<instance; format="ID"> = <ids.(instance)>}; separator=",\n">
   };

   <ids:{instance|static const da::InstanceID <
      instance; format="ID">( "<instance; format="ID">", (byte)Instances::<instance; format="ID"> );}; separator="\n">

   const da::InstanceID & getInstanceID( byte value ) {
      switch( static_cast\<Instances>( value )) {
      <ids:{instance|case Instances::<instance; format="ID">: return <instance; format="ID">;}; separator="\n">
      }
      throw util::Unexpected( UTIL_CTXT, "value: %d", value );
   }
}
using namespace <namespace>;

ComponentFactory::ComponentFactory( void ) :
   _running ( false   ),
   _executor( nullptr ),
   _network ( nullptr )<process.instance:{instance|,
   _<instance.name; format="argument">Dispatcher( nullptr )}><proxies:{proxy|,
   _<proxy.interface; format="argument">For<proxy.from; format="cap">( nullptr )}><dataPublishers:{proxy|,
   _<proxy.interface; format="argument">Publisher( nullptr )}>
{
   UTIL_LOG_HERE();
   _socket.bind( "<processImpl.address>", <processImpl.port> );

   //-- dispatchers init --
<process.instance:{instance|
   _<instance.name; format="argument">Dispatcher = new <modules.(instance.component)>::<instance.component.name
      >Dispatcher( _running, <instance.name; format="ID">, _socket, _<instance.name; format="argument"> );
}>
   <componentFactoryBodyProxiesInit()>

<process.instance:{instance|
   //-- instances init --
   _<instance.name; format="argument">.init();
}>

   //-- lancement du thread d'écoute du réseau --
   _executor = new os::Thread( _execute, this );
   _network  = new os::Thread( _listen , this );
   while( ! _running ) {
      os::sleep( 100 );
   }
   // On rend la main quand le thread est réellement en service
   UTIL_LOG_DONE();
}

ComponentFactory:: ~ ComponentFactory( void ) {
   UTIL_LOG_HERE();
   _running = false;
   shutdown( _socket, SHUT_RD );
   closesocket( _socket );
   _executor->join();
   _network ->join();
   delete _executor;
   delete _network;
<proxies:{proxy|
   delete _<proxy.interface; format="argument">For<proxy.from; format="cap">;
}><dataPublishers:{proxy|
   delete _<proxy.interface; format="argument">Publisher;
}>   UTIL_LOG_DONE();
}

void ComponentFactory::join( void ) {
   UTIL_LOG_HERE();
   _network ->join();
   _executor->join();
   UTIL_LOG_DONE();
}

void ComponentFactory::execute( void ) {
   try {
<queues:{iface|
      <ifacesNamespace>::<iface.name>Interface::Message <iface.name; format="argument">Msg;
}>< if( hasTimeout )
>      <ifacesNamespace>::TimeoutInterface::Message timeoutMsg;
< endif>
      _running = true;
      while( _running ) {
         _queuesMutex.take();
         bool isEmpty = true;
         while( true ) {
<queues:{iface|
            if( ! _<iface.name; format="argument">Queue._queue.is_empty()) {
               isEmpty = false;
               break;
            \}
}>< if( hasTimeout )
>            if( ! _timeoutQueue._queue.is_empty()) {
               isEmpty = false;
               break;
            \}
< endif>            break;
         }
         if( isEmpty ) {
            UTIL_LOG_MSG( "waiting..." );
            _queuesEvent.wait( _queuesMutex );
            UTIL_LOG_MSG( "signaled!" );
         }
         da::BaseFacetMessage\<hpms::interfaces::Interfaces> * message = nullptr;
         while( true ) {
<queues:{iface|
            if( _<iface.name; format="argument">Queue._queue.pop_front( <iface.name; format="argument">Msg )) {
               message = &<iface.name; format="argument">Msg;
               break;
            \}
}>< if( hasTimeout )
>            if( _timeoutQueue._queue.pop_front( timeoutMsg )) {
               message = &timeoutMsg;
               break;
            \}
< endif>            break;
         }
         _queuesMutex.release();
         if( message == nullptr ) { // Ce serait surprenant...
            continue;
         }
         bool hasDispatched = false;
         switch( static_cast\<Instances>((byte)message->_instance )) {
<process.instance:{instance|
         case Instances::<instance.name; format="ID">:
            if( _<instance.name; format="argument">Dispatcher->hasDispatched( *message )) {
               hasDispatched = true;
            \}
            break;
}>         default:
            UTIL_LOG_ARGS( "Unexpected instance: %s", message->_instance.toString());
            break;
         }
<process.instance:{instance|
         if( hasDispatched ) {
            _<instance.name; format="argument">Dispatcher->afterDispatch();
         \}
}>      }
   }
   catch( const util::Runtime & x ) {
      _running = false;
      UTIL_LOG_MSG( x.what());
   }
}

void ComponentFactory::listen( void ) {
   UTIL_LOG_HERE();
   byte raw[64*1024];
   io::ByteBuffer in( raw, 64*1024 );
   _running = true;
   try {
      while( _running ) {
         in.clear();
         sockaddr_in from;
         _socket.receive( in, from );
         in.flip();
         if( in.remaining() >= 4 ) {
            byte                   interfaceByte = in.getByte();
            byte                   event         = in.getByte();
            byte                   instanceByte  = in.getByte();
            byte                   fromByte      = in.getByte();
            const da::InstanceID & instance      = getInstanceID( instanceByte );
            const da::InstanceID & fromInstance  = getInstanceID( fromByte );
            <ifacesNamespace>::Interfaces intrfc = static_cast\<<ifacesNamespace>::Interfaces>( interfaceByte );
            UTIL_LOG_ARGS( "intrfc = %s, event = %d, instance = %s \<== %s from %s",
               toString( intrfc ), event, instance.toString(), fromInstance.toString(), inet_ntoa( from.sin_addr ));
            bool activate = false;
            _queuesMutex.take();
            switch( intrfc ) {
< if( hasTimeout )>
            case <ifacesNamespace>::Interfaces::TIMEOUT:
               activate = _timeoutQueue.enqueue( from, event, instance );
               break;
< endif><queues:{iface|
            case <ifacesNamespace>::Interfaces::<iface.name; format="ID">:
               activate = _<iface.name; format="argument">Queue.enqueue( from, event, instance, fromInstance, in );
               break;
}>            default:
            case <ifacesNamespace>::Interfaces::FIRST:
            case <ifacesNamespace>::Interfaces::LAST:
               break;
            }
            _queuesMutex.release();
            if( activate ) {
               _queuesMutex.take();
               _queuesEvent.signal();
               _queuesMutex.release();
            }
         }
         else if( _running ) {
            UTIL_LOG_ARGS( "Expected at least 4 bytes, %u received", (unsigned)in.remaining());
         }
      }
   }
   catch( const util::Runtime & x ) {
      UTIL_LOG_MSG( x.what());
   }
   _running = false;
}

>>
