package sk.stuba.fiit.reputator.plugin.ui;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import sk.stuba.fiit.reputator.plugin.ui.SearchPageHistory;
import sk.stuba.fiit.reputator.plugin.ui.SearchPageOptions;
import sk.stuba.fiit.reputator.plugin.SearchQuery;
import sk.stuba.fiit.reputator.plugin.ui.SearchPage;


public final class SearchPage extends DialogPage implements ISearchPage {
	
	static final String SEARCH_PAGE_ID = "sk.stuba.fiit.reputator.plugin.search.view";
	final SearchPageHistory history;
	ISearchPageContainer container;
	Combo query;
	Combo filePath;
	Button ignoreCase;
	
	public SearchPage() {
		this.history = getSharedHistory();
	}

	@Override
	public void createControl(Composite parent) {
		this.initializeDialogUnits(parent);

		Composite  result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, true);
		
		layout.horizontalSpacing = 10;
		
		result.setLayout(layout);

		Control query = this.createQueryControl(result);
		query.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));

		Label separator = new Label(result, SWT.NONE);
		separator.setVisible(false);
		
		GridData data = new GridData(GridData.FILL, GridData.FILL, false, false, 2, 1);
		data.heightHint = this.convertHeightInCharsToPixels(1) / 3;
		separator.setLayoutData(data);
		
		Control filePath = this.createFilePathControl(result);
		filePath.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));

		Control fields = this.createButtonsControl(result);
		fields.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));

		this.setControl(result);
		Dialog.applyDialogFont(result);
		this.loadOptions(this.history.getOptions());
		this.query.setFocus();
		
	}

	@Override
	public boolean performAction() {
		if (!this.validate()) {
			MessageDialog.openInformation(this.getShell(), "Reputator Search", "Query is not set, unable to perform search.");
			
			return false;
		}
		
		String query  = this.getQuery();
		String filePath = this.getFilePath();
		boolean ignoreCase = this.ignoreCase.getSelection();
		
		this.storeOptions(query, filePath, ignoreCase);
		
		SearchQuery searchQuery = new SearchQuery(query, filePath, ignoreCase);  
		NewSearchUI.runQueryInBackground(searchQuery);  
		
		return true; 
	}
	
	void storeOptions(String query, String filePath, boolean ignoreCase) {
		this.history.store(query, filePath, ignoreCase);
	}
	
	private final Control createQueryControl(final Composite parent) {
		Composite  result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		
		layout.marginWidth  = 0;
		layout.marginHeight = 0;
		
		result.setLayout(layout);

		Label label = new Label(result, SWT.LEFT);
		
		label.setText("Query:");
		label.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false, 2, 1));

		this.query = new Combo(result, SWT.SINGLE | SWT.BORDER);
		
		QueryListener listener = new QueryListener();
		
		this.query.addModifyListener(listener);
		this.query.addSelectionListener(listener);
		
		GridData data = new GridData(GridData.FILL, GridData.FILL, true, false, 1, 1);
		
		data.widthHint = convertWidthInCharsToPixels(50);
		
		this.query.setLayoutData(data);

		return result;
	}
	
	private final Control createFilePathControl(final Composite parent) {
		Composite  result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		
		layout.marginWidth  = 0;
		layout.marginHeight = 0;
		
		result.setLayout(layout);

		Label label = new Label(result, SWT.LEFT);
		
		label.setText("File path:");
		label.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false, 2, 1));

		this.filePath = new Combo(result, SWT.SINGLE | SWT.BORDER);
		
		QueryListener listener = new QueryListener();
		
		this.filePath.addModifyListener(listener);
		this.filePath.addSelectionListener(listener);
		
		GridData data = new GridData(GridData.FILL, GridData.FILL, true, false, 1, 1);
		
		data.widthHint = convertWidthInCharsToPixels(50);
		
		this.filePath.setLayoutData(data);

		return result;
	}
	
	private final Control createButtonsControl(final Composite parent) {
		Group result = new Group(parent, SWT.NONE);
		
		result.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		result.setText("Options");
		result.setLayout(new GridLayout(2, false));
		
		this.ignoreCase = createFieldButton(result, "Ignore Case");


		return result;
	}
	
	private static final Button createFieldButton(final Composite parent, String text) {
		Button button = new Button(parent, SWT.CHECK);
		
		button.setText(text);
		button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		return button;
	}
	
	final class QueryListener implements ModifyListener, SelectionListener {
		QueryListener() {
		}
		
		public final void modifyText(final ModifyEvent e) {
			SearchPage.this.validate();
		}

		public final void widgetSelected(final SelectionEvent e) {
			SearchPage.this.loadOptions(SearchPage.this.getSelectedOptions());
			
			SearchPage.this.validate();
		}

		public final void widgetDefaultSelected(final SelectionEvent e) {
			this.widgetSelected(e);
		}
	}
	
	final boolean validate() {
		boolean enabled = this.hasQuery();
		
		this.container.setPerformActionEnabled(enabled);
		
		return enabled;
	}
	
	public final boolean hasQuery() {
		return !this.getQuery().isEmpty();
	}
	
	final SearchPageOptions getSelectedOptions() {
		String query = this.query.getItem(this.query.getSelectionIndex());
		
		for (SearchPageOptions options: this.history) {
			if (query.equals(options.query)) {
				return options;
			}
		}
		
		throw new IllegalStateException();
	}
	
	public final String getQuery() {
		return this.query.getText();
	}
	
	public final String getFilePath() {
		return this.filePath.getText().isEmpty() ? "" : this.filePath.getText();
	}
	
	public static final class SharedHistory {
		static final SearchPageHistory history = SearchPageHistory.create(); 
	}
	
	static final SearchPageHistory getSharedHistory() {
		return SharedHistory.history;
	}
	
	final void loadOptions(final SearchPageOptions options) {
		this.query.setItems(this.history.getQueries());

		this.setQuery(options.query);
		
		this.filePath.setItems(this.history.getFilePaths());
		
		this.setFilePath(options.filePath);
	}
	
	public final void setQuery(final String query) {
		this.query.setText(query);
		
		this.validate();
	}
	
	public final void setFilePath(final String filePath) {
		this.filePath.setText(filePath);
	}

	@Override
	public final void setContainer(final ISearchPageContainer container) {
		this.container = container;
	}

}
