package disapp.generator;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.stringtemplate.v4.ST;

import disapp.generator.model.AutomatonType;
import disapp.generator.model.ComponentType;
import disapp.generator.model.EnumerationType;
import disapp.generator.model.EventType;
import disapp.generator.model.FieldType;
import disapp.generator.model.InterfaceType;
import disapp.generator.model.InterfaceUsageType;
import disapp.generator.model.StructType;

public class CppGenerator extends BaseGenerator {

   public CppGenerator( Model model ) {
      super( model, "cpp.stg", new BaseRenderer() );
   }

   private void generateEnumHeader( String name ) throws IOException {
      final EnumerationType enm  = _model.getEnum( name );
      final ST              tmpl = _group.getInstanceOf( "/enumHeader" );
      tmpl.add( "namespace", _package );
      tmpl.add( "enum"     , enm      );
      write( "", name + ".hpp", tmpl );
   }

   private void generateEnumBody( String name ) throws IOException {
      final EnumerationType enm  = _model.getEnum( name );
      final ST              tmpl = _group.getInstanceOf( "/enumBody" );
      tmpl.add( "namespace", _package );
      tmpl.add( "enum"     , enm      );
      write( "", name + ".cpp", tmpl );
   }

   @Override
   protected void generateEnum( String name ) throws IOException {
      generateEnumHeader( name );
      generateEnumBody  ( name );
   }

   private void generateStructHeader( String name ) throws IOException {
      final StructType struct = _model.getStruct( name );
      final ST         tmpl   = _group.getInstanceOf( "/structHeader" );
      tmpl.add( "namespace", _package );
      tmpl.add( "struct"   , struct   );
      write( "", name + ".hpp", tmpl );
   }

   private void generateStructBody( String name ) throws IOException {
      final StructType      struct = _model.getStruct( name );
      final List<FieldType> fields = struct.getField();
      final ST              tmpl   = _group.getInstanceOf( "/structBody" );
      tmpl.add( "namespace", _package );
      tmpl.add( "struct"   , struct   );
      setRendererFieldsMaxWidth( fields );
      write( "", name + ".cpp", tmpl );
   }

   @Override
   protected void generateStruct( String name ) throws IOException {
      generateStructHeader( name );
      generateStructBody  ( name );
   }

   private void generateRequiredInterfaces( ComponentType component ) throws IOException {
      for( final InterfaceUsageType required : _model.getRequiresOf( component )) {
         final String            ifaceName  = required.getInterface();
         final InterfaceType     iface      = _model.getInterface( ifaceName );
         final SortedSet<String> usedTypes  = _model.getUsedTypesBy( ifaceName );
         final int               rawSize    = _model.getBufferCapacity( iface );
         final ST                tmpl       = _group.getInstanceOf( "/requiredInterface" );
         tmpl.add( "namespace", _package  );
         tmpl.add( "usedTypes", usedTypes );
         tmpl.add( "ifaceName", ifaceName );
         tmpl.add( "rawSize"  , rawSize   );
         tmpl.add( "iface"    , iface     );
         write( "", 'I' + required.getInterface() + ".hpp", tmpl );
      }
   }

   private void generateRequiredImplementations( ComponentType component ) throws IOException {
      for( final InterfaceUsageType required : _model.getRequiresOf( component )) {
         final String            ifaceName  = required.getInterface();
         final InterfaceType     iface      = _model.getInterface( ifaceName );
         final SortedSet<String> usedTypes  = _model.getUsedTypesBy( ifaceName );
         final int               rawSize    = _model.getBufferCapacity( iface );
         final int               ifaceID    = _model.getInterfaceID( ifaceName );
         final ST                tmpl       = _group.getInstanceOf( "/requiredImplementation" );
         tmpl.add( "namespace", _package  );
         tmpl.add( "usedTypes", usedTypes );
         tmpl.add( "ifaceName", ifaceName );
         tmpl.add( "rawSize"  , rawSize   );
         tmpl.add( "iface"    , iface     );
         tmpl.add( "ifaceID"  , ifaceID   );
         setRendererFieldsMaxWidth( iface );
         write( "net", ifaceName + ".cpp", tmpl );
      }
   }

