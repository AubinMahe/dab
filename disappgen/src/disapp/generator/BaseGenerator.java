package disapp.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.bind.JAXBElement;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.misc.STMessage;

import disapp.generator.model.AutomatonType;
import disapp.generator.model.ComponentType;
import disapp.generator.model.DataType;
import disapp.generator.model.DurationUnits;
import disapp.generator.model.EnumerationType;
import disapp.generator.model.EventType;
import disapp.generator.model.FieldType;
import disapp.generator.model.FieldtypeType;
import disapp.generator.model.InterfaceType;
import disapp.generator.model.LiteralType;
import disapp.generator.model.OfferedInterfaceUsageType;
import disapp.generator.model.RequestType;
import disapp.generator.model.RequiredInterfaceUsageType;
import disapp.generator.model.StructType;
import disapp.generator.model.TypesImplType;
import disapp.generator.model.TypesType;

abstract class BaseGenerator {

   protected final SortedSet<File>     _generatedFiles = new TreeSet<>();
   protected final SortedSet<File>     _generatedTypes = new TreeSet<>();
   protected final Map<String, String> _genDirTypes    = new HashMap<>();
   protected final Model               _model;
   protected final STGroup             _group;
   protected final BaseRenderer        _renderer;
   protected /* */ String              _genDir;
   protected /* */ String              _moduleName;

   class DisAppErrListener implements STErrorListener {

      @Override
      public void compileTimeError( STMessage msg ) {
         System.err.println( msg.toString());
         System.exit( 1 );
      }

      @Override
      public void runTimeError( STMessage msg ) {
         System.err.println( msg.toString());
         System.exit( 1 );
      }

      @Override
      public void IOError( STMessage msg ) {
         System.err.println( msg.toString());
         System.exit( 1 );
      }

      @Override
      public void internalError( STMessage msg ) {
         System.err.println( msg.toString());
         System.exit( 1 );
      }
   }

   protected BaseGenerator( Model model, String language, String templateName, BaseRenderer renderer ) {
      _model    = model;
      _group    = new STGroupFile( getClass().getResource( "/resources/" + templateName ), "utf-8", '<', '>' );
      _renderer = renderer;
      final EventOrRequestOrDataAdaptor eoroda = new EventOrRequestOrDataAdaptor();
      _group.setListener(  new DisAppErrListener());
      _group.registerRenderer( String.class, _renderer );
      _group.registerModelAdaptor( FieldType      .class, new FieldAdaptor());
      _group.registerModelAdaptor( EnumerationType.class, new EnumerationAdaptor());
      _group.registerModelAdaptor( JAXBElement    .class, new ActionAdaptor());
      _group.registerModelAdaptor( DurationUnits  .class, new DurationUnitAdaptor());
      _group.registerModelAdaptor( EventType      .class, eoroda );
      _group.registerModelAdaptor( RequestType    .class, eoroda );
      _group.registerModelAdaptor( DataType       .class, eoroda );
      for( final TypesType types : _model.getApplication().getTypes()) {
         for( final TypesImplType impl : types.getImplementation()) {
            if( impl.getLanguage().equals( language )) {
               _genDirTypes.put( types.getModuleName(), impl.getSrcDir());
            }
         }
      }
   }

   abstract protected void gEnum ( String xUser ) throws IOException;
   abstract protected void struct( String xUser ) throws IOException;

   private void generateTypesUsedBy( InterfaceType iface ) throws IOException {
      final SortedSet<String> used = _model.getUsedTypesBy( iface.getName());
      if( used != null ) {
         for( final String typeName : used ) {
            if( _model.isEnum( typeName )) {
               gEnum( typeName );
            }
            else if( _model.isStruct( typeName )){
               struct( typeName );
            }
         }
      }
   }

   protected void typesUsedBy( ComponentType component ) throws IOException {
      for( final OfferedInterfaceUsageType usage : component.getOffers()) {
         generateTypesUsedBy((InterfaceType)usage.getInterface());
      }
      for( final RequiredInterfaceUsageType usage : component.getRequires()) {
         generateTypesUsedBy((InterfaceType)usage.getInterface());
      }
      final AutomatonType automaton = component.getAutomaton();
      if( automaton != null ) {
         gEnum( automaton.getStateEnum().getName());
         gEnum( automaton.getEventEnum().getName());
      }
   }

   private void configureRendererWidthsCumulative( FieldType field ) {
      int maxLength    = (Integer)_renderer.get(    "width" );
      int maxStrLength = (Integer)_renderer.get( "strWidth" );
      final String cname = _renderer.name( field.getName());
      maxLength = Math.max( maxLength, cname.length());
      if( field.getType() == FieldtypeType.STRING ) {
         maxStrLength = Math.max( maxStrLength, cname.length());
      }
      _renderer.set( "width"   , maxLength    );
      _renderer.set( "strWidth", maxStrLength );
   }

