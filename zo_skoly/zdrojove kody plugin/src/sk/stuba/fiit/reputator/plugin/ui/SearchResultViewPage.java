package sk.stuba.fiit.reputator.plugin.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;

import sk.stuba.fiit.reputator.plugin.MySearchResultEvent;
import sk.stuba.fiit.reputator.plugin.model.FileInfoBean;

public class SearchResultViewPage implements ISearchResultPage, ISearchResultListener {
	private String fId;  
	private Composite fRootControl;  
	private IPageSite fSite;
	private Tree ftree;

	@Override
	public void searchResultChanged(SearchResultEvent event) {
		if (event instanceof MySearchResultEvent) {  
			Display.getDefault().asyncExec(new Runnable() {  
				@Override  
			    public void run() {
					
					
					FileInfoBean fib = ((MySearchResultEvent) event).getfTreeResult();
					
					TreeItem rootItem = new TreeItem(ftree, 0);
					rootItem.setText(fib.getFileInfo());
					rootItem.setData(fib.getFilePath());
					
					fib.getLines().forEach(v -> {
						TreeItem child = new TreeItem(rootItem, 0);
						child.setText((String) v);
					});
			    }  
			});  
		}
	}

	@Override
	public IPageSite getSite() {
		return fSite;
	}

	@Override
	public void init(IPageSite site) throws PartInitException {
		fSite = site;
	}

	@Override
	public void createControl(Composite parent) {
		fRootControl = new Composite(parent, SWT.NULL);  
		fRootControl.setLayout(new FillLayout(SWT.HORIZONTAL));  
		 
		ftree = new Tree(fRootControl, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		ftree.addListener(SWT.Selection, new Listener() {
		   public void handleEvent(Event e) {		    	
		   		TreeItem[] selection = ftree.getSelection();
		   		if(selection[0].getData() == null) {
		   			return;
		   		}
		   		IPath path = new Path(selection[0].getData().toString());
		   		try{
					IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
					
					if(file.exists()) {
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						IWorkbenchPage page = window.getActivePage();
						
						try {
							IDE.openEditor(page, file, true);
						} catch (PartInitException e1) {
							e1.printStackTrace();
						}
					} else {
					    createMsgBox(parent, "This file is not present in the workspace.");
					}
		   		} catch(IllegalArgumentException | NullPointerException e1) {
		   			createMsgBox(parent, "This file was not found in the workspace.");
		   		}
		    }
		 });	
	}

	@Override
	public void dispose() {
	}

	@Override
	public Control getControl() {
		return fRootControl;
	}

	@Override
	public void setActionBars(IActionBars actionBars) {
	}

	@Override
	public void setFocus() {
		fRootControl.setFocus();
	}

	@Override
	public Object getUIState() {
		return null;
	}

	@Override
	public void setInput(ISearchResult search, Object uiState) {
		if(search != null) {
			search.addListener(this);
		}
		ftree.removeAll();
	}

	@Override
	public void setViewPart(ISearchResultViewPart part) {
	}

	@Override
	public void restoreState(IMemento memento) {
	}

	@Override
	public void saveState(IMemento memento) {
	}

	@Override
	public void setID(String id) {
		fId = id;
		
	}

	@Override
	public String getID() {
		return fId;
	}

	@Override
	public String getLabel() {
		return "Reputator Search Results";
	}
	
	private void createMsgBox(Composite parent, String text) {
		MessageBox messageDialog = new MessageBox(parent.getShell(), SWT.ERROR);
	    messageDialog.setText("Error opening a file");
	    messageDialog.setMessage(text);
	    messageDialog.open();
	}

}
