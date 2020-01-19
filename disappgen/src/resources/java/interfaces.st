interfaces( package, iface, facets, types ) ::= <<
package <package>;
// interfaces

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import da.FacetMessagesQueue;
import da.InstanceID;

import io.ByteBufferHelper;

public class <iface.name>Interface {

   public static enum Facets {
      FIRST,

<facets:{facet|
< if( facet.isRequest )>
      <facet.name; format="ID">,
      <facet.name; format="ID">_RESPONSE,
< else>
      <facet.name; format="ID">,
< endif>
}>
      LAST;

      public void put( ByteBuffer target ) {
         target.put((byte)ordinal());
      }

      static public Facets valueOf( int value ) {
         if( FIRST.ordinal() \< value && value \< Facets.LAST.ordinal()) {
            return values()[value];
         }
         return null;
      }
   }

   public FacetMessagesQueue\<Interfaces> _queue = new FacetMessagesQueue\<>( "<iface.name>", <iface.queueSize>, true );

<facets:{facet|
< if( facet.isEvent )>
< if( facet.field )>
   public static final class <facet.name; format="cap"> {
      <facet.field:{field|<common/dataGen( "public", field )>}
>   \}
< endif>< elseif( facet.isRequest )>
   public static final class <facet.name; format="cap"> {
      <facet.arguments.field:{field|<common/dataGen( "public", field )>}
>   \}
< endif>
}>
<facets:{facet|
< if( facet.isEvent )>
   public static void <facet.name>( // Event
< if( facet.field )>
      <facet.field:{field|<common/type(field)> <field.name>}; separator=",\n">,
< endif>
      InstanceID from,
      InstanceID to,
      ByteBuffer _out )
   {
      _out.clear();
      Interfaces.<iface.name; format="ID">.put( _out );
      Facets.<facet.name; format="ID">.put( _out );
      to  .put( _out );
      from.put( _out );
      <common/byteBufferPut( facet.field )
>      _out.flip();
   \}

< elseif( facet.isRequest )>
   public static void <facet.name>( // Request
      <facet.arguments.field:{field|<common/type(field)> <field.name>}; separator=",\n">,
      InstanceID from,
      InstanceID to,
      ByteBuffer _out )
   {
      _out.clear();
      Interfaces.<iface.name; format="ID">.put( _out );
      Facets.<facet.name; format="ID">.put( _out );
      to  .put( _out );
      from.put( _out );
      <common/byteBufferPut( facet.arguments.field )>
      _out.flip();
   \}
 
   public static void <facet.name>( // Request response
      <types.(facet.type)> <facet.name>,
      InstanceID from,
      InstanceID to,
      ByteBuffer _out )
   {
      _out.clear();
      Interfaces.<iface.name; format="ID">.put( _out );
      Facets.<facet.name; format="ID">.put( _out );
      to  .put( _out );
      from.put( _out );
      <facet.name>.put( _out );
      _out.flip();
   \}

< elseif( facet.isData )>
   public static void <facet.name>( // Data
      <types.(facet.type)> <facet.name>,
      InstanceID from,
      InstanceID to,
      ByteBuffer _out )
   {
      _out.clear();
      Interfaces.<iface.name; format="ID">.put( _out );
      Facets.<facet.name; format="ID">.put( _out );
      to  .put( _out );
      from.put( _out );
      <facet.name>.put( _out );
      _out.flip();
   \}

< endif>
}>   public boolean enqueue(
      SocketAddress from,
      byte          event,
      InstanceID    instance,
      InstanceID    fromInstance,
      ByteBuffer    in           )
   {
      final Facets evt = Facets.valueOf( event );
      if( evt == null ) {
         util.Log.printf( "%d is out of range", event );
      }
      else {
         switch( evt ) {
<facets:{facet|
< if( facet.isEvent)>
         case <facet.name; format="ID">:{ // event
<    if( facet.field )>
            final da.FacetMessage\<Interfaces, Facets, <facet.name; format="cap">\> msg =
               new da.FacetMessage\<>( from, Interfaces.<iface.name; format="ID">, evt, instance, fromInstance,
                  new <facet.name; format="cap">());
            <byteBufferGetToMessage( facet.name, facet.field )
>            _queue.add< if( facet.isImmediate )>First< endif>( msg );
<    else>
            _queue.add< if( facet.isImmediate )>First< endif
               >( new da.FacetMessage\<>( from, Interfaces.<iface.name; format="ID">, evt, instance, fromInstance ));
<    endif>
            return <facet.isActivatingOrImmediate>; \}
< elseif( facet.isRequest )>
         case <facet.name; format="ID">:{ // request
            final da.FacetMessage\<Interfaces, Facets, <facet.name; format="cap">\> msg =
               new da.FacetMessage\<>( from, Interfaces.<iface.name; format="ID">, evt, instance, fromInstance,
                  new <facet.name; format="cap">());
            <byteBufferGetToMessage( facet.name, facet.arguments.field )
>            _queue.add< if( facet.isImmediate )>First< endif>( msg );
            return <facet.isActivatingOrImmediate>; \}
         case <facet.name; format="ID">_RESPONSE:{ // response
            final da.FacetMessage\<Interfaces, Facets, <types.(facet.type)>\> msg =
               new da.FacetMessage\<>( from, Interfaces.<iface.name; format="ID">, evt, instance, fromInstance,
                  new <types.(facet.type)>());
            msg._payload.get( in );
            _queue.add< if( facet.isImmediate )>First< endif>( msg );
            return <facet.isActivatingOrImmediate>; \}
< elseif( facet.isData )>
         case <facet.name; format="ID">:{ // data
            final da.FacetMessage\<Interfaces, Facets, <types.(facet.type)>\> msg =
               new da.FacetMessage\<>( from, Interfaces.<iface.name; format="ID">, evt, instance, fromInstance,
                  new <types.(facet.type)>());
            msg._payload.get( in );
            _queue.add< if( facet.isImmediate )>First< endif>( msg );
            return <facet.isActivatingOrImmediate>; \}
< endif>
}>         case FIRST:
         case LAST:
         default:
            util.Log.printf( "Unexpected event %d", event );
            break;
         }
      }
      return false;
   }
}

>>