   private void configureRendererWidthCumulative( LiteralType literal ) {
      int maxLength = (Integer)_renderer.get(    "width" );
      final String cname = _renderer.name( literal.getName());
      maxLength = Math.max( maxLength, cname.length());
      _renderer.set( "width", maxLength    );
   }

   private void configureRendererWidthCumulative( DataType data ) {
      int maxLength = (Integer)_renderer.get(    "width" );
      final String cname = _renderer.name( data.getName());
      maxLength = Math.max( maxLength, cname.length());
      _renderer.set( "width", maxLength    );
   }

   protected void setRendererMaxWidth( EnumerationType enm ) {
      _renderer.set( "width"   , 0 );
      _renderer.set( "strWidth", 0 );
      for( final LiteralType literal : enm.getLiteral()) {
         configureRendererWidthCumulative( literal );
      }
   }

   protected void setRendererFieldsMaxWidth( StructType struct ) {
      _renderer.set( "width"   , 0 );
      _renderer.set( "strWidth", 0 );
      for( final FieldType field : struct.getField()) {
         configureRendererWidthsCumulative( field );
      }
   }

   protected void setRendererFieldsMaxWidth( InterfaceType iface ) {
      _renderer.set( "width"   , 0 );
      _renderer.set( "strWidth", 0 );
      final List<Object> eventsOrRequests = _model.getFacets().get( iface.getName());
      for( final Object o : eventsOrRequests ) {
         if( o instanceof EventType ) {
            final EventType event = (EventType)o;
            for( final FieldType field : event.getField()) {
               configureRendererWidthsCumulative( field );
            }
         }
         else if( o instanceof RequestType ) {
            final RequestType request = (RequestType)o;
            for( final FieldType field : request.getArguments().getField()) {
               configureRendererWidthsCumulative( field );
            }
            final StructType struct = _model.getResponse( request );
            for( final FieldType field : struct.getField()) {
               configureRendererWidthsCumulative( field );
            }
         }
         else if( o instanceof DataType ) {
            final DataType data = (DataType)o;
            configureRendererWidthCumulative( data );
         }
         else {
            throw new IllegalStateException( "Unexpected class: " + o.getClass());
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

   protected static void generateMakefileSourcesList(
      SortedSet<File> files,
      String          genDir,
      String          moduleName,
      String          headerExt,
      String          srcEx      ) throws FileNotFoundException
   {
      final STGroupFile group  = new STGroupFile( BaseGenerator.class.getResource( "/resources/mk.stg" ), "utf-8", '<', '>' );
      final ST          mk     = group.getInstanceOf( "/mk" );
      final File        parent = new File( genDir ).getParentFile();
      final String      subDir = genDir.substring( parent.getPath().length() + 1 );
      mk.add( "path"   , subDir + "/" + moduleName );
      mk.add( "srcs"   , files.stream().filter( f -> f.getName().endsWith( srcEx     )).toArray());
      mk.add( "headers", files.stream().filter( f -> f.getName().endsWith( headerExt )).toArray());
      final File target = new File( parent, "generated-files.mk" );
      target.getParentFile().mkdirs();
      try( final PrintStream ps = new PrintStream( target )) {
         ps.print( mk.render());
      }
      System.out.printf( "%s written\n", target.getPath());
      files.clear();
   }

   protected void writeType( String modelModuleName, String subPath, String filename, ST source ) throws IOException {
      final String genDir = _genDirTypes.get( modelModuleName );
      final File   target = new File( genDir + '/' + subPath + '/' + filename );
      if( ! _model.isUpToDate( target ) && ! _generatedTypes.contains( target )) {
         target.getParentFile().mkdirs();
         try( final PrintStream ps = new PrintStream( target )) {
            ps.print( source.render());
         }
         System.out.printf( "%s written\n", target.getPath());
      }
      _generatedTypes.add( target );
   }

   protected void write( String filename, ST source ) throws IOException {
      final String path = _moduleName.replaceAll( "\\.", "/" ).replaceAll( "::", "/" );
      final File target = new File( _genDir, path + '/' + filename );
      if( ! _model.isUpToDate( target ) && ! _generatedFiles.contains( target )) {
         target.getParentFile().mkdirs();
         try( final PrintStream ps = new PrintStream( target )) {
            ps.print( source.render());
         }
         System.out.printf( "%s written\n", target.getPath());
      }
      _generatedFiles.add( target );
   }
}
