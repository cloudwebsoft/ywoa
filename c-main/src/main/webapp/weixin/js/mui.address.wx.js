(function ($, window, document, undefined) {
    var w = window;
    var d = document;
    var self;
    // var ADDRESS_AJAX_URL = "../../public/address/list.do";
    var ADDRESS_AJAX_URL = "../../public/android/user/listAddress.do";
    $.Address = $.Class.extend({
        init: function (element, options) {
            this.element = element,
                this.default = {
                    "formSelector": ".mui-input-group",
                    "ulSelector": ".mui-table-view"
                }
            this.options = $.extend(true, this.default, options);
        },
        addrInit: function () {  //选择用户初始化
            var self = this;
            var list = self.element;
            var ajax_param = {"skey":self.options.skey, "type":self.options.type};
            $.get(ADDRESS_AJAX_URL, ajax_param, function (data) {
                var res = data.res;
                if (res == 0) {
                    var arr = data.datas;
                    $.each(arr, function (index, item) {
                        var isGroup = item.isGroup;
                        var pyName = item.pyName;
                        var name = item.name;
                        var li = '';
                        if (isGroup) {
                            li += '<li data-group="' + pyName + '" class="mui-table-view-divider mui-indexed-list-group">' + name + '</li>';
                        } else {
                            var person = item.user.person;
                            var mobile = item.user.mobile;
                            var gender = item.user.gender;
                            var imgSrc = gender == '1' ? "../images/user_46_03.png" : "../images/user_46_01.png";

                            li += '<li data-tags=' + pyName + ' class="mui-table-view-cell mui-indexed-list-item mui-left">';
                            li += '<span addrId=' + item.user.id + '><img class="mui-media-object mui-pull-left" src="' + imgSrc + '"></span>';
                            li += '<div class="mui-media-body">';
                            li += '<span addrId=' + item.user.id + '>' + person + '</span>';
                            li += "<p class='mui-ellipsis'>";
                            li += "<span addrId='" + item.user.id + "' class='mui-pull-left'>" + mobile + "</span>";
                            if (mobile != "") {
                                li += "<a href='tel:" + item.user.mobile + "' class='mui-icon mui-icon-phone mui-pull-right' style='font-size:25px'></a>";
                            }
                            li += "</p>";
                            li += "<p class='mui-ellipsis'>";
                            li += '<span addrId=' + item.user.id + '>' + item.user.email + '</span>';
                            li += "</p>";
                            li += "<p class='mui-ellipsis'>";
                            li += '<span addrId=' + item.user.id + '>' + item.user.company + '</span>';
                            li += "</p>";
                            li += '</div>';
                            li += '</li>';
                        }
                        jQuery(".mui-table-view").append(li);
                    });

                    window.indexedList = new mui.IndexedList(list);
                    self.bindEvent();
                }
            }, "json");
        },
        bindEvent: function () {
            mui('.mui-indexed-list-item').on('tap', 'span', function() {
                window.location.href = "address_show.jsp?id=" + jQuery(this).attr('addrId') + "&isUniWebview=" + self.options.isUniWebview;
            });
        }
    })
})(mui, document, window)
