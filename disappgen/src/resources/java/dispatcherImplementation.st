dispatcherImplementation(
   package, internalPckg, component, offers, events,
   rawSize, respRawSize, offRequests, reqRequests, data, types
) ::= <<
package <package>;
// dispatcherImplementation

import java.io.IOException;
< if( component.timeout )>
import java.net.SocketAddress;
< endif>
< if( offRequests || component.timeout )>
import java.nio.ByteBuffer;
< endif>
import java.nio.channels.DatagramChannel;

import da.FacetMessage;
import da.InstanceID;
import <internalPckg>.Interfaces;
<offers     :{iface    |import <internalPckg>.<iface.name>Event;}  ; separator="\n">
<reqRequests:{ifaceName|import <internalPckg>.<ifaceName >Request;}; separator="\n">
<data       :{iface    |import <internalPckg>.<iface.name>Data;}   ; separator="\n">
< if( offRequests || component.timeout )>
import io.ByteBufferHelper;< endif>


public class <component.name>Dispatcher {

< if( offRequests )
>   private final ByteBuffer      _out = ByteBuffer.allocate( <respRawSize> );
< endif>
< if( offRequests || component.timeout )>
   private final DatagramChannel _channel;
< endif>
   private final <component.name>Component _component;

   public <component.name>Dispatcher( <
if( !(offRequests) && ! (component.timeout))
>@SuppressWarnings("unused") <
endif>DatagramChannel channel, <component.name>Component component ) {
< if( offRequests || component.timeout )>
      _channel   = channel;
< endif>
      _component = component;
< if( component.timeout )>
      component.set( this );
< endif>
   }

   <offers     :{iface    |<dispatcherImplOneInterface()>}; separator="\n\n">
   <reqRequests:{ifaceName|<dispatcherImplResponse()>}    ; separator="\n\n">
   <data       :{iface    |<dispatcherImplData()>}        ; separator="\n\n">
   < if( component.timeout )><dispatcherImplTimeout()>< endif>

   @SuppressWarnings("incomplete-switch")
   public boolean hasDispatched( FacetMessage\<Interfaces, ?, InstanceID> message ) throws IOException {
      util.Log.printf( "event: %s, from: %s, to: %s", message._event, message._fromInstance, message._instance );
      switch( message._interface ) {
< if( component.timeout )
>      case LOOPBACK: return dispatch((LoopbackEvent)message._event );
< endif><
offers     :{iface|
      case <(common/concat(iface.name, "Event"  )); format="ID">: return dispatch((<iface.name>Event)message._event, message );
}><
data       :{iface|
      case <(common/concat(iface.name, "Data"   )); format="ID">: return dispatch((<iface.name>Data)message._event, message );
}><
reqRequests:{ifaceName|
      case <(common/concat(ifaceName , "Request")); format="ID">: return dispatch((<ifaceName>Request)message._event, message );
}>      }
      return false;
   }

   public void afterDispatch() throws IOException {
      _component.afterDispatch();
   }
}

>>
