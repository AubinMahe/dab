package disapp.generator;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import disapp.generator.model.EnumerationType;
import disapp.generator.model.EventType;
import disapp.generator.model.FieldType;
import disapp.generator.model.FieldtypeType;
import disapp.generator.model.InterfaceType;
import disapp.generator.model.InterfaceUsageType;

abstract class BaseGenerator {

   protected final Model        _model;
   protected final STGroup      _group;
   protected final BaseRenderer _renderer;
   protected /* */ String       _genDir;
   protected /* */ String       _package;

   protected BaseGenerator( Model model, String templateName, BaseRenderer renderer ) {
      _model    = model;
      _group    = new STGroupFile( getClass().getResource( "/resources/" + templateName ), "utf-8", '<', '>' );
      _renderer = renderer;
      _group.registerRenderer( String.class, _renderer );
      final TypeAdaptor ta = new TypeAdaptor();
      _group.registerModelAdaptor( FieldType      .class, ta );
      _group.registerModelAdaptor( EnumerationType.class, ta );
   }

   abstract protected void generateEnum  ( String xUser ) throws IOException;
   abstract protected void generateStruct( String xUser ) throws IOException;

   protected void generateTypesUsedBy( List<InterfaceUsageType> usages, List<String> generated ) throws IOException {
      for( final InterfaceUsageType usage : usages ) {
         final String        interfaceName = usage.getInterface();
         final InterfaceType iface         = _model.getInterface( interfaceName );
         for( final EventType event : iface.getEvent()) {
            for( final FieldType field : event.getField()) {
               final FieldtypeType type  = field.getType();
               if(( type == FieldtypeType.ENUM )||( type == FieldtypeType.STRUCT )) {
                  final String typeName = field.getUserTypeName();
                  if( ! generated.contains( typeName )) {
                     if( type == FieldtypeType.ENUM ) {
                        generateEnum( typeName );
                     }
                     else if( type == FieldtypeType.STRUCT ) {
                        generateStruct( typeName );
                     }
                     else {
                        throw new IllegalStateException( type + " is not an Enum nor a Struct" );
                     }
                     generated.add( typeName );
                  }
               }
            }
         }
      }
   }

   private void configureRendererWidthsCumulative( List<FieldType> fields ) {
      int maxLength    = (Integer)_renderer.get(    "width" );
      int maxStrLength = (Integer)_renderer.get( "strWidth" );
      for( final FieldType field : fields ) {
         final String cname = _renderer.name( field.getName());
         maxLength = Math.max( maxLength, cname.length());
         if( field.getType() == FieldtypeType.STRING ) {
            maxStrLength = Math.max( maxStrLength, cname.length());
         }
      }
      _renderer.set( "width"   , maxLength    );
      _renderer.set( "strWidth", maxStrLength );
   }

   protected void setRendererFieldsMaxWidth( List<FieldType> fields ) {
      _renderer.set( "width"   , 0 );
      _renderer.set( "strWidth", 0 );
      configureRendererWidthsCumulative( fields );
   }

   protected void setRendererFieldsMaxWidth( InterfaceType iface ) {
      _renderer.set( "width"   , 0 );
      _renderer.set( "strWidth", 0 );
      final List<EventType> events = iface.getEvent();
      for( final EventType event : events ) {
         configureRendererWidthsCumulative( event.getField() );
      }
   }

   void setRendererInterfaceMaxWidth( String property, List<InterfaceUsageType> ifaces ) {
      int intrfcMaxWidth = 0;
      for( final InterfaceUsageType iface : ifaces ) {
         intrfcMaxWidth = Math.max( BaseRenderer.toID( iface.getInterface()).length(), intrfcMaxWidth );
      }
      _renderer.set( property, intrfcMaxWidth );
   }

   protected void write( String subDir, String filename, ST source ) throws IOException {
      final File target = new File( _genDir, _package + '/' + subDir + '/' + filename );
      if( _model.isUpToDate( target )) {
         return;
      }
      target.getParentFile().mkdirs();
      try( final PrintStream ps = new PrintStream( target )) {
         ps.print( source.render());
      }
      System.out.printf( "%s written\n", target.getPath());
   }
}
