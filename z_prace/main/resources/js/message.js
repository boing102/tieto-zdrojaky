define("search-fields-DI/components/message", [], function () {
    'use strict';

    return function(containerElementId, parentNode) {

        if (typeof containerElementId == "undefined") {
            containerElementId = "gadget-message-container";
        }

        var containerElementSelector = "#" + containerElementId;

        if (typeof parentNode == "undefined") {
            parentNode = document.body;
        }

        if (document.getElementById(containerElementId) == null) {
            var cont = document.createElement("div");
            cont.setAttribute("id", containerElementId);
            parentNode.appendChild(cont);
        }

        return {
            // white/blue message with "i" icon
            generic: function generic(text) {
                AJS.messages.generic(containerElementSelector, {body: text, fadeout: true});
            },

            // red message with "!" icon
            error: function error(text) {
                AJS.messages.error(containerElementSelector, {body: text, fadeout: false});
            },

            // green message with "v" icon
            success: function success(text) {
                AJS.messages.success(containerElementSelector, {body: text, fadeout: true});
            },

            // orange message with "!" icon
            warning: function warning(text) {
                AJS.messages.warning(containerElementSelector, {body: text, fadeout: true});
            }
        }
    };
});
