
Fader = function (config) {
  this.element = config.element;
  this.elementID = config.elementID;
  this.style = config.style;
  this.num = config.num;
  this.maxMove = config.maxMove;
  this.finishNum = "string";
  this.interval = config.interval || 10;
  this.step = config.step || 20;
  this.onFinish = config.onFinish;
  this.isFinish = false;
  this.timer = null;
  this.method = this.num >= 0;
  this.c = this.elementID ? $("#" + this.elementID) : this.element;
  this.run = function () {
    clearInterval(this.timer);
    this.fade();
    if (this.isFinish) {
      this.onFinish && this.onFinish();
    } else {
      var f = this;
      this.timer = setInterval(function () {
        f.run();
      }, this.interval);
    }
  };
  this.fade = function () {
    if (this.finishNum == "string") {
      this.finishNum = (parseInt(this.c.css(this.style)) || 0) + this.num;
    }
    var a = parseInt(this.c.css(this.style)) || 0;
    if (this.finishNum > a && this.method) {
      a += this.step;
      if (a >= 0) {
        this.finishNum = a = 0;
      }
    } else {
      if (this.finishNum < a && !this.method) {
        a -= this.step;
        if (a * -1 >= this.maxMove) {
          this.finishNum = a = this.maxMove * -1;
        }
      }
    }
    if (this.finishNum <= a && this.method || this.finishNum >= a && !this.method) {
      this.c.css(this.style, this.finishNum + "px");
      this.isFinish = true;
      this.finishNum = "string";
    } else {
      this.c.css(this.style, a + "px");
    }
  };
};

