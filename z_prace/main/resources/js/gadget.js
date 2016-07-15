define("search-fields-DI/jira-filter-dashboard-item", [
    'underscore',
    'jquery',
    'jira-dashboard-items/components/filter-picker',
    'search-fields-DI/components/message'
], function (
    _,
    $,
    FilterPicker,
    Message
) {
    var DashboardItem = function (API) {
        this.API = API;
    };
    /**
     * Called to render the view for a fully configured dashboard item.
     *
     * @param context The surrounding <div/> context that this items should render into.
     * @param preferences The user preferences saved for this dashboard item (e.g. filter id, number of results...)
     */
    DashboardItem.prototype.render = function (context, preferences) {
        var self = this;
        var displayPrefs = $.extend({}, preferences);
        var $element = this.$element = $(context);
        var labelsArr = [];

        $.ajax({
           type: "GET",
           url: AJS.contextPath() + "/rest/eea-dashboard-item-search/1.0/gadget/searchfieldnames",
           data: {
               sf1: displayPrefs.searchField1,
               sf2: displayPrefs.searchField2,
               sf3: displayPrefs.searchField3
           },
           success: _.bind(function(data) {
               callback(self, data);
           }, this),
           error: _.bind(function(data) {
               self.API.showLoadingBar();
               self.messageModule = new Message();
               self.messageModule.error(AJS.I18n.getText("sk.eea.jira.dashoard.item.search.fields.server.error"));
           }, this)
       });

        function callback(self, labelsArr) {

            //Init result view of the gadget
            context.empty().html(Plugin.Item.Filter.view(
                {
                    labels: labelsArr
                }));
            var $form = this.$form = $element.find("#searchform");
            self.API.resize();


            //Event handlers
            $form.submit(_.bind(function (e) {
                e.preventDefault();
                self.API.showLoadingBar();

                $.ajax({
                           type: "GET",
                           url: AJS.contextPath()
                                + "/rest/eea-dashboard-item-search/1.0/gadget/redirect",
                           data: {
                               filterid: displayPrefs.filterId,
                               mode: displayPrefs.mode,
                               value1: $("#text-field-0").val(),
                               value2: $("#text-field-1").val(),
                               value3: $("#text-field-2").val(),
                               field1: displayPrefs.searchField1,
                               field2: displayPrefs.searchField2,
                               field3: displayPrefs.searchField3,
                           },
                           success: _.bind(function (data) {
                               window.location.replace(data.URI);
                           }, this),
                           error: _.bind(function (data) {
                               self.API.showLoadingBar();
                               self.messageModule = new Message();
                               self.messageModule.error(AJS.I18n.getText(
                                   "sk.eea.jira.dashoard.item.search.fields.server.error"));
                           }, this)
                       });
            }, this));
        }
    };
    /**
     * Called to render the configuration form for this dashboard item if preferences.isConfigured
     * has not been set yet.
     *
     * @param context The surrounding <div/> context that this items should render into.
     * @param preferences The user preferences saved for this dashboard item
     */
    DashboardItem.prototype.renderEdit = function (context, preferences) {
        var self = this;
        var prefix = self.API.getGadgetId() + "-";

        $.ajax({
            type: "GET",
            url: AJS.contextPath() + "/rest/eea-dashboard-item-search/1.0/gadget/searchfields",
            success: _.bind(function(data) {
                callback(self, data);
            }, this),
            error: _.bind(function(data) {
                this.API.showLoadingBar();
                self.messageModule = new Message();
                self.messageModule.error(AJS.I18n.getText("sk.eea.jira.dashoard.item.search.fields.server.error"));
            }, this)
        });

        function callback(self, autocompleteOptions) {

            //Init config view of the gadget
            context.empty().html(Plugin.Item.Filter.Config(
                {
                    preferences: {
                        isConfigured: preferences.isConfigured
                    },
                    options: autocompleteOptions,
                    modeOptions: [
                        {
                            label: AJS.I18n.getText(
                                'sk.eea.jira.dashoard.item.search.fields.mode.or'),
                            value: 'OR'
                        },
                        {
                            label: AJS.I18n.getText(
                                'sk.eea.jira.dashoard.item.search.fields.mode.and'), value: 'AND'
                        }
                    ],
                    selectedValues: {
                        mode: preferences.mode,
                        first: preferences.searchField1,
                        second: preferences.searchField2,
                        third: preferences.searchField3
                    }
                }));
            $("#first-search-field").auiSelect2();
            $("#second-search-field").auiSelect2();
            $("#third-search-field").auiSelect2();
            $("#mode-field").auiSelect2();

            var form = context.find("form");

            form.find("fieldset:first").prepend(JIRA.DashboardItem.Common.Config.Templates
                                                    .filterPicker({
                                                                      prefix: prefix,
                                                                      id: "saved-filter"
                                                                  }));
            //Instrument the filter picker
            self.filterPicker = new FilterPicker().init(
                {
                    errorContainer: context.find(".dashboard-item-error"),
                    element: form.find("#" + prefix + "saved-filter"),
                    selectedValue: preferences.filterId !== undefined
                        ? preferences.filterId.replace('filter-', '') : preferences.filterId,
                    parentElement: form
                });

            self.API.resize();

            //Event handlers
            form.on("submit", _.bind(function (e) {
                e.preventDefault();

                var form = $(e.target);
                onSave({
                           mode: $("#mode-field").val(),
                           searchField1: $("#first-search-field").val(),
                           searchField2: $("#second-search-field").val(),
                           searchField3: $("#third-search-field").val()
                       });
            }, this));

            form.find(".buttons-container .cancel").on("click", _.bind(function () {
                this.API.closeEdit.bind(this.API);
            }, this));

            function onSave(prefs) {
                var validFilter = self.filterPicker.validate();
                if (validFilter) {
                    var selectedFilter = self.filterPicker.getValue();
                    prefs.filterId = selectedFilter.id;
                    self.API.savePreferences(prefs);
                }
                else {
                    self.API.resize();
                }
            }
        }
    };
    return DashboardItem;
});