   private void generateOfferedInterface( ComponentType component ) throws IOException {
      final String                   compName   = component.getName();
      final List<InterfaceUsageType> allOffered = _model.getOffersOf( component );
      final SortedSet<String>        usedTypes  = _model.getUsedTypesBy( allOffered );
      final List<EventType>          events     = _model.getEventsOf( allOffered );
      final ST                       tmpl       = _group.getInstanceOf( "/offeredInterface" );
      tmpl.add( "namespace", _package  );
      tmpl.add( "name"     , compName  );
      tmpl.add( "usedTypes", usedTypes );
      tmpl.add( "events"   , events    );
      write( "", 'I' + component.getName() + ".hpp", tmpl );
   }

   private void generateDispatcherInterface( ComponentType component, int rawSize ) throws IOException {
      final String compName = component.getName();
      final ST     tmpl     = _group.getInstanceOf( "/dispatcherInterface" );
      tmpl.add( "namespace", _package );
      tmpl.add( "name"     , compName );
      tmpl.add( "rawSize"  , rawSize );
      write( "", 'I' + component.getName() + "Dispatcher.hpp", tmpl );
   }

   private void generateDispatcherImplementation( ComponentType component, int rawSize ) throws IOException {
      final String                       compName     = component.getName();
      final List<InterfaceUsageType>     ifaces       = _model.getOffersOf( component );
      final Map<String, Integer>         interfaceIDs = _model.getInterfaceIDs( ifaces );
      final Map<String, List<EventType>> events       = _model.getEventsMapOf ( ifaces );
      final SortedSet<String>            usedTypes    = _model.getUsedTypesBy ( ifaces );
      final ST                           tmpl         = _group.getInstanceOf( "/dispatcherImplementation" );
      tmpl.add( "namespace", _package     );
      tmpl.add( "compName" , compName     );
      tmpl.add( "ifaces"   , interfaceIDs );
      tmpl.add( "events"   , events       );
      tmpl.add( "usedTypes", usedTypes    );
      tmpl.add( "rawSize"  , rawSize      );
      setRendererInterfaceMaxWidth( "width", ifaces );
      write( "net", component.getName() + "Dispatcher.cpp", tmpl );
   }

   private void generateAutomaton( AutomatonType automaton ) throws IOException {
      if( automaton != null ) {
         final ST header = _group.getInstanceOf( "/automatonHeader" );
         header.add( "namespace", _package  );
         header.add( "automaton", automaton );
         write( "", "Automaton.hpp", header );
         final ST body = _group.getInstanceOf( "/automatonBody" );
         body.add( "namespace", _package  );
         body.add( "automaton", automaton );
         write( "", "Automaton.cpp", body );
      }
   }

   void generateComponent( ComponentType component, String srcDir, String moduleName ) throws IOException {
      _genDir  = srcDir;
      _package = moduleName;
      final List<InterfaceUsageType> offers        = _model.getOffersOf  ( component );
      final List<InterfaceUsageType> requires      = _model.getRequiresOf( component );
      final int                      offersRawSize = _model.getBufferCapacity( offers );
      final List<String>             generated     = new LinkedList<>();
      generateTypesUsedBy             ( offers  , generated );
      generateTypesUsedBy             ( requires, generated );
      generateTypesUsedBy             ( component.getAutomaton(), generated );
      generateRequiredInterfaces      ( component );
      generateRequiredImplementations ( component );
      generateOfferedInterface        ( component );
      generateDispatcherInterface     ( component, offersRawSize );
      generateDispatcherImplementation( component, offersRawSize );
      generateAutomaton               ( component.getAutomaton());
   }
}
