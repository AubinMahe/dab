package disapp.generator;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import disapp.generator.model.DataType;
import disapp.generator.model.EventType;
import disapp.generator.model.RequestType;

public class EventOrRequestOrDataAdaptor extends ObjectModelAdaptor {

   @Override
   public Object getProperty( Interpreter interpreter, ST self, Object o, Object property, String propertyName ) throws STNoSuchPropertyException {
      if( propertyName.equals( "isEvent" )) {
         return o instanceof EventType;
      }
      if( propertyName.equals( "isData" )) {
         return o instanceof DataType;
      }
      if( propertyName.equals( "isRequest" )) {
         return o instanceof RequestType;
      }
      return super.getProperty( interpreter, self, o, property, propertyName );
   }
}
