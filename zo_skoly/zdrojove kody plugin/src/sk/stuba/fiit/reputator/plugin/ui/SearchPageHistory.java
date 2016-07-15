package sk.stuba.fiit.reputator.plugin.ui;

import java.util.Deque;
import java.util.Iterator;
import com.google.common.collect.Lists;

/**
 * Logika triedy prevzdana z https://github.com/perconik/perconik
 *
 */
final class SearchPageHistory implements Iterable<SearchPageOptions> {
	private static final int HISTORY_LIMIT = 10;
		
	private final Deque<SearchPageOptions> queue;
	
	private final int limit;
	
	private SearchPageHistory() {
		this.queue = Lists.newLinkedList();
		this.limit = HISTORY_LIMIT;
		
		this.queue.add(SearchPageOptions.initial);
	}
	
	static final SearchPageHistory create() {
		return new SearchPageHistory();
	}
	
	final void store(final String query, final String filePath, final boolean ignoreCase) {
		if (this.queue.size() > (this.limit + 1)) {
			this.queue.removeLast();
		}
		
		if (!query.isEmpty()) {
			SearchPageOptions options = SearchPageOptions.of(query, filePath, ignoreCase);
			
			for (SearchPageOptions other: this.queue) {
				if (query.equals(other.query)) {
					this.queue.remove(other);
					
					break;
				}
			}
			
			this.queue.addFirst(options);
		}
	}
	
	final void store(final SearchPageOptions options) {
		this.store(options.query, options.filePath, options.ignoreCase);
	}

	public final Iterator<SearchPageOptions> iterator() {
		return this.queue.iterator();
	}

	final SearchPageOptions getOptions() {
		return this.queue.getFirst();
	}

	final String[] getQueries() {
		String[] queries = new String[this.queue.size() - 1];
		
		Iterator<SearchPageOptions> iterator = this.queue.iterator();
	
		for (int i = 0; i < queries.length; i ++) {
			queries[i] = iterator.next().query;
		}
	
		return queries;
	}
	
	final String[] getFilePaths() {
		String[] filePaths = new String[this.queue.size() - 1];
		
		Iterator<SearchPageOptions> iterator = this.queue.iterator();
	
		for (int i = 0; i < filePaths.length; i ++) {
			filePaths[i] = iterator.next().filePath;
		}
	
		return filePaths;
	}
}
