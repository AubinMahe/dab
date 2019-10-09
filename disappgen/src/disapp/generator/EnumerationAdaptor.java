package disapp.generator;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import disapp.generator.model.EnumType;
import disapp.generator.model.EnumerationType;

public class EnumerationAdaptor extends ObjectModelAdaptor {

   @Override
   public Object getProperty( Interpreter interpreter, ST self, Object o, Object property, String propertyName ) throws STNoSuchPropertyException {
      if( propertyName.startsWith( "is" )) {
         final EnumerationType type = (EnumerationType)o;
         try {
            return type.getType() == EnumType.valueOf( propertyName.substring( 2 ).toUpperCase());
         }
         catch( final Exception e ) {
            throw new STNoSuchPropertyException( e, o, propertyName );
         }
      }
      return super.getProperty( interpreter, self, o, property, propertyName );
   }
}
