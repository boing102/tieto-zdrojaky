package sk.stuba.fiit.reputator.plugin.ui;

import com.google.common.base.Preconditions;

/**
 * Logika triedy prevzdana z https://github.com/perconik/perconik
 *
 */
final class SearchPageOptions {
	static final SearchPageOptions initial = new SearchPageOptions("", "", false);
	
	final String query;
	final String filePath;
	final boolean ignoreCase;
	
	
	private SearchPageOptions(final String query, final String filePath, final boolean ignoreCase) {
		this.query  = Preconditions.checkNotNull(query);
		this.filePath = Preconditions.checkNotNull(filePath);
		this.ignoreCase = Preconditions.checkNotNull(ignoreCase);
	}
	
	static final SearchPageOptions of() {
		return initial;
	}
	
	static final SearchPageOptions of(final String query, final String filePath, final boolean ignoreCase) {
		if (query.isEmpty()) {
			return of();
		}
		
		return new SearchPageOptions(query , filePath, ignoreCase);
	}
}
