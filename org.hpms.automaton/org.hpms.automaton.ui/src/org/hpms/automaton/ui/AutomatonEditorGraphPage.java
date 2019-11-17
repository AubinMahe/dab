package org.hpms.automaton.ui;

import java.util.Iterator;
import java.util.prefs.Preferences;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IModelStateListener;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutEntity;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.w3c.dom.Element;

/**
 * An example showing how to create a multi-page editor.
 * This example has 3 pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
@SuppressWarnings("restriction")
class AutomatonEditorGraphPage implements Constants, IXMLModel {

   private StructuredTextEditor xmlEditor;
   private Element              root;
   private Text                 stateOrEventText;
   private Text                 onEntryAction;
   private Text                 onExitAction;
   private GraphViewer          graph;
   private int                  nodeID = 0;

   @Override
   public Element getRoot() {
      if( root != null ) { // simple cache, cleared by setInput
         return root;
      }
      final IDocumentProvider documentProvider = xmlEditor.getDocumentProvider();
      final IDocument doc = documentProvider.getDocument( xmlEditor.getEditorInput());
      final IModelManager modelManager = StructuredModelManager.getModelManager();
      IDOMModel model = (IDOMModel)modelManager.getExistingModelForEdit( doc );
      if( model == null ) {
         model = (IDOMModel)modelManager.getModelForEdit((IStructuredDocument)doc );
      }
      final IDOMDocument domDoc = model.getDocument();
      model.addModelStateListener( new IModelStateListener() {
         @Override public void modelAboutToBeChanged( IStructuredModel arg0 ) {/**/}
         @Override public void modelAboutToBeReinitialized( IStructuredModel arg0 ) {/**/}
         @Override public void modelChanged( IStructuredModel arg0 ) { refresh(); }
         @Override public void modelDirtyStateChanged( IStructuredModel arg0, boolean arg1 ) {/**/}
         @Override public void modelReinitialized( IStructuredModel arg0 ) {/**/}
         @Override public void modelResourceDeleted( IStructuredModel arg0 ) {/**/}
         @Override public void modelResourceMoved( IStructuredModel arg0, IStructuredModel arg1 ) {/**/}
      });
      return root = domDoc.getDocumentElement();
   }

   private void renameSelection( String newValue ) {
      try {
         final IStructuredSelection selection = graph.getStructuredSelection();
         if( selection.size() == 1 ) {
            final Object selected = selection.getFirstElement();
            if( selected instanceof String ) {
               XmlModelHelper.remaneState( getRoot(), (String)selected, newValue );
            }
            else if( selected instanceof EntityConnectionData ) {
               XmlModelHelper.remaneEvent( getRoot(), (EntityConnectionData)selected, newValue );
            }
            else {
               System.err.printf( "%s.renameSelection|%s\n", getClass().getName(),
                  selected.getClass().getName());
            }
         }
      }
      catch( final Throwable t ) {
         t.printStackTrace();
      }
   }

   private void deleteSelection() {
      try {
         final IStructuredSelection selection = graph.getStructuredSelection();
         for( final Iterator<?> it = selection.iterator(); it.hasNext(); ) {
            final Object selected = it.next();
            if( selected instanceof String ) {
               XmlModelHelper.removeState( getRoot(), (String)selected );
            }
            else if( selected instanceof EntityConnectionData ) {
               XmlModelHelper.removeTransition( getRoot(), (EntityConnectionData)selected );
            }
            else {
               System.err.printf( "%s.removeSelection|%s\n", getClass().getName(),
                  selected.getClass().getName());
            }
         }
      }
      catch( final Throwable t ) {
         t.printStackTrace();
      }
   }

   private void itemSelected( SelectionChangedEvent event ) {
      final IStructuredSelection selection = event.getStructuredSelection();
      if( selection.size() == 1 ) {
         final Object selected = selection.getFirstElement();
         if( selected instanceof String ) {
            final String state = (String)selected;
            stateOrEventText.setText( state );
            try {
               onEntryAction.setText( XmlModelHelper.getOnEntryAction( getRoot(), state ));
               onExitAction .setText( XmlModelHelper.getOnExitAction ( getRoot(), state ));
            }
            catch( final Throwable t ) {
               t.printStackTrace();
            }
         }
         else if( selected instanceof EntityConnectionData ) {
            final EntityConnectionData ecd = (EntityConnectionData)selected;
            final LabelProvider lp = (LabelProvider)graph.getLabelProvider();
            stateOrEventText.setText( lp.getText( ecd ));
         }
         else {
            System.err.printf( "%s.itemSelected|%s\n", getClass().getName(),
               selected.getClass().getName());
         }
      }
      else {
         stateOrEventText.setText( "" );
      }
   }

   private String createLabel( String dflt ) {
      String label = stateOrEventText.getText();
      if( label == null || label.isEmpty()) {
         label = dflt + ++nodeID;
      }
      stateOrEventText.setText("");
      return label;
   }

   private void createTransition() {
      final IStructuredSelection selection = graph.getStructuredSelection();
      final String source;
      final String dest;
      if( selection.size() == 1 ) {
         source = (String)selection.getFirstElement();
         dest   = createLabel( TRANSITION_DEST_ATTR );
      }
      else if( selection.size() == 2 ) {
         final Iterator<?> it = selection.iterator();
         source = (String)it.next();
         dest   = (String)it.next();
      }
      else {
         source = createLabel( TRANSITION_SOURCE_ATTR );
         dest   = createLabel( TRANSITION_DEST_ATTR );
      }
      final String event = createLabel( TRANSITION_EVENT_ATTR );
      XmlModelHelper.createTransition( getRoot(), source, event, dest );
      graph.addNode( source );
      graph.addNode( dest );
      graph.addRelationship( event, source, dest );
   }

   private void setOnEntryAction( String action ) {
      try {
         final IStructuredSelection selection = graph.getStructuredSelection();
         if( selection.size() != 1 ) {
            return;
         }
         final String state = (String)selection.getFirstElement();
         XmlModelHelper.setOnEntryAction( getRoot(), state, action );
      }
      catch( final Throwable e ) {
         e.printStackTrace();
      }
   }

   private void setOnExitAction( String action ) {
      try {
         final IStructuredSelection selection = graph.getStructuredSelection();
         if( selection.size() != 1 ) {
            return;
         }
         final String state = (String)selection.getFirstElement();
         XmlModelHelper.setOnExitAction( getRoot(), state, action );
      }
      catch( final Throwable e ) {
         e.printStackTrace();
      }
   }

   void saveNodesLayout() {
      try {
         final FileEditorInput fei = (FileEditorInput)xmlEditor.getEditorInput();
         final IFile file = fei.getFile();
         final String model = file.getName();
         final Preferences prefs = Preferences.userNodeForPackage( getClass());
         for( final Object o : graph.getGraphControl().getNodes()) {
            final GraphNode gn = (GraphNode)o;
            final LayoutEntity lEntity = gn.getLayoutEntity();
            final String prefix = model + '.' + gn.getText() + ".";
            prefs.putDouble( prefix + 'x', lEntity.getXInLayout());
            prefs.putDouble( prefix + 'y', lEntity.getYInLayout());
         }
         prefs.flush();
      }
      catch( final Throwable e ) {
         e.printStackTrace();
      }
   }

   boolean loadNodesLayout() {
      boolean layoutLoaded = false;
      final FileEditorInput fei = (FileEditorInput)xmlEditor.getEditorInput();
      final IFile file = fei.getFile();
      final String model = file.getName();
      final Preferences prefs = Preferences.userNodeForPackage( getClass());
      final Graph g = graph.getGraphControl();
      for( final Object o : g.getNodes()) {
         final GraphNode gn = (GraphNode)o;
         final LayoutEntity le = gn.getLayoutEntity();
         final String prefix = model + '.' + gn.getText() + ".";
         final double x = prefs.getDouble( prefix + 'x' , -1_000_000 );
         final double y = prefs.getDouble( prefix + 'y' , -1_000_000 );
         if( x > -999_999 && y > -999_999 ) {
            le.setLocationInLayout( x, y );
            layoutLoaded = true;
         }
      }
      graph.getGraphControl().redraw();
      return layoutLoaded;
   }

   void refresh() {
      graph.refresh();
   }

   private Composite createTopControls( Composite parent ) {
      final ImageRegistry imgReg = Activator.getDefault().getImageRegistry();
      final Composite top = new Composite( parent, SWT.HORIZONTAL );
      GridData constraints = new GridData();
      constraints.horizontalAlignment = SWT.FILL;
      constraints.grabExcessHorizontalSpace = true;
      top.setLayoutData( constraints );
      final GridLayout topLayout = new GridLayout( 8, false );
      top.setLayout( topLayout );
      //-- 1 : New ----------------------------------------
      final Button createState = new Button( top, SWT.PUSH );
      createState.addListener( SWT.Selection, e -> createTransition());
      createState.setToolTipText(
         "Ajoute un nouvel état à cet automate ou, si deux états sont sélectionnés, " +
            "une nouvelle transition entre ces derniers.\n" +
         "Utilise le contenu du champ texte s'il n'est pas vide." );
      createState.setImage( imgReg.get( ICON_KEY_NEW ));
      //-- 2, 3 : Nom ----------------------------------------
      Label lbl = new Label( top, 0 );
      lbl.setText( "Nom : " );
      stateOrEventText = new Text( top, SWT.SINGLE );
      stateOrEventText.setToolTipText(
         "Sélectionnez un état ou un événement puis faites \"entrée\" dans ce champ pour le renommer" );
      stateOrEventText.addTraverseListener( e -> {
         if( e.detail == SWT.TRAVERSE_RETURN ) {
            renameSelection( stateOrEventText.getText());
         }
      });
      constraints = new GridData();
      constraints.horizontalAlignment = SWT.FILL;
      constraints.grabExcessHorizontalSpace = true;
      stateOrEventText.setLayoutData( constraints );
      //-- 4, 5: On-Entry -----------------------------------
      lbl = new Label( top, 0 );
      lbl.setText( "On-Entry action : " );
      onEntryAction = new Text( top, SWT.SINGLE );
      onEntryAction.setToolTipText( "Action exécutée à l'entrée dans l'état" );
      onEntryAction.addTraverseListener( e -> {
         if( e.detail == SWT.TRAVERSE_RETURN ) {
            setOnEntryAction( onEntryAction.getText());
         }
      });
      constraints = new GridData();
      constraints.horizontalAlignment = SWT.FILL;
      constraints.grabExcessHorizontalSpace = true;
      onEntryAction.setLayoutData( constraints );
      //-- 6, 7: On-Exit ------------------------------------
      lbl = new Label( top, 0 );
      lbl.setText( "On-Exit action : " );
      onExitAction = new Text( top, SWT.SINGLE );
      onExitAction.setToolTipText( "Action exécutée à l'entrée dans l'état" );
      onExitAction.addTraverseListener( e -> {
         if( e.detail == SWT.TRAVERSE_RETURN ) {
            setOnExitAction( onExitAction.getText());
         }
      });
      constraints = new GridData();
      constraints.horizontalAlignment = SWT.FILL;
      constraints.grabExcessHorizontalSpace = true;
      onExitAction.setLayoutData( constraints );
      //-- 8: Delete -------------------------------------
      final Button removeSelection = new Button( top, SWT.PUSH );
      removeSelection.addListener( SWT.Selection, e -> deleteSelection());
      removeSelection.setImage( imgReg.get( ICON_KEY_DELETE ));
      removeSelection.setToolTipText(
         "Sélectionnez un état ou un événement pour le supprimer" );
      return top;
   }

   Composite create( Composite parent, StructuredTextEditor xmlEditor_ ) {
      xmlEditor = xmlEditor_;
      final Composite composite = new Composite( parent, SWT.NONE );
      final GridLayout layout = new GridLayout( 1, false );
      composite.setLayout( layout );
      createTopControls( composite );
      graph = new GraphViewer( composite, SWT.NONE );
      graph.setContentProvider( new GraphContentProvider( this ));
      graph.setLabelProvider( new GraphLabelProvider( this ));
      final GridData constraints = new GridData();
      constraints.horizontalAlignment = SWT.FILL;
      constraints.verticalAlignment = SWT.FILL;
      constraints.grabExcessHorizontalSpace = true;
      constraints.grabExcessVerticalSpace = true;
      graph.getControl().setLayoutData( constraints );
      graph.addSelectionChangedListener( this::itemSelected );
      graph.setConnectionStyle( ZestStyles.CONNECTIONS_DIRECTED );
      graph.setInput( getRoot());
      final LayoutAlgorithm la = new SpringLayoutAlgorithm( LayoutStyles.NO_LAYOUT_NODE_RESIZING );
      graph.setLayoutAlgorithm( la, true );
      Display.getDefault().asyncExec(() -> {
         if( loadNodesLayout()) {
            graph.setLayoutAlgorithm( new NullLayoutAlgorithm(), true );
         }
      });
      return composite;
   }
}
