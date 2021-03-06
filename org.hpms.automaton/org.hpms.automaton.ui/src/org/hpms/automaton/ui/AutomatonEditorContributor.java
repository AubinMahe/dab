package org.hpms.automaton.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * Manages the installation/deinstallation of global actions for multi-page editors.
 * Responsible for the redirection of global actions to the active editor.
 * Multi-page contributor replaces the contributors for the individual editors in the multi-page editor.
 */
public class AutomatonEditorContributor extends MultiPageEditorActionBarContributor {

	private IEditorPart activeEditorPart;
	private Action      sampleAction;

	/**
	 * Creates a multi-page contributor.
	 */
	public AutomatonEditorContributor() {
		super();
		createActions();
	}

	/**
	 * Returns the action registed with the given text editor.
	 * @return IAction or null if editor is null.
	 */
	@SuppressWarnings("static-method")
   protected IAction getAction(ITextEditor editor, String actionID) {
		return ( editor == null ) ? null : editor.getAction( actionID );
	}

	/* (non-JavaDoc)
	 * Method declared in AbstractMultiPageEditorActionBarContributor.
	 */
	@Override
   public void setActivePage(IEditorPart part) {
		if( activeEditorPart == part ) {
		   return;
		}
		activeEditorPart = part;
		final IActionBars actionBars = getActionBars();
		if( actionBars != null ) {
		   if( part instanceof ITextEditor ) {
   			final ITextEditor editor = (ITextEditor) part;
   			actionBars.setGlobalActionHandler( ActionFactory.DELETE.getId(),
   				getAction(editor, ITextEditorActionConstants.DELETE));
   			actionBars.setGlobalActionHandler( ActionFactory.UNDO.getId(),
   				getAction(editor, ITextEditorActionConstants.UNDO));
   			actionBars.setGlobalActionHandler( ActionFactory.REDO.getId(),
   				getAction(editor, ITextEditorActionConstants.REDO));
   			actionBars.setGlobalActionHandler( ActionFactory.CUT.getId(),
   				getAction(editor, ITextEditorActionConstants.CUT));
   			actionBars.setGlobalActionHandler( ActionFactory.COPY.getId(),
   				getAction(editor, ITextEditorActionConstants.COPY));
   			actionBars.setGlobalActionHandler( ActionFactory.PASTE.getId(),
   				getAction(editor, ITextEditorActionConstants.PASTE));
   			actionBars.setGlobalActionHandler( ActionFactory.SELECT_ALL.getId(),
   				getAction(editor, ITextEditorActionConstants.SELECT_ALL));
   			actionBars.setGlobalActionHandler( ActionFactory.FIND.getId(),
   				getAction(editor, ITextEditorActionConstants.FIND));
   			actionBars.setGlobalActionHandler( IDEActionFactory.BOOKMARK.getId(),
   				getAction(editor, IDEActionFactory.BOOKMARK.getId()));
   			actionBars.updateActionBars();
		   }
		}
	}

	private void createActions() {
		sampleAction = new Action() {
			@Override
         public void run() {
				MessageDialog.openInformation(null, "Zest", "Sample Action Executed");
			}
		};
		sampleAction.setText("Sample Action");
		sampleAction.setToolTipText("Sample Action tool tip");
		sampleAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(IDE.SharedImages.IMG_OBJS_TASK_TSK));
	}
	@Override
   public void contributeToMenu(IMenuManager manager) {
		final IMenuManager menu = new MenuManager("Editor &Menu");
		manager.prependToGroup(IWorkbenchActionConstants.MB_ADDITIONS, menu);
		menu.add(sampleAction);
	}
	@Override
   public void contributeToToolBar(IToolBarManager manager) {
		manager.add(new Separator());
		manager.add(sampleAction);
	}
}
