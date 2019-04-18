  ratingMsgs = new Array(6);
  ratingMsgColors = new Array(6);
  barColors = new Array(6);
  ratingMsgs[0] = "太短";
  ratingMsgs[1] = "弱";
  ratingMsgs[2] = "一般";
  ratingMsgs[3] = "很好";
  ratingMsgs[4] = "极佳";
  ratingMsgs[5] = "未评级"; //If the password server is down
  ratingMsgColors[0] = "#676767";
  ratingMsgColors[1] = "#aa0033";
  ratingMsgColors[2] = "#f5ac00";
  ratingMsgColors[3] = "#6699cc";
  ratingMsgColors[4] = "#008000";
  ratingMsgColors[5] = "#676767";
  barColors[0] = "#dddddd";
  barColors[1] = "#aa0033";
  barColors[2] = "#ffcc33";
  barColors[3] = "#6699cc";
  barColors[4] = "#008000";
  barColors[5] = "#676767";
  function CreateRatePasswdReq(pwd) {
    if (!isBrowserCompatible) {
      return;
    }
    
   // if(!document.getElementById) return false;
   // var pwd = document.getElementById("pwd");
    if(!pwd) return false;  
    passwd=pwd.value;
    var min_passwd_len = 6;  
    if (passwd.length < min_passwd_len)  {
      if (passwd.length > 0) {
        DrawBar(0);
      } else {
        ResetBar();
      }
    } else {
     //We need to escape the password now so it won't mess up with length test
       rating = checkPasswdRate(passwd);
       DrawBar(rating);
  
    }
  }
 function getElement(name) {
    if (document.all) {
        return document.all(name);
    }
    return document.getElementById(name);
}
  
  function DrawBar(rating) {
    var posbar = getElement('posBar');
    var negbar = getElement('negBar');
    var passwdRating = getElement('passwdRating');
    var barLength = getElement('passwdBar').width;
    if (rating >= 0 && rating <= 4) {  //We successfully got a rating
      posbar.style.width = barLength / 4 * rating + "px";
      negbar.style.width = barLength / 4 * (4 - rating) + "px";
    } else {
      posbar.style.width = "0px";
      negbar.style.width = barLength + "px";
      rating = 5; // Not rated Rating
    }
    posbar.style.background = barColors[rating];
    passwdRating.innerHTML = "<font color='" + ratingMsgColors[rating] +
                             "'>" + ratingMsgs[rating] + "</font>";
  }
   
  //Resets the password strength bar back to its initial state without any message showing.
  function ResetBar() {
    var posbar = getElement('posBar');
    var negbar = getElement('negBar');
    var passwdRating = getElement('passwdRating');
    var barLength = getElement('passwdBar').width;
    posbar.style.width = "0px";
    negbar.style.width = barLength + "px";
    passwdRating.innerHTML = "";
  }
  /* Checks Browser Compatibility */
  var agt = navigator.userAgent.toLowerCase();
  var is_op = (agt.indexOf("opera") != -1);
  var is_ie = (agt.indexOf("msie") != -1) && document.all && !is_op;
  var is_mac = (agt.indexOf("mac") != -1);
  var is_gk = (agt.indexOf("gecko") != -1);
  var is_sf = (agt.indexOf("safari") != -1);
  function gff(str, pfx) {
    var i = str.indexOf(pfx);
    if (i != -1) {
      var v = parseFloat(str.substring(i + pfx.length));
      if (!isNaN(v)) {
      return v;
      }
    }
    return null;
  }
  function Compatible() {
    if (is_ie && !is_op && !is_mac) {
      var v = gff(agt, "msie ");
      if (v != null) {
        return (v >= 6.0);
      }
    }
    if (is_gk && !is_sf) {
      var v = gff(agt, "rv:");
      if (v != null) {
         return (v >= 1.4);
      } else {
         v = gff(agt, "galeon/");
         if (v != null) {
           return (v >= 1.3);
         }
      }
    }
    if (is_sf) {
      var v = gff(agt, "applewebkit/");
      if (v != null) {
        return (v >= 124);
      }
    }
    return false;
  }
  
  /* We also try to create an xmlhttp object to see if the browser supports it */
  var isBrowserCompatible = Compatible();

//CharMode函数  
//测试某个字符是属于哪一类.  
function CharMode(iN){  
if (iN>=48 && iN <=57) //数字  
return 1;  
if (iN>=65 && iN <=90) //大写字母  
return 2;  
if (iN>=97 && iN <=122) //小写  
return 4;  
else  
return 8; //特殊字符  
}  
//bitTotal函数  
//计算出当前密码当中一共有多少种模式  
function bitTotal(num){  
modes=0;  
for (i=0;i<4;i++){  
if (num & 1) modes++;  
num>>>=1;  
}  
return modes;
}  
//checkStrong函数  
//返回密码的强度级别  
function checkPasswdRate(sPW){  
if (sPW.length<=4)
return 0; //密码太短  
Modes=0;  
for (i=0;i<sPW.length;i++){  
//测试每一个字符的类别并统计一共有多少种模式.  
Modes|=CharMode(sPW.charCodeAt(i));  
}  
return bitTotal(Modes);  
}