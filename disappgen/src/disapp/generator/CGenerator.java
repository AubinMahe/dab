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

public class CGenerator extends BaseGenerator {

   public CGenerator( Model model ) {
      super( model, "c.stg", new CRenderer());
   }

   private void generateEnumHeader( String name ) throws IOException {
      final EnumerationType enm  = _model.getEnum( name );
      final ST              tmpl = _group.getInstanceOf( "/enumHeader" );
      tmpl.add( "prefix", _package );
      tmpl.add( "enum"  , enm      );
      write( "", CRenderer.cname( name ) + ".h", tmpl );
   }

   private void generateEnumBody( String name ) throws IOException {
      final EnumerationType enm  = _model.getEnum( name );
      final ST              tmpl = _group.getInstanceOf( "/enumBody" );
      tmpl.add( "prefix", _package );
      tmpl.add( "enum"  , enm      );
      write( "", CRenderer.cname( name ) + ".c", tmpl );
   }

   @Override
   protected void generateEnum( String name ) throws IOException {
      generateEnumHeader( name );
      generateEnumBody  ( name );
   }

   private void generateStructHeader( String name ) throws IOException {
      final StructType struct = _model.getStruct( name );
      final ST         tmpl   = _group.getInstanceOf( "/structHeader" );
      tmpl.add( "prefix", _package );
      tmpl.add( "struct", struct   );
      write( "", CRenderer.cname( name ) + ".h", tmpl );
   }

   private void generateStructBody( String name ) throws IOException {
      final StructType      struct = _model.getStruct( name );
      final List<FieldType> fields = struct.getField();
      final ST              tmpl   = _group.getInstanceOf( "/structBody" );
      tmpl.add( "prefix", _package );
      tmpl.add( "struct", struct   );
      setRendererFieldsMaxWidth( fields );
      write( "", CRenderer.cname( name ) + ".c", tmpl );
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
         tmpl.add( "prefix"   , _package   );
         tmpl.add( "usedTypes", usedTypes  );
         tmpl.add( "ifaceName", ifaceName );
         tmpl.add( "rawSize"  , rawSize    );
         tmpl.add( "iface"    , iface      );
         write( "", CRenderer.cname( ifaceName ) + ".h", tmpl );
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
         tmpl.add( "prefix"   , _package   );
         tmpl.add( "usedTypes", usedTypes  );
         tmpl.add( "ifaceName", ifaceName );
         tmpl.add( "rawSize"  , rawSize    );
         tmpl.add( "iface"    , iface      );
         tmpl.add( "ifaceID"  , ifaceID    );
         setRendererFieldsMaxWidth( iface );
         write( "net", CRenderer.cname( ifaceName ) + ".c", tmpl );
      }
   }

   private void generateOfferedInterface( ComponentType component ) throws IOException {
      final String                   compName   = component.getName();
      final List<InterfaceUsageType> allOffered = _model.getOffersOf( component );
      final SortedSet<String>        usedTypes  = _model.getUsedTypesBy( allOffered );
      final List<EventType>          events     = _model.getEventsOf( allOffered );
      final ST tmpl = _group.getInstanceOf( "/offeredInterface" );
      tmpl.add( "prefix"   , _package  );
      tmpl.add( "name"     , compName  );
      tmpl.add( "usedTypes", usedTypes );
      tmpl.add( "events"   , events    );
      write( "", CRenderer.cname( component.getName()) + ".h", tmpl );
   }

   private void generateDispatcherInterface( ComponentType component, int rawSize ) throws IOException {
      final String compName = component.getName();
      final ST     tmpl     = _group.getInstanceOf( "/dispatcherInterface" );
      tmpl.add( "prefix", _package );
      tmpl.add( "name"   , compName );
      tmpl.add( "rawSize", rawSize );
      write( "", CRenderer.cname( component.getName()) + "_dispatcher.h", tmpl );
   }

   private void generateDispatcherImplementation( ComponentType component, int rawSize ) throws IOException {
      final String                       compName     = component.getName();
      final List<InterfaceUsageType>     ifaces       = _model.getOffersOf( component );
      final Map<String, Integer>         interfaceIDs = _model.getInterfaceIDs( ifaces );
      final Map<String, List<EventType>> events       = _model.getEventsMapOf ( ifaces );
      final SortedSet<String>            usedTypes    = _model.getUsedTypesBy ( ifaces );
      final ST                           tmpl         = _group.getInstanceOf( "/dispatcherImplementation" );
      tmpl.add( "prefix"   , _package     );
      tmpl.add( "compName" , compName     );
      tmpl.add( "ifaces"   , interfaceIDs );
      tmpl.add( "events"   , events       );
      tmpl.add( "usedTypes", usedTypes    );
      tmpl.add( "rawSize"  , rawSize      );
      setRendererInterfaceMaxWidth( "width", ifaces );
      write( "net", CRenderer.cname( compName ) + "_dispatcher.c", tmpl );
   }

   private void generateAutomaton( AutomatonType automaton ) throws IOException {
      if( automaton != null ) {
         final ST header = _group.getInstanceOf( "/automatonHeader" );
         header.add( "prefix"   , _package  );
         header.add( "automaton", automaton );
         write( "", "automaton.h", header );
         final ST body = _group.getInstanceOf( "/automatonBody" );
         body.add( "prefix"   , _package  );
         body.add( "automaton", automaton );
         write( "", "automaton.c", body );
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
