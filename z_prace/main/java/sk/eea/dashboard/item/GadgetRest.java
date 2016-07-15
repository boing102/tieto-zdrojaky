package sk.eea.dashboard.item;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.SearchHandler.ClauseRegistration;
import com.atlassian.jira.issue.search.SearchHandler.SearcherRegistration;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.builder.ConditionBuilder;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.Query;
import com.atlassian.query.operator.Operator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Path("/gadget")
@Named
public class GadgetRest {
    private static final Logger LOG = LoggerFactory.getLogger(GadgetRest.class);

    protected final SearchService searchService;
    protected final CustomFieldManager cfManager;
    protected final JiraAuthenticationContext authenticationContext;
    protected final FieldManager fieldManager;
    protected final CustomFieldManager customFieldManager;
    protected final SearchRequestService searchRequestService;

    protected final ApplicationProperties applicationProperties;
    private final CompatibilityWrapper compatibilityWrapper;

    @Inject
    public GadgetRest(
            @ComponentImport SearchService searchService,
            @ComponentImport CustomFieldManager cfManager,
            @ComponentImport JiraAuthenticationContext authenticationContext,
            @ComponentImport FieldManager fieldManager,
            @ComponentImport CustomFieldManager customFieldManager,
            @ComponentImport SearchRequestService searchRequestService,
            @ComponentImport ApplicationProperties applicationProperties,
            @ComponentImport BuildUtilsInfo buildUtilsInfo) {
        this.searchService = searchService;
        this.cfManager = cfManager;
        this.authenticationContext = authenticationContext;
        this.fieldManager = fieldManager;
        this.customFieldManager = customFieldManager;
        this.searchRequestService = searchRequestService;
        this.applicationProperties = applicationProperties;
        this.compatibilityWrapper = new CompatibilityWrapper(buildUtilsInfo);
    }

    // @precondition: string.trim().equals(string)
    protected boolean isParamEmpty(String string) {
        // http://marxsoftware.blogspot.sk/2011/09/checking-for-null-or-empty-or-white.html
        return string == null || string.isEmpty();
    }

    private SearchableField getFieldForId(String id) {
        Field field = (id != null && !id.equals("-1")) ? fieldManager.getField(id) : null;
        if (field instanceof SearchableField) return (SearchableField) field;
        else return null;
    }

    private SearchableField[] getFieldArrayForIds(String id1, String id2, String id3) {
        SearchableField[] sfArray = new SearchableField[3];
        sfArray[0] = getFieldForId(id1);
        sfArray[1] = getFieldForId(id2);
        sfArray[2] = getFieldForId(id3);
        return sfArray;
    }

    /**
     * Returns clause information for constructing query with LIKE or EQUALS.
     * @param field system or custom field
     * @return null if no suitable handler is found
     */
    private ClauseInformation getFieldInformation(SearchableField field) {
        SearchHandler handler;
        if ((handler = field.createAssociatedSearchHandler()) == null) return null;

        SearcherRegistration registration;
        if ((registration = handler.getSearcherRegistration()) == null) return null;

        List<ClauseRegistration> clauseHandlers;
        if ((clauseHandlers = registration.getClauseHandlers()) == null) return null;

        for (ClauseRegistration cr : clauseHandlers) {
            ClauseHandler clauseHandler;
            if ((clauseHandler = cr.getHandler()) == null) continue;

            Set<Operator> operators = clauseHandler.getInformation().getSupportedOperators();
            if (operators.contains(Operator.LIKE) || operators.contains(Operator.EQUALS)) {
                return clauseHandler.getInformation();
            }
        }
        return null;
    }

    /**
     * Returns first suitable operator
     */
    private Operator guessFieldOperator(ClauseInformation clauseInformation) {
        if (clauseInformation != null) {
            Set<Operator> operators = clauseInformation.getSupportedOperators();
            if (operators.contains(Operator.LIKE)) {
                return Operator.LIKE;
            }
            if (operators.contains(Operator.EQUALS)) {
                return Operator.EQUALS;
            }
        }
        return null;
    }

