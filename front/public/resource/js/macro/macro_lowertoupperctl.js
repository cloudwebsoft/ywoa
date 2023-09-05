function onlowerchange(event, obj, srcObj) {
	if (event!=null) {
		var srcElement = event.srcElement || event.target;
		obj.value = cmycurd(srcElement.value);
	}
	else {
		obj.value = cmycurd(srcObj.value);
	}
}

// 在flow_dispose_free.jsp中，用jQuery(document).ready就会报 缺少对象
// 原因是flow_doc_archive_content.jsp中缺少引入jquery.js造成的
$(document).ready(function() {
	$("input[lowerFieldCode]").each(function() {
		var obj = $(this);
		var lowerCtl = $(this).attr("lowerFieldCode");

		// var f = $("input[name='" + lowerCtl + "']")[0]; // jquery有可能取不到该元素
		var f = findObj(lowerCtl);
		if (f == null) {
			console.error(obj.name + ' 对应的小写控件：' + lowerCtl + ' 不存在');
			return;
		}
		var fName = "";
		if (f.id!=null && f.id!="") {
				fName = f.id;
		}
		else {
				fName = f.name;
		}
					
		eval("var oldValue_" + fName + "='" + f.value + "';");
		var sint = setInterval(function(){
			var oldVal = eval("oldValue_" + fName);
			if ($(f)[0]) {
				if (oldVal != $(f).val()) {
					onlowerchange(event, obj[0], f);
					eval("oldValue_" + fName + "=" + f.value);
				}
			} else {
				clearInterval(sint);
			}
		},500);
		
		$(this).val(cmycurd(f.value));
	});
});

function cmycurd(num) { //转成人民币大写金额形式

	var str1 = "零壹贰叁肆伍陆柒捌玖"; //0-9所对应的汉字
	var str2 = "万仟佰拾亿仟佰拾万仟佰拾元角分"; //数字位所对应的汉字
	var str3; //从原num值中取出的值
	var str4; //数字的字符串形式
	var str5 = ""; //人民币大写金额形式
	var i; //循环变量
	var j; //num的值乘以100的字符串长度
	var ch1; //数字的汉语读法
	var ch2; //数字位的汉字读法
	var nzero = 0; //用来计算连续的零值是几个

	num = Math.abs(num).toFixed(2); //将num取绝对值并四舍五入取2位小数
	str4 = (num * 100).toFixed(0).toString(); //将num乘100并转换成字符串形式
	j = str4.length; //找出最高位
	if (j > 15) {
		return '溢出';
	}
	str2 = str2.substr(15 - j); //取出对应位数的str2的值。如：200.55,j为5所以str2=佰拾元角分

	//循环取出每一位需要转换的值
	for (i = 0; i < j; i++) {
		str3 = str4.substr(i, 1); //取出需转换的某一位的值
		if (i != (j - 3) && i != (j - 7) && i != (j - 11) && i != (j - 15)) { //当所取位数不为元、万、亿、万亿上的数字时
			if (str3 == '0') {
				ch1 = '';
				ch2 = '';
				nzero = nzero + 1;
			} else {
				if (str3 != '0' && nzero != 0) {
					ch1 = '零' + str1.substr(str3 * 1, 1);
					ch2 = str2.substr(i, 1);
					nzero = 0;
				} else {
					ch1 = str1.substr(str3 * 1, 1);
					ch2 = str2.substr(i, 1);
					nzero = 0;
				}
			}
		} else { //该位是万亿，亿，万，元位等关键位
			if (str3 != '0' && nzero != 0) {
				ch1 = "零" + str1.substr(str3 * 1, 1);
				ch2 = str2.substr(i, 1);
				nzero = 0;
			} else {
				if (str3 != '0' && nzero == 0) {
					ch1 = str1.substr(str3 * 1, 1);
					ch2 = str2.substr(i, 1);
					nzero = 0;
				} else {
					if (str3 == '0' && nzero >= 3) {
						ch1 = '';
						ch2 = '';
						nzero = nzero + 1;
					} else {
						if (j >= 11) {
							ch1 = '';
							nzero = nzero + 1;
						} else {
							ch1 = '';
							ch2 = str2.substr(i, 1);
							nzero = nzero + 1;
						}
					}
				}
			}
		}
		if (i == (j - 11) || i == (j - 3)) { //如果该位是亿位或元位，则必须写上
			ch2 = str2.substr(i, 1);
		}
		str5 = str5 + ch1 + ch2;

		if (i == j - 1 && str3 == '0') { //最后一位（分）为0时，加上"整"
			str5 = str5 + '整';
		}
	}
	if (num == 0) {
		str5 = "零元整";
	}
	return str5;
}