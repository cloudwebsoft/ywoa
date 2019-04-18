function moveWindowUp() {
 amount = parseInt((winHeight-total) * 0.2);
 total += amount;
 if(total<winHeight && amount>0) {
  try {
  window.moveBy(0, (-1) * amount);
  }
  catch(e){
  }
 }
 else {
  clearInterval(handle);
 }
}

var amount=0;
var total=0;
var handle ;
var winHeight, winWidth;

// 启动滚动窗口
function START(){
 winHeight= document.body.offsetHeight + 30;
 winWidth = document.body.offsetWidth + 10 ;
 try {
 window.moveTo(window.screen.availWidth-winWidth, window.screen.availHeight);
 }
 catch (e) {}
 handle = setInterval("moveWindowUp()",70);
}

