componentImplementation(
	package, intrfcPckg, component, ifaces, requires, actions, offData, reqData, responses, imports, types
) ::= <<
package <package>;
// componentImplementation

import java.io.IOException;
< if( component.timeout )>
import java.time.Duration;
< endif>
 
import da.IMainLoop;
import da.InstanceID;
< if( component.timeout )>
import util.Timeout;
< endif>
 
public abstract class <component.name>Component implements <ifaces:{iface|I<iface>}; separator=", "> {

<component.timeout:{to|
   private static final Duration <to.name; format="ID">_DURATION = Duration.of<to.unit.toJava; format="cap">( <to.duration> );
}>
   protected final InstanceID _instanceID;
   protected final IMainLoop  _mainLoop;
< if( component.timeout )>
   private <component.name>Dispatcher _dispatcher;

< endif>
<requires:{iface|
   protected I<iface.name> _<iface.name; format="argument">;
}><offData:{iface|<offData.(iface):{data|<data:{dataType|
   protected final <types.(dataType.type)> _<dataType.name; format="argument"> = new <types.(dataType.type)>(); // offered data
}>}
>   protected <iface.name>Publisher _<iface.name; format="argument">Publisher; // data publisher
}><
if( component.automaton )
>   protected final Automaton _automaton = new Automaton( this );
< endif><reqData:{iface|<reqData.(iface):{data|
   protected final <types.(data.type)> _<data.name; format="argument"> = new <types.(data.type)>(); // required data
}>}><component.timeout:{to|
   protected final Timeout _<to.name> = new Timeout( <to.name; format="ID">_DURATION, () -> loopback( <
      component.name>Dispatcher.LoopbackEvent.<to.name; format="ID"> ));
}>
   <component.timeout:{to|protected abstract void <to.name>Elapsed() throws IOException;
}>
   <actions:{action|protected abstract void <action>();
}>
   public <component.name>Component( InstanceID instance, IMainLoop mainLoop ) {
      _instanceID = instance;
      _mainLoop   = mainLoop;
   }

   public void init() {/* */}
< if( component.timeout )>
 
   private void loopback( <component.name>Dispatcher.LoopbackEvent event ) {
      try {
         _dispatcher.loopback( event );
      }
      catch( final Throwable t ) {
         t.printStackTrace();
      }
   }

   void set( <component.name>Dispatcher dispatcher ) {
      _dispatcher = dispatcher;
   }
< endif>
<requires:{iface|
 
   public void connect( I<iface.name> <iface.name; format="argument"> ) {
      _<iface.name; format="argument"> = <iface.name; format="argument">;
   \}

   public I<iface.name> get<iface.name>() {
      return _<iface.name; format="argument">;
   \}}>
<reqData:{iface|<reqData.(iface):{data|
 
   public <types.(data.type)> get<data.name; format="cap">() {
      return _<data.name; format="argument">;
   \}}>}>
<offData:{iface|

   public void connect( <iface.name>Publisher publisher ) {
      _<iface.name; format="argument">Publisher = publisher;
   \}

   public <iface.name>Publisher get<iface.name; format="cap">Publisher() {
      return _<iface.name; format="argument">Publisher;
   \}}>

   @SuppressWarnings("unused")
   protected void afterDispatch() throws IOException {/* */}
}

>>
