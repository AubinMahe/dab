package disapp.generator;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

public class DataAdaptor extends ObjectModelAdaptor {

   @Override
   public Object getProperty( Interpreter interpreter, ST self, Object o, Object property, String propertyName ) throws STNoSuchPropertyException {
      if( propertyName.equals( "isData" )) {
         return true;
      }
      return super.getProperty( interpreter, self, o, property, propertyName );
   }
}
