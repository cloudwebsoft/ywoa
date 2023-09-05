//计算元素集合的总宽度
function calSumWidth(elements) {
	var width = 0;
	$(elements).each(function () {
		width += $(this).outerWidth(true);
	});
	return width;
}
//滚动到指定选项卡
function scrollToTab(element) {
	var marginLeftVal = calSumWidth($(element).prevAll()), marginRightVal = calSumWidth($(element).nextAll());
	// 可视区域非tab宽度
	var tabOuterWidth = calSumWidth($(".content-tabs").children().not(".J_menuTabs"));
	//可视区域tab宽度
	var visibleWidth = $(".content-tabs").outerWidth(true) - tabOuterWidth;
	//实际滚动宽度
	var scrollVal = 0;
	if ($(".page-tabs-content").outerWidth() < visibleWidth) {
		scrollVal = 0;
	} else if (marginRightVal <= (visibleWidth - $(element).outerWidth(true) - $(element).next().outerWidth(true))) {
		if ((visibleWidth - $(element).next().outerWidth(true)) > marginRightVal) {
			scrollVal = marginLeftVal;
			var tabElement = element;
			while ((scrollVal - $(tabElement).outerWidth()) > ($(".page-tabs-content").outerWidth() - visibleWidth)) {
				scrollVal -= $(tabElement).prev().outerWidth();
				tabElement = $(tabElement).prev();
			}
		}
	} else if (marginLeftVal > (visibleWidth - $(element).outerWidth(true) - $(element).prev().outerWidth(true))) {
		scrollVal = marginLeftVal - $(element).prev().outerWidth(true);
	}
	$('.page-tabs-content').animate({
		marginLeft: 0 - scrollVal + 'px'
	}, "fast");
}

function renewFrameset(iframeObj) {
	// 防止嵌套的frameset当tab切换回来时内容不能显示，如：组织机构
	var frm = $(iframeObj).contents().find("frameset")[0];
    if (frm) {
		var rows = frm.rows;
		if (rows.indexOf(",")!=-1) {
			frm.rows = rows;
		}
		else {
			var cols = frm.cols;
			frm.cols = cols;
		}
	}
	else {
		frm = $(iframeObj).contents().find("iframe")[0];
        if (frm) {
			// 刷新会使得看起来比较奇怪，如：组织管理页面点击部门后，原本看到的是部门中的人员，刷新后回到了页面的初始状态
			// frm.contentWindow.location.reload();
            // IE中iframe中再嵌套iframe，然后再嵌套frameset（），切换tab回来时会不显示，chrome下无此问题
            if (frm.height!="") {
                frm.height = parseInt(frm.height) - 1;
            }
            else {
                console.log("frm.height为空")
            }
        }
	}
}

/**
 * 刷新当前选项卡窗口
 * @return
 */
function refreshTabFrame() {
	$('.J_menuTab').each(function () {		
		if ($(this).hasClass('active')) {
			var dataUrl = $(this).data('id');
			$('.J_mainContent .J_iframe').each(function () {
				if ($(this).data('id') == dataUrl) {
					this.contentWindow.location.reload();
					return false;
				}
			});
		}
	});	
}

function reloadTabFrame(tabName) {
	$('.J_menuTab').each(function () {
		if ($(this).data('name') == tabName) {
			$('.J_mainContent .J_iframe').each(function () {
				if ($(this).data('name') == tabName) {
				    // 如果是module_list.jsp或module_list_relate.jsp这样的页面，其中有doQuery方法
                    var isFound = false;
                    // 如果页面中有frame，则先检测frame页中是否有doQuery
                    $(this.contentWindow.document).find('frame').each(function() {
                        if (typeof(this.contentWindow.doQuery)=="function") {
                            isFound = true;
                            this.contentWindow.doQuery();
                        }
                    });

                    if (!isFound) {
                        if (typeof(this.contentWindow.doQuery)=="function") {
                            this.contentWindow.doQuery();
                        }
                        else {
                            this.contentWindow.location.reload();
                        }
                    }
					return false;
				}
			});
		}
	});	
}

function getTabWin(tabIdOpener) {
    var win;
    $('.J_menuTab').each(function () {
        if ($(this).data('name') == tabIdOpener) {
            $('.J_mainContent .J_iframe').each(function () {
                if ($(this).data('name') == tabIdOpener) {
                    win = this.contentWindow;
                    return false;
                }
            });
        }
    });
    return win;
}

