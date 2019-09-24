package disapp.generator.st4;

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

   protected final Model   _model;
   protected final STGroup _group;
   protected /* */ String  _genDir;
   protected /* */ String  _package;

   protected BaseGenerator( Model model, String templateName ) {
      _model = model;
      _group = new STGroupFile( getClass().getResource( "/resources/" + templateName ), "utf-8", '<', '>' );
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
