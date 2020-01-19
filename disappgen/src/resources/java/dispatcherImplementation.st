dispatcherImplementation(
   package, intrfcPckg, component, offers, events,
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

import <intrfcPckg>.Interfaces;
<offers     :{iface    |import <intrfcPckg>.<iface.name>Interface;}; separator="\n">
<reqRequests:{ifaceName|import <intrfcPckg>.<ifaceName >Interface;}; separator="\n">
<data       :{iface    |import <intrfcPckg>.<iface.name>Interface;}; separator="\n">
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

   @SuppressWarnings({ "incomplete-switch", "unchecked" })
   public boolean hasDispatched( FacetMessage\<Interfaces, ?, ?> message ) throws IOException {
      util.Log.printf( "event: %s, from: %s, to: %s", message._event, message._fromInstance, message._instance );
      switch( message._interface ) {
< if( component.timeout )
>      case TIMEOUT: return dispatchTimeout((Timeout.Facets)message._event );
< endif><
offers     :{iface|
      case <iface.name; format="ID">:
         return dispatch<iface.name>((FacetMessage\<Interfaces, <iface.name>Interface.Facets, ?>)message );
}><
data       :{iface|
      case <iface.name; format="ID">:
         return dispatch<iface.name>((FacetMessage\<Interfaces, <iface.name>Interface.Facets, ?>)message );
}><
reqRequests:{ifaceName|
      case <ifaceName; format="ID">:
         return dispatch<ifaceName>((FacetMessage\<Interfaces, <ifaceName>Interface.Facets, ?>)message );
}>      }
      return false;
   }

   public void afterDispatch() throws IOException {
      _component.afterDispatch();
   }
}

>>
