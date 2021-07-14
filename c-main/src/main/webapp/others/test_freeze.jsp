<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>new document</title>
    <script type="text/javascript"
            src="http://code.jquery.com/jquery-1.6.1.min.js"></script>

    <script type="text/javascript">
        function FixTable(TableID, FixColumnNumber, width, height) {

            $("#" + TableID).after("<div id='" + TableID + "_tableLayout' style='overflow:hidden;height:" + height + "px; width:" + width + "px;'></div>");

            $('<div id="' + TableID + '_tableColumn"></div>' + '<div id="' + TableID + '_tableData"></div>').appendTo("#" + TableID + "_tableLayout");
            var oldtable = $("#" + TableID);
            var tableColumnClone = oldtable.clone(true);
            tableColumnClone.attr("id", TableID + "_tableColumnClone");
            $("#" + TableID + "_tableColumn").append(tableColumnClone);
            $("#" + TableID + "_tableData").append(oldtable);

            var ColumnsWidth = 0;
            var ColumnsNumber = 0;
            $("#" + TableID + "_tableColumn tr:last td:lt(" + FixColumnNumber + ")").each(function () {
                ColumnsWidth += $(this).outerWidth(true);
                ColumnsNumber++;
            });
            ColumnsWidth += 2;
            if ($.browser.msie) {
                switch ($.browser.version) {
                    case "7.0":
                        if (ColumnsNumber >= 3) ColumnsWidth--;
                        break;
                    case "8.0":
                        if (ColumnsNumber >= 2) ColumnsWidth--;
                        break;
                }
            }

            $("#" + TableID + "_tableColumn").css("width", ColumnsWidth);

            $("#" + TableID + "_tableData").scroll(function () {
                $("#" + TableID + "_tableColumn").scrollTop($("#" + TableID + "_tableData").scrollTop());
                if (ColumnsWidth <= $("#" + TableID + "_tableData").scrollLeft()) {
                    // return false;
                }
            });


            $("#" + TableID + "_tableColumn").css({"overflow": "hidden", "height": height, "position": "relative", "z-index": "40", "background-color": "Silver"});
            $("#" + TableID + "_tableData").css({"overflow": "scroll", "width": width, "height": height, "position": "relative", "z-index": "35"});
            if ($("#" + TableID + "_tableColumn").height() > $("#" + TableID + "_tableColumn table").height()) {
                $("#" + TableID + "_tableColumn").css("height", $("#" + TableID + "_tableColumn table").height());
                $("#" + TableID + "_tableData").css("height", $("#" + TableID + "_tableColumn table").height() + 17);
            }

            $("#" + TableID + "_tableColumn").offset($("#" + TableID + "_tableLayout").offset());
            $("#" + TableID + "_tableData").offset($("#" + TableID + "_tableLayout").offset());

        }

        $(document).ready(function () {
            FixTable("MyTable", 1, 600, 400);
        });
    </script>
</head>
<body>
<table
        style="margin: 0; border-bottom-color: black; border-top-color: black; width: 1000px; color: #000000; border-right-color: black; font-size: medium; border-left-color: black"
        id="MyTable" border="1" cellspacing="0" cellpadding="0">
    <thead>
    <tr>
        <th style="text-align: center; width: 80px" rowspan="3">姓名</th>
        <th style="text-align: center; width: 80px" rowspan="3">班级</th>
        <th style="text-align: center" colspan="10">成绩</th>
    </tr>
    <tr>
        <th style="text-align: center" colspan="3">主科</th>
        <th style="text-align: center" colspan="3">文科</th>
        <th style="text-align: center" colspan="3">理科</th>
        <th style="text-align: center; width: 80px" rowspan="2">总分</th>
    </tr>
    <tr>
        <th style="text-align: center; width: 80px">语文</th>
        <th style="text-align: center; width: 80px">数学</th>
        <th style="text-align: center; width: 80px">英语</th>
        <th style="text-align: center; width: 80px">政治</th>
        <th style="text-align: center; width: 80px">历史</th>
        <th style="text-align: center; width: 80px">地理</th>
        <th style="text-align: center; width: 80px">物理</th>
        <th style="text-align: center; width: 80px">化学</th>
        <th style="text-align: center; width: 80px">生物</th>
    </tr>
    <!--
 <tr>
 <th style="width: 80px; text-align: center;" _mce_style="width: 80px; text-align: center;">
           姓名
         </th>
         <th style="width: 80px; text-align: center;" _mce_style="width: 80px; text-align: center;">
           班级
         </th>
         <th style="width: 80px; text-align: center;" _mce_style="width: 80px; text-align: center;">
           语文
         </th>
         <th style="width: 80px; text-align: center;" _mce_style="width: 80px; text-align: center;">
           数学
         </th>
         <th style="width: 80px; text-align: center;" _mce_style="width: 80px; text-align: center;">
           英语
         </th>
         <th style="width: 80px; text-align: center;" _mce_style="width: 80px; text-align: center;">
           政治
         </th>
         <th style="width: 80px; text-align: center;" _mce_style="width: 80px; text-align: center;">
           历史
         </th>
         <th style="width: 80px; text-align: center;" _mce_style="width: 80px; text-align: center;">
           地理
         </th>
         <th style="width: 80px; text-align: center;" _mce_style="width: 80px; text-align: center;">
           物理
         </th>
         <th style="width: 80px; text-align: center;" _mce_style="width: 80px; text-align: center;">
           化学
         </th>
         <th style="width: 80px; text-align: center;" _mce_style="width: 80px; text-align: center;">
           生物
         </th>
         <th style="width: 80px; text-align: center;" _mce_style="width: 80px; text-align: center;">
           总分
         </th>

 </tr>
