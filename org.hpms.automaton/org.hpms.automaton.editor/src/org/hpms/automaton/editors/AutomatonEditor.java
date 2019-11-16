package org.hpms.automaton.editors;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IModelStateListener;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.sse.ui.internal.IModelProvider;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IGraphEntityContentProvider;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutEntity;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
public class AutomatonEditor extends MultiPageEditorPart
   implements
      Constants,
      IResourceChangeListener
{
   private final XPath                 xPath = XPathFactory.newInstance().newXPath();
   private /* */ StructuredTextEditor  xmlEditor;
   private /* */ Element               root;
   private /* */ Text                  stateOrEventText;
   private /* */ GraphViewer           graph;
   private /* */ int                   nodeID = 0;

   class GraphContentProvider implements IGraphEntityContentProvider {
      @Override public Object[] getElements( Object inputElement ) {
         final NodeList transitions = getRoot().getElementsByTagName( "transition" );
         final int count = transitions.getLength();
         final Set<String> result = new HashSet<>();
         for( int i = 0; i < count; ++i ) {
            final Element tr = (Element)transitions.item( i );
            result.add( tr.getAttribute( "from" ));
            result.add( tr.getAttribute( "futur" ));
         }
         return result.toArray();
      }
      @Override public Object[] getConnectedTo( Object entity ) {
         try {
            final NodeList transitions = getTransitionsFrom((String)entity );
            final int count = transitions.getLength();
            final Object[] result = new Object[count];
            for( int i = 0; i < count; ++i ) {
               final Attr attr = (Attr)transitions.item( i );
               result[i] = attr.getValue();
            }
            return result;
         }
         catch( final Throwable t ) {
            t.printStackTrace();
         }
         return new Object[0];
      }
   }

   class GraphLabelProvider extends LabelProvider {
      @Override
      public String getText( Object element ) {
         if( element instanceof String ) {
            return (String)element;
         }
         if( element instanceof EntityConnectionData ) {
            final EntityConnectionData ecd = (EntityConnectionData)element;
            try {
               final Element transition = getTransition( ecd );
               return transition.getAttribute( "event" );
            }
            catch( final Throwable t ) {
               t.printStackTrace();
            }
            return ecd.toString();
         }
         System.err.printf( "%s.getText|element = %s, class =%s\n", getClass().getName(),
            element.toString(), element.getClass());
         return "...";
      }
   }

   public AutomatonEditor() {
      super();
      ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
   }

   Element getRoot() {
      if( root != null ) {
         return root;
      }
      final IDocumentProvider documentProvider = xmlEditor.getDocumentProvider();
      IDOMModel model;
      if( documentProvider instanceof IModelProvider ) {
         final IModelProvider modelProvider = (IModelProvider)documentProvider;
         model = (IDOMModel)modelProvider.getModel( getEditorInput());
      }
      else {
         final IDocument doc = documentProvider.getDocument( xmlEditor.getEditorInput());
         final IModelManager modelManager = StructuredModelManager.getModelManager();
         model = (IDOMModel)modelManager.getExistingModelForEdit( doc );
         if( model == null ) {
            model = (IDOMModel)modelManager.getModelForEdit((IStructuredDocument)doc );
         }
      }
      final IDOMDocument doc = model.getDocument();
      return root = doc.getDocumentElement();
   }

   void createXMLView() {
      try {
         xmlEditor = new StructuredTextEditor();
         final int index = addPage( xmlEditor, getEditorInput());
         setPageText( index, xmlEditor.getTitle());
      }
      catch( final PartInitException e ) {
         ErrorDialog.openError(
            getSite().getShell(),
            "Error creating nested text editor",
            null,
            e.getStatus());
      }
   }

   private void createTransition( String from, String event, String futur ) {
      final Document doc = getRoot().getOwnerDocument();
      final Element  tr  = doc.createElement( "transition" );
      tr.setAttribute( "from" , from );
      tr.setAttribute( "event", event );
      tr.setAttribute( "futur", futur );
      doc.appendChild( tr );
      from  = tr.getAttribute( "from" );
      event = tr.getAttribute( "event" );
      futur = tr.getAttribute( "futur" );
      graph.addNode( from );
      graph.addNode( futur );
      graph.addRelationship( event, from, futur );
   }

   private NodeList getStates( String state ) throws XPathExpressionException {
      return (NodeList)xPath.evaluate(
         "//transition[@from='"   + state + "']/@from"  +
         "|//transition[@futur='" + state + "']/@futur" +
         "|//shortcut[@futur='"   + state + "']/@futur" +
         "|//on-entry[@state='"   + state + "']/@futur" +
         "|//on-exit[@state='"    + state + "']/@futur", getRoot(), XPathConstants.NODESET );
   }

//   private NodeList getEvents( String event ) throws XPathExpressionException {
//      return (NodeList)xPath.evaluate(
//         "//transition[@event='"   + event + "']/@event", getRoot(), XPathConstants.NODESET );
//   }

   Element getTransition( EntityConnectionData ecd ) throws XPathExpressionException {
      return (Element)xPath.evaluate(
         "//transition[@from='" + ecd.source + "' and @futur='" + ecd.dest + "']",
         getRoot(), XPathConstants.NODE );
   }

   public NodeList getTransitionsFrom( String from ) throws XPathExpressionException {
      return (NodeList)xPath.evaluate(
         "//transition[@from='" + from + "']/@futur", getRoot(), XPathConstants.NODESET );
   }

   private void removeAllTransitionsFrom( String from ) throws XPathExpressionException {
      final NodeList transitions = (NodeList)xPath.evaluate(
         "//transition[@from='" + from + "']", getRoot(), XPathConstants.NODESET );
      for( int i = 0, count = transitions.getLength(); i < count; ++i ) {
         final Element tr = (Element)transitions.item( i );
         getRoot().removeChild( tr );
      }
   }

   private void removeTransition( EntityConnectionData ecd ) throws XPathExpressionException {
      getRoot().removeChild( getTransition( ecd ));
   }

   private void renameSelection( String newValue ) {
      final IStructuredSelection selection = graph.getStructuredSelection();
      if( selection.size() == 1 ) {
         final Object selected = selection.getFirstElement();
         if( selected instanceof String ) {
            try {
               final NodeList attrs = getStates((String)selected );
               for( int i = 0, count = attrs.getLength(); i < count; ++i ) {
                  final Attr attr = (Attr)attrs.item( i );
                  attr.setValue( newValue );
               }
            }
            catch( final Throwable t ) {
               t.printStackTrace();
            }
         }
         else if( selected instanceof EntityConnectionData ) {
            final EntityConnectionData ecd = (EntityConnectionData)selected;
            try {
               final Element transition = getTransition( ecd );
               transition.setAttribute( "event", newValue );
            }
            catch( final Throwable t ) {
               t.printStackTrace();
            }
         }
         else {
            System.err.printf( "%s.renameSelection|ss: %s\n", getClass().getName(), selected.getClass().getName());
         }
      }
   }

   private void removeSelection() {
      System.err.printf( "%s.removeSelection\n", getClass().getName());
      final IStructuredSelection selection = graph.getStructuredSelection();
      for( final Iterator<?> it = selection.iterator(); it.hasNext(); ) {
         final Object selected = it.next();
         try {
            if( selected instanceof String ) {
               removeAllTransitionsFrom((String)selected );
            }
            else if( selected instanceof EntityConnectionData ) {
               removeTransition((EntityConnectionData)selected );
            }
            else {
               System.err.printf( "%s.itemSelected|ss: ", getClass().getName(), selected.getClass().getName());
            }
         }
         catch( final Throwable t ) {
            t.printStackTrace();
         }
      }
   }

   private void itemSelected( SelectionChangedEvent event ) {
      final IStructuredSelection ss = event.getStructuredSelection();
      if( ss.size() == 1 ) {
         final Object selected = ss.getFirstElement();
         if( selected instanceof String ) {
            stateOrEventText.setText((String)selected );
         }
         else if( selected instanceof EntityConnectionData ) {
            final EntityConnectionData ecd = (EntityConnectionData)selected;
            final LabelProvider lp = (LabelProvider)graph.getLabelProvider();
            stateOrEventText.setText( lp.getText( ecd ));
         }
         else {
            System.err.printf( "%s.itemSelected|ss: ", getClass().getName(), ss.getClass().getName());
         }
      }
      else {
         stateOrEventText.setText( "" );
      }
   }

   private String createLabel( boolean forState ) {
      String label = stateOrEventText.getText();
      if( label == null || label.isEmpty()) {
         label = ( forState ? "STATE" : "EVENT" ) + ++nodeID;
      }
      stateOrEventText.setText("");
      return label;
   }

   private void createStateOrEvent() {
      final IStructuredSelection selection = graph.getStructuredSelection();
      if( selection.size() != 2 ) {
         createTransition( createLabel( true ), createLabel( false ), createLabel( true ));
      }
      else {
         final Iterator<?> it    = selection.iterator();
         final Object      from  = it.next();
         final Object      futur = it.next();
         createTransition((String)from, null, (String)futur );
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

   private void createGraphView() {
      final ImageRegistry imgReg = Activator.getDefault().getImageRegistry();
      final Composite composite = new Composite(getContainer(), SWT.NONE );
      final GridLayout layout = new GridLayout( 1, false );
      composite.setLayout( layout );
      final ToolBar toolBar = new ToolBar( composite, SWT.HORIZONTAL );
      GridData constraints = new GridData();
      constraints.horizontalAlignment = SWT.LEFT;
      toolBar.setLayoutData( constraints );
      final ToolItem createState = new ToolItem( toolBar, SWT.PUSH );
      createState.addListener( SWT.Selection, e -> createStateOrEvent());
      createState.setToolTipText(
         "Ajoute un nouvel état à cet automate ou, si deux états sont sélectionnés, " +
            "une nouvelle transition entre ces derniers.\n" +
         "Utilise le contenu du champ texte s'il n'est pas vide." );
      createState.setImage( imgReg.get( ICON_KEY_NEW ));
      stateOrEventText = new Text( toolBar, SWT.SINGLE );
      stateOrEventText.setToolTipText(
         "Sélectionnez un état ou un événement puis faites \"entrée\" dans ce champ pour le renommer" );
      stateOrEventText.addTraverseListener( e -> {
         if( e.detail == SWT.TRAVERSE_RETURN ) {
            renameSelection( stateOrEventText.getText());
         }
      });
      final ToolItem sep = new ToolItem( toolBar, SWT.SEPARATOR );
      sep.setWidth( 400 );
      sep.setControl( stateOrEventText );
      final ToolItem removeSelection = new ToolItem ( toolBar, SWT.PUSH );
      removeSelection.addListener( SWT.Selection, e -> removeSelection());
      removeSelection.setImage( imgReg.get( ICON_KEY_DELETE ));
      removeSelection.setToolTipText(
         "Sélectionnez un état ou un événement pour le supprimer" );
      graph = new GraphViewer( composite, SWT.NONE );
      graph.setContentProvider( new GraphContentProvider());
      graph.setLabelProvider( new GraphLabelProvider());
      constraints = new GridData();
      constraints.horizontalAlignment = SWT.FILL;
      constraints.verticalAlignment = SWT.FILL;
      constraints.grabExcessHorizontalSpace = true;
      constraints.grabExcessVerticalSpace = true;
      graph.getControl().setLayoutData( constraints );
      graph.addSelectionChangedListener( this::itemSelected );
      graph.setConnectionStyle( ZestStyles.CONNECTIONS_DIRECTED );
      final int index = addPage( composite );
      setPageText( index, "Graph" );
      final IDOMDocument doc = (IDOMDocument)getRoot().getOwnerDocument();
      final IDOMModel model = doc.getModel();
      model.addModelStateListener( new IModelStateListener() {
         @Override public void modelAboutToBeChanged( IStructuredModel arg0 ) {/**/}
         @Override public void modelAboutToBeReinitialized( IStructuredModel arg0 ) {/**/}
         @Override public void modelChanged( IStructuredModel arg0 ) { refresh(); }
         @Override public void modelDirtyStateChanged( IStructuredModel arg0, boolean arg1 ) {/**/}
         @Override public void modelReinitialized( IStructuredModel arg0 ) {/**/}
         @Override public void modelResourceDeleted( IStructuredModel arg0 ) {/**/}
         @Override public void modelResourceMoved( IStructuredModel arg0, IStructuredModel arg1 ) {/**/}
      });
      graph.setInput( getRoot());
      final LayoutAlgorithm la = new SpringLayoutAlgorithm( LayoutStyles.NO_LAYOUT_NODE_RESIZING );
      graph.setLayoutAlgorithm( la, true );
      Display.getDefault().asyncExec(() -> {
         if( loadNodesLayout()) {
            graph.setLayoutAlgorithm( new NullLayoutAlgorithm(), true );
         }
      });
   }

   @Override
   protected void createPages() {
      createXMLView();
      createGraphView();
      final IWorkbenchPage page = getSite().getWorkbenchWindow().getPages()[0];
      page.addPartListener( new IPartListener() {
         @Override public void partOpened( IWorkbenchPart part ) { /**/ }
         @Override public void partDeactivated( IWorkbenchPart part ) { saveNodesLayout(); }
         @Override public void partClosed( IWorkbenchPart part ) { /**/ }
         @Override public void partBroughtToTop( IWorkbenchPart part ) { /**/ }
         @Override public void partActivated( IWorkbenchPart part ) { /**/ }
      });
   }

   /**
    * The <code>MultiPageEditorPart</code> implementation of this
    * <code>IWorkbenchPart</code> method disposes all nested editors.
    * Subclasses may extend.
    */
   @Override
   public void dispose() {
      ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
      super.dispose();
   }
   /**
    * Saves the multi-page editor's document.
    */
   @Override
   public void doSave(IProgressMonitor monitor) {
      saveNodesLayout();
      getEditor(0).doSave(monitor);
   }
   /**
    * Saves the multi-page editor's document as another file.
    * Also updates the text for page 0's tab, and updates this multi-page editor's input
    * to correspond to the nested editor's.
    */
   @Override
   public void doSaveAs() {
      final IEditorPart editor1 = getEditor(0);
      editor1.doSaveAs();
      setPageText(0, editor1.getTitle());
      setInput(editor1.getEditorInput());
   }
   /* (non-Javadoc)
    * Method declared on IEditorPart
    */
   public void gotoMarker(IMarker marker) {
      setActivePage(0);
      IDE.gotoMarker(getEditor(0), marker);
   }
   /**
    * The <code>MultiPageEditorExample</code> implementation of this method
    * checks that the input is an instance of <code>IFileEditorInput</code>.
    */
   @Override
   public void init(IEditorSite site, IEditorInput editorInput)
      throws PartInitException {
      if (!(editorInput instanceof IFileEditorInput)){
         throw new PartInitException("Invalid Input: Must be IFileEditorInput");
      }
      super.init(site, editorInput);
   }
   /* (non-Javadoc)
    * Method declared on IEditorPart.
    */
   @Override
   public boolean isSaveAsAllowed() {
      return true;
   }
   /**
    * Calculates the contents of page 2 when the it is activated.
    */
   @Override
   protected void pageChange(int newPageIndex) {
      super.pageChange( newPageIndex );
      if( newPageIndex == 1 ) {
         graph.refresh();
      }
   }
   /**
    * Closes all project files on project close.
    */
   @Override
   public void resourceChanged( final IResourceChangeEvent event ) {
      if( event.getType() == IResourceChangeEvent.PRE_CLOSE ) {
         Display.getDefault().asyncExec(() -> {
            final IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
            for (int i = 0; i<pages.length; i++){
               final FileEditorInput fei = (FileEditorInput)xmlEditor.getEditorInput();
               final IProject project = fei.getFile().getProject();
               if( project.equals( event.getResource())) {
                  final IEditorPart editorPart = pages[i].findEditor( xmlEditor.getEditorInput());
                  pages[i].closeEditor( editorPart, true );
               }
            }
         });
      }
   }
}
