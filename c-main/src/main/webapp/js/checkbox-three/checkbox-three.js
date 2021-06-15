/**
 * 置属性为status="three"
 * value 0未选中 1选中 2默认选中
 * <i><input type="checkbox" name="w" value="1" status="three"></i>
 */
;(function ($) {
    var defaults = {
        // 默认为三态，否则为两态
        mode : 3
    };

    $.extend({
        checkboxThree: function (options) {
            this.settings = $.extend({}, defaults, options);
            var self = this;

            function check(el, cl) {
                clearCheckboxClass(el);
                $(el).parent('i').addClass(cl);
            }

            function clearCheckboxClass(obj) {
                $(obj).parent('i').removeClass('checkbox-0');
                $(obj).parent('i').removeClass('checkbox-1');
                $(obj).parent('i').removeClass('checkbox-2');
            }

            $('input[type="checkbox"][status="three"]').on('click', function () {
                var val = $(this).val();
                if (self.settings.mode==3) {
                    if (val == 0) {
                        check(this, 'checkbox-1');
                        $(this).val(1);
                    } else if (val == 1) {
                        check(this, 'checkbox-2');
                        $(this).val(2);
                    } else {
                        check(this, 'checkbox-0');
                        $(this).val(0);
                    }
                }
                else {
                    if (val == 0) {
                        check(this, 'checkbox-1');
                        $(this).val(1);
                    } else if (val == 1) {
                        check(this, 'checkbox-0');
                        $(this).val(0);
                    }
                }

                if (self.settings.check) {
                    self.settings.check(this);
                }
            });

            $('input[type="checkbox"][status="three"]').each(function () {
                clearCheckboxClass(this);
                $(this).parent('i').addClass('checkbox-' + $(this).val());
                $(this).parent('i').addClass('checkbox-bg');
            });
        }
    })
})(jQuery);

/*
$(function () {
    $.checkboxThree();
});*/
