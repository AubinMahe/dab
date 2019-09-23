package disapp.generator.st4;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import disapp.generator.model.FieldType;
import disapp.generator.model.FieldtypeType;

public class FieldAdaptor extends ObjectModelAdaptor {

   @Override
   public Object getProperty( Interpreter interpreter, ST self, Object o, Object property, String propertyName ) throws STNoSuchPropertyException {
      final FieldType field = (FieldType)o;
      if( propertyName.startsWith( "is" )) {
         try {
            return field.getType() == FieldtypeType.valueOf( propertyName.substring( 2 ).toUpperCase());
         }
         catch( final Exception e ) {
            throw new STNoSuchPropertyException( e, o, propertyName );
         }
      }
      return super.getProperty( interpreter, self, o, property, propertyName );
   }
}
