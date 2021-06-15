$(function () {
    var contextPath = $("#contextPath").val();
    var skinPath = $("#skinPath").val();

    //lzm点赞
    $(".mywork-list-praise").live("click", function () {
        var $this = $(this);
        var id = $(this).attr("id");
        var p_id = ".p_praise_detail_" + id;
        var p_id_name = "p_praise_detail_" + id;
        var praiseCount = parseInt($this.attr("praiseCount"));
        var apraiseType = $(this).attr("apraiseType");

        $.ajax({
            type: "post",
            url: contextPath + '/mywork/apraiseWorkLog',
            data: {
                "apraiseType": apraiseType,
                "workLogId": id
            },
            dataType: "json",
            beforeSend: function (XMLHttpRequest) {
            },
            success: function (data, status) {
                var res = data.res;
                consoleLog(res);
                if (res == 0) {
                    var users = data.praiseUsers;
                    var count = data.praiseCount;
                    var expands_id = "#expands_" + id + " .mywork-list-filleting";
                    consoleLog("praiseCount=" + praiseCount);
                    consoleLog("users=" + users);
                    consoleLog(expands_id);
                    if (praiseCount == 0) {
                        var html = " <p class='" + p_id_name + "'>";
                        html += "<img src='" + skinPath + "/images/mywork/icon_praise_count.png' width='20' height='20'/>";
                        html += '<span class="span_praisecount">赞(' + count + ')</span>';
                        html += '<span class="span_praiseusers">' + users + '</span>';
                        html += "</p>";
                        $(expands_id).prepend($(html));
                    } else if (praiseCount == 1) {
                        if (count > praiseCount) {
                            $(p_id).find(".span_praiseusers").text(users);
                            $(p_id).find(".span_praisecount").text("赞(" + count + ")");
                        } else {
                            $(p_id).remove();
                        }
                    } else {
                        $(p_id).find(".span_praiseusers").text(users);
                        $(p_id).find(".span_praisecount").text("赞(" + count + ")");
                    }

                    if (apraiseType == '0') {
                        $this.attr("apraiseType", "1");
                        $this.css("background-image", "url(" + skinPath + "/images/mywork/icon_praise.png)");
                    } else {
                        $this.attr("apraiseType", "0");
                        $this.css("background-image", "url(" + skinPath + "/images/mywork/icon_praise_selected.png)");
                    }
                    $this.attr("praiseCount", count);
                }
            },
            complete: function (XMLHttpRequest, status) {
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, "");
            }
        });
    })
});

function praiseStatus(curUser, list) {
    var res = false;
    if (list != null && list.length > 0) {
        $.each(list, function (i, n) {
            if (n.name == curUser) {
                res = true;
            }
        })
    }
    return res;
}

function praiseUsers(list) {
    var name = '';
    if (list != null && list.length > 0) {
        $.each(list, function (i, n) {
            if (name == '') {
                name = n.userName;
            } else {
                name += "," + n.userName;
            }
        })
    }
    return name;
}
