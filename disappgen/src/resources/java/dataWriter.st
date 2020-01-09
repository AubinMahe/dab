dataWriter( package, internalPckg, interface, data, rawSize, types ) ::= <<
package <package>;
// dataWriter

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import da.InstanceID;
import da.Targets;
import <internalPckg>.<interface.name>Data;

public class <interface.name>Publisher {

   private final DatagramChannel       _channel;
   private final InstanceID            _from;
   private final Targets\<InstanceID>[] _targets;
   private final ByteBuffer            _out = ByteBuffer.allocate( <rawSize> );

   @SafeVarargs
   public <interface.name>Publisher( DatagramChannel channel, InstanceID from, Targets\<InstanceID> ... targets ) {
      _channel = channel;
      _from    = from;
      _targets = targets;
   }

<data:{dataType|
   public void publish<dataType.name; format="cap">( <types.(dataType.type)> data ) throws IOException {
      for( final Targets\<InstanceID> targets : _targets ) {
         for( final InstanceID to : targets._instances ) {
            <interface.name>Data.<dataType.name>( data, _from, to, _out );
            final int size = _out.remaining();
            _channel.send( _out, targets._process );
            util.Log.printf( "%d byte(s) sent from instance %s to instance %s @ %s", size, _from, to, targets._process );
         \}
      \}
   \}
}>}

>>
