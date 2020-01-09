dispatcherImplTimeout() ::= <<
// dispatcherImplTimeout
public static enum LoopbackEvent {

   <component.timeout:{to|<to.name; format="ID">( <i> ),
}>      ;

   private final byte _value;

   LoopbackEvent( int value ) {
      _value = (byte)value;
   }

   static LoopbackEvent valueOf( int value ) {
      for( final LoopbackEvent e : values()) {
         if( e._value == value ) {
            return e;
         }
      }
      return null;
   }

   public void put( ByteBuffer target ) {
      target.put( _value );
   }

   public static boolean enqueue(
      SocketAddress                     from,
      byte                              event,
      InstanceID                        instance,
      da.FacetMessagesQueue\<Interfaces> queue    )
   {
      final LoopbackEvent evt = LoopbackEvent.valueOf( event );
      if( evt == null ) {
         util.Log.printf( "%d is out of range", event );
      }
      else {
         switch( evt ) {
<component.timeout:{to|
         case <to.name; format="ID">:{
            queue.addFirst( new da.FacetMessage\<>( from, Interfaces.LOOPBACK, evt, instance, instance ));
            util.Log.printf( "%s enqueued", evt );
            return true;\}
}>         default:
            util.Log.printf( "Unexpected event: %s", event );
            break;
         }
      }
      return false;
   }
}

private boolean dispatch( LoopbackEvent event ) throws IOException {
   switch( event ) {
<component.timeout:{to|
   case <to.name; format="ID">:{
      _component.<to.name; format="argument">Elapsed();
      return true;\}
}>   default:
      util.Log.printf( "Unexpected event: %s", event );
      return false;
   }
}

void loopback( LoopbackEvent event ) throws IOException {
   final ByteBuffer out = ByteBuffer.allocate( 4 );
   Interfaces.LOOPBACK.put( out );
   event.put( out );
   _component._instanceID.put( out );
   _component._instanceID.put( out );
   out.flip();
   _channel.send( out, _channel.getLocalAddress());
}
>>
