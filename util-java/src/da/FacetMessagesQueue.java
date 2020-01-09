package da;

public class FacetMessagesQueue<I extends Enum<I>> {

   private final String                           _name;
   private final FacetMessage<I, ?, InstanceID>[] _queue;
   private final boolean                          _discardOldWhenFull;
   private /* */ int                              _count;

   @SuppressWarnings("unchecked")
   public FacetMessagesQueue( String name, int capacity, boolean discardOldWhenFull ) {
      _name               = name;
      _queue              = new FacetMessage[capacity];
      _discardOldWhenFull = discardOldWhenFull;
   }

   public FacetMessage<I, ?, InstanceID> removeFirst() {
      if( _count > 0 ) {
         final FacetMessage<I, ?, InstanceID> first = _queue[0];
         System.arraycopy( _queue, 1, _queue, 0, --_count );
         util.Log.printf( "Message removed from '%s': %s, from: %s, to %s",
            _name, first._event, first._fromInstance, first._instance );
         return first;
      }
      util.Log.printf( "Queue '%s' is empty (%d)", _name, _count );
      return null;
   }

   public boolean isEmpty() {
      return _count == 0;
   }

   public FacetMessage<I, ?, InstanceID> addFirst( FacetMessage<I, ?, InstanceID> message ) {
      if( _count < _queue.length ) {
         System.arraycopy( _queue, 0, _queue, 1, Math.max( _count, _queue.length - 1 ));
         _queue[0] = message;
         ++_count;
         util.Log.printf( "Message added to '%s': %s, from: %s, to %s",
            _name, message._event, message._fromInstance, message._instance );
         return null;
      }
      if( _discardOldWhenFull ) {
         final FacetMessage<I, ?, InstanceID> old = _queue[0];
         _queue[0] = message;
         ++_count;
         util.Log.printf( "Message added to '%s': %s, from: %s, to %s, message discarted: %s, from: %s, to %s",
            _name, message._event, message._fromInstance, message._instance,
                   old._event, old._fromInstance, old._instance );
         return old;
      }
      util.Log.printf( "Queue '%s' is full (%d)", _name, _count );
      return null;
   }

   public FacetMessage<I, ?, InstanceID> add( FacetMessage<I, ?, InstanceID> message ) {
      if( _count < _queue.length ) {
         _queue[_count++] = message;
         util.Log.printf( "Message added to '%s': %s, from: %s, to %s",
            _name, message._event, message._fromInstance, message._instance );
         return null;
      }
      if( _discardOldWhenFull ) {
         final FacetMessage<I, ?, InstanceID> old = _queue[0];
         System.arraycopy( _queue, 1, _queue, 0, _count - 1 );
         _queue[_count-1] = message;
         util.Log.printf( "Message added to '%s': %s, from: %s, to %s, message discarted: %s, from: %s, to %s",
            _name, message._event, message._fromInstance, message._instance,
                   old._event, old._fromInstance, old._instance );
         return old;
      }
      util.Log.printf( "Queue '%s' is full (%d)", _name, _count );
      return null;
   }

   public int size() {
      return _count;
   }
}
