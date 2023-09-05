(function () {

	var util = {
		css: function (elem, obj) {
			for (var i in obj) {
				elem.style[i] = obj[i];
			}
		},
		hasClass: function (elem, classN) {
			var className = elem.getAttribute("class");
			return className.indexOf(classN) != -1;
		}
	};

	function Colorpicker(opt) {
		if (this === window) throw `Colorpicker: Can't call a function directly`;
		this.init(opt);
	};

	Colorpicker.prototype = {
		init(opt) {
			let { el, initColor = "rgb(255,0,0)", allMode = ['hex', 'rgb'], color = '' } = opt;
			var elem = document.getElementById(el);

			if (!(elem && elem.nodeType && elem.nodeType === 1)) {
				throw `Colorpicker: not found  ID:${el}  HTMLElement,not ${{}.toString.call(el)}`;
			}

			this.Opt = {
				...opt,
				el,
				initColor,
				allMode,
				color
			}

			this.bindElem = elem; // 绑定的元素
			this.elem_wrap = null; // 最外层容器
			this.fixedBg = null; // 拾色器后面固定定位的透明div 用于点击隐藏拾色器
			this.elem_colorPancel = null; // 色彩面板
			this.elem_picker = null; // 拾色器色块按钮
			this.elem_barPicker1 = null; // 颜色条
			this.elem_barPicker2 = null; //透明度
			this.elem_hexInput = null; // 显示hex的表单
			this.elem_showColor = null; // 显示当前颜色
			this.elem_showModeBtn = null; // 切换输入框模式按钮
			this.elem_inputWrap = null; // 输入框外层容器

			this.pancelLeft = 0;
			this.pancelTop = 0;

			this.downX = 0;
			this.downY = 0;
			this.moveX = 0;
			this.moveY = 0;

			this.pointLeft = 0;
			this.pointTop = 0;

			this.current_mode = 'hex'; // input框当前的模式

			this.rgba = { r: 0, g: 0, b: 0, a: 1 };
			this.hsb = { h: 0, s: 100, b: 100 };


			var _this = this, rgb = initColor.slice(4, -1).split(",");

			this.rgba.r = parseInt(rgb[0]);
			this.rgba.g = parseInt(rgb[1]);
			this.rgba.b = parseInt(rgb[2]);

			var rgbCur = color.slice(5, -1).split(",");
			var rgbC = { r: 0, g: 0, b: 0 };
			rgbC.r = parseInt(rgbCur[0]);
			rgbC.g = parseInt(rgbCur[1]);
			rgbC.b = parseInt(rgbCur[2]);
			if (rgbCur[3]) {
				this.rgba.a = rgbCur[3];
			}
			this.hsb = _this.rgbToHsb(rgbC);
			color = "#" + _this.rgbToHex(rgbC);

			var body = document.getElementsByTagName("body")[0],
				div = document.createElement("div");

			div.innerHTML = this.render();
			body.appendChild(div);

			this.elem_wrap = div;
			this.fixedBg = div.children[0];
			this.elem_colorPancel = div.getElementsByClassName("color-pancel")[0];
			this.pancel_width = this.elem_colorPancel.offsetWidth;
			this.pancel_height = this.elem_colorPancel.offsetHeight;
			this.elem_picker = div.getElementsByClassName("pickerBtn")[0];
			this.elem_colorPalette = div.getElementsByClassName("color-palette")[0];
			this.elem_showColor = div.getElementsByClassName("colorpicker-showColor")[0];
			this.elem_barPicker1 = div.getElementsByClassName("colorBar-color-picker")[0];
			this.barPicker1ParentNode_width = this.elem_barPicker1.parentNode.offsetWidth;
			this.elem_barPicker2 = div.getElementsByClassName("colorBar-opacity-picker")[0];
			this.barPicker2ParentNode_width = this.elem_barPicker2.parentNode.offsetWidth;
			this.elem_hexInput = div.getElementsByClassName("colorpicker-hexInput")[0];
			this.elem_showModeBtn = div.getElementsByClassName("colorpicker-showModeBtn")[0];
			this.elem_inputWrap = div.getElementsByClassName("colorpicker-inputWrap")[0];
			var elem = this.bindElem;
			var top = elem.offsetTop;
			var left = elem.offsetLeft;
			while (elem.offsetParent) {
				top += elem.offsetParent.offsetTop;
				left += elem.offsetParent.offsetLeft;
				elem = elem.offsetParent;
			}

			this.pancelLeft = left;
			this.pancelTop = top + this.bindElem.offsetHeight;
			util.css(div, {
				"position": "absolute",
				"z-index": 2,
				"display": 'none',
				"left": left + "px",
				"top": top + this.bindElem.offsetHeight + "px"
			});
			this.bindMove(this.elem_colorPancel, this.setPosition, true);
			this.bindMove(this.elem_barPicker1.parentNode, this.setBar, false);
		    this.bindMove(this.elem_barPicker2.parentNode,this.setBar,false);

			this.bindElem.addEventListener("click", function () {
				_this.show();
			}, false);

			this.fixedBg.addEventListener("click", function (e) {
				_this.hide();
			}, false)

			this.elem_showModeBtn.addEventListener("click", function () {
				_this.switch_current_mode();
			}, false)

			this.elem_wrap.addEventListener("input", function (e) {
				var target = e.target, value = target.value;
				_this.setColorByInput(value);
			}, false);

			this.elem_colorPalette.addEventListener("click", function (e) {
				if (e.target.className=='color-palette-item') {
					let colorStr = e.target.style.background;
					let rgb = colorStr.slice(4, -1).split(",");
					let rgba = {
						r: parseInt(rgb[0]),
						g: parseInt(rgb[1]),
						b: parseInt(rgb[2])
					}
					switch (_this.current_mode) {
						case "hex":
							_this.setColorByInput("#" + _this.rgbToHex(rgba))
							break;
						case 'rgb':
							let inputs = _this.elem_wrap.getElementsByTagName("input")
							inputs[0].value = rgba.r;
							inputs[1].value = rgba.g;
							inputs[2].value = rgba.b;
							_this.setColorByInput(colorStr);
							break;
					}
					_this.setInputWrap();
				}
			}, false);
			(color != '' && this.setColorByInput(color));
		},
		render: function () {
			var tpl =
				`<div class="colorpicker-mask"></div>
				<div class="colorpicker-base">
					<div class="colorpicker-pancel">
						<div class="color-board">
							<div class="color-pancel" style="background: rgb(${this.rgba.r},${this.rgba.g},${this.rgba.b});">
								<div class="saturation-white">
									<div class="saturation-black"></div>
									<div class="pickerBtn">
										<div class="pickerBtn-in"></div>
									</div>
								</div>
							</div>
						</div>
						<div class="color_hue">
							<div class="flexbox-fix flexbox-fix1">
								<div class="huering_l">
									<div class="color_showBox">
										<div class="colorpicker-showColor" style=" background:rgb(${this.rgba.r},${this.rgba.g},${this.rgba.b});"></div>
									</div>
								</div>
								<div class="huering_r">
								    <div class="hueringBox">
										<div class="hueringBox-in">
											<div class="hue-horizontal">
												<div  class="colorBar-color-picker">
													<div class="colorBar-colorhue-cur">
													</div>
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>
							<div class="flexbox-fix flexbox-fix2">
								<div class="transparencyBox">
								    <div class="transparency-color">
										<div class="colorBar-opacity-picker">
											<div class="transparency-cur">
											</div>
										</div>
								    </div>
								</div>
							</div>
							<div class="flexbox-fix flexbox-fix3">
								<div class="flexbox-fix colorpicker-inputWrap">
										${this.getInputTpl()}
								</div>
								<div class="colorpicker-showModeBtn">
									<div class="showModeBtn-in">
										<svg viewBox="0 0 24 24" style="width: 24px; height: 24px; border: 1px solid transparent; border-radius: 5px;"><path fill="#333" d="M12,5.83L15.17,9L16.58,7.59L12,3L7.41,7.59L8.83,9L12,5.83Z"></path><path fill="#333" d="M12,18.17L8.83,15L7.42,16.41L12,21L16.59,16.41L15.17,15Z"></path></svg>
									</div>
								</div>
							</div>
						</div>
					</div>
					<div class="color-palette">
						${this.getPaletteColorsItem()}
					</div>
				</div>`;
			return tpl;
		},
		getInputTpl: function () {
			var current_mode_html = "";
			switch (this.current_mode) {
				case 'hex':
					var hex = "#" + this.rgbToHex(this.HSBToRGB(this.hsb));
					current_mode_html += `
							<div class="colorpicker-hexBox">
								<div class="colorpicker-hexBox-in">
									<input class="colorpicker-hexInput" value="${hex}" spellcheck="false">
									<span class="colorpicker-hexSpan">hex</span>
								</div>
							</div>`;
					break;
				case 'rgb':
					for (var i = 0; i < 3; i++) {
						current_mode_html +=
							`<div class="colorpicker-rgbBox">
								<div class="colorpicker-rgbBox-in">
									<input class="colorpicker-hexInput" value="${this.rgba['rgb'[i]]}" spellcheck="false">
									<span class="colorpicker-hexSpan">${'rgb'[i]}</span>
								</div>
							</div>`;
					}
				default:
			}
			return current_mode_html;
		},
		getPaletteColorsItem: function () {
			let str = '';
			let palette = ["rgb(216, 27, 67)", "rgb(142, 36, 170)", "rgb(81, 45, 168)", "rgb(48, 63, 159)", "rgb(30, 136, 229)", "rgb(0, 137, 123)",
				"rgb(67, 160, 71)", "rgb(251, 192, 45)", "rgb(245, 124, 0)", "rgb(230, 74, 25)", "rgb(233, 30, 78)", "rgb(156, 39, 176)",
				"rgb(94, 53, 177)", "rgb(57, 73, 171)", "rgb(33, 150, 243)", "rgb(0, 150, 136)", "rgb(76, 175, 80)", "rgb(253, 216, 53)",
				"rgb(251, 140, 0)", "rgb(244, 81, 30)", "rgb(236, 64, 100)", "rgb(171, 71, 188)", "rgb(103, 58, 183)", "rgb(92, 107, 192)",
				"rgb(66, 165, 245)", "rgb(38, 166, 154)", "rgb(129, 199, 132)", "rgb(255, 235, 59)", "rgb(255, 167, 38)", "rgb(255, 87, 34)",
				"rgb(240, 98, 125)", "rgb(186, 104, 200)", "rgb(126, 87, 194)", "rgb(121, 134, 203)", "rgb(100, 181, 246)", "rgb(128, 203, 196)", "rgb(165, 214, 167)", "rgb(255, 241, 118)", "rgb(255, 183, 77)", "rgb(255, 138, 101)", "rgb(244, 143, 160)", "rgb(206, 147, 216)", "rgb(149, 117, 205)", "rgb(159, 168, 218)", "rgb(144, 202, 249)", "rgb(178, 223, 219)", "rgb(200, 230, 201)", "rgb(255, 245, 157)", "rgb(255, 204, 128)", "rgb(255, 171, 145)", "rgb(255, 255, 255)", "rgb(224, 224, 224)", "rgb(182, 182, 182)", "rgb(153, 153, 153)", "rgb(137, 137, 137)", "rgb(90, 90, 90)", "rgb(55, 55, 55)", "rgb(35, 35, 35)", "rgb(22, 22, 22)", "rgb(0, 0, 0)"]
			palette.forEach(item => str += `<div style='background:${item};' class="color-palette-item"></div>`)
			return str;
		},
		setPosition(x, y) {
			var LEFT = parseInt(x - this.pancelLeft),
				TOP = parseInt(y - this.pancelTop);

			this.pointLeft = Math.max(0, Math.min(LEFT, this.pancel_width));
			this.pointTop = Math.max(0, Math.min(TOP, this.pancel_height));

			util.css(this.elem_picker, {
				left: this.pointLeft + "px",
				top: this.pointTop + "px"
			})
			this.hsb.s = parseInt(100 * this.pointLeft / this.pancel_width);
			this.hsb.b = parseInt(100 * (this.pancel_height - this.pointTop) / this.pancel_height);

			this.setShowColor();
			this.setValue(this.rgba);

		},
		setBar: function (elem, x) {
			var elem_bar = elem.getElementsByTagName("div")[0],
				rect = elem.getBoundingClientRect(),
				elem_width = elem.offsetWidth,
				X = Math.max(0, Math.min(x - rect.x, elem_width));

			if (elem_bar === this.elem_barPicker1) {
				util.css(elem_bar, {
					left: X + "px"
				});
				this.hsb.h = parseInt(360 * X / elem_width);
			} else {
				util.css(elem_bar, {
					left: X + "px"
				});
				this.rgba.a = X / elem_width;
			}

			this.setPancelColor(this.hsb.h);
			this.setShowColor();
			this.setValue(this.rgba);

		},
		setPancelColor: function (h) {
			var rgb = this.HSBToRGB({ h: h, s: 100, b: 100 });

			util.css(this.elem_colorPancel, {
				background: 'rgb(' + rgb.r + ',' + rgb.g + ',' + rgb.b + ')'
			});
		},
		setShowColor: function () {
			var rgb = this.HSBToRGB(this.hsb);

			this.rgba.r = rgb.r;
			this.rgba.g = rgb.g;
			this.rgba.b = rgb.b;

			util.css(this.elem_showColor, {
				background: 'rgba(' + rgb.r + ',' + rgb.g + ',' + rgb.b + ',' + this.rgba.a + ')'
			});
			util.css(this.elem_barPicker2.parentNode, {
				background: 'linear-gradient(to right, rgba(255,255,255,0), rgb(' + rgb.r + ',' + rgb.g + ',' + rgb.b  + '))'
			});
			
		},
		setValue: function (rgb) {
			this.elem_inputWrap.innerHTML = this.getInputTpl();
			var rgba ='rgba(' + rgb.r + ',' + rgb.g + ',' + rgb.b + ',' + this.rgba.a + ')'
			this.Opt.change(this.bindElem, rgba );
		},
		setColorByInput: function (value) {
			var _this = this;
			//var HEX_REQUIRED = [/^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$/];
			//var RGB_REQUIRED = [/^(2[0-4][0-9]|25[0-5]|[01]?[0-9][0-9]?)$/];
			switch (this.current_mode) {
				case "hex":
					value = value.slice(1);
					if (value.length == 3) {
						value = '#' + value[0] + value[0] + value[1] + value[1] + value[2] + value[2];
						this.hsb = this.hexToHsb(value);
					} else if (value.length == 6) {
						this.hsb = this.hexToHsb(value);
					}
					break;
				case 'rgb':
					var inputs = this.elem_wrap.getElementsByTagName("input"),
						rgb = {
							r: inputs[0] && parseInt(inputs[0].value) >= 0 && parseInt(inputs[0].value) < 256 ? parseInt(inputs[0].value) : 0,
							g: inputs[1] && parseInt(inputs[1].value) >= 0 && parseInt(inputs[1].value) < 256 ? parseInt(inputs[1].value) : 0,
							b: inputs[2] && parseInt(inputs[2].value) >= 0 && parseInt(inputs[2].value) < 256 ? parseInt(inputs[2].value) : 0
						};

					this.hsb = this.rgbToHsb(rgb);
			}
			this.changeViewByHsb();
		},
		changeViewByHsb: function () {
			this.pointLeft = parseInt(this.hsb.s * this.pancel_width / 100);
			this.pointTop = parseInt((100 - this.hsb.b) * this.pancel_height / 100);
			util.css(this.elem_picker, {
				left: this.pointLeft + "px",
				top: this.pointTop + "px"
			});

			this.setPancelColor(this.hsb.h);
			this.setShowColor();
			util.css(this.elem_barPicker1, {
				left: this.hsb.h / 360 * (this.barPicker1ParentNode_width) + "px"
			});
			util.css(this.elem_barPicker2, {
				left: this.rgba.a * (this.barPicker2ParentNode_width) + "px"
			});
			var rgb = this.HSBToRGB(this.hsb);
			var rgba ='rgba(' + rgb.r + ',' + rgb.g + ',' + rgb.b + ',' + this.rgba.a + ')'
			this.Opt.change(this.bindElem, rgba );
		},
		switch_current_mode: function () {
			this.current_mode = this.current_mode == 'hex' ? 'rgb' : 'hex';
			this.elem_inputWrap.innerHTML = this.getInputTpl();
		},
		setInputWrap: function () {
			this.elem_inputWrap.innerHTML = this.getInputTpl();
		},
		bindMove: function (elem, fn, bool) {
			var _this = this;

			elem.addEventListener("mousedown", function (e) {
				_this.downX = e.pageX;
				_this.downY = e.pageY;
				bool ? fn.call(_this, _this.downX, _this.downY) : fn.call(_this, elem, _this.downX, _this.downY);

				document.addEventListener("mousemove", mousemove, false);
				function mousemove(e) {
					_this.moveX = e.pageX;
					_this.moveY = e.pageY;
					bool ? fn.call(_this, _this.moveX, _this.moveY) : fn.call(_this, elem, _this.moveX, _this.moveY);
					e.preventDefault();
				}
				document.addEventListener("mouseup", mouseup, false);
				function mouseup(e) {
					document.removeEventListener("mousemove", mousemove, false)
					document.removeEventListener("mouseup", mouseup, false)
				}
			}, false);
		},
		show: function () {
			util.css(this.elem_wrap, {
				"display": "block"
			})
		},
		hide: function () {
			util.css(this.elem_wrap, {
				"display": "none"
			})
		},
		HSBToRGB: function (hsb) {
			var rgb = {};
			var h = Math.round(hsb.h);
			var s = Math.round(hsb.s * 255 / 100);
			var v = Math.round(hsb.b * 255 / 100);

			if (s == 0) {
				rgb.r = rgb.g = rgb.b = v;
			} else {
				var t1 = v;
				var t2 = (255 - s) * v / 255;
				var t3 = (t1 - t2) * (h % 60) / 60;

				if (h == 360) h = 0;

				if (h < 60) { rgb.r = t1; rgb.b = t2; rgb.g = t2 + t3 }
				else if (h < 120) { rgb.g = t1; rgb.b = t2; rgb.r = t1 - t3 }
				else if (h < 180) { rgb.g = t1; rgb.r = t2; rgb.b = t2 + t3 }
				else if (h < 240) { rgb.b = t1; rgb.r = t2; rgb.g = t1 - t3 }
				else if (h < 300) { rgb.b = t1; rgb.g = t2; rgb.r = t2 + t3 }
				else if (h < 360) { rgb.r = t1; rgb.g = t2; rgb.b = t1 - t3 }
				else { rgb.r = 0; rgb.g = 0; rgb.b = 0 }
			}

			return { r: Math.round(rgb.r), g: Math.round(rgb.g), b: Math.round(rgb.b) };
		},
		rgbToHex: function (rgb) {
			var hex = [
				rgb.r.toString(16),
				rgb.g.toString(16),
				rgb.b.toString(16)
			];
			hex.map(function (str, i) {
				if (str.length == 1) {
					hex[i] = '0' + str;
				}
			});

			return hex.join('');
		},
		hexToRgb: function (hex) {
			var hex = parseInt(((hex.indexOf('#') > -1) ? hex.substring(1) : hex), 16);
			return { r: hex >> 16, g: (hex & 0x00FF00) >> 8, b: (hex & 0x0000FF) };
		},
		hexToHsb: function (hex) {
			return this.rgbToHsb(this.hexToRgb(hex));
		},
		rgbToHsb: function (rgb) {
			var hsb = { h: 0, s: 0, b: 0 };
			var min = Math.min(rgb.r, rgb.g, rgb.b);
			var max = Math.max(rgb.r, rgb.g, rgb.b);
			var delta = max - min;
			hsb.b = max;
			hsb.s = max != 0 ? 255 * delta / max : 0;
			if (hsb.s != 0) {
				if (rgb.r == max) hsb.h = (rgb.g - rgb.b) / delta;
				else if (rgb.g == max) hsb.h = 2 + (rgb.b - rgb.r) / delta;
				else hsb.h = 4 + (rgb.r - rgb.g) / delta;
			} else hsb.h = -1;
			hsb.h *= 60;
			if (hsb.h < 0) hsb.h += 360;
			hsb.s *= 100 / 255;
			hsb.b *= 100 / 255;
			return hsb;
		}
	}

	Colorpicker.create = function (opt) {
		return new Colorpicker(opt)
	}

	window.Colorpicker = Colorpicker;
})()