-->
    </thead>
    <tbody>
    <!-- 数据行 -->
    <tr>
        <td>学生32</td>
        <td>班级1</td>
        <td>29</td>
        <td>25</td>
        <td>146</td>
        <td>28</td>
        <td>79</td>
        <td>73</td>
        <td>47</td>
        <td>8</td>
        <td>91</td>
        <td>526</td>
    </tr>
    <tr>
        <td>学生32</td>
        <td>班级1</td>
        <td>29</td>
        <td>25</td>
        <td>146</td>
        <td>28</td>
        <td>79</td>
        <td>73</td>
        <td>47</td>
        <td>8</td>
        <td>91</td>
        <td>526</td>
    </tr>
    <tr>
        <td>学生32</td>
        <td>班级1</td>
        <td>29</td>
        <td>25</td>
        <td>146</td>
        <td>28</td>
        <td>79</td>
        <td>73</td>
        <td>47</td>
        <td>8</td>
        <td>91</td>
        <td>526</td>
    </tr>
    <tr>
        <td>学生32</td>
        <td>班级1</td>
        <td>29</td>
        <td>25</td>
        <td>146</td>
        <td>28</td>
        <td>79</td>
        <td>73</td>
        <td>47</td>
        <td>8</td>
        <td>91</td>
        <td>526</td>
    </tr>
    <tr>
        <td>学生32</td>
        <td>班级1</td>
        <td>29</td>
        <td>25</td>
        <td>146</td>
        <td>28</td>
        <td>79</td>
        <td>73</td>
        <td>47</td>
        <td>8</td>
        <td>91</td>
        <td>526</td>
    </tr>
    <tr>
        <td>学生32</td>
        <td>班级1</td>
        <td>29</td>
        <td>25</td>
        <td>146</td>
        <td>28</td>
        <td>79</td>
        <td>73</td>
        <td>47</td>
        <td>8</td>
        <td>91</td>
        <td>526</td>
    </tr>
    <tr>
        <td>学生32</td>
        <td>班级1</td>
        <td>29</td>
        <td>25</td>
        <td>146</td>
        <td>28</td>
        <td>79</td>
        <td>73</td>
        <td>47</td>
        <td>8</td>
        <td>91</td>
        <td>526</td>
    </tr>
    <tr>
        <td>学生32</td>
        <td>班级1</td>
        <td>29</td>
        <td>25</td>
        <td>146</td>
        <td>28</td>
        <td>79</td>
        <td>73</td>
        <td>47</td>
        <td>8</td>
        <td>91</td>
        <td>526</td>
    </tr>
    <tr>
        <td>学生32</td>
        <td>班级1</td>
        <td>29</td>
        <td>25</td>
        <td>146</td>
        <td>28</td>
        <td>79</td>
        <td>73</td>
        <td>47</td>
        <td>8</td>
        <td>91</td>
        <td>526</td>
    </tr>
    <tr>
        <td>学生32</td>
        <td>班级1</td>
        <td>29</td>
        <td>25</td>
        <td>146</td>
        <td>28</td>
        <td>79</td>
        <td>73</td>
        <td>47</td>
        <td>8</td>
        <td>91</td>
        <td>526</td>
    </tr>
    <tr>
        <td>学生32</td>
        <td>班级1</td>
        <td>29</td>
        <td>25</td>
        <td>146</td>
        <td>28</td>
        <td>79</td>
        <td>73</td>
        <td>47</td>
        <td>8</td>
        <td>91</td>
        <td>526</td>
    </tr>
    <tr>
        <td>学生32</td>
        <td>班级1</td>
        <td>29</td>
        <td>25</td>
        <td>146</td>
        <td>28</td>
        <td>79</td>
        <td>73</td>
        <td>47</td>
        <td>8</td>
        <td>91</td>
        <td>526</td>
    </tr>
    <tr>
        <td>学生32</td>
        <td>班级1</td>
        <td>29</td>
        <td>25</td>
        <td>146</td>
        <td>28</td>
        <td>79</td>
        <td>73</td>
        <td>47</td>
        <td>8</td>
        <td>91</td>
        <td>526</td>
    </tr>
    <tr>
        <td>学生32</td>
        <td>班级1</td>
        <td>29</td>
        <td>25</td>
        <td>146</td>
        <td>28</td>
        <td>79</td>
        <td>73</td>
        <td>47</td>
        <td>8</td>
        <td>91</td>
        <td>526</td>
    </tr>
    <tr>
        <td>学生32</td>
        <td>班级1</td>
        <td>29</td>
        <td>25</td>
        <td>146</td>
        <td>28</td>
        <td>79</td>
        <td>73</td>
        <td>47</td>
        <td>8</td>
        <td>91</td>
        <td>526</td>
    </tr>
    <tr>
        <td>学生32</td>
        <td>班级1</td>
        <td>29</td>
        <td>25</td>
        <td>146</td>
        <td>28</td>
        <td>79</td>
        <td>73</td>
        <td>47</td>
        <td>8</td>
        <td>91</td>
        <td>526</td>
    </tr>
    </tbody>
</table>
</body>
</html>