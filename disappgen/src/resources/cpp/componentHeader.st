componentHeader( namespace, ifacesNamespace, component, requires, actions, offData, reqData, responses, includes, types ) ::= <<
#pragma once
// componentHeader

#include \<io/DatagramSocket.hpp>
< if( component.timeout )>

#include \<util/Timeout.hpp>

< endif>
<component.offers  :{offered |#include \<<namespace; format="NamespaceToPath">/I<offered .interface.name>.hpp>}; separator="\n">
<component.requires:{required|#include \<<namespace; format="NamespaceToPath">/<required.interface.name >.hpp>}; separator="\n">
<responses         :{iface   |#include \<<namespace; format="NamespaceToPath">/I<iface.name    >Responses.hpp>}; separator="\n">
<offData           :{iface   |#include \<<namespace; format="NamespaceToPath">/<iface.name          >Publisher.hpp>}; separator="\n">
<reqData           :{iface   |#include \<<namespace; format="NamespaceToPath">/I<iface.name         >Data.hpp>}; separator="\n">
#include \<<                              namespace; format="NamespaceToPath">/<component.name>Dispatcher.hpp>
< if( component.automaton )>

#include \<<                              namespace; format="NamespaceToPath">/Automaton.hpp>
< endif>


namespace <namespace> {

   class <component.name>Component :<
component.offers:{offered|  public I<offered.interface.name>}; separator=", "><
reqData         :{iface  |, public I<iface.name        >Data}; separator=", "><
responses       :{iface  |, public I<iface.name   >Responses}; separator=", "> {
   public:

      <component.name>Component( void ) :
         _dispatcher( 0 )<requires:{iface|,
         _<iface.name; format="argument">( 0 )}><offData:{iface|,
         _<iface.name; format="argument">Data( 0 )}><
if( component.automaton )>,
         _automaton( *this )<
endif><component.timeout:{to|,
         _<to.name; format="argument">( <to.name; format="ID">_DURATION, [this](){ timeout( <
            ifacesNamespace>::TimeoutInterface::Facets::<component.name; format="ID">_<to.name; format="ID"> );\})}>
      {}

   public:

      void setDispatcher( <component.name>Dispatcher & dispatcher ) {
         _dispatcher = &dispatcher;
      }

      bool isRunning( void ) const {
         return _dispatcher->isRunning();
      }

      void terminate( void ) const {
         _dispatcher->terminate();
      }

<reqData:{iface|<reqData.(iface):{data|
      const <types.(data.type)> & get<data.name; format="cap">( void ) const { return _<data.name; format="argument">; \}
      <types.(data.type)> &       get<data.name; format="cap">( void )       { return _<data.name; format="argument">; \}
}>}>
      <actions:{action|virtual void <action>( void ) = 0;
}>
      <component.timeout:{to|virtual void <to.name>Elapsed( void ) = 0;
}>
   public:

      virtual void init( void ) {}

      virtual void afterDispatch( void ) {}
<requires:{iface|


      void connect( <iface.name> & <iface.name; format="argument"> ) {
         _<iface.name; format="argument"> = &<iface.name; format="argument">;
      \}

      <iface.name> & get<iface.name>( void ) {
         return *_<iface.name; format="argument">;
      \}
}>
<offData:{iface|
      void connect( <iface.name>Publisher & <iface.name; format="argument"> ) {
         _<iface.name; format="argument">Data = &<iface.name; format="argument">;
      \}
}>
< if( component.timeout )>
   private:

      void timeout( <ifacesNamespace>::TimeoutInterface::Facets event );

< endif>
   protected:

      <component.name>Dispatcher * _dispatcher;
<requires:{iface|
      <iface.name> * _<iface.name; format="argument">;
}><reqData:{iface|<reqData.(iface):{data|
      <types.(data.type)> _<data.name; format="argument">;     // consumed data
}>}><offData:{iface|<offData.(iface):{data|
      <types.(data.type)> _<data.name; format="argument">;     // published data}>
      <iface.name>Publisher * _<iface.name; format="argument">Data; // data publisher
}><
if( component.automaton )>      Automaton             _automaton;
< endif><component.timeout:{to|
      util::TimeoutCallback _<to.name>;
}>
<component.timeout:{to|
      static const unsigned <to.name; format="ID">_DURATION = <to.duration><to.unit.toCpp; format="cap">;
}>
   private:
      <component.name>Component( const <component.name>Component & ) = delete;
      <component.name>Component & operator = ( const <component.name>Component & ) = delete;
   };
}

>>
