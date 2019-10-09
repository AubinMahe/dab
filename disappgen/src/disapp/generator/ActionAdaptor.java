package disapp.generator;

import javax.xml.bind.JAXBElement;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

public class ActionAdaptor extends ObjectModelAdaptor {

   @Override
   public Object getProperty( Interpreter interpreter, ST self, Object o, Object property, String propertyName ) throws STNoSuchPropertyException {
      if( propertyName.equals( "isOnEntry" )) {
         final JAXBElement<?> type = (JAXBElement<?>)o;
         return type.getName().getLocalPart().equals( "on-entry" );
      }
      if( propertyName.equals( "isOnExit" )) {
         final JAXBElement<?> type = (JAXBElement<?>)o;
         return type.getName().getLocalPart().equals( "on-exit" );
      }
      return super.getProperty( interpreter, self, o, property, propertyName );
   }
}
