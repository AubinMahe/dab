eventsEnum( package, ifaceName, className, facets, types, isEvent ) ::= <<
package <package>;
// eventsEnum

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import da.InstanceID;
import io.ByteBufferHelper;

public enum <className> {

   <facets:{facet|<facet.name; format="ID">( <i> )}; separator=",\n">
   ;

   private final byte _value;

   <className>( int value ) {
      _value = (byte)value;
   }

   static public <className> valueOf( int value ) {
      for( final <className> e : values()) {
         if( e._value == value ) {
            return e;
         }
      }
      return null;
   }
   
   public void put( ByteBuffer target ) {
      target.put( _value );
   }
< if( isEvent )>
<facets:{facet|
< if( facet.isRequest )>

   public static void <facet.name>( <common/signature(facet.arguments.field)>, InstanceID from, InstanceID to, ByteBuffer _out ) {
< else>

   public static void <facet.name>( <common/signature(facet.field)><
if(facet.field)>, < endif>InstanceID from, InstanceID to, ByteBuffer _out ) {
< endif>
      _out.clear();
      Interfaces.<className; format="ID">.put( _out );
      <facet.name; format="ID">.put( _out );
      to  .put( _out );
      from.put( _out );
< if( facet.isRequest )>
      <common/byteBufferPut( facet.arguments.field )><
else>
      <common/byteBufferPut( facet.field )><
endif>
      _out.flip();
   \}
}>
< else>
<facets:{facet|
 
   // Request response or data published
   public static void <facet.name>( <types.(facet.type)> <facet.name>, InstanceID from, InstanceID to, ByteBuffer _out ) {
      _out.clear();
      Interfaces.<className; format="ID">.put( _out );
      <facet.name; format="ID">.put( _out );
      to  .put( _out );
      from.put( _out );
      <facet.name>.put( _out );
      _out.flip();
   \}
}>< endif>

   public static boolean enqueue(
      SocketAddress                     from,
      byte                              event,
      InstanceID                        instance,
      InstanceID                        fromInstance,
      ByteBuffer                        in,
      da.FacetMessagesQueue\<Interfaces> queue )
   {
      final <className> evt = <className>.valueOf( event );
      if( evt == null ) {
         util.Log.printf( "%d is out of range", event );
      }
      else {
         switch( evt ) {
<facets:{facet|
         case <facet.name; format="ID">:{
< if( facet.isData )>
            final <types.(facet.type)> <facet.name> = new <types.(facet.type)>();
            <facet.name>.get( in );
            queue.add< if( facet.isImmediate )>First< endif>( new da.FacetMessage1\<>( from, Interfaces.<
                (common/concat(ifaceName,"Data")); format="ID">, evt, instance, fromInstance, <facet.name> ));
            return <facet.isActivatingOrImmediate>; \}
< elseif( facet.isRequest )><
if( isEvent )>
            <facet.arguments.field:{field|<common/byteBufferGet( field )
>}>            queue.add< if( facet.isImmediate )>First< endif
                >( new da.FacetMessage< length(facet.arguments.field)>\<>( from, Interfaces.<
                    (common/concat(ifaceName,"Event")); format="ID">, evt, instance, fromInstance, <
                    facet.arguments.field:{field|<field.name>}; separator=", "> ));
< else>
            final <types.(facet.type)> <facet.name> = new <types.(facet.type)>();
            <facet.name>.get( in );
            queue.add< if( facet.isImmediate )>First< endif>( new da.FacetMessage1\<>( from, Interfaces.<
                (common/concat(ifaceName,"Request")); format="ID">, evt, instance, fromInstance, <facet.name> ));
< endif>
            return <facet.isActivatingOrImmediate>;\}
< elseif( facet.field )>
            <facet.field:{field|<common/byteBufferGet( field )>}
>            queue.add< if( facet.isImmediate )>First< endif
                >( new da.FacetMessage< length(facet.field)>\<>( from, Interfaces.<
                (common/concat(ifaceName,"Event")); format="ID">, evt, instance, fromInstance, <
                facet.field:{field|<field.name>}; separator=", "> ));
            return <facet.isActivatingOrImmediate>;\}
< else>
            queue.add< if( facet.isImmediate )>First< endif
                >( new da.FacetMessage\<>( from, Interfaces.<
                (common/concat(ifaceName,"Event")); format="ID">, evt, instance, fromInstance ));
            return <facet.isActivatingOrImmediate>;\}
< endif>
}>         }
      }
      return false;
   }
}

>>
