package sk.stuba.fiit.reputator.plugin.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;

@SuppressWarnings("restriction")
public final class SearchCommandHandler extends AbstractHandler
{
	@Override
	public final Object execute(ExecutionEvent event) throws ExecutionException
	{
		IWorkbenchWindow window = org.eclipse.search.internal.ui.SearchPlugin.getActiveWorkbenchWindow();
		new org.eclipse.search.internal.ui.SearchDialog(window, SearchPage.SEARCH_PAGE_ID).open();

		return null;
	}
}
