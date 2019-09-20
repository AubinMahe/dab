package disapp.generator;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

abstract class BaseGenerator {

   protected final Model                          _model;
   protected final Map<String, SortedSet<String>> _typesUsage = new HashMap<>();

   public BaseGenerator( Model model ) {
      _model = model;
   }

   protected static String toID( String eventName ) {
      final StringBuilder sb = new StringBuilder( 2*eventName.length());
      for( int i = 0, count = eventName.length(); i < count; ++i ) {
         final char    c          = eventName.charAt( i );
         final boolean nextExists = (( i + 1 ) < count );
         final char    next       = nextExists ? eventName.charAt( i + 1 ) : 0;
         sb.append( Character.toUpperCase( c ));
         if( Character.isLowerCase( c ) && nextExists && Character.isUpperCase( next )) {
            sb.append( '_' );
         }
      }
      return sb.toString().replaceAll( "__", "_" );
   }

   protected void generateTypesUsedBy(
      NodeList     facets,
      List<String> generated,
      String       genDir,
      String       pckg ) throws IOException
   {
      for( int i = 0, iCount = facets.getLength(); i < iCount; ++i ) {
         final Element               facet         = (Element)facets.item( i );
         final String                interfaceName = facet.getAttribute( "interface" );
         final Map<String, NodeList> iface         = _model.getInterface( interfaceName );
         for( final var xFields : iface.values()) {
            for( int j = 0, jCount = xFields.getLength(); j < jCount; ++j ) {
               final Element xField = (Element)xFields.item( j );
               final String  xType  = xField.getAttribute( "type" );
               if( xType.equals( "user" )) {
                  final String xUser = xField.getAttribute( "user" );
                  if(( xUser != null )&&( ! generated.contains( xUser ))) {
                     SortedSet<String> types = _typesUsage.get( interfaceName );
                     if( types == null ) {
                        _typesUsage.put( interfaceName, types = new TreeSet<>());
                     }
                     types.add( xUser );
                     if( _model.enumIsDefined( xUser )) {
                        generateEnum( xUser, genDir, pckg );
                     }
                     else if( _model.structIsDefined( xUser )) {
                        generateStruct( xUser, genDir, pckg );
                     }
                     else {
                        throw new IllegalStateException( xType + " is not an Enum nor a Struct" );
                     }
                     generated.add( xUser );
                  }
               }
            }
         }
      }
   }

   abstract protected void   generateEnum  ( String xUser, String genDir, String pckg ) throws IOException;
   abstract protected void   generateStruct( String xUser, String genDir, String pckg ) throws IOException;
   abstract protected String getSignature  ( NodeList xFields, String pckg );
   abstract protected void   generateComponent(
      String   name,
      NodeList xOffers,
      NodeList xRequires,
      String   genDir,
      String   pckg     ) throws IOException;
}
