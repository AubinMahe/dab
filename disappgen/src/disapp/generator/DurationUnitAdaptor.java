package disapp.generator;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import disapp.generator.model.DurationUnits;

public class DurationUnitAdaptor extends ObjectModelAdaptor {

   @Override
   public Object getProperty( Interpreter interpreter, ST self, Object o, Object property, String propertyName ) throws STNoSuchPropertyException {
      if( propertyName.equals( "toJava" )) {
         final DurationUnits unit = (DurationUnits)o;
         return BaseRenderer.cap( unit.value());
      }
      if( propertyName.equals( "toCpp" )) {
         final DurationUnits unit = (DurationUnits)o;
         switch( unit ) {
         case DAYS        : return "*24*60*60*1000";
         case HOURS       : return "*60*60*1000";
         case MINUTES     : return "*60*1000";
         case SECONDS     : return "*1000";
         case MILLISECONDS: return "";
         default          : return "???";
         }
      }
      return super.getProperty( interpreter, self, o, property, propertyName );
   }
}
