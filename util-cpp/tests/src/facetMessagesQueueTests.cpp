#include <types.hpp>

enum class Interface : byte {

   LOOPBACK = 0,
   IHMEVENT = 1,
   UNITE_DE_TRAITEMENT_EVENT = 2,
   UNITE_DE_TRAITEMENT_DATA = 3,
   MAINTENABLE_EVENT = 4,
   SITE_CENTRAL_EVENT = 5,
   SITE_CENTRAL_REQUEST = 6,

   LAST  = SITE_CENTRAL_REQUEST,
   FIRST = LOOPBACK,
};

const char * toString( const Interface & intrfc ) {
   switch( intrfc ) {
   case Interface::LOOPBACK                 : return "LOOPBACK";
   case Interface::IHMEVENT                 : return "IHMEVENT";
   case Interface::UNITE_DE_TRAITEMENT_EVENT: return "UNITE_DE_TRAITEMENT_EVENT";
   case Interface::UNITE_DE_TRAITEMENT_DATA : return "UNITE_DE_TRAITEMENT_DATA";
   case Interface::MAINTENABLE_EVENT        : return "MAINTENABLE_EVENT";
   case Interface::SITE_CENTRAL_EVENT       : return "SITE_CENTRAL_EVENT";
   case Interface::SITE_CENTRAL_REQUEST     : return "SITE_CENTRAL_REQUEST";
   }
   return "Interface ???";
}

enum class Event : byte {
   FIRST,

   E1,
   E2,
   E3,
   E4,

   LAST = E4
};

const char * toString( Interface iface, byte event ) {
   switch( iface ) {
   case Interface::UNITE_DE_TRAITEMENT_EVENT:
      switch( static_cast<Event>( event )) {
      case Event::FIRST: return "FIRST";
      case Event::E1   : return "E1";
      case Event::E2   : return "E2";
      case Event::E3   : return "E3";
      case Event::E4   : return "E4";
      }
      break;
   default:
      break;
   }
   return "Event ???";
}

#include <da/FacetMessagesQueue.hpp>

da::InstanceID SC_instance  ( "SC"  , 1 );
da::InstanceID IHM1_instance( "IHM1", 2 );
da::InstanceID UDT1_instance( "UDT1", 3 );
da::InstanceID IHM2_instance( "IHM2", 4 );
da::InstanceID UDT2_instance( "UDT2", 5 );

#define assertEqualsInt(E,A)if((E)!=(A))fprintf( stderr, "%s fail: expected %d, got %d\n", HPMS_FUNCNAME, E, A )
#define assertEqualsMsg(E,A)if( 0 == compare( E, A ))fprintf( stderr, "%s fail: expected {%s,%s,%s}, got {%s,%s,%s}\n", HPMS_FUNCNAME,\
   toString( Interface::UNITE_DE_TRAITEMENT_EVENT, (E)._event ), (E)._fromInstance.toString(), (E)._instance.toString(),\
   toString( Interface::UNITE_DE_TRAITEMENT_EVENT, (A)._event ), (A)._fromInstance.toString(), (A)._instance.toString()  )
#define assertTrue( T ) if(!(T))fprintf( stderr, "%s fail: expected true, %s is false\n", HPMS_FUNCNAME, #T )
#define assertFalse( T ) if(T)fprintf( stderr, "%s fail: expected false, %s is false\n", HPMS_FUNCNAME, #T )

struct Payload {

};

typedef da::FacetMessage<Interface, Payload> msg_t;

int compare( const msg_t & expected, const msg_t & observed ) {
   // On n'exploite pas ici _from et _payload
   int diff = (byte)expected._interface - (byte)observed._interface;
   if( diff ) return diff;
   diff = expected._event - observed._event;
   if( diff ) return diff;
   diff = (byte)expected._instance - (byte)observed._instance;
   if( diff ) return diff;
   return (byte)expected._fromInstance - (byte)observed._fromInstance;
}

static const sockaddr_in FROM = {};
static msg_t MESSAGE_1( FROM, Interface::IHMEVENT                , (byte)Event::E1, IHM1_instance, UDT1_instance );
static msg_t MESSAGE_2( FROM, Interface::SITE_CENTRAL_EVENT      , (byte)Event::E2, SC_instance  , UDT2_instance );
static msg_t MESSAGE_3( FROM, Interface::UNITE_DE_TRAITEMENT_DATA, (byte)Event::E3, IHM2_instance, UDT2_instance );
static msg_t MESSAGE_4( FROM, Interface::SITE_CENTRAL_REQUEST    , (byte)Event::E4, UDT1_instance, SC_instance   );

msg_t msg1, msg2, msg3;

static void addOne( void ) {
   da::FacetMessagesQueue<Interface, Payload, 3> queue( Interface::UNITE_DE_TRAITEMENT_EVENT, "Queue-not-discard-old", false );
   assertEqualsInt( queue.size(), 0 );
   assertTrue     ( queue.push_back( MESSAGE_1 ));
   assertEqualsInt( queue.size(), 1 );
   assertTrue     ( queue.pop_front( msg1 ));
   assertEqualsMsg( MESSAGE_1, msg1 );
   assertEqualsInt( queue.size(), 0 );
}

static void addFirstOne( void ) {
   da::FacetMessagesQueue<Interface, Payload, 3> queue( Interface::UNITE_DE_TRAITEMENT_EVENT, "Queue-not-discard-old", false );
   assertEqualsInt( queue.size(), (byte)0 );
   assertTrue     ( queue.push_front( MESSAGE_1 ));
   assertEqualsInt( queue.size(), (byte)1 );
   assertTrue     ( queue.pop_front( msg1 ));
   assertEqualsMsg( MESSAGE_1, msg1 );
   assertEqualsInt( queue.size(), (byte)0 );
}

