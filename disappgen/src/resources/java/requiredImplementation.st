requiredImplementation( package, intrfcPckg, iface, rawSize, hasRequest ) ::= <<
package <package>;
// requiredImplementation

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import da.InstanceID;
import da.Targets;

public class <iface.name> implements I<iface.name> {

   private final DatagramChannel       _channel;
   private final InstanceID            _from;
   private final Targets\<InstanceID>[] _targets;
   private final ByteBuffer            _out = ByteBuffer.allocate( <rawSize>< if( hasRequest )> + 1< endif> );

   @SafeVarargs
   public <iface.name>( DatagramChannel channel, InstanceID from, Targets\<InstanceID> ... targets ) {
      _channel = channel;
      _from    = from;
      _targets = targets;
   }
<iface.eventOrRequestOrData:{facet|< if( facet.isData )>< else>

   @Override
   public void <facet.name; format="argument">(<
if( facet.isRequest )><
    common/signature( facet.arguments.field )><
else><
    common/signature( facet.field )><
endif>) throws IOException {
      for( final Targets\<InstanceID> targets : _targets ) {
         for( final InstanceID to : targets._instances ) {
            <intrfcPckg>.<iface.name>Interface.<facet.name>( <
if( facet.isRequest )><
                facet.arguments.field:{field|<field.name>}; separator=","><
if(             facet.arguments.field )>, < endif>_from, to, _out );
< else><
                facet          .field:{field|<field.name>}; separator=","><
if(             facet          .field )>, < endif>_from, to, _out );
< endif>
            final int size = _out.remaining();
            _channel.send( _out, targets._process );
            util.Log.printf( "%d byte(s) sent from instance %s to instance %s @ %s", size, _from, to, targets._process );
         \}
      \}
   \}< endif>
}>
}

>>
