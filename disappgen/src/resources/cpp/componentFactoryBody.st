componentFactoryBody(
   namespace, deployment, deploymentImpl, process, processImpl, processes, processesImpl,
   proxies, dataPublishers, consumedData, types, modules, ids
) ::= <<
// ComponentFactoryBody

#include "ComponentFactory.hpp"

#include \<io/sockets.hpp>
#include \<os/sleep.hpp>
#include \<util/Exceptions.hpp>
#include \<util/Log.hpp>

#include \<stdio.h>

using namespace <namespace>;

<ids:{instance|#define INSTANCES_<instance; format="ID"> ((byte)<ids.(instance)>)}; separator="\n">

ComponentFactory::ComponentFactory( void ) :
   _running( false ),
   _thread( nullptr )<process.instance:{instance|,
   _<instance.name; format="argument">Dispatcher( 0 )}><proxies:{proxy|,
   _<proxy.interface; format="argument">For<proxy.from; format="cap">( 0 )}>
{
   UTIL_LOG_HERE();
   _socket.bind( "<processImpl.address>", <processImpl.port> );

   <componentFactoryBodyDispatchersInit()>
   <componentFactoryBodyProxiesInit()>
   <componentFactoryBodyInstancesInit()>
   //-- lancement du thread d'écoute du réseau --
   _thread = new os::Thread( _run, this );
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
   _thread->join();
   delete _thread;
   UTIL_LOG_DONE();
}

void ComponentFactory::join( void ) {
   UTIL_LOG_HERE();
   _thread->join();
   UTIL_LOG_DONE();
}

void ComponentFactory::run( void ) {
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
         if( in.remaining() >= 3 ) {
            byte intrfc   = in.getByte();
            byte event    = in.getByte();
            byte instance = in.getByte();
<process.instance:{instance|
            _<instance.name; format="cname">Dispatcher->beforeDispatch();}>
            bool dispatched = false;
            while( true ) {
<process.instance:{instance|
               if( _<instance.name; format="cname">Dispatcher->hasDispatched( intrfc, event, instance, from, in )) {
                  dispatched = true;
                  break;
               \}}>
               break;
            }
<process.instance:{instance|
            _<instance.name; format="cname">Dispatcher->afterDispatch( dispatched );}>
         }
         else {
            UTIL_LOG_ARGS( "3 bytes expected, got only %u", (unsigned)in.remaining());
         }
      }
   }
   catch( const util::Runtime & x ) {
      UTIL_LOG_MSG( x.what());
   }
   _running = false;
}

>>
