dataWriter( package, intrfcPckg, interface, data, rawSize, types ) ::= <<
package <package>;
// dataWriter

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import da.InstanceID;
import da.Targets;
import <intrfcPckg>.<interface.name>Interface;

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
      <interface.name>Interface.<dataType.name>( data, _from, _from, _out );
      final int size = _out.remaining();
      for( final Targets\<InstanceID> targets : _targets ) {
         for( final InstanceID to : targets._instances ) {
            _out.rewind();
            _out.put( 2, to.value());
            _channel.send( _out, targets._process );
            util.Log.printf( "%d byte(s) sent from instance %s to instance %s @ %s", size, _from, to, targets._process );
         \}
      \}
   \}
}>}

>>