function closeLteTab(tabName) {
    $('.J_menuTab').each(function () {
        if ($(this).data('name') == tabName) {
            $(this).find('i').trigger("click");
            return false;
        }
    });
}

function closeMyTab(tabName) {
    $('.J_menuTab').each(function () {
        if ($(this).html().indexOf(tabName)>=0) {
            $(this).find('i').trigger("click");
            return false;
        }
    });
}

// 取得处于激活状态的选项卡的ID
function getActiveTabName() {
	var tabName = "";
	$('.J_menuTab').each(function () {		
		if ($(this).hasClass('active')) {
			tabName = $(this).data('name');
			return;
		}
	});
	return tabName;
}

function setActiveTabTitle(title) {
    var tabName = "";
    $('.J_menuTab').each(function () {
        if ($(this).hasClass('active')) {
            $(this).html(title + '<i class="fa fa-times-circle"></i>');
            return;
        }
    });
    return tabName;
}

var tabNameIndex = 0;
function showMenuItem(dataUrl, dataIndex, menuName) {
    var flag = true;
	// 选项卡菜单已存在
	$('.J_menuTab').each(function () {
		if ($(this).data('id') == dataUrl) {
			var dataName = $(this).data('name');
			if (!$(this).hasClass('active')) {
				$(this).addClass('active').siblings('.J_menuTab').removeClass('active');
				scrollToTab(this);
				// 显示tab对应的内容区
				$('.J_mainContent .J_iframe').each(function () {
					if ($(this).data('id') == dataUrl) {
						$(this).show().siblings('.J_iframe').hide();
						renewFrameset(this);
						return false;
					}
				});
			}
			else {
				// 如果选项卡是当前选项卡，则直接打开链接
				$('.J_mainContent .J_iframe').each(function () {
					if ($(this).data('name') == dataName) {
						this.contentWindow.location.href = dataUrl;
						return false;
					}
				});
			}
			// console.log("$(this).data('id')=" + $(this).data('id') + " dataUrl=" + $(this).data('id'));
			flag = false;
			return false;
		}
	});

    // 选项卡菜单不存在
	if (flag) {
		tabNameIndex ++;
		
		var str = '<a href="javascript:;" data-name="' + tabNameIndex + '" class="active J_menuTab" data-id="' + dataUrl + '">' + menuName + ' <i class="fa fa-times-circle"></i></a>';
		$('.J_menuTab').removeClass('active');

		// 添加选项卡对应的iframe
		var str1 = '<iframe class="J_iframe" data-name="' + tabNameIndex + '" name="iframe' + dataIndex + '" width="100%" height="100%" src="' + dataUrl + '" frameborder="0" data-id="' + dataUrl + '" seamless></iframe>';
        /*console.log('dataUrl=' + dataUrl);
        if (dataUrl == '/oa/doc_show.jsp?id=5') {
            return;
        }*/
		$('.J_mainContent').find('iframe.J_iframe').hide().parents('.J_mainContent').append(str1);

		//显示loading提示
//            var loading = layer.load();
//
//            $('.J_mainContent iframe:visible').load(function () {
//                //iframe加载完成后隐藏loading提示
//                layer.close(loading);
//            });
		// 添加选项卡
		$('.J_menuTabs .page-tabs-content').append(str);

		// 20200126 注释掉也无妨，如果不注释在chrome中点击文件柜文章型目录，在右侧打开的文章中点击编辑后，再切换回文件柜，目录节点的点击事件会变得无效，仅管仍可以选中目录
		// scrollToTab($('.J_menuTab.active'));
	}	
}

