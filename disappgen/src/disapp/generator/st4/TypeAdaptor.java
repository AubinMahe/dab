package disapp.generator.st4;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import disapp.generator.model.EnumType;
import disapp.generator.model.EnumerationType;
import disapp.generator.model.FieldType;
import disapp.generator.model.FieldtypeType;

public class TypeAdaptor extends ObjectModelAdaptor {

   @Override
   public Object getProperty( Interpreter interpreter, ST self, Object o, Object property, String propertyName ) throws STNoSuchPropertyException {
      if( propertyName.startsWith( "is" )) {
         if( o instanceof FieldType ) {
            final FieldType field = (FieldType)o;
            try {
               return field.getType() == FieldtypeType.valueOf( propertyName.substring( 2 ).toUpperCase());
            }
            catch( final Exception e ) {
               throw new STNoSuchPropertyException( e, o, propertyName );
            }
         }
         else if( o instanceof EnumerationType ) {
            final EnumerationType type = (EnumerationType)o;
            try {
               return type.getType() == EnumType.valueOf( propertyName.substring( 2 ).toUpperCase());
            }
            catch( final Exception e ) {
               throw new STNoSuchPropertyException( e, o, propertyName );
            }
         }
         else {
            System.err.println( o );
         }
      }
      return super.getProperty( interpreter, self, o, property, propertyName );
   }
}
