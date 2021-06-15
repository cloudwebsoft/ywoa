/*
 * jQuery Mobile Framework : plugin to provide a mobile dialog Widget. 
 * Copyright (c) Julian.zhu
 * email: 87036211@qq.com
 */

(function ($, undefined) {
    $.widget("mobile.jqmtree", $.mobile.widget, {
        options: {
            version: "1.0.1-2015081000",
            title:'',
            data: [],
            collapsed: true,
            textTag:'h4'
        },
        _create: function () {
            var self = this,
                o = this.options,
                theme = $.mobile.getInheritedTheme(".ui-active-page");

            self.internalID = new Date().getTime();           
            if (self.element.length > 0) {
                self._makeHtml();
                for (var i = 0; i < self.element.length; i++) {
                    $(self.element[i]).collapsible({
                        collapsed: o.collapsed
                    });
                }
            }
        },
        _makeHtml: function () {
            var self = this,
               o = this.options,
               theme = $.mobile.getInheritedTheme(".ui-active-page");
            if (o.data.length > 0) {
                var html = self._combineHtml();
                for (var i = 0; i < self.element.length; i++) {
                    $(self.element[i]).html(html);
                }
            }
        },
        _combineHtml: function () {
            var self = this,
            o = this.options;
            var html = "<" + o.textTag + ">" + o.title + "</" + o.textTag + ">";
            for (var i = 0; i < o.data.length; i++) {
                if (typeof(o.data[i].pid) == 'undefined')
                    o.data[i].pid = 0;
                var node = o.data[i];
                if (node.pid == 0) {
                    html = html + self._combineNodeHtml(node);
                }
            }
            return html;
        },
        _combineNodeHtml: function (node) {
            var self = this,
            o = this.options;
            var html = '';
            var nodeId = node.id;
            var hasSon = false;
            for (var i = 0; i < o.data.length; i++) {
                if (o.data[i].id == nodeId)
                    continue;
                if (typeof (o.data[i].pid) == 'undefined')
                    o.data[i].pid = 0;
                if (o.data[i].pid == nodeId) {
                    hasSon = true;
                    break;
                }
            }
            if (node.pid == 0 && hasSon) {
                html = html + "<div data-role=\"collapsible\"><" + o.textTag + ">" + node.title + "</" + o.textTag + ">";
                for (var i = 0; i < o.data.length; i++) {
                    if (o.data[i].id == nodeId)
                        continue;
                    if (typeof (o.data[i].pid) == 'undefined')
                        o.data[i].pid = 0;
                    if (o.data[i].pid == nodeId) {
                        html = html + self._combineNodeHtml(o.data[i]);
                    }
                }
                html = html + "</div>";
            }
            else if (node.pid == 0 && !hasSon) {
                html = html + "<a dirCode='" + node.dirCode + "' class=\"ui-btn ui-corner-all\" href=\"javascript:void(0)\">" + node.title + "</a>";
            }
            else if (node.pid != 0 && hasSon) {
                html = html + "<div data-role=\"collapsible\"><" + o.textTag + ">" + node.title + "</" + o.textTag + ">";
                for (var i = 0; i < o.data.length; i++) {
                    if (o.data[i].id == nodeId)
                        continue;
                    if (typeof (o.data[i].pid) == 'undefined')
                        o.data[i].pid = 0;
                    if (o.data[i].pid == nodeId) {
                        html = html + self._combineNodeHtml(o.data[i]);
                    }
                }
                html = html + "</div>";
            }
            else if (node.pid != 0 && !hasSon) {
                html = html + "<a dirCode='" + node.dirCode + "' class=\"ui-btn ui-corner-all\" href=\"javascript:void(0)\">" + node.title + "</a>";
            }

            return html;
        }
    });
})(jQuery);
