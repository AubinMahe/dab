componentFactory(
   package, intrfcPckg, deployment, deploymentImpl, process, processImpl, processes, processesImpl,
   interfaces, data, requests, components, proxies, dataPublishers, consumedData, types, modules, ids, timers
) ::= <<
package <package>;
// componentFactory

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import da.FacetMessage;
import da.FacetMessagesQueue;
import da.IMainLoop;
import da.InstanceID;
import da.Targets;
import <intrfcPckg>.Interfaces;
<interfaces:{iface|import <intrfcPckg>.<iface.name>Event;}  ; separator="\n">
<requests  :{iface|import <intrfcPckg>.<iface.name>Request;}; separator="\n">
<data      :{iface|import <intrfcPckg>.<iface.name>Data;}   ; separator="\n">
import util.Log;

public final class ComponentFactory extends Thread implements IMainLoop {

   private final DatagramChannel _channel;
   private /* */ boolean         _running;
   //-- queues --
< if( timers )>
   private final FacetMessagesQueue\<Interfaces>   _loopback = new FacetMessagesQueue\<>( "Loopback", < length( timers )>, false );
< endif>
<interfaces:{iface|
   private final FacetMessagesQueue\<Interfaces>   _<iface.name; format="argument">Event = new FacetMessagesQueue\<>( "<
      iface.name>Event", <iface.queueSize>, false );
}><data:{iface|
   private final FacetMessagesQueue\<Interfaces>   _<iface.name; format="argument">Data = new FacetMessagesQueue\<>( "<
      iface.name>Data", 1, true );
}><requests:{iface|
   private final FacetMessagesQueue\<Interfaces>   _<iface.name; format="argument">Request = new FacetMessagesQueue\<>( "<
      iface.name>Request", 1, true );
}>   @SuppressWarnings("unchecked")
   private final FacetMessagesQueue\<Interfaces>[] _queues = new FacetMessagesQueue[] {
< if( timers )>
      _loopback,
< endif>
<interfaces:{iface|
      _<iface.name; format="argument">Event,
}><data:{iface|
      _<iface.name; format="argument">Data,
}><requests:{iface|
      _<iface.name; format="argument">Request,
}>   };
   //-- instances init --
<process.instance:{instance|
   private final <modules.(instance.component)>.<instance.component.name> _<instance.name; format="argument"
      > = new <modules.(instance.component)>.<instance.component.name>( Instances.<instance.name; format="ID">, this );
}>
   //-- dispatchers --
<process.instance:{instance|
   private final <modules.(instance.component)>.<instance.component.name>Dispatcher _<instance.name>Dispatcher;
}>
   public ComponentFactory() throws IOException {
      _running = false;
      _channel = DatagramChannel
         .open     ( StandardProtocolFamily.INET )
         .setOption( StandardSocketOptions.SO_REUSEADDR, true )
         .bind     ( new InetSocketAddress( "<processImpl.address>", <processImpl.port> ));

      <componentFactoryProxiesInit()>
      <componentFactoryDispatchersInit()>
      <componentFactoryInstancesConnect()>
      <componentFactoryDataPublishers()>

      final Thread executor = new Thread( this::execute, getClass().getName() + "-executor" );
      executor.setDaemon( true );
      executor.start();

      setName( getClass().getName());
      setDaemon( true );
      start();
   }

   @Override
   public boolean isRunning() {
      return _running;
   }

   @Override
   public void terminate() throws IOException {
      _running = false;
      _channel.close();
   }

<process.instance:{instance|
   public <modules.(instance.component)>.<instance.component.name> get<instance.name; format="cap">() {
      return _<instance.name; format="argument">;
   \}

}>   @SuppressWarnings("incomplete-switch")
   private void execute() {
      _running = true;
      while( _running ) {
         try {
            FacetMessage\<Interfaces, ?, InstanceID> message = null;
            synchronized( _queues ) {
               boolean isEmpty = true;
               for( final FacetMessagesQueue\<Interfaces> queue : _queues ) {
                  if( ! queue.isEmpty()) {
                     isEmpty = false;
                     break;
                  }
               }
               if( isEmpty ) {
                  util.Log.printf( "waiting..." );
                  _queues.wait();
                  util.Log.printf( "signaled!" );
               }
               for( final FacetMessagesQueue\<Interfaces> queue : _queues ) {
                  message = queue.removeFirst();
                  if( message != null ) {
                     break;
                  }
               }
            }
            if( message == null ) { // Ce serait surprenant...
               util.Log.printf( "message is null!" );
               continue;
            }
            boolean hasDispatched = false;
            switch((Instances)message._instance ) {
<process.instance:{instance|
            case <instance.name; format="ID">:
               if( _<instance.name>Dispatcher.hasDispatched( message )) {
                  hasDispatched = true;
               \}
               break;
}>            default:
               util.Log.printf( "unexpected instance: %s", message._instance );
               break;
            \}
            if( hasDispatched ) {
<process.instance:{instance|
               _<instance.name>Dispatcher.afterDispatch();
}>            \}
         }
         catch( final Throwable t ) {
            t.printStackTrace();
            _running = false;
         }
      }
   }

   @SuppressWarnings("incomplete-switch")
   @Override
   public final void run() {
      final ByteBuffer in = ByteBuffer.allocate( 64*1024 );
      while( _running ) {
         try {
            in.clear();
            final SocketAddress from = _channel.receive( in );
            in.flip();
            if( in.remaining() >= 4 ) {
               final byte       interfaceByte = in.get();
               final byte       event         = in.get();
               final byte       instanceByte  = in.get();
               final byte       fromByte      = in.get();
               final Interfaces intrfc        = Interfaces.valueOf( interfaceByte );
               final InstanceID instance      = Instances.valueOf( instanceByte );
               final InstanceID fromInstance  = Instances.valueOf( fromByte );
               Log.printf( "intrfc = %s, event = %d, instance = %s from %s", intrfc, event, instance, from.toString());
               if( intrfc != null ) {
                  if( instance != null ) {
                     if( fromInstance != null ) {
                        boolean activate = false;
                        synchronized( _queues ) {
                           switch( intrfc ) {
< if( timers )>
                           case LOOPBACK:
<components:{component|<
if( component.timeout )>
                              activate = <modules.(component)>.<
                                 component.name>Dispatcher.LoopbackEvent.enqueue(
                                    from, event, instance, _loopback );
< endif>}>                              break;
< endif>
<interfaces:{iface|
                           case <(common/concat(iface.name, "Event")); format="ID">:// offered interface
                              activate = <iface.name>Event.enqueue(
                                 from, event, instance, fromInstance, in, _<iface.name; format="argument">Event );
                              break;
}><data:{iface|
                           case <(common/concat(iface.name, "Data" )); format="ID">:// consumed data
                              activate = <iface.name>Data.enqueue(
                                 from, event, instance, fromInstance, in, _<iface.name; format="argument">Data );
                              break;
}><requests:{iface|
                           case <(common/concat(iface.name, "Request" )); format="ID">:// responses (Request)
                              activate = <iface.name>Request.enqueue(
                                 from, event, instance, fromInstance, in, _<iface.name; format="argument">Request );
                              break;
}>                           }
                        }
                        if( activate ) {
                           synchronized( _queues ) { _queues.notify(); }
                        }
                     }
                     else {
                        Log.printf( "fromByte %d is out of range", fromByte );
                     }
                  }
                  else {
                     Log.printf( "Instance %d is out of range", instanceByte );
                  }
               }
               else {
                  Log.printf( "Interface %d is out of range", interfaceByte );
               }
            }
            else {
               Log.printf( "Expected at least 4 bytes, %d received", in.remaining());
            }
         }
         catch( final IOException e ) {
            e.printStackTrace();
            _running = false;
         }
      }
   }
}

>>
