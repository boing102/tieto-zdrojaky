package sk.stuba.fiit.reputator.plugin;

import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.SearchResultEvent;

import sk.stuba.fiit.reputator.plugin.model.FileInfoBean;

public class MySearchResultEvent extends SearchResultEvent{

	private static final long serialVersionUID = -7214704041694129741L;
	private final FileInfoBean fTreeResult;
	
	public MySearchResultEvent(ISearchResult searchResult, FileInfoBean fTreeResult) {
		super(searchResult);
		this.fTreeResult = fTreeResult;
	}

	public FileInfoBean getfTreeResult() {
		return fTreeResult;
	}

}