$(function () {
	// 判断是否为轻简型页面
	if (!$('.J_menuTab')[0]) {
		return;
	}
	
    //查看左侧隐藏的选项卡
    function scrollTabLeft() {
        var marginLeftVal = Math.abs(parseInt($('.page-tabs-content').css('margin-left')));
        // 可视区域非tab宽度
        var tabOuterWidth = calSumWidth($(".content-tabs").children().not(".J_menuTabs"));
        //可视区域tab宽度
        var visibleWidth = $(".content-tabs").outerWidth(true) - tabOuterWidth;
        //实际滚动宽度
        var scrollVal = 0;
        if ($(".page-tabs-content").width() < visibleWidth) {
            return false;
        } else {
            var tabElement = $(".J_menuTab:first");
            var offsetVal = 0;
            while ((offsetVal + $(tabElement).outerWidth(true)) <= marginLeftVal) {//找到离当前tab最近的元素
                offsetVal += $(tabElement).outerWidth(true);
                tabElement = $(tabElement).next();
            }
            offsetVal = 0;
            if (calSumWidth($(tabElement).prevAll()) > visibleWidth) {
                while ((offsetVal + $(tabElement).outerWidth(true)) < (visibleWidth) && tabElement.length > 0) {
                    offsetVal += $(tabElement).outerWidth(true);
                    tabElement = $(tabElement).prev();
                }
                scrollVal = calSumWidth($(tabElement).prevAll());
            }
        }
        $('.page-tabs-content').animate({
            marginLeft: 0 - scrollVal + 'px'
        }, "fast");
    }
    //查看右侧隐藏的选项卡
    function scrollTabRight() {
        var marginLeftVal = Math.abs(parseInt($('.page-tabs-content').css('margin-left')));
        // 可视区域非tab宽度
        var tabOuterWidth = calSumWidth($(".content-tabs").children().not(".J_menuTabs"));
        //可视区域tab宽度
        var visibleWidth = $(".content-tabs").outerWidth(true) - tabOuterWidth;
        //实际滚动宽度
        var scrollVal = 0;
        if ($(".page-tabs-content").width() < visibleWidth) {
            return false;
        } else {
            var tabElement = $(".J_menuTab:first");
            var offsetVal = 0;
            while ((offsetVal + $(tabElement).outerWidth(true)) <= marginLeftVal) {//找到离当前tab最近的元素
                offsetVal += $(tabElement).outerWidth(true);
                tabElement = $(tabElement).next();
            }
            offsetVal = 0;
            while ((offsetVal + $(tabElement).outerWidth(true)) < (visibleWidth) && tabElement.length > 0) {
                offsetVal += $(tabElement).outerWidth(true);
                tabElement = $(tabElement).next();
            }
            scrollVal = calSumWidth($(tabElement).prevAll());
            if (scrollVal > 0) {
                $('.page-tabs-content').animate({
                    marginLeft: 0 - scrollVal + 'px'
                }, "fast");
            }
        }
    }

    //通过遍历给菜单项加上data-index属性
    $(".J_menuItem").each(function (index) {
        if (!$(this).attr('data-index')) {
            $(this).attr('data-index', index);
        }
    });

    function menuItem() {
        // 获取标识数据
        var dataUrl = $(this).attr('href'),
            dataIndex = $(this).data('index'),
            menuName = $.trim($(this).text()),
            target = $(this).attr("target");
        if (dataUrl == undefined || $.trim(dataUrl).length == 0)
			return false;
        
		if (menuName=="") {
			// 控制面板
			menuName = $(this).data('name');
		}        

		if (target=="_blank") {
		    window.open(dataUrl);
		    return false;
        }
		else if (target=="_top" || target=="_parent" || target=="_self") {
            window.location.href = dataUrl;
            return false;
        }

		showMenuItem(dataUrl, dataIndex, menuName);
        return false;
    }

    $('.J_menuItem').on('click', menuItem);

    // 关闭选项卡菜单
    function closeTab() {
		var $tab;		
		// 如果是点击了叉号
		if ($(this).parents('.J_menuTab')[0]) {
			$tab = $(this).parents('.J_menuTab');
		}
		else {
			// 如果是双击了选项卡
			$tab = $(this);
		}

        var closeTabId = $tab.data('id');
		var currentWidth = $tab.width();

        // 当前元素处于活动状态
        if ($tab.hasClass('active')) {
            // 当前元素后面有同辈元素，使后面的一个元素处于活动状态
            if ($tab.next('.J_menuTab').size()) {
                var activeId = $tab.next('.J_menuTab:eq(0)').data('id');
                $tab.next('.J_menuTab:eq(0)').addClass('active');

                $('.J_mainContent .J_iframe').each(function () {
                    if ($(this).data('id') == activeId) {
                        $(this).show().siblings('.J_iframe').hide();
						
						// 防止嵌套的frameset当tab切换回来时内容不能显示
						renewFrameset(this);
						
                        return false;
                    }
                });

                var marginLeftVal = parseInt($('.page-tabs-content').css('margin-left'));
                if (marginLeftVal < 0) {
                    $('.page-tabs-content').animate({
                        marginLeft: (marginLeftVal + currentWidth) + 'px'
                    }, "fast");
                }

                //  移除当前选项卡
                $tab.remove();

                // 移除tab对应的内容区
                $('.J_mainContent .J_iframe').each(function () {
                    if ($(this).data('id') == closeTabId) {
                        $(this).remove();
                        return false;
                    }
                });
            }

            // 当前元素后面没有同辈元素，使当前元素的上一个元素处于活动状态
            if ($tab.prev('.J_menuTab').size()) {
                var activeId = $tab.prev('.J_menuTab:last').data('id');
                $tab.prev('.J_menuTab:last').addClass('active');
                $('.J_mainContent .J_iframe').each(function () {
                    if ($(this).data('id') == activeId) {
                        $(this).show().siblings('.J_iframe').hide();
                        
						// 防止嵌套的frameset当tab切换回来时内容不能显示
						renewFrameset(this);
						
                        return false;
                    }
                });

                //  移除当前选项卡
                $tab.remove();

                // 移除tab对应的内容区
                $('.J_mainContent .J_iframe').each(function () {
                    if ($(this).data('id') == closeTabId) {
                        $(this).remove();
                        return false;
                    }
                });
            }
        }
        // 当前元素不处于活动状态
        else {
            //  移除当前选项卡
            $tab.remove();

            // 移除相应tab对应的内容区
            $('.J_mainContent .J_iframe').each(function () {
                if ($(this).data('id') == closeTabId) {
                    $(this).remove();
                    return false;
                }
            });
            scrollToTab($('.J_menuTab.active'));
        }
        return false;
    }

    $('.J_menuTabs').on('click', '.J_menuTab i', closeTab);
    $('.J_menuTabs').on('dblclick', '.J_menuTab', closeTab);

    //关闭其他选项卡
    function closeOtherTabs(){
        $('.page-tabs-content').children("[data-id]").not(":first").not(".active").each(function () {
            $('.J_iframe[data-id="' + $(this).data('id') + '"]').remove();
            $(this).remove();
        });
        $('.page-tabs-content').css("margin-left", "0");
    }
    $('.J_tabCloseOther').on('click', closeOtherTabs);

    //滚动到已激活的选项卡
    function showActiveTab(){
        scrollToTab($('.J_menuTab.active'));
    }
    $('.J_tabShowActive').on('click', showActiveTab);

    // 点击选项卡菜单
    function activeTab() {
        if (!$(this).hasClass('active')) {
            var currentId = $(this).data('id');
            // console.log("$(this).data('id')=" + $(this).data('id') + " dataUrl=" + $(this).data('id'));
            // 显示tab对应的内容区
            $('.J_mainContent .J_iframe').each(function () {
                if ($(this).data('id') == currentId) {
                    $(this).show().siblings('.J_iframe').hide();
                    // console.log('activeTab renewFrameset');
                    // 防止嵌套的frameset当tab切换回来时内容不能显示
					renewFrameset(this);
                    return false;
                }
            });
            $(this).addClass('active').siblings('.J_menuTab').removeClass('active');
            scrollToTab(this);
        }
    }

    $('.J_menuTabs').on('click', '.J_menuTab', activeTab);

    //刷新iframe
    function refreshTab() {
        var target = $('.J_iframe[data-id="' + $(this).data('id') + '"]');
        var url = target.attr('src');
//        //显示loading提示
//        var loading = layer.load();
//        target.attr('src', url).load(function () {
//            //关闭loading提示
//            layer.close(loading);
//        });
    }

    $('.J_menuTabs').on('dblclick', '.J_menuTab', refreshTab);

    // 左移按扭
    $('.J_tabLeft').on('click', scrollTabLeft);

    // 右移按扭
    $('.J_tabRight').on('click', scrollTabRight);

    // 关闭全部
    $('.J_tabCloseAll').on('click', function () {
        $('.page-tabs-content').children("[data-id]").not(":first").each(function () {
            $('.J_iframe[data-id="' + $(this).data('id') + '"]').remove();
            $(this).remove();
        });
        $('.page-tabs-content').children("[data-id]:first").each(function () {
            $('.J_iframe[data-id="' + $(this).data('id') + '"]').show();
            $(this).addClass("active");
        });
        $('.page-tabs-content').css("margin-left", "0");
    });

});
