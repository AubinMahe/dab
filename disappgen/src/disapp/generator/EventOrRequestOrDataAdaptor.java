package disapp.generator;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import disapp.generator.model.DataType;
import disapp.generator.model.EventType;
import disapp.generator.model.QueuingPolicy;
import disapp.generator.model.RequestType;
import disapp.generator.model.ThreadingPolicy;

public class EventOrRequestOrDataAdaptor extends ObjectModelAdaptor {

   @Override
   public Object getProperty( Interpreter interpreter, ST self, Object o, Object property, String propertyName ) throws STNoSuchPropertyException {
      switch( propertyName ) {
      case "isEvent"  : return o instanceof EventType;
      case "isData"   : return o instanceof DataType;
      case "isRequest": return o instanceof RequestType;
      case "isQueued":
         if( o instanceof EventType ) {
            final EventType facet = (EventType)o;
            return facet.getQueuingPolicy() == QueuingPolicy.QUEUED;
         }
         else if( o instanceof DataType ) {
            final DataType facet = (DataType)o;
            return facet.getQueuingPolicy() == QueuingPolicy.QUEUED;
         }
         else if( o instanceof RequestType ) {
            final RequestType facet = (RequestType)o;
            return facet.getQueuingPolicy() == QueuingPolicy.QUEUED;
         }
         break;
      case "isActivating":
         if( o instanceof EventType ) {
            final EventType facet = (EventType)o;
            return facet.getQueuingPolicy() == QueuingPolicy.ACTIVATING;
         }
         else if( o instanceof DataType ) {
            final DataType facet = (DataType)o;
            return facet.getQueuingPolicy() == QueuingPolicy.ACTIVATING;
         }
         else if( o instanceof RequestType ) {
            final RequestType facet = (RequestType)o;
            return facet.getQueuingPolicy() == QueuingPolicy.ACTIVATING;
         }
         break;
      case "isImmediate":
         if( o instanceof EventType ) {
            final EventType facet = (EventType)o;
            return facet.getQueuingPolicy() == QueuingPolicy.IMMEDIATE;
         }
         else if( o instanceof DataType ) {
            final DataType facet = (DataType)o;
            return facet.getQueuingPolicy() == QueuingPolicy.IMMEDIATE;
         }
         else if( o instanceof RequestType ) {
            final RequestType facet = (RequestType)o;
            return facet.getQueuingPolicy() == QueuingPolicy.IMMEDIATE;
         }
         break;
      case "isActivatingOrImmediate":
         if( o instanceof EventType ) {
            final EventType facet = (EventType)o;
            final QueuingPolicy qp = facet.getQueuingPolicy();
            return qp == QueuingPolicy.ACTIVATING || qp == QueuingPolicy.IMMEDIATE;
         }
         else if( o instanceof DataType ) {
            final DataType facet = (DataType)o;
            final QueuingPolicy qp = facet.getQueuingPolicy();
            return qp == QueuingPolicy.ACTIVATING || qp == QueuingPolicy.IMMEDIATE;
         }
         else if( o instanceof RequestType ) {
            final RequestType facet = (RequestType)o;
            final QueuingPolicy qp = facet.getQueuingPolicy();
            return qp == QueuingPolicy.ACTIVATING || qp == QueuingPolicy.IMMEDIATE;
         }
         break;
      case "threadingPolicyIsDedicated":
         if( o instanceof EventType ) {
            final EventType facet = (EventType)o;
            return facet.getThreadingPolicy() == ThreadingPolicy.DEDICATED;
         }
         else if( o instanceof DataType ) {
            final DataType facet = (DataType)o;
            return facet.getThreadingPolicy() == ThreadingPolicy.DEDICATED;
         }
         else if( o instanceof RequestType ) {
            final RequestType facet = (RequestType)o;
            return facet.getThreadingPolicy() == ThreadingPolicy.DEDICATED;
         }
         break;
      }
      return super.getProperty( interpreter, self, o, property, propertyName );
   }
}