static void addMany( void ) {
   da::FacetMessagesQueue<Interface, Payload, 3> queue( Interface::UNITE_DE_TRAITEMENT_EVENT, "Queue-not-discard-old", false );
   assertTrue     ( queue.is_empty());
   assertEqualsInt( queue.size(), 0 );
   assertTrue     ( queue.push_back( MESSAGE_1 ));
   assertEqualsInt( queue.size(), 1 );
   assertTrue     ( queue.push_back( MESSAGE_2 ));
   assertEqualsInt( queue.size(), 2 );
   assertTrue     ( queue.push_back( MESSAGE_3 ));
   assertEqualsInt( queue.size(), 3 );
   assertTrue     ( queue.pop_front( msg1 ));
   assertEqualsMsg( MESSAGE_1, msg1 );
   assertEqualsInt( queue.size(), 2 );
   assertTrue     ( queue.pop_front( msg2 ));
   assertEqualsMsg( MESSAGE_2, msg2 );
   assertEqualsInt( queue.size(), 1 );
   assertTrue     ( queue.pop_front( msg3 ));
   assertEqualsMsg( MESSAGE_3, msg3 );
   assertEqualsInt( queue.size(), 0 );
   assertTrue     ( queue.is_empty());
}

static void addFirstMany( void ) {
   da::FacetMessagesQueue<Interface, Payload, 3> queue( Interface::UNITE_DE_TRAITEMENT_EVENT, "Queue-not-discard-old", false );
   assertTrue     ( queue.is_empty());
   assertEqualsInt( queue.size(), 0 );
   assertTrue     ( queue.push_front( MESSAGE_1 ));
   assertEqualsInt( queue.size(), 1 );
   assertTrue     ( queue.push_front( MESSAGE_2 ));
   assertEqualsInt( queue.size(), 2 );
   assertTrue     ( queue.push_front( MESSAGE_3 ));
   assertEqualsInt( queue.size(), 3 );
   assertTrue     ( queue.pop_front( msg1 ));
   assertEqualsMsg( MESSAGE_3, msg1 );
   assertEqualsInt( queue.size(), 2 );
   assertTrue     ( queue.pop_front( msg2 ));
   assertEqualsMsg( MESSAGE_2, msg2 );
   assertEqualsInt( queue.size(), 1 );
   assertTrue     ( queue.pop_front( msg3 ));
   assertEqualsMsg( MESSAGE_1, msg3 );
   assertEqualsInt( queue.size(), 0 );
   assertTrue     ( queue.is_empty());
}

static void addTooMany( void ) {
   da::FacetMessagesQueue<Interface, Payload, 3> queue( Interface::UNITE_DE_TRAITEMENT_EVENT, "Queue-not-discard-old", false );
   assertTrue     ( queue.is_empty());
   assertEqualsInt( queue.size(), 0 );
   assertTrue     ( queue.push_back( MESSAGE_1 ));
   assertEqualsInt( queue.size(), 1 );
   assertTrue     ( queue.push_back( MESSAGE_2 ));
   assertEqualsInt( queue.size(), 2 );
   assertTrue     ( queue.push_back( MESSAGE_3 ));
   assertEqualsInt( queue.size(), 3 );
   assertFalse    ( queue.push_back( MESSAGE_4 ));
   assertEqualsInt( queue.size(), 3 );
   assertTrue     ( queue.pop_front( msg1 ));
   assertEqualsMsg( MESSAGE_1, msg1 );
   assertEqualsInt( queue.size(), 2 );
   assertTrue     ( queue.pop_front( msg2 ));
   assertEqualsMsg( MESSAGE_2, msg2 );
   assertEqualsInt( queue.size(), 1 );
   assertTrue     ( queue.pop_front( msg3 ));
   assertEqualsMsg( MESSAGE_3, msg3 );
   assertEqualsInt( queue.size(), 0 );
   assertTrue     ( queue.is_empty());
}

static void addTooManyDiscardOld( void ) {
   da::FacetMessagesQueue<Interface, Payload, 3> queue( Interface::UNITE_DE_TRAITEMENT_EVENT, "Queue-discard-old", true );
   assertTrue     ( queue.is_empty());
   assertEqualsInt( queue.size(), 0 );
   assertTrue     ( queue.push_back( MESSAGE_1 ));
   assertEqualsInt( queue.size(), 1 );
   assertTrue     ( queue.push_back( MESSAGE_2 ));
   assertEqualsInt( queue.size(), 2 );
   assertTrue     ( queue.push_back( MESSAGE_3 ));
   assertEqualsInt( queue.size(), 3 );
   assertTrue     ( queue.push_back( MESSAGE_4 ));
   assertEqualsInt( queue.size(), 3 );
   assertTrue     ( queue.pop_front( msg1 ));
   assertEqualsMsg( MESSAGE_2, msg1 );
   assertEqualsInt( queue.size(), 2 );
   assertTrue     ( queue.pop_front( msg2 ));
   assertEqualsMsg( MESSAGE_3, msg2 );
   assertEqualsInt( queue.size(), 1 );
   assertTrue     ( queue.pop_front( msg3 ));
   assertEqualsMsg( MESSAGE_4, msg3 );
   assertEqualsInt( queue.size(), 0 );
   assertTrue     ( queue.is_empty());
}

int facetMessagesQueueTests( void ) {
   addOne();
   addFirstOne();
   addMany();
   addFirstMany();
   addTooMany();
   addTooManyDiscardOld();
   return 0;
}
