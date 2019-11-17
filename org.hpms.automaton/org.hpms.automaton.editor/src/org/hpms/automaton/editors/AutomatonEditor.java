package org.hpms.automaton.editors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
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
import org.eclipse.wst.sse.ui.StructuredTextEditor;

/**
 * An example showing how to create a multi-page editor.
 * This example has 3 pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
public class AutomatonEditor extends MultiPageEditorPart
   implements
      Constants,
      IResourceChangeListener
{
   private StructuredTextEditor xmlEditor;
   final AutomatonEditorGraphPage graphPage = new AutomatonEditorGraphPage();

   public AutomatonEditor() {
      super();
      ResourcesPlugin.getWorkspace().addResourceChangeListener( this );
   }

   void createXMLView() {
      try {
         xmlEditor = new StructuredTextEditor();
         final int index = addPage( xmlEditor, getEditorInput());
         setPageText( index, "XML text" );
      }
      catch( final PartInitException e ) {
         ErrorDialog.openError( getSite().getShell(), "Error creating nested text editor", null, e.getStatus());
      }
   }

   @Override
   protected void createPages() {
      createXMLView();
      final int index = addPage( graphPage.create( getContainer(), xmlEditor ));
      setPageText( index, "Automaton graph" );
      final IWorkbenchPage page = getSite().getWorkbenchWindow().getPages()[0];
      page.addPartListener( new IPartListener() {
         @Override public void partOpened( IWorkbenchPart part ) { /**/ }
         @Override public void partDeactivated( IWorkbenchPart part ) { graphPage.saveNodesLayout(); }
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
      graphPage.saveNodesLayout();
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
         graphPage.refresh();
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
