package sk.eea.dashboard.item;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import sk.eea.jira7.compatibility.api.CompatibilityAdapter;
import sk.eea.jira7.compatibility.impl.CompatibilityAdapter6x;
import sk.eea.jira7.compatibility.impl.CompatibilityAdapter7x;

public class CompatibilityWrapper {
    private final CompatibilityAdapter adapter;

    public CompatibilityWrapper(BuildUtilsInfo buildUtilsInfo) {
        this.adapter = buildUtilsInfo.getVersionNumbers()[0] < 7
                ? new CompatibilityAdapter6x()
                : new CompatibilityAdapter7x();
    }

    public ApplicationUser getLoggedInUser(JiraAuthenticationContext jiraAuthenticationContext) {
        return this.adapter.getLoggedInUser(jiraAuthenticationContext);
    }

    public SearchResults search(SearchService searchService, ApplicationUser user, Query query, PagerFilter pagerFilter)
            throws SearchException {
        return this.adapter.search(searchService, user, query, pagerFilter);

    }
}