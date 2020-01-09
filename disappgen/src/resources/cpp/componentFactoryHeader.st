componentFactoryHeader( namespace, process, proxies, consumedData, modules, types ) ::= <<
#pragma once
// ComponentFactoryHeader

#include \<io/DatagramSocket.hpp>
#include \<os/Thread.hpp>
<process.instance:{instance|
#include \<<modules.(instance.component); format="NamespaceToPath">/<instance.component.name>.hpp\>
}>
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

      void run( void );

   private:

      static void * _run( void * userCtxt ) {
         ((ComponentFactory *)userCtxt)->run();
         return nullptr;
      }

   private:

      bool               _running;
      os::Thread *       _thread;
      io::DatagramSocket _socket;
      //-- instances --
<process.instance:{instance|
      <modules.(instance.component)>::<instance.component.name>             _<instance.name; format="argument">;
}>      //-- dispatchers --
<process.instance:{instance|
      <modules.(instance.component)>::<instance.component.name>Dispatcher * _<instance.name; format="argument">Dispatcher;
}>      //-- requires --
<proxies:{proxy|
      <proxy.module>::<proxy.interface> * _<proxy.interface; format="argument">For<proxy.from; format="cap">;
}>
   private:
      ComponentFactory( const ComponentFactory & ) = delete;
      ComponentFactory & operator = ( const ComponentFactory & ) = delete;
   };
}

>>
