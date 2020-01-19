componentFactoryHeader(
   namespace, ifacesNamespace, process, queues, proxies, dataPublishers, consumedData, hasTimeout, modules, types
) ::= <<
#pragma once
// componentFactoryHeader

#include \<io/DatagramSocket.hpp>
#include \<os/Condition.hpp>
#include \<os/Mutex.hpp>
#include \<os/Thread.hpp>

<process.instance:{instance|
#include \<<modules.(instance.component); format="NamespaceToPath">/<instance.component.name>.hpp\>
}>
<queues:{iface        |#include \<<ifacesNamespace; format="NamespaceToPath">/<iface.name>Interface.hpp>
}>< if( hasTimeout   )>#include \<<ifacesNamespace; format="NamespaceToPath">/TimeoutInterface.hpp>
< endif>

namespace <namespace> {

   class ComponentFactory {
   public:

      ComponentFactory( void );
      ~ ComponentFactory( void );
   
   public:
   
      <process.instance:{instance|<modules.(instance.component)>::<instance.component.name> & get<instance.name; format="cap"
         >( void ) { return _<instance.name; format="argument">; \}}; separator="\n">

      void join( void );

   private:

      void execute( void );
      void listen ( void );

   private:

      static void * _execute( void * userCtxt ) {
         ((ComponentFactory *)userCtxt)->execute();
         return nullptr;
      }

      static void * _listen( void * userCtxt ) {
         ((ComponentFactory *)userCtxt)->listen();
         return nullptr;
      }

   private:

      io::DatagramSocket _socket;
      bool               _running;
      os::Thread *       _executor;
      os::Thread *       _network;
      os::Mutex          _queuesMutex;
      os::Condition      _queuesEvent;
<queues:{iface|
      <ifacesNamespace>::<iface.name>Interface _<iface.name; format="argument">Queue;
}>< if( hasTimeout )
>      <ifacesNamespace>::TimeoutInterface _timeoutQueue;
< endif>

      //-- instances --
<process.instance:{instance|
      <modules.(instance.component)>::<instance.component.name>             _<instance.name; format="argument">;
}>      //-- dispatchers --
<process.instance:{instance|
      <modules.(instance.component)>::<instance.component.name>Dispatcher * _<instance.name; format="argument">Dispatcher;
}>      //-- requires --
<proxies:{proxy|
      <proxy.module>::<proxy.interface> * _<proxy.interface; format="argument">For<proxy.from; format="cap">;
}><dataPublishers:{proxy|
      <proxy.module>::<proxy.interface>Publisher * _<proxy.interface; format="argument">Publisher;
}>
   private:
      ComponentFactory( const ComponentFactory & ) = delete;
      ComponentFactory & operator = ( const ComponentFactory & ) = delete;
   };
}

>>
