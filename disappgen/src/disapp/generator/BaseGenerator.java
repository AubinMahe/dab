package disapp.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import disapp.generator.model.AutomatonType;
import disapp.generator.model.ComponentType;
import disapp.generator.model.EnumerationType;
import disapp.generator.model.EventType;
import disapp.generator.model.FieldType;
import disapp.generator.model.FieldtypeType;
import disapp.generator.model.InterfaceType;
import disapp.generator.model.OfferedInterfaceUsageType;
import disapp.generator.model.RequestType;
import disapp.generator.model.RequiredInterfaceUsageType;

abstract class BaseGenerator {

   protected final SortedSet<File> _generatedFiles = new TreeSet<>();
   protected final Model           _model;
   protected final STGroup         _group;
   protected final BaseRenderer    _renderer;
   protected /* */ String          _genDir;
   protected /* */ String          _moduleName;

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

   private void generateTypesUsedBy( InterfaceType iface ) throws IOException {
      final SortedSet<String> used = _model.getUsedTypesBy( iface.getName());
      if( used != null ) {
         for( final String typeName : used ) {
            if( _model.enumIsDefined( typeName )) {
               generateEnum( typeName );
            }
            else if( _model.structIsDefined( typeName )){
               generateStruct( typeName );
            }
         }
      }
   }

   protected void generateTypesUsedBy( ComponentType component ) throws IOException {
      for( final OfferedInterfaceUsageType usage : component.getOffers()) {
         generateTypesUsedBy((InterfaceType)usage.getInterface());
      }
      for( final RequiredInterfaceUsageType usage : component.getRequires()) {
         generateTypesUsedBy((InterfaceType)usage.getInterface());
      }
      final AutomatonType automaton = component.getAutomaton();
      if( automaton != null ) {
         generateEnum( automaton.getStateEnum().getName());
         generateEnum( automaton.getEventEnum().getName());
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
      final List<Object> eventsOrRequests = _model.getEventsOrRequests().get( iface.getName());
      for( final Object o : eventsOrRequests ) {
         if( o instanceof EventType ) {
            final EventType event = (EventType)o;
            configureRendererWidthsCumulative( event.getField() );
         }
         else {
            final RequestType request = (RequestType)o;
            configureRendererWidthsCumulative( request.getArguments().getField());
            configureRendererWidthsCumulative( request.getResponse().getField());
         }
      }
   }

   void setRendererInterfaceMaxWidth( String property, List<OfferedInterfaceUsageType> ifaces ) {
      int intrfcMaxWidth = 0;
      for( final OfferedInterfaceUsageType offered : ifaces ) {
         final InterfaceType iface     = (InterfaceType)offered.getInterface();
         final String        ifaceName = iface.getName();
         intrfcMaxWidth = Math.max( BaseRenderer.toID( ifaceName ).length(), intrfcMaxWidth );
      }
      _renderer.set( property, intrfcMaxWidth );
   }

   protected void generateMakefileSourcesList( String headerExt, String srcEx ) throws FileNotFoundException {
      final STGroupFile group = new STGroupFile( getClass().getResource( "/resources/mk.stg" ), "utf-8", '<', '>' );
      final ST          mk    = group.getInstanceOf( "/mk" );
      final File   parent = new File( _genDir ).getParentFile();
      final String subDir = _genDir.substring( parent.getPath().length() + 1 );
      mk.add( "path"   , subDir + "/" + _moduleName );
      mk.add( "srcs"   , _generatedFiles.stream().filter( f -> f.getName().endsWith( srcEx     )).toArray());
      mk.add( "headers", _generatedFiles.stream().filter( f -> f.getName().endsWith( headerExt )).toArray());
      final File target = new File( parent, "generated-files.mk" );
      target.getParentFile().mkdirs();
      try( final PrintStream ps = new PrintStream( target )) {
         ps.print( mk.render());
      }
      System.out.printf( "%s written\n", target.getPath());
   }

   protected void write( String filename, ST source ) throws IOException {
      final File target = new File( _genDir, _moduleName + '/' + filename );
      if( ! _model.isUpToDate( target )) {
         target.getParentFile().mkdirs();
         try( final PrintStream ps = new PrintStream( target )) {
            ps.print( source.render());
         }
         System.out.printf( "%s written\n", target.getPath());
      }
      _generatedFiles.add( target );
   }
}
