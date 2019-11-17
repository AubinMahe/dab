package org.hpms.automaton.editors;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.zest.core.viewers.EntityConnectionData;

final class GraphLabelProvider extends LabelProvider implements Constants {

   private final IXMLModel model;

   GraphLabelProvider( IXMLModel model_ ) {
      model = model_;
   }

   @Override
   public String getText( Object element ) {
      try {
         if( element instanceof String ) {
            return (String)element;
         }
         if( element instanceof EntityConnectionData ) {
            final EntityConnectionData ecd = (EntityConnectionData)element;
            return XmlModelHelper.getTransition( model.getRoot(), ecd ).getValue();
         }
      }
      catch( final Throwable t ) {
         t.printStackTrace();
      }
      System.err.printf( "%s.getText|element = %s, class =%s\n", getClass().getName(),
         element.toString(), element.getClass());
      return "...";
   }
}
