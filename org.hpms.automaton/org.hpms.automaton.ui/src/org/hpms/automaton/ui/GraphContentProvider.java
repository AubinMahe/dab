package org.hpms.automaton.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.zest.core.viewers.IGraphEntityContentProvider;
import org.w3c.dom.Attr;
import org.w3c.dom.NodeList;

final class GraphContentProvider implements IGraphEntityContentProvider, Constants {

   private final IXMLModel model;

   GraphContentProvider( IXMLModel model_ ) {
      model = model_;
   }

   @Override
   public Object[] getElements( Object inputElement ) {
      try {
         final NodeList attrs = XmlModelHelper.getStates( model.getRoot());
         final Set<String> result = new HashSet<>();
         for( int i = 0, count = attrs.getLength(); i < count; ++i ) {
            final Attr state = (Attr)attrs.item( i );
            result.add( state.getValue());
         }
         final NodeList shortcuts = XmlModelHelper.getShortcuts( model.getRoot());
         if( shortcuts.getLength() > 0 ) {
            result.add( PSEUDO_STATE_ANY );
         }
         return result.toArray();
      }
      catch( final Throwable t ) {
         t.printStackTrace();
      }
      return new Object[0];
   }

   @Override
   public Object[] getConnectedTo( Object entity ) {
      try {
         final Object[] result;
         if( "*".equals( entity )) {
            final NodeList shortcuts = XmlModelHelper.getShortcuts( model.getRoot());
            final int count = shortcuts.getLength();
            result = new Object[count];
            for( int i = 0; i < count; ++i ) {
               final Attr attr = (Attr)shortcuts.item( i );
               result[i] = attr.getValue();
            }
         }
         else {
            final NodeList transitions =
               XmlModelHelper.getTransitionsFrom( model.getRoot(), (String)entity );
            final int count = transitions.getLength();
            result = new Object[count];
            for( int i = 0; i < count; ++i ) {
               final Attr attr = (Attr)transitions.item( i );
               result[i] = attr.getValue();
            }
         }
         return result;
      }
      catch( final Throwable t ) {
         t.printStackTrace();
      }
      return new Object[0];
   }
}
