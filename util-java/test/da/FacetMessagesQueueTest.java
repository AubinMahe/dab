package da;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@SuppressWarnings("static-method")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FacetMessagesQueueTest {

   private static final SocketAddress FROM = new InetSocketAddress( "127.0.0.1", 80 );
   private static final FacetMessage<Interface, Event, InstanceID> MESSAGE_1 =
      new FacetMessage<>( FROM, Interface.FIRST, Event.E1, Instance.FIRST, Instance.LAST );
   private static final FacetMessage<Interface, Event, InstanceID> MESSAGE_2 =
      new FacetMessage<>( FROM, Interface.LAST, Event.E2, Instance.LAST, Instance.FIRST );
   private static final FacetMessage<Interface, Event, InstanceID> MESSAGE_3 =
      new FacetMessage<>( FROM, Interface.FIRST, Event.E3, Instance.FIRST, Instance.LAST );
   private static final FacetMessage<Interface, Event, InstanceID> MESSAGE_4 =
      new FacetMessage<>( FROM, Interface.FIRST, Event.E4, Instance.LAST, Instance.FIRST );

   enum Interface {
      FIRST,
      LAST,
   }

   enum Event {
      E1,
      E2,
      E3,
      E4,
   }

   enum Instance implements InstanceID {
      FIRST,
      LAST,
      ;
      @Override
      public void put( ByteBuffer target ) {
         //
      }
   }

   @Test
   @Order(1)
   public void addOne() {
      final FacetMessagesQueue<Interface> queue = new FacetMessagesQueue<>( "Queue-not-discard-old", 3, false );
      assertEquals( queue.size(), 0 );
      assertNull  ( queue.add( MESSAGE_1 ));
      assertEquals( queue.size(), 1 );
      assertEquals( MESSAGE_1, queue.removeFirst());
      assertEquals( queue.size(), 0 );
   }

   @Test
   @Order(2)
   public void addFirstOne() {
      final FacetMessagesQueue<Interface> queue = new FacetMessagesQueue<>( "Queue-not-discard-old", 3, false );
      assertEquals( queue.size(), 0 );
      assertNull  ( queue.addFirst( MESSAGE_1 ));
      assertEquals( queue.size(), 1 );
      assertEquals( MESSAGE_1, queue.removeFirst());
      assertEquals( queue.size(), 0 );
   }

   @Test
   @Order(3)
   public void addMany() {
      final FacetMessagesQueue<Interface> queue = new FacetMessagesQueue<>( "Queue-not-discard-old", 3, false );
      assertEquals( queue.size(), 0 );
      assertNull  ( queue.add( MESSAGE_1 ));
      assertNull  ( queue.add( MESSAGE_2 ));
      assertNull  ( queue.add( MESSAGE_3 ));
      assertEquals( queue.size(), 3 );
      assertEquals( MESSAGE_1, queue.removeFirst());
      assertEquals( queue.size(), 2 );
      assertEquals( MESSAGE_2, queue.removeFirst());
      assertEquals( queue.size(), 1 );
      assertEquals( MESSAGE_3, queue.removeFirst());
      assertEquals( queue.size(), 0 );
   }

   @Test
   @Order(4)
   public void addFirstMany() {
      final FacetMessagesQueue<Interface> queue = new FacetMessagesQueue<>( "Queue-not-discard-old", 3, false );
      assertEquals( queue.size(), 0 );
      assertNull  ( queue.addFirst( MESSAGE_1 ));
      assertNull  ( queue.addFirst( MESSAGE_2 ));
      assertNull  ( queue.addFirst( MESSAGE_3 ));
      assertEquals( queue.size(), 3 );
      assertEquals( MESSAGE_3, queue.removeFirst());
      assertEquals( queue.size(), 2 );
      assertEquals( MESSAGE_2, queue.removeFirst());
      assertEquals( queue.size(), 1 );
      assertEquals( MESSAGE_1, queue.removeFirst());
      assertEquals( queue.size(), 0 );
   }

   @Test
   @Order(5)
   public void addTooMany() {
      final FacetMessagesQueue<Interface> queue = new FacetMessagesQueue<>( "Queue-not-discard-old", 3, false );
      assertEquals( queue.size(), 0 );
      assertNull  ( queue.add( MESSAGE_1 ));
      assertNull  ( queue.add( MESSAGE_2 ));
      assertNull  ( queue.add( MESSAGE_3 ));
      assertNull  ( queue.add( MESSAGE_4 ));
      assertEquals( queue.size(), 3 );
      assertEquals( MESSAGE_1, queue.removeFirst());
      assertEquals( queue.size(), 2 );
      assertEquals( MESSAGE_2, queue.removeFirst());
      assertEquals( queue.size(), 1 );
      assertEquals( MESSAGE_3, queue.removeFirst());
      assertEquals( queue.size(), 0 );
   }

   @Test
   @Order(6)
   public void addTooManyDiscardOld() {
      final FacetMessagesQueue<Interface> queue = new FacetMessagesQueue<>( "Queue-discard-old", 3, true );
      assertEquals( queue.size(), 0 );
      assertNull  ( queue.add( MESSAGE_1 ));
      assertNull  ( queue.add( MESSAGE_2 ));
      assertNull  ( queue.add( MESSAGE_3 ));
      assertEquals( queue.add( MESSAGE_4 ), MESSAGE_1 );
      assertEquals( queue.size(), 3 );
      assertEquals( MESSAGE_2, queue.removeFirst());
      assertEquals( queue.size(), 2 );
      assertEquals( MESSAGE_3, queue.removeFirst());
      assertEquals( queue.size(), 1 );
      assertEquals( MESSAGE_4, queue.removeFirst());
      assertEquals( queue.size(), 0 );
   }
}