    /**
     * Returns a link to a search result according to the search params.
     * name - name of the custom field (from gadget config);
     * value - value of the custom field (from the gadget form)
     * @param filterid id of chosen filter
     * @param mode logical operator to be used when adding filter results with fields value
     * @param value1 value of the first field
     * @param value2 value of the second field
     * @param value3 value of the third field
     * @param field1 id of first field
     * @param field2 id of second field
     * @param field3 id of third field
     * @return link to search result
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/redirect")
    public Response redirect(
            @QueryParam("filterid") final String filterid,
            @QueryParam("mode") final String mode,
            @QueryParam("value1") final String value1,
            @QueryParam("value2") final String value2,
            @QueryParam("value3") final String value3,
            @QueryParam("field1") final String field1,
            @QueryParam("field2") final String field2,
            @QueryParam("field3") final String field3)
    {
        final int SIZE = 3;
        SearchableField[] fields = getFieldArrayForIds(field1, field2, field3);

        String[] params = new String[SIZE];
        params[0] = parseValue(value1);
        params[1] = parseValue(value2);
        params[2] = parseValue(value3);

        // suffixOr[i] = true <=> !isParamEmpty(paramArray[i]) || .. ||
        // !isParamEmpty(paramArray[SIZE])
        boolean[] suffixOr = new boolean[SIZE + 1];
        suffixOr[SIZE] = false;

        for (int i = SIZE - 1; i >= 0; i--) {
            suffixOr[i] = suffixOr[i + 1] || !isParamEmpty(params[i]);
        }
        //id => "filter-10330"
        final Long parsedFilterId = parseFilterId(filterid);
        SearchRequest filter = searchRequestService.getFilter(
                new JiraServiceContextImpl(compatibilityWrapper.getLoggedInUser(authenticationContext)), parsedFilterId);

        JqlClauseBuilder jqb = JqlQueryBuilder.newBuilder().where();
        boolean needAndSub = false;
        if (filter != null && filter.getQuery().getWhereClause() != null) {
            System.err.println(filter.getQuery().getWhereClause());
            jqb.addClause(filter.getQuery().getWhereClause());
            if (suffixOr[0]) {
                needAndSub = true;
                jqb.and().sub();
            }
        }

        boolean needOperator = false;
        for (int i = 0; i < SIZE; i++) {
            if (isParamEmpty(params[i])) continue;
            ClauseInformation clauseInformation = getFieldInformation(fields[i]);
            if (clauseInformation != null) {
                if (needOperator) jqb = "AND".equals(mode) ? jqb.and() : jqb.or();
                ConditionBuilder cb = jqb.field(clauseInformation.getJqlClauseNames().getPrimaryName());
                Operator operator = guessFieldOperator(clauseInformation);
                if (operator == Operator.LIKE) jqb = cb.like(params[i]);
                else if (operator == Operator.EQUALS) jqb = cb.eq(params[i]);
                needOperator = true;
            }
        }

        if (needAndSub) jqb.endsub();

        Query query = jqb.endWhere().buildQuery();
        String jqlQuery = searchService.getJqlString(query);
        LOG.debug("Calculated query: " + jqlQuery);

        UriBuilder builder = UriBuilder.fromUri(applicationProperties.getString(APKeys.JIRA_BASEURL));
        Collection<Issue> issues;
        try {
            ApplicationUser user = compatibilityWrapper.getLoggedInUser(authenticationContext);
            issues = compatibilityWrapper.search(searchService, user, query, new PagerFilter<Issue>(2)).getIssues();
        } catch (SearchException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        if (issues.size() != 1) {
            builder.path("issues/").queryParam("filter", parsedFilterId).queryParam("jql", jqlQuery);
        } else {
            builder.path("browse").path(issues.iterator().next().getKey());
        }

        String jsonString = null;
        try {
            jsonString = new JSONObject().put("URI", builder.build().toString()).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Response.ok(jsonString).build();
    }

    /**
     * Returns a list of all the searchable fields in JIRA.
     * @return
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/searchfields")
    // @returns available text search fields
    public Response getIssueFieldList() {
        Set<SearchableField> fields = fieldManager.getAllSearchableFields();
        JSONArray array = new JSONArray();
        for (SearchableField sf : fields) {
            if (getFieldInformation(sf) != null) {
                 HashMap<String, String> object = new HashMap<>();
                object.put("label", sf.getName());
                object.put("value", sf.getId());
                array.put(object);
            }
        }
        return Response.ok(array.toString()).build();
    }


    /**
     * Retruns name and operator for given search fields.
     * @param searchField1 type of a search field
     * @param searchField2 type of a search field
     * @param searchField3 type of a search field
     * @return JSON with name and operators for search fields.
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/searchfieldnames")
    public Response getSearchFieldNames(@QueryParam("sf1") final String searchField1,
                                        @QueryParam("sf2") final String searchField2,
                                        @QueryParam("sf3") final String searchField3) {
        SearchableField[] fields = getFieldArrayForIds(searchField1, searchField2, searchField3);
        JSONArray array = new JSONArray();
        for (SearchableField f : fields) {
            array.put((f == null) ? null : f.getName() + ' ' + operatorToCharacter(guessFieldOperator(f)));
        }
        return Response.ok(array.toString()).build();
    }

    private String operatorToCharacter(Operator operator) {
        return (operator != null) ? operator.getDisplayString() : "?";
    }

    private Long parseFilterId(String filterId) {
        if (filterId != null) return Long.valueOf(filterId.startsWith("filter-") ? filterId.substring(7) : filterId);
        else return null;
    }

    /**
     * Returns first suitable operator
     */
    private Operator guessFieldOperator(SearchableField field) {
        return guessFieldOperator(getFieldInformation(field));
    }

    private String parseValue(final String value) {
        System.out.println(value);
        return (value != null) ? value.trim() : "";
    }
}