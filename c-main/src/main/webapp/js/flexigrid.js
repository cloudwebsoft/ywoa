/*
 * Flexigrid for jQuery - New Wave Grid
 * 
 * Copyright (c) 2008 Paulo P. Marinas (webplicity.net/flexigrid) Dual licensed
 * under the MIT (MIT-LICENSE.txt) and GPL (GPL-LICENSE.txt) licenses.
 * 
 * $Date: 2008-07-14 00:09:43 +0800 (Tue, 14 Jul 2008) $
 */

(function($) {
	// 本方法暂无用
	function detectZoom (){
		var ratio = 1,
			screen = window.screen;
		var os = getOS();
		if (os == 1) { // ie
			if (window.devicePixelRatio) {
				ratio = window.devicePixelRatio;
			}
			else if (screen.deviceXDPI && screen.logicalXDPI) {
				ratio = screen.deviceXDPI / screen.logicalXDPI;
			}
		} else if (os == 3) { // chrome
			ratio = window.top.outerWidth / window.top.innerWidth;
		}
		else if (window.outerWidth !== undefined && window.innerWidth !== undefined) { // firefox、opera
			if (window.devicePixelRatio) {
				ratio = window.devicePixelRatio;
			}
			else {
				ratio = window.outerWidth / window.innerWidth;
			}
		}
		ratio = Math.round(ratio * 100) / 100;
		return ratio;
	}
	
	$.addFlex = function(t, p) {
		
		if (t.grid)
			return false; // return if already exist

		// apply default properties
		p = $.extend({
			autoHeight: true,
			height : 300, // flexigrid插件的高度，单位为px
			width : 'auto', // 宽度值，auto表示根据每列的宽度自动计算
			striped : true, // 是否显示斑纹效果，默认是奇偶交互的形式
			novstripe : false,
			checkOnRowClick : true, // 点击行时选中checkbox
			minwidth : 30, // 列的最小宽度
			minheight : 80, // 列的最小高度
			resizable : true, // 是否可伸缩
			url : false, // ajax方式对应的url地址
			method : 'POST', // 数据发送方式
			dataType : 'xml', // 数据加载的类型
			checkbox : false,// 是否要多选框
			errormsg : '连接错误!',// 错误提示信息
			usepager : false, // 是否分页
			nowrap : true, // 是否不换行
			page : 1, // 默认当前页
			total : 1, // 总页面数
			useRp : true, // 是否可以动态设置每页显示的结果数
			rp : 20, // 每页默认的结果数
			rpOptions : [5, 10, 15, 20, 25, 30, 40, 50, 60, 80, 100],// 可选择设定的每页结果数
			title : false,// 是否包含标题
			pagestat : '显示第 {from} 条到 {to} 条, 共 {total} 条数据',// 显示当前页和总页面的样式
			procmsg : '正在处理,请稍候 ...',// 正在处理的提示信息
			query : '',// 搜索查询的条件
			qtype : '',// 搜索查询的类别
			nomsg : '没有数据存在!',// 无结果的提示信息
			minColToggle : 1, // 允许显示的最小列数
			showToggleBtn : true, // 是否允许显示隐藏列，该属性有bug设置成false点击头脚本报错
			hideOnSubmit : true,// 隐藏提交
			autoload : true,// 自动加载
			blockOpacity : 0.5,// 透明度设置
			onLoad : false,
			onToggleCol : false,// 当在行之间转换时，可在此方法中重写默认实现，基本无用
			onChangeSort : false,// 当改变排序时，可在此方法中重写默认实现，自行实现客户端排序
			onSuccess : false,// 成功后执行
			onSubmit : false,
			// 调用自定义的计算函数
			onRowDblclick: false, 	// 双击行
			onColSwitch: false,		// 拖动变换列
			onColResize: false,		// 变化列宽
			onToolbarInited: false  // 工具条初始化结束，此时可以重新初始化工具条中include进来的控件的事件
		  }, p);

		$(t).show() // show if hidden
		    .attr({
			        cellPadding : 0,
			        cellSpacing : 0,
			        border : 0
		        }) // remove padding and spacing
		    .removeAttr('width') // remove width properties
		;

		// create grid class
		var g = {
			colsFreezedNum : 0,
			hset : {},
			rePosDrag : function() {
				$(g.cDrag).css({
					    top : g.hDiv.offsetTop + 1
				    });

				$('div', g.cDrag).hide();

				$('thead tr:first th:visible', this.hDiv).each(function () {
					var n = $('thead tr:first th:visible', g.hDiv).index(this);
					var cdpos = $(this).offset().left + $(this).width();
					$('div:eq(' + n + ')', g.cDrag).css({
						'left': cdpos + 'px'
					}).show();
				});

			},
			fixHeight : function(newH) {
				newH = false;
				if (!newH)
					newH = $(g.bDiv).height();
				var hdHeight = $(this.hDiv).height();
				$('div', this.cDrag).each(function() {
					    $(this).height(newH + hdHeight);
				    });

				var nd = parseInt($(g.nDiv).height());

				if (nd > newH)
					$(g.nDiv).height(newH).width(200);
				else
					$(g.nDiv).height('auto').width('auto');

				$(g.block).css({
					    height : newH,
					    marginBottom : (newH * -1)
				    });

				var hrH = g.bDiv.offsetTop + newH;
				if (p.height != 'auto' && p.resizable)
					hrH = g.vDiv.offsetTop;
				$(g.rDiv).css({
					    height : hrH
				    });
			},
			fixIEBug : function() {
				if (p.autoHeight) {
					var siblingsFlexiH = 0; // flexigrid 的兄弟元素的高度合计
					$(".flexigrid").siblings().each(function (i) {
						if ($(this).is(":visible")) {
							// consoleLog("flexigrid siblings: id=" + $(this).attr("id") + " " + $(this)[0].tagName + " height=" + $(this).height() + " " + $(this).attr("class"));
							siblingsFlexiH += $(this).height();
						}
					});

					var siblingsH = 0; // bDiv 的兄弟元素的高度合计
					// bDiv为body container
					$(g.bDiv).siblings().each(function (i) {
						if ($(this).is(":visible")) {
							// 当module_list.jsp中的记录数为0时，如果来回切换选项卡，会因为gBlock出现，而使得页码区上移贴紧至表头位置
							if ($(this).attr("class")!="gBlock") {
								// consoleLog("bDiv siblings: id=" + $(this).attr("id") + " " + $(this)[0].tagName + " height=" + $(this).height() + " " + $(this).attr("class"));
								siblingsH += $(this).height();
							}
						}
					});

					// $.browser.msie在高版本的jQuery中已取消，所以不会进入到此if中，只会进入else中
					if ($.browser.msie && ($.browser.version == "6.0") && !$.support.style) {
						// 修复IE6的bug
						$(g.bDiv).height(document.documentElement.clientHeight - siblingsH - siblingsFlexiH - 5); // 4应为4条边框线所占高度
						
						// 调整bDiv宽度
						var isFrame = false;
						var walt = 2;
						// 嵌套于框架中
						try {
							if (self.frameElement) {
								if(self.frameElement.tagName=="FRAME") {
									// 页面在frame中时处理
									walt = 6;
									isFrame = true;
								}
							}
						}
						catch(e) {alert("aa")}
						
						$(g.bDiv).width(document.documentElement.offsetWidth - walt);
						
						$(g.mDiv).width($(g.bDiv).width());
						$(g.hDiv).width($(g.bDiv).width());
						$(g.tDiv).width($(g.bDiv).width());
						$(g.pDiv).width($(g.bDiv).width());
						$(".flexigrid").width($(g.bDiv).width());
						//$(".flexigrid").css("background-color", "red");
						if (siblingsFlexiH>0)
							$(g.bDiv).height($(g.bDiv).height() + 13); // -34);
						else {
							if ($(g.mDiv).is(":visible"))
								$(g.bDiv).height($(g.bDiv).height() + 12); // mDiv显示时可能又因多了一个像素的border，而使得要少加1
							else {
								if (!isFrame)
									$(g.bDiv).height($(g.bDiv).height() + 13); // -34);
							}
						}
					}
					else {
						// 20190129 fgf 先将bDiv的高度减小至200，以免此时因bDiv过高，使得右侧出现滚动条，而使得document.documentElement.clientWidth的宽度少了滚动条的宽度
						// bDiv为列表区域中去除表头部分
						$(g.bDiv).height(200);
						$(".flexigrid").width(document.documentElement.clientWidth);
						// 恢复高度
						$(g.bDiv).height(document.documentElement.clientHeight - siblingsH - siblingsFlexiH - 6); // 4应为4条边框线所占高度

						// 因为当grid有兄弟节点时，如在其上部有选项卡，在IE8中高度表现为先调整到位（通过在方法中加入alert('...')，可以看到已到位）
						// 然后又因横向滚动条出现，使自动上移16px(相当于滚动条的高度，因此需再往下移16px)
						// consoleLog(p.width + "--" + document.documentElement.clientWidth);
						if (siblingsFlexiH>0) {
							// 20191102 fgf 如果宽度大于客户区宽度，说明出现了横向滚动条，需微调高度，使横向滚动条不再出现
							if (p.width > document.documentElement.clientWidth) {
								if (isIE()) {
									$(g.bDiv).height($(g.bDiv).height() - 5);
								} else {
									$(g.bDiv).height($(g.bDiv).height() - 7);
								}
							}

							// 置border为1px，在IE8下，当内容较多，出现上下滚动条时，可以使自动出现的滚动条消失，因为该滚动条在IE8下面，会自动出现，而当鼠标从右侧滚动条滑过时，该滚动条又会自动消息
							// 如果用{border:"1px"}，则横向滚动条会消失，但是边框没了颜色，注意这里的问题会使得不同皮肤下的边界线可能会产生问题
							// 用$(g.bDiv).css("border", $(g.bDiv.css("border"))也不行
							
							$(g.bDiv).css({border:"1px solid #ccc"});
						}
						if ($(g.mDiv).is(":visible")) { // mDiv显示时，可能又因多了一个像素的border，而使得出现滚动条使bDiv高度缩小一个滚动条高度(16px)
							$(g.bDiv).height($(g.bDiv).height() + 12);
						}
					}
					//调整可能出现的窗口抖动问题，当初始化flex时，document 没有完全加载完成，上面的调整会触发window.resize 事件
					//标识window.resize事件是因为调整引起的
					t.isIEBugResize = true;
				}
			},
			dragStart : function(dragtype, e, obj) { // default drag function start
				if (dragtype == 'colresize') // column resize
				{
					$(g.nDiv).hide();
					$(g.nBtn).hide();
					var n = $('div', this.cDrag).index(obj);
					var ow = $('th:visible div:eq(' + n + ')', this.hDiv).width();
					$(obj).addClass('dragging').siblings().hide();
					$(obj).prev().addClass('dragging').show();

					this.colresize = {
						startX : e.pageX,
						ol : parseInt(obj.style.left),
						ow : ow,
						n : n
					};
					$('body').css('cursor', 'col-resize');
				} else if (dragtype == 'vresize') // table resize
				{
					var hgo = false;
					$('body').css('cursor', 'row-resize');
					if (obj) {
						hgo = true;
						$('body').css('cursor', 'col-resize');
					}
					this.vresize = {
						h : p.height,
						sy : e.pageY,
						w : p.width,
						sx : e.pageX,
						hgo : hgo
					};

				}

				else if (dragtype == 'colMove') // column header drag
				{
					$(g.nDiv).hide();
					$(g.nBtn).hide();
					this.hset = $(this.hDiv).offset();
					this.hset.right = this.hset.left + $('table', this.hDiv).width();
					this.hset.bottom = this.hset.top + $('table', this.hDiv).height();
					this.dcol = obj;
					this.dcoln = $('th', this.hDiv).index(obj);

					this.colCopy = document.createElement("div");
					this.colCopy.className = "colCopy";
					this.colCopy.innerHTML = obj.innerHTML;
					if ($.browser.msie) {
						this.colCopy.className = "colCopy ie";
					}
					$(this.colCopy).css({
						    position : 'absolute',
						    float : 'left',
						    display : 'none',
						    textAlign : obj.align
					    });
					$('body').append(this.colCopy);
					$(this.cDrag).hide();

				}

				$('body').noSelect();

			},
			dragMove : function(e) {

				if (this.colresize) // column resize
				{
					var n = this.colresize.n;
					var diff = e.pageX - this.colresize.startX;
					var nleft = this.colresize.ol + diff;
					var nw = this.colresize.ow + diff;
					if (nw > p.minwidth) {
						$('div:eq(' + n + ')', this.cDrag).css('left', nleft);
						this.colresize.nw = nw;
					}
				} else if (this.vresize) // table resize
				{
					var v = this.vresize;
					var y = e.pageY;
					var diff = y - v.sy;

					if (!p.defwidth)
						p.defwidth = p.width;

					if (p.width != 'auto' && !p.nohresize && v.hgo) {
						var x = e.pageX;
						var xdiff = x - v.sx;
						var newW = v.w + xdiff;
						if (newW > p.defwidth) {
							this.gDiv.style.width = newW + 'px';
							p.width = newW;
						}
					}

					var newH = v.h + diff;
					if ((newH > p.minheight || p.height < p.minheight) && !v.hgo) {
						this.bDiv.style.height = newH + 'px';
						p.height = newH;
						this.fixHeight(newH);
					}
					v = null;
				} else if (this.colCopy) {
					// if (isIE11) {
						// console.log("this.colCopy remove class thOver");
					// }
					// 注释掉以后，可以解决win7 IE11点击表头无法排序的情况，此时变为了拖动表头模式
					// $(this.dcol).addClass('thMove').removeClass('thOver');
					if (e.pageX > this.hset.right || e.pageX < this.hset.left
					    || e.pageY > this.hset.bottom || e.pageY < this.hset.top) {
						// this.dragEnd();
						$('body').css('cursor', 'move');
					} else
						$('body').css('cursor', 'pointer');
					$(this.colCopy).css({
						    top : e.pageY + 10,
						    left : e.pageX + 20,
						    display : 'block'
					    });
				}

			},
			dragEnd : function() {

				if (this.colresize) {
					var n = this.colresize.n;
					var nw = this.colresize.nw;

					$('th:visible div:eq(' + n + ')', this.hDiv).css('width', nw);
					$('tr', this.bDiv).each(function() {
						    $('td:visible div:eq(' + n + ')', this).css('width', nw);
					    });
					this.hDiv.scrollLeft = this.bDiv.scrollLeft;

					$('div:eq(' + n + ')', this.cDrag).siblings().show();
					$('.dragging', this.cDrag).removeClass('dragging');
					this.rePosDrag();
					this.fixHeight();
					this.colresize = false;
					
					if (p.onColResize) p.onColResize(n, nw);
				} else if (this.vresize) {
					this.vresize = false;
				} else if (this.colCopy) {
					$(this.colCopy).remove();
					if (this.dcolt != null) {
						if (this.dcoln > this.dcolt)
							$('th:eq(' + this.dcolt + ')', this.hDiv).before(this.dcol);
						else
							$('th:eq(' + this.dcolt + ')', this.hDiv).after(this.dcol);

						this.switchCol(this.dcoln, this.dcolt);
						$(this.cdropleft).remove();
						$(this.cdropright).remove();
						this.rePosDrag();

						if (p.onColSwitch) p.onColSwitch(this.dcoln, this.dcolt);
					}

					this.dcol = null;
					this.hset = null;
					this.dcoln = null;
					this.dcolt = null;
					this.colCopy = null;

					$('.thMove', this.hDiv).removeClass('thMove');
					$(this.cDrag).show();
				}
				$('body').css('cursor', 'default');
				$('body').noSelect(false);
			},
			toggleCol : function(cid, visible) {

				var ncol = $("th[axis='col" + cid + "']", this.hDiv)[0];
				var n = $('thead th', g.hDiv).index(ncol);
				var cb = $('input[value=' + cid + ']', g.nDiv)[0];

				if (visible == null) {
					visible = ncol.hide;
				}

				if ($('input:checked', g.nDiv).length < p.minColToggle && !visible)
					return false;

				if (visible) {
					ncol.hide = false;
					$(ncol).show();
					cb.checked = true;
				} else {
					ncol.hide = true;
					$(ncol).hide();
					cb.checked = false;
				}

				$('tbody tr', t).each(function() {
					    if (visible)
						    $('td:eq(' + n + ')', this).show();
					    else
						    $('td:eq(' + n + ')', this).hide();
				    });

				this.rePosDrag();

				if (p.onToggleCol)
					p.onToggleCol(cid, visible);

				return visible;
			},
			switchCol : function(cdrag, cdrop) { // switch columns

				$('tbody tr', t).each(function() {
					if (cdrag > cdrop)
						$('td:eq(' + cdrop + ')', this).before($('td:eq(' + cdrag + ')',
						    this));
					else
						$('td:eq(' + cdrop + ')', this).after($('td:eq(' + cdrag + ')',
						    this));
				});

				// switch order in nDiv
				if (cdrag > cdrop)
					$('tr:eq(' + cdrop + ')', this.nDiv).before($('tr:eq(' + cdrag + ')',
					    this.nDiv));
				else
					$('tr:eq(' + cdrop + ')', this.nDiv).after($('tr:eq(' + cdrag + ')',
					    this.nDiv));

				if ($.browser.msie && $.browser.version < 7.0)
					$('tr:eq(' + cdrop + ') input', this.nDiv)[0].checked = true;

				this.hDiv.scrollLeft = this.bDiv.scrollLeft;
			},
			scroll : function() {
				this.hDiv.scrollLeft = this.bDiv.scrollLeft;
				this.rePosDrag();
			},
			addData : function(data) { // parse data

				if (p.preProcess)
					data = p.preProcess(data);

				$('.pReload', this.pDiv).removeClass('loading');
				this.loading = false;

				if (!data) {
					$('.pPageStat', this.pDiv).html(p.errormsg);
					return false;
				}

				if (p.dataType == 'xml')
					p.total = +$('rows total', data).text();
				else
					p.total = data.total;
					
				if (p.total == 0) {
					$('tr, a, td, div', t).unbind();
					$(t).empty();
					this.buildpager();
					$('.pPageStat', this.pDiv).html(p.nomsg);
					return false;
				}

				if (p.dataType == 'xml')
					p.page = +$('rows page', data).text();
				else
					p.page = data.page;

				this.buildpager();

			    var scripts = ""; // 用于保存宏控件中convertToHtml()中的js脚本
				// build new body
				var tbody = document.createElement('tbody');
				// 修改json格式
				if (p.dataType == 'json') {
					$.each(data.rows, function(i, row) {
						    var tr = document.createElement('tr');
						    if (i % 2 && p.striped)
							    tr.className = 'erow';

						    if (row.id)
							    tr.id = 'row' + row.id;
							if (row.ID)
								tr.id = 'row' + row.ID;

						    // by anson
						    var tdVal = [];
						    // 给每行添加id
						    if (p.rowId) {
							    $.each(data.rows[i], function(x, y) {
								        if (p.rowId == x) {
									        tr.setAttribute('id', y);
								        }
							        })
						    }
						    if (p.colModel) {
							    for (j = 0; j < p.colModel.length; j++) {
								    var cm = p.colModel[j];
								    // 取列名
								    var seleceName = cm.name;
								    // json Bug修复://打包文件中未加入,请自行加入
								    if (typeof(data.rows[i][seleceName]) == 'undefined') {
									    data.rows[i][seleceName] = ''
								    }
								    // 过滤key
								    $.each(data.rows[i], function(x, y) {
									        if (seleceName == x) {
										        tdVal.push(y)
									        }
								        })
							    }
						    }
						    // add cell
						    $('thead tr:first th', g.hDiv).each(function(col) {
									// 
							        var td = document.createElement('td');
									
									if (!$(this).attr('axis')) { // checkbox列
										var isCheckbox = $(this).html().indexOf("checkbox")!=-1;
										// 添加多选
										if (p.checkbox) {
											var cth = $('<td />');

											// 如果rowId小于0，则可能为统计列所在行，不需要显示复选框
											var rowId = parseInt($(tr).attr('id').substring(3));
											if (rowId >= 0) {
												var cthch = $('<input type="checkbox" value="' + $(tr).attr('id') + '"/>');
												var objTr = $(tr);
												// cthch.addClass("noborder").click(function() {//add matychen 不要noborder，加入后ie8显示有问题。
												cthch.click(function() {
													if (this.checked) {
														objTr.addClass('trSelected');
													} else {
														objTr.removeClass('trSelected');
													}
												});
											}

											cth.addClass("cth").attr({
													width : "22"
												}).append(cthch);
												
											cth.width($(this).width());
											
											$(tr).prepend(cth);
										}
										
										// alert($(this)[0].outerHTML);
										/*
										td.align = this.align;
										$(td).width($(this).width());
										$(tr).append(td);
										td = null;
										*/
										return;
									}
									
							        var idx = $(this).attr('axis').substr(3);
							        td.align = this.align;
							        td.id = "td_" + (i+1) + "_" + col;
						        	td.innerHTML = tdVal[idx];

									// tdValue可能为数值型，而数据值是不能运用.indexOf方法
						        	var tdValue = tdVal[idx] + "";
									var pIndex = tdValue.indexOf("<script>");
									if (pIndex!=-1) {
										var q = tdValue.indexOf("</script>", pIndex);
										var src = tdValue.substring(pIndex + 8, q);
										scripts += src + "\n";
										// console.log(src);
									}

							        $(tr).append(td);
							        td = null;
						        });

						    if ($('thead', this.gDiv).length < 1) // handle if grid has no// headers
						    {
							    for (idx = 0; idx < cell.length; idx++) {
								    var td = document.createElement('td');
								    // td.innerHTML = row.cell[idx];
								    td.innerHTML = tdVal[idx];
								    $(tr).append(td);
								    td = null;
							    }
						    }

						    $(tbody).append(tr);
						    tr = null;
					    });
				} else if (p.dataType == 'xml') {
					i = 1;
					$("rows row", data).each(function() {
						    i++;
						    var tr = document.createElement('tr');
						    if (i % 2 && p.striped)
							    tr.className = 'erow';
						    var nid = $(this).attr('id');
						    if (nid)
							    tr.id = 'row' + nid;
						    nid = null;
						    var robj = this;
						    $('thead tr:first th', g.hDiv).each(function() {
							        var td = document.createElement('td');
							        var idx = $(this).attr('axis').substr(3);
							        td.align = this.align;
							        td.innerHTML = $("cell:eq(" + idx + ")", robj).text();
							        $(tr).append(td);
							        td = null;
						        });
						    if ($('thead', this.gDiv).length < 1) // handle if grid has no  headers
						    {
							    $('cell', this).each(function() {
								        var td = document.createElement('td');
								        td.innerHTML = $(this).text();
								        $(tr).append(td);
								        td = null;
							        });
						    }
						    $(tbody).append(tr);
						    tr = null;
						    robj = null;
					    });
				}
				$('tr', t).unbind();
				$(t).empty();
				$(t).append(tbody);
				this.addCellProp();
				this.addRowProp();
				// this.fixHeight($(this.bDiv).height());
				this.rePosDrag();
				tbody = null;
				data = null;
				i = null;

				if (p.onSuccess)
					p.onSuccess();
				if (p.hideOnSubmit)
					$(g.block).remove();// $(t).show();
				this.hDiv.scrollLeft = this.bDiv.scrollLeft;
				if ($.browser.opera)
					$(t).css('visibility', 'visible');
				
				// 运行scripts
				if (scripts!="") {
		            setTimeout(function(){
					    var myScript = document.createElement("script");
					    myScript.type = "text/javascript";
						try {
							myScript.appendChild(document.createTextNode(scripts));
						} catch (ex) {
							// 兼容IE8
							myScript.text = scripts;
						}
						document.body.appendChild(myScript);
		            }, 50);
				}
			},
			changeSort : function(th) { // change sortorder
				if (this.loading)
					return true;
				$(g.nDiv).hide();
				$(g.nBtn).hide();
				if (p.sortname == $(th).attr('abbr')) {
					if (p.sortorder == 'asc')
						p.sortorder = 'desc';
					else
						p.sortorder = 'asc';
				}

				$(th).addClass('sorted').siblings().removeClass('sorted');
				$('.sdesc', this.hDiv).removeClass('sdesc');
				$('.sasc', this.hDiv).removeClass('sasc');
				$('div', th).addClass('s' + p.sortorder);
				p.sortname = $(th).attr('abbr');

				if (p.onChangeSort)
					p.onChangeSort(p.sortname, p.sortorder);
				else
					this.populate();
			},
			buildpager : function() { // rebuild pager based on new properties
				if (p.total == 0) {
					p.pages = 1;
					p.page = 1;
				}
				else
					p.pages = Math.ceil(p.total / p.rp);
			
				$('.pcontrol input', this.pDiv).val(p.page);
				$('.pcontrol span', this.pDiv).html(p.pages);

				var r1 = (p.page - 1) * p.rp + 1;
				var r2 = r1 + p.rp - 1;

				if (p.total < r2)
					r2 = p.total;

				var stat = p.pagestat;

				stat = stat.replace(/{from}/, r1);
				stat = stat.replace(/{to}/, r2);
				stat = stat.replace(/{total}/, p.total);
				
				$('.pPageStat', this.pDiv).html(stat);

				//fgf 20140104
				//判断当前页是否具有上一页
				var isCanPrev = p.page <= 1 ;
				
				//计算页面总数
				var totalPage = 1;
				if (p.total == 0) {
						totalPage = 1;				
				} else {
						totalPage = Math.ceil(p.total / p.rp);
				}
	
				//判断当前页面是否具有下一页
				var isCanNext = p.page >= totalPage;			
				//如果具有上一夜 添加点击事件  如果不具有上一页 修改鼠标样式为默认
				if(!isCanPrev) {
					$('.pPrev', g.pDiv).css("cursor","pointer");							
					$('.pPrev', g.pDiv).click(function() {
							g.changePage('prev')
					});
				} else {
					$('.pPrev', g.pDiv).css("cursor", "auto");
				}
				//如果具有上一页 添加点击事件  如果不具有上一页 修改鼠标样式为默认
				if(!isCanNext) {
					$('.pNext', g.pDiv).css("cursor","pointer");
					$('.pNext', g.pDiv).click(function() {
							g.changePage('next')
					});
				} else {
					$('.pNext', g.pDiv).css("cursor","auto");
				}
			},
			// 加入右键菜单
			initHeaderContextMenu : function() {
				var menu = new BootstrapMenu('.hDiv th', {
					//fetchElementData获取元数据
					fetchElementData:function($rowElem){
						var data = $rowElem;
						return data;    //return的目的是给下面的onClick传递参数
					},
					actions: [{
						name: '冻结',
						width:300,
						iconClass: 'fa-columns',
						onClick: function (obj) {
							// eq(0)表示第1个表格，eq(1)为clone的表用于显示冻住效果
							$(g.hDiv).find('table').eq(0).find('th').each(function(i) {
								if ($(this).text()==obj.text()) {
									$(t).flexFreeze(i+1);
									g.colsFreezedNum = i+1;
								}
							})
						}
					}]
				});

				var menuUnFreeze = new BootstrapMenu('#' + t.id + '_h_fixed th', {
					//fetchElementData获取元数据
					fetchElementData:function($rowElem){
						var data = $rowElem;
						return data;    //return的目的是给下面的onClick传递参数
					},
					actions: [{
						name: '解冻',
						width:300,
						iconClass: 'fa-columns',
						onClick: function (obj) {
							// eq(0)表示第1个表格，eq(1)为clone的表用于显示冻住效果
							$(g.hDiv).find('table').eq(1).find('th').each(function(i) {
								if ($(this).text()==obj.text()) {
									$(t).flexUnFreeze();
								}
							})
							return false;
						}
					}]
				});
			},
			populate : function() { // get latest data
				if (this.loading)
					return true;
				if (p.onSubmit) {
					var gh = p.onSubmit();
					if (!gh)
						return false;
				}
				this.loading = true;
				if (!p.url)
					return false;

				$('.pPageStat', this.pDiv).html(p.procmsg);

				$('.pReload', this.pDiv).addClass('loading');

				$(g.block).css({
					    top : g.bDiv.offsetTop
				    });

				if (p.hideOnSubmit)
					$(this.gDiv).prepend(g.block); // $(t).hide();

				if ($.browser.opera)
					$(t).css('visibility', 'hidden');

				if (!p.newp)
					p.newp = 1;

				if (p.page > p.pages)
					p.page = p.pages;
				// var param = {page:p.newp, rp: p.rp, sortname: p.sortname, sortorder:
				// p.sortorder, query: p.query, qtype: p.qtype};
				var param = [{
					    name : 'page',
					    value : p.newp
				    }, {
					    name : 'rp',
					    value : p.rp
				    }, {
					    name : 'sortname',
					    value : p.sortname
				    }, {
					    name : 'sortorder',
					    value : p.sortorder
				    }, {
					    name : 'query',
					    value : p.query
				    }, {
					    name : 'qtype',
					    value : p.qtype
				    }];

				if (p.params) {
					for (var pi = 0; pi < p.params.length; pi++)
						param[param.length] = p.params[pi];
				}

				$.ajax({
					    type : p.method,
					    url : p.url,
					    data : param,
					    dataType : p.dataType,
						contentType : "application/x-www-form-urlencoded; charset=iso8859-1",					    
					    success : function(data) {
						    g.addData(data);

							if (typeof(onLoad)=="function") {
								onLoad();
							}

							// 如果当前被冻结的列数大于0，则说明处于冻结状态，需要重新冻结，以使得clone的区域被更新
							if (g.colsFreezedNum>0) {
								$(t).flexFreeze(g.colsFreezedNum);
							}
						},
					    error : function(data) {
						    try {
							    if (p.onError)
								    p.onError(data);
						    } catch (e) {
						    }
					    }
				    });
			},
			doSearch : function() {
				p.query = $('input[name=q]', g.sDiv).val();
				p.qtype = $('select[name=qtype]', g.sDiv).val();
				p.newp = 1;
				this.populate();
			},
			changePage : function(ctype) { // change page
				if (this.loading)
					return true;
				p.newp = 1;
				switch (ctype) {
					case 'first' :
						p.newp = 1;
						break;
					case 'prev' :
						if (p.page > 1)
							p.newp = parseInt(p.page) - 1;
						break;
					case 'next' :
						if (p.page < p.pages)
							p.newp = parseInt(p.page) + 1;
						break;
					case 'last' :
						p.newp = p.pages;
						break;
					case 'input' :
						var nv = parseInt($('.pcontrol input', this.pDiv).val());
						if (isNaN(nv))
							nv = 1;
						if (nv < 1)
							nv = 1;
						else if (nv > p.pages)
							nv = p.pages;
						$('.pcontrol input', this.pDiv).val(nv);
						p.newp = nv;
						break;
				}
				
				if (p.newp == p.page)
					return false;
					
				if (p.onChangePage)
					p.onChangePage(p.newp);
				else
					this.populate();
			},
			addCellProp : function() {
				$('tbody tr td', g.bDiv).each(function() {
					var tdDiv = document.createElement('div');
					var n = $('td', $(this).parent()).index(this);
					var pth = $('th:eq(' + n + ')', g.hDiv).get(0);

					if (pth != null) {
						if (p.sortname == $(pth).attr('abbr') && p.sortname) {
							this.className = 'sorted';
						}
												
						$(tdDiv).css({
							    textAlign : pth.align,
							    width : $('div:first', pth)[0].style.width
						    });

						if (pth.hide)
							$(this).css('display', 'none');

					}
					if (p.nowrap == false)
						$(tdDiv).css('white-space', 'normal');

					if (this.innerHTML == '')
						this.innerHTML = '&nbsp;';
					// tdDiv.value = this.innerHTML; //store preprocess value
					tdDiv.innerHTML = this.innerHTML;
					var prnt = $(this).parent()[0];
					var pid = false;
					if (prnt.id)
						pid = prnt.id.substr(3);

					if (pth != null) {
						// 20181023 fgf 增加colName参数
						if (pth.process) {
							var colName = "";
							if (p.colModel) {
								var cm = p.colModel[n-1];
								if ("name" in cm) {
									colName = cm.name;
								}
							}				
							else {
								colName = $(pth).attr("abbr")
							}
							pth.process(tdDiv, pid, colName);
						}
					}

					$(this).empty().append(tdDiv).removeAttr('width'); // wrap content
					  // add editable event here 'dblclick'
				  });
			},
			getCellDim : function(obj) // get cell prop for editable event
			{
				var ht = parseInt($(obj).height());
				var pht = parseInt($(obj).parent().height());
				var wt = parseInt(obj.style.width);
				var pwt = parseInt($(obj).parent().width());
				var top = obj.offsetParent.offsetTop;
				var left = obj.offsetParent.offsetLeft;
				var pdl = parseInt($(obj).css('paddingLeft'));
				var pdt = parseInt($(obj).css('paddingTop'));
				return {
					ht : ht,
					wt : wt,
					top : top,
					left : left,
					pdl : pdl,
					pdt : pdt,
					pht : pht,
					pwt : pwt
				};
			},
			addRowProp : function() {
				$('tbody tr', g.bDiv).each(function() {
					    $(this).click(function(e) {
						        var obj = (e.target || e.srcElement);
						        if (obj.href || obj.type)
							        return true;

						        if (!p.checkOnRowClick) {
						        	return false;
								}

						        $(this).toggleClass('trSelected');
						        // 添加多选框
						        if (p.checkbox) {
							        if ($(this).hasClass('trSelected')) {
								        $(this).find('input')[0].checked = true;
							        } else {
								        $(this).find('input')[0].checked = false
							        }
						        }

						        if (p.singleSelect)
							        $(this).siblings().removeClass('trSelected');
					        }).mousedown(function(e) {
						        if (e.shiftKey) {
							        $(this).toggleClass('trSelected');
							        g.multisel = true;
							        this.focus();
							        $(g.gDiv).noSelect();
						        }
					        }).mouseup(function() {
						        if (g.multisel) {
							        g.multisel = false;
							        $(g.gDiv).noSelect(false);
						        }
					        }).hover(function(e) {
						        if (g.multisel) {
							        $(this).toggleClass('trSelected');
						        }
					        }, function() {
					        }).dblclick(    
								function (e) {
									var rowData = new Object();
									 $.each($(this).find('div'),function(i){
										 if (p.colModel) {
											 if (p.colModel[i]) {
												 $(rowData).data(p.colModel[i].name, $(this).text());   
											 }
										 }
										 else {
											 var th = $('th:eq(' + i + ')', this.hDiv);
											 if (th.is(":visible")) {
												 if (!th.attr('name')) {
												 	var ckbox = $(this).find('input');
													$(rowData).data('rowId', ckbox.val());
												 }
												 else {
													 $(rowData).data(th.attr('name'), $(this).text());
												 }
											 }
										 }
									 });

									if (p.onRowDblclick) p.onRowDblclick($(rowData));    
							});
	 
					    if ($.browser.msie && $.browser.version < 7.0) {
						    $(this).hover(function() {
							        $(this).addClass('trOver');
						        }, function() {
							        $(this).removeClass('trOver');
						        });
					    }
				    });

			},
			pager : 0
		};

		// create model if any
		if (p.colModel) {
			thead = document.createElement('thead');
			tr = document.createElement('tr');

			for (i = 0; i < p.colModel.length; i++) {
				var cm = p.colModel[i];
				var th = document.createElement('th');

				th.innerHTML = cm.display;

				if (cm.name && cm.sortable)
					$(th).attr('abbr', cm.name);

				// th.idx = i;
				$(th).attr('axis', 'col' + i);

				if (cm.align && cm.align!="undefined")
					th.align = cm.align;

				if (cm.width)
					$(th).attr('width', cm.width);

				if (cm.hide) {
					th.hide = true;
				}

				if (cm.process) {
					th.process = cm.process;
				}

				$(tr).append(th);
			}
			$(thead).append(tr);
			$(t).prepend(thead);
		} // end if p.colmodel

		// init divs
		g.gDiv = document.createElement('div'); // create global container
		g.mDiv = document.createElement('div'); // create title container
		g.hDiv = document.createElement('div'); // create header container
		g.bDiv = document.createElement('div'); // create body container
		g.vDiv = document.createElement('div'); // create grip
		g.rDiv = document.createElement('div'); // create horizontal resizer
		g.cDrag = document.createElement('div'); // create column drag
		g.block = document.createElement('div'); // creat blocker
		g.nDiv = document.createElement('div'); // create column show/hide popup
		g.nBtn = document.createElement('div'); // create column show/hide button
		g.iDiv = document.createElement('div'); // create editable layer
		g.tDiv = document.createElement('div'); // create toolbar
		g.sDiv = document.createElement('div');

		if (p.usepager)
			g.pDiv = document.createElement('div'); // create pager container
		g.hTable = document.createElement('table');

		// set gDiv
		g.gDiv.className = 'flexigrid';
		if (p.width != 'auto')
			g.gDiv.style.width = p.width + 'px';

		// add conditional classes
		if ($.browser.msie)
			$(g.gDiv).addClass('ie');

		if (p.novstripe)
			$(g.gDiv).addClass('novstripe');

		$(t).before(g.gDiv);
		$(g.gDiv).append(t);

		// set toolbar
		if (p.buttons) {
			g.tDiv.className = 'tDiv';
			var tDiv2 = document.createElement('div');
			tDiv2.className = 'tDiv2';

			for (i = 0; i < p.buttons.length; i++) {
				var btn = p.buttons[i];
				// 兼容IE8
				if (!btn) {
					continue;
				}
				if (btn.type != null) {
					if (btn.type=="include") {
						var incDiv = document.createElement('div');
						incDiv.className = btn.bclass;
						if (!$.browser.msie) {
							incDiv.style="float:left"; // 否则firefox中会折行，导致看不见搜索框
						}
						// incDiv.id = "myToolbarDiv" + i;
						// $("#"+btn.id).html();
						// alert($("#"+btn.id td).children('td').eq(0).html());
						incDiv.innerHTML = $("#"+btn.id).html();
						// alert($("#"+btn.id).clone().html());
						// $(incDiv).html($("#"+btn.id).clone().html());
						// $(incDiv).append(document.getElementById(btn.id));
						// incDiv.appendChild(document.getElementById(btn.id));
						// alert(document.getElementById(btn.id).outerHTML);
						/*
						incDiv.appendChild(document.getElementById(btn.id));
						alert(incDiv.outerHTML);
						*/

/*
var chary = $("#"+btn.id).children();
var chlen = chary.length;
for (var k=0; k<chlen; k++) {
	alert(chary[k].html());
	var tmp = $("#"+btn.id).children()[k].detach();
	$(incDiv).append(tmp);
}
*/
/*
var tmp = $("#"+btn.id).children().detach();

alert(tmp.html());

$(incDiv).append(tmp);
*/
/*
						$("#"+btn.id).children().each(function() {
							var tmp = $(this).detach();
							
							// tmp.prependTo("#" + incDiv.id);
							
							// alert(tmp[0].outerHTML);
							
							// incDiv.appendChild(tmp[0]);
							try {
							$(incDiv).append(tmp[0]);
							}
							catch(e) {}

							// alert($(this)[0].innerHTML);
							// incDiv.appendChild($(tmp).clone(true));
							
							// tmp.remove();
							// $(incDiv).append($(this));
						});
*/
						// alert(incDiv.outerHTML);

						var searchBtn = $("input[type='submit'][class='tSearch']", $(incDiv));
						searchBtn.val('');
						
						$(tDiv2).append(incDiv);
						$("#"+btn.id).remove();
					}
				}
				else {
					if (!btn.separator) {
						var btnDiv = document.createElement('div');
						btnDiv.className = 'fbutton';
						if (btn.id){
						  btnDiv.innerHTML = "<div><span id='"+ btn.id +"'>" + btn.name + "</span></div>";
						}else{
						  btnDiv.innerHTML = "<div><span>" + btn.name + "</span></div>";
						}
						
						if (btn.bclass)
							$('span', btnDiv).addClass(btn.bclass).css({
									paddingLeft : 20
								});
						btnDiv.onpress = btn.onpress;
						btnDiv.name = btn.name;
						if (btn.onpress) {
							$(btnDiv).click(function(e) {
								e.preventDefault();
								this.onpress(this.name, g.gDiv);
							});
						}
						$(tDiv2).append(btnDiv);
						if ($.browser.msie && $.browser.version < 7.0) {
							$(btnDiv).hover(function() {
									$(this).addClass('fbOver');
								}, function() {
									$(this).removeClass('fbOver');
								});
						}
					} else {
						$(tDiv2).append("<div class='btnseparator'></div>");
					}
				}
			}
			
			$(g.tDiv).append(tDiv2);
			$(g.tDiv).append("<div style='clear:both'></div>");
			$(g.gDiv).prepend(g.tDiv);
			
			if (p.onToolbarInited)
				p.onToolbarInited();
		}

		// set hDiv
		g.hDiv.className = 'hDiv';

		$(t).before(g.hDiv);

		// set hTable
		g.hTable.cellPadding = 0;
		g.hTable.cellSpacing = 0;
		$(g.hDiv).append('<div class="hDivBox"></div>');
		$('div', g.hDiv).append(g.hTable);
		var thead = $("thead:first", t).get(0);
		if (thead)
			$(g.hTable).append(thead);
		thead = null;

		if (!p.colmodel)
			var ci = 0;

		// setup thead
		$('thead tr:first th', g.hDiv).each(function() {
			var thdiv = document.createElement('div');

			if ($(this).attr('abbr')) {
				$(this).click(function(e) {
						if (isIE11) {
							// console.log("$(this).hasClass('thOver')=" + $(this).hasClass('thOver'));
						}
					    if (!$(this).hasClass('thOver'))
						    return false;
					    var obj = (e.target || e.srcElement);
					    if (obj.href || obj.type)
						    return true;
														
					    g.changeSort(this);
				    });

				if ($(this).attr('abbr') == p.sortname) {
					this.className = 'sorted';
					thdiv.className = 's' + p.sortorder;
				}
			}

			if (this.hide)
				$(this).hide();

			if (!p.colmodel) {
				$(this).attr('axis', 'col' + ci++);
			}

			$(thdiv).css({
				    textAlign : this.align,
				    width : this.width + 'px'
			    });

			thdiv.innerHTML = this.innerHTML;

			$(this).empty().append(thdiv).removeAttr('width').mousedown(function(e) {
				    g.dragStart('colMove', e, this);
			    }).hover(function() {
				if (!g.colresize && !$(this).hasClass('thMove') && !g.colCopy) {
					$(this).addClass('thOver');
					// if (isIE11)
					//	console.log("add Class thOver");
				}

				if ($(this).attr('abbr') != p.sortname && !g.colCopy && !g.colresize
				    && $(this).attr('abbr'))
					$('div', this).addClass('s' + p.sortorder);
				else if ($(this).attr('abbr') == p.sortname && !g.colCopy
				    && !g.colresize && $(this).attr('abbr')) {
					var no = '';
					if (p.sortorder == 'asc')
						no = 'desc';
					else
						no = 'asc';
					$('div', this).removeClass('s' + p.sortorder).addClass('s' + no);
				}

				if (g.colCopy) {
					var n = $('th', g.hDiv).index(this);

					if (n == g.dcoln)
						return false;

					if (n < g.dcoln)
						$(this).append(g.cdropleft);
					else
						$(this).append(g.cdropright);

					g.dcolt = n;

				} else if (!g.colresize) {
					// 计算显示/隐藏列下拉菜单的位置，IE下取其拖拽层的左边缘坐标，计算不准确，会向左偏移
          if (isIE()) {
					  var nv = $('th:visible', g.hDiv).index(this);
					  var onl = parseInt($('div:eq(' + nv + ')', g.cDrag).css('left'));
          }
          else {
					  // chrome下取所浮的th的右侧边缘
					  var onl = $(this).position().left + $(this).width();
          }
					var nw = parseInt($(g.nBtn).width())
					    + parseInt($(g.nBtn).css('borderLeftWidth'));
					nl = onl - nw + Math.floor(p.cgwidth / 2);

					$(g.nDiv).hide();
					$(g.nBtn).hide();

					$(g.nBtn).css({
						    'left' : nl,
						    top : g.hDiv.offsetTop
					    }).show();

					var ndw = parseInt($(g.nDiv).width());

					$(g.nDiv).css({
						    top : g.bDiv.offsetTop
					    });

					if ((nl + ndw) > $(g.gDiv).width())
						$(g.nDiv).css('left', onl - ndw + 1);
					else
						$(g.nDiv).css('left', nl);

					if ($(this).hasClass('sorted'))
						$(g.nBtn).addClass('srtd');
					else
						$(g.nBtn).removeClass('srtd');

				}

			}, function() {
				if (isIE11) {
					// console.log("hover remove class thOver");
				}
				$(this).removeClass('thOver');
				if ($(this).attr('abbr') != p.sortname)
					$('div', this).removeClass('s' + p.sortorder);
				else if ($(this).attr('abbr') == p.sortname) {
					var no = '';
					if (p.sortorder == 'asc')
						no = 'desc';
					else
						no = 'asc';

					$('div', this).addClass('s' + p.sortorder).removeClass('s' + no);
				}
				if (g.colCopy) {
					$(g.cdropleft).remove();
					$(g.cdropright).remove();
					g.dcolt = null;
				}
			}); // wrap content
		});

		// set bDiv
		g.bDiv.className = 'bDiv';
		$(t).before(g.bDiv);
		$(g.bDiv).css({
			    height : (p.height == 'auto') ? 'auto' : p.height + "px"
		    }).scroll(function(e) {
			    g.scroll()
		    }).append(t);

		if (p.height == 'auto') {
			$('table', g.bDiv).addClass('autoht');
		}
		// add td properties
		g.addCellProp();

		// add row properties
		g.addRowProp();

		// add strip
		if (p.striped)
			$('tbody tr:odd', g.bDiv).addClass('erow');
		if (p.resizable && p.height != 'auto') {
			g.vDiv.className = 'vGrip';
			$(g.vDiv).mousedown(function(e) {
				    g.dragStart('vresize', e)
			    }).html('<span></span>');
			$(g.bDiv).after(g.vDiv);
		}

		if (p.resizable && p.width != 'auto' && !p.nohresize) {
			g.rDiv.className = 'hGrip';
			$(g.rDiv).mousedown(function(e) {
				    g.dragStart('vresize', e, true);
			    }).html('<span></span>').css('height', $(g.gDiv).height());
			if ($.browser.msie && $.browser.version < 7.0) {
				$(g.rDiv).hover(function() {
					    $(this).addClass('hgOver');
				    }, function() {
					    $(this).removeClass('hgOver');
				    });
			}
			$(g.gDiv).append(g.rDiv);
		}

		// add pager
		if (p.usepager) {
			g.pDiv.className = 'pDiv';
			g.pDiv.innerHTML = '<div class="pDiv2"></div>';
			$(g.bDiv).after(g.pDiv);
			var html = ' <div class="pGroup"> <div class="pFirst pButton" title="首页"><span></span></div><div class="pPrev pButton" title="上一页"><span></span></div> </div> <div class="btnseparator"></div> <div class="pGroup"><span class="pcontrol">第 <input type="text" size="4" value="1" />页, 共 <span> 1 </span>页</span></div> <div class="btnseparator"></div> <div class="pGroup"> <div class="pNext pButton" title="下一页"><span></span></div><div class="pLast pButton" title="尾页"><span></span></div> </div> <div class="btnseparator"></div> <div class="pGroup"> <div class="pReload pButton"  title="刷新"><span></span></div> </div> <div class="btnseparator"></div> <div class="pGroup"><span class="pPageStat"></span></div>';
			$('div', g.pDiv).html(html);
			//判断当前页是否具有上一页
			var isCanPrev = p.page <= 1 ;
			
			//计算页面总数
			var totalPage = 1;
			if (p.total == 0) {
					totalPage = 1;				
			} else {
					totalPage = Math.ceil(p.total / p.rp);
			}

			//判断当前页面是否具有下一页
			var isCanNext = p.page >= totalPage;
			// alert("page=" + p.page + "--total=" + p.total + "--totalpage=" + totalPage + "--rp=" + p.rp);
			$('.pReload', g.pDiv).click(function() {
					if (onReload)
						onReload();
					else
					    g.populate()
			    });
			
			$('.pFirst', g.pDiv).click(function() {
					g.changePage('first')
			});
			
			//如果具有上一夜 添加点击事件  如果不具有上一页 修改鼠标样式为默认
			if(!isCanPrev) {
				$('.pPrev', g.pDiv).click(function() {
						g.changePage('prev')
				});
			} else {
				$('.pPrev', g.pDiv).css("cursor", "auto");
			}
			//如果具有上一页 添加点击事件  如果不具有上一页 修改鼠标样式为默认
			if(!isCanNext) {
				$('.pNext', g.pDiv).click(function() {
						g.changePage('next')
				});
			} else {
				$('.pNext', g.pDiv).css("cursor","auto");
			}
			$('.pLast', g.pDiv).click(function() {
				    g.changePage('last')
			    });
			$('.pcontrol input', g.pDiv).keydown(function(e) {
				    if (e.keyCode == 13)
					    g.changePage('input')
			    });
			if ($.browser.msie && $.browser.version < 7)
				$('.pButton', g.pDiv).hover(function() {
					    $(this).addClass('pBtnOver');
				    }, function() {
					    $(this).removeClass('pBtnOver');
				    });

			if (p.useRp) {
				var opt = "";
				for (var nx = 0; nx < p.rpOptions.length; nx++) {
					if (p.rp == p.rpOptions[nx])
						sel = 'selected="selected"';
					else
						sel = '';
					opt += "<option value='" + p.rpOptions[nx] + "' " + sel + " >"
					    + p.rpOptions[nx] + "&nbsp;&nbsp;</option>";
				};
				$('.pDiv2', g.pDiv).prepend("<div class='pGroup'><select name='rp'>"
				    + opt + "</select></div> <div class='btnseparator'></div>");
				$('select', g.pDiv).change(function() {
					    if (p.onRpChange)
						    p.onRpChange(+this.value);
					    else {
						    p.newp = 1;
						    p.rp = +this.value;
						    g.populate();
					    }
				    });
			}

			// add search button
			if (p.searchitems) {
				$('.pDiv2', g.pDiv)
				    .prepend("<div class='pGroup'> <div class='pSearch pButton'><span></span></div> </div>  <div class='btnseparator'></div>");
				$('.pSearch', g.pDiv).click(function() {
					    $(g.sDiv).slideToggle('fast', function() {
						        $('.sDiv:visible input:first', g.gDiv).trigger('focus');
					        });
				    });
				// add search box
				g.sDiv.className = 'sDiv';

				sitems = p.searchitems;

				var sopt = "";
				for (var s = 0; s < sitems.length; s++) {
					if (p.qtype == '' && sitems[s].isdefault == true) {
						p.qtype = sitems[s].name;
						sel = 'selected="selected"';
					} else
						sel = '';
					sopt += "<option value='" + sitems[s].name + "' " + sel + " >"
					    + sitems[s].display + "&nbsp;&nbsp;</option>";
				}

				if (p.qtype == '')
					p.qtype = sitems[0].name;

				$(g.sDiv)
				    .append("<div class='sDiv2'>快速查找 <input type='text' size='30' name='q' class='qsbox' /> <select name='qtype'>"
				        + sopt + "</select> <input type='button' value='清除' /></div>");

				$('input[name=q],select[name=qtype]', g.sDiv).keydown(function(e) {
					    if (e.keyCode == 13)
						    g.doSearch()
				    });
				$('input[value="清除"]', g.sDiv).click(function() {
					    $('input[name=q]', g.sDiv).val('');
					    p.query = '';
					    g.doSearch();
				    });
				$(g.bDiv).after(g.sDiv);

			}
			
		}
		
		$(g.pDiv, g.sDiv).append("<div style='clear:both'></div>");

		// add title
		if (p.title) {
			g.mDiv.className = 'mDiv';
			g.mDiv.innerHTML = '<div class="ftitle">' + p.title + '</div>';
			$(g.gDiv).prepend(g.mDiv);
			if (p.showTableToggleBtn) {
				$(g.mDiv)
				    .append('<div class="ptogtitle" title="Minimize/Maximize Table"><span></span></div>');
				$('div.ptogtitle', g.mDiv).click(function() {
					    $(g.gDiv).toggleClass('hideBody');
					    $(this).toggleClass('vsble');
				    });
			}
			// g.rePosDrag();
		}

		// setup cdrops
		g.cdropleft = document.createElement('span');
		g.cdropleft.className = 'cdropleft';
		g.cdropright = document.createElement('span');
		g.cdropright.className = 'cdropright';

		// add block
		g.block.className = 'gBlock';
		var gh = $(g.bDiv).height();
		var gtop = g.bDiv.offsetTop;
		$(g.block).css({
			    width : g.bDiv.style.width,
			    height : gh,
			    background : 'white',
			    position : 'relative',
			    marginBottom : (gh * -1),
			    zIndex : 1,
			    top : gtop,
			    left : '0px'
		    });
		$(g.block).fadeTo(0, p.blockOpacity);

		// add column control
		if ($('th', g.hDiv).length) {

			g.nDiv.className = 'nDiv';
			g.nDiv.innerHTML = "<table cellpadding='0' cellspacing='0'><tbody></tbody></table>";
			$(g.nDiv).css({
				    marginBottom : (gh * -1),
				    display : 'none',
				    top : gtop
			    }).noSelect();

			var cn = 0;

			$('th div', g.hDiv).each(function() {
				var kcol = $("th[axis='col" + cn + "']", g.hDiv)[0];
				var chk = 'checked="checked"';
				if (kcol.style.display == 'none')
					chk = '';

				$('tbody', g.nDiv)
				    .append('<tr><td class="ndcol1"><input type="checkbox" ' + chk
				        + ' class="togCol" value="' + cn
				        + '" /></td><td class="ndcol2">' + this.innerHTML
				        + '</td></tr>');
				cn++;
			});

			if ($.browser.msie && $.browser.version < 7.0)
				$('tr', g.nDiv).hover(function() {
					    $(this).addClass('ndcolover');
				    }, function() {
					    $(this).removeClass('ndcolover');
				    });

			$('td.ndcol2', g.nDiv).click(function() {
				if ($('input:checked', g.nDiv).length <= p.minColToggle
				    && $(this).prev().find('input')[0].checked)
					return false;
				return g.toggleCol($(this).prev().find('input').val());
			});

			$('input.togCol', g.nDiv).click(function() {

				if ($('input:checked', g.nDiv).length < p.minColToggle
				    && this.checked == false)
					return false;
				$(this).parent().next().trigger('click');
				  // return false;
			});

			$(g.gDiv).prepend(g.nDiv);

			$(g.nBtn).addClass('nBtn').html('<div></div>').attr('title',
			    '隐藏/显示列').click(function() {
				    $(g.nDiv).toggle();
				    return true;
			    });

			if (p.showToggleBtn)
				$(g.gDiv).prepend(g.nBtn);

		}
		
		if (p.checkbox) {
			// 添加表头多选框
			$('tr', g.hDiv).each(function() {
				var cth = $('<th />');

				var cthch = $('<input type="checkbox"/>');

				cthch.click(function() {
					if (this.checked) {
						$('tbody tr', g.bDiv).each(function() {
							var chkObj = $(this).addClass('trSelected').find('input')[0];
							if (chkObj && !chkObj.disabled) {
								chkObj.checked = true;
							}
						})
					} else {
						$('tbody tr', g.bDiv).each(function() {
							var chkObj = $(this).removeClass('trSelected').find('input')[0];
							if (chkObj) {
								chkObj.checked = false;
							}
						})
					}
				})
				
				var tdDiv = $('<div/>');
				tdDiv.addClass("cth").css({
						width : "22px"
					}).append(cthch);
				cth.append(tdDiv);

				$(this).prepend(cth);
			})
			// 添加每行的多选框
			$('tr', g.bDiv).each(function() {
				var cth = $('<td/>');

				var cthch = $('<input type="checkbox" value="' + $(this).attr('id') + '"/>');

				var tdDiv = $('<div/>');
				tdDiv.addClass("cth").css({
						width : "22px"
					}).append(cthch);
				cth.append(tdDiv);
				$(this).prepend(cth);
			})
		};

		// set cDrag
		var cdcol = $('thead tr:first th:first', g.hDiv).get(0);
		if (cdcol != null) {
			g.cDrag.className = 'cDrag';
			g.cdpad = 0;

			g.cdpad += (isNaN(parseInt($('div', cdcol).css('borderLeftWidth')))
			    ? 0
			    : parseInt($('div', cdcol).css('borderLeftWidth')));
			g.cdpad += (isNaN(parseInt($('div', cdcol).css('borderRightWidth')))
			    ? 0
			    : parseInt($('div', cdcol).css('borderRightWidth')));
			g.cdpad += (isNaN(parseInt($('div', cdcol).css('paddingLeft')))
			    ? 0
			    : parseInt($('div', cdcol).css('paddingLeft')));
			g.cdpad += (isNaN(parseInt($('div', cdcol).css('paddingRight')))
			    ? 0
			    : parseInt($('div', cdcol).css('paddingRight')));
			g.cdpad += (isNaN(parseInt($(cdcol).css('borderLeftWidth')))
			    ? 0
			    : parseInt($(cdcol).css('borderLeftWidth')));
			g.cdpad += (isNaN(parseInt($(cdcol).css('borderRightWidth')))
			    ? 0
			    : parseInt($(cdcol).css('borderRightWidth')));
			g.cdpad += (isNaN(parseInt($(cdcol).css('paddingLeft')))
			    ? 0
			    : parseInt($(cdcol).css('paddingLeft')));
			g.cdpad += (isNaN(parseInt($(cdcol).css('paddingRight')))
			    ? 0
			    : parseInt($(cdcol).css('paddingRight')));

			$(g.bDiv).before(g.cDrag);

			var cdheight = $(g.bDiv).height();
			var hdheight = $(g.hDiv).height();

			$(g.cDrag).css({
				    top : -hdheight + 'px'
			    });

			$('thead tr:first th', g.hDiv).each(function() {
				    var cgDiv = document.createElement('div');
				    $(g.cDrag).append(cgDiv);
				    if (!p.cgwidth)
					    p.cgwidth = $(cgDiv).width();
				    $(cgDiv).css({
					        height : cdheight + hdheight
				        }).mousedown(function(e) {
					        g.dragStart('colresize', e, this);
				        });
				    if ($.browser.msie && $.browser.version < 7.0) {
					    g.fixHeight($(g.gDiv).height());
					    $(cgDiv).hover(function() {
						        g.fixHeight();
						        $(this).addClass('dragging')
					        }, function() {
						        if (!g.colresize)
							        $(this).removeClass('dragging')
					        });
				    }
			    });

			// g.rePosDrag();

		}
		
		// add date edit layer
		$(g.iDiv).addClass('iDiv').css({
			    display : 'none'
		    });
		$(g.bDiv).append(g.iDiv);

		// add flexigrid events
		$(g.bDiv).hover(function() {
			    $(g.nDiv).hide();
			    $(g.nBtn).hide();
		    }, function() {
			    if (g.multisel)
				    g.multisel = false;
		    });
		$(g.gDiv).hover(function() {
		    }, function() {
			    $(g.nDiv).hide();
			    $(g.nBtn).hide();
		    });

		// add document events
		$(document).mousemove(function(e) {
			    g.dragMove(e)
		    }).mouseup(function(e) {
			    g.dragEnd()
		    }).hover(function() {
		    }, function() {
			    g.dragEnd()
		    });

		// browser adjustments
		if ($.browser.msie && $.browser.version < 7.0) {
			$('.hDiv,.bDiv,.mDiv,.pDiv,.vGrip,.tDiv, .sDiv', g.gDiv).css({
				    width : '100%'
			    });
			$(g.gDiv).addClass('ie6');
			if (p.width != 'auto')
				$(g.gDiv).addClass('ie6fullwidthbug');
		}

		g.rePosDrag();
		g.buildpager();
		g.fixHeight();
		g.fixIEBug();

		// make grid functions accessible
		t.p = p;
		t.grid = g;

		// load data
		if (p.url && p.autoload) {
			g.populate();
		}
		try {
			g.initHeaderContextMenu();
		}
		catch (e) {
			if (isIE()) {
				consoleLog("flexigrid.js: Bootstrap Menu is not included.");
			}
		}

		t.windowResize = function (widthTemp,heightTemp) {
			this.p.width = widthTemp;
			this.p.height = heightTemp;
			this.grid.rePosDrag();
			this.grid.buildpager();
			this.grid.fixHeight();
			this.grid.fixIEBug();
		}

		return t;

	};

	// 窗口是否已加载好
	var docloaded = false;
	$(document).ready(function() {
		docloaded = true;	
	});
	
	$.fn.flexigrid = function(p) {
		return this.each(function() {
			    if (!docloaded) {
				    $(this).hide();
				    var t = this; // t为table
				    $(document).ready(function() {
					        var tab = $.addFlex(t, p);
							$(window).resize(function() {
							  	// 如果是用于desktop界面
								var isDeskTop = typeof(window.top.myDesktop)=="object";
								if(isDeskTop || typeof(tab.isIEBugResize) == "undefined" || !tab.isIEBugResize ) {
									var w = document.documentElement.clientWidth;
									var h = document.documentElement.clientHeight - 84;
									tab.windowResize(w,h);
								} else {
									tab.isIEBugResize = false;

									var w = document.documentElement.clientWidth;
									var h = document.documentElement.clientHeight - 84;
									tab.windowResize(w,h);
								}
							});
				     });
			    } else {
					var tab = $.addFlex(this, p);
					tab.isIEBugResize = false;
					$(window).resize(function () {
						var w = document.documentElement.clientWidth;
						var h = document.documentElement.clientHeight - 84;
						tab.windowResize(w, h);
					});
			    }
		    });

	}; // end flexigrid

	$.fn.flexReload = function(p) { // function to reload grid

		return this.each(function() {
			    if (this.grid && this.p.url) {
				    this.grid.populate();
				}
		    });

	}; // end flexReload

	$.fn.flexUnFreeze = function() {
		var tableId = $(this).attr('id');
		return this.each(function() {
			if (this.grid) {
				var divTableHFixed = tableId + '_h_fixed';
				$('#' + divTableHFixed).remove();
				var divTableFixed = tableId + '_fixed';
				$('#' + divTableFixed).remove();
			}
		});
	};

	$.fn.flexFreeze = function (fixedColNum) {
		var tableId = $(this).attr('id');
		return this.each(function () {
			if (this.grid) {
				this.p.checkOnRowClick = false; // 点击行时选中需置为false，否则选中后，如果复制的checkbox也选中，会导致重复

				var $hDiv = $(this.grid.hDiv);

				var colWidth = 0;
				$("#" + tableId + " tr:last td:lt(" + fixedColNum + ")").each(function () {
					colWidth += $(this).outerWidth(true);
				});

				// --------------处理表头部分----------------
				// 复制表头
				// var headerCloned = $('.hDiv').find('table').clone();
				var headerCloned = $hDiv.find('table').clone();

				$hDivBox = $hDiv.find('.hDivBox');
				// 将复制的表头置于表头冻结区divTableHFixed，并将该div加至.hDivBox
				var divTableHFixed = tableId + '_h_fixed';
				// 如果原来存在则删除，以免多次调用后产生问题
				$('#' + divTableHFixed).remove();
				$('<div id="' + divTableHFixed + '">').append(headerCloned).appendTo($hDivBox);

				// 因为表头部分的table是不带有背景色的，因此需把hDiv的背景色复制过来
				$('#' + divTableHFixed).css("background-color", $hDiv.css("background-color"));

				// 置数据冻结区的宽度、高度等样式，使其与hDiv的左上角保持一致
				$("#" + divTableHFixed).css("width", colWidth);
				var height = $hDiv.find('table').height();
				$("#" + divTableHFixed).css({"overflow": "hidden", "height": height, "position": "relative", "z-index": "500", "border-right": "1px solid #00aa00"});
				$("#" + divTableHFixed).offset({top: $hDiv.offset().top + 1, left: $hDiv.offset().left + 1});

				// 表头冻结区的滚动处理
				$hDiv.scroll(function () {
					$("#" + divTableHFixed).offset({top: $(this).scrollTop, left: $(this).offset().left + 1});
					if (colWidth <= $("#" + divTableHFixed).scrollLeft()) {
						// return false;
					}
				});

				// ------------------处理表格的数据部分-----------------------
				// 复制表格数据部分
				var tableCloned = $('#' + tableId).clone();

				// 将复制的表格置于数据冻结区divTableFixed中，并将该div加至.bDiv
				var $bDiv = $(this.grid.bDiv);
				var divTableFixed = tableId + '_fixed';
				// 如果原来存在则删除，以免多次调用后产生问题
				$('#' + divTableFixed).remove();
				$('<div id="' + divTableFixed + '">').append(tableCloned).appendTo($bDiv);

				// 置数据冻结区的宽度、高度等样式，使其与bDiv的左上角保持一致，bDiv的左上角top始终为0
				$("#" + divTableFixed).css("width", colWidth);
				var height = $('#' + tableId).height();
				$("#" + divTableFixed).css({"overflow": "hidden", "height": height, "position": "relative", "z-index": "500", "border-right": "1px solid #00aa00", "background-color": "Silver"});
				$("#" + divTableFixed).offset({top: $bDiv.offset().top + 1, left: $bDiv.offset().left + 1});

				// 表格数据冻结区的滚动事件处理
				$bDiv.scroll(function () {
					$("#" + divTableFixed).offset({top: $(this).scrollTop, left: $(this).offset().left + 1});
				});

				// 置表头上checkbox全选事件，仅选择被克隆的部分，以免与原有的一起checkbox被选中，造成重复
				headerCloned.find('input:checkbox').each(function () {
					$(this).click(function () {
						if (this.checked) {
							$('tbody tr', tableCloned).each(function () {
								var chkObj = $(this).addClass('trSelected').find('input')[0];
								if (chkObj && !chkObj.disabled) {
									chkObj.checked = true;
								}
							})
						} else {
							$('tbody tr', tableCloned).each(function () {
								var chkObj = $(this).removeClass('trSelected').find('input')[0];
								if (chkObj) {
									chkObj.checked = false;
								}
							})
						}
					})
				});

				// 使鼠标浮到行上时候展现的效果在两个区域内同步，否则只会显示冻结区域或数据区域的效果，看起来表格好像被从中间截成了两半
				var $trs = $('#' + tableId).find('tr');
				$('tbody tr', $('#' + divTableFixed)).each(function (i) {
					$(this).hover(function () {
						$(this).addClass('trOver');
						$trs.eq(i).addClass('trOver');
					}, function () {
						$(this).removeClass('trOver');
						$trs.eq(i).removeClass('trOver');
					});
				});
				var $trsFiexed = $('#' + divTableFixed).find('tr');
				$('tbody tr', $('#' + tableId)[0]).each(function (i) {
					$(this).hover(function () {
						$(this).addClass('trOver');
						$trsFiexed.eq(i).addClass('trOver');
					}, function () {
						$(this).removeClass('trOver');
						$trsFiexed.eq(i).removeClass('trOver');
					});
				});


			}
		});
	}

	$.fn.flexOptions = function(p) { // function to update general options

		return this.each(function() {
			    if (this.grid)
				    $.extend(this.p, p);
		    });

	}; // end flexOptions

	$.fn.flexToggleCol = function(cid, visible) { // function to reload grid

		return this.each(function() {
			    if (this.grid)
				    this.grid.toggleCol(cid, visible);
		    });

	}; // end flexToggleCol

	$.fn.flexAddData = function(data) { // function to add data to grid

		return this.each(function() {
			    if (this.grid)
				    this.grid.addData(data);
		    });

	};

	$.fn.noSelect = function(p) { // no select plugin by me :-)

		if (p == null)
			prevent = true;
		else
			prevent = p;

		if (prevent) {

			return this.each(function() {
				    if ($.browser.msie || $.browser.safari)
					    $(this).bind('selectstart', function() {
						        return false;
					        });
				    else if ($.browser.mozilla) {
					    $(this).css('MozUserSelect', 'none');
					    $('body').trigger('focus');
				    } else if ($.browser.opera)
					    $(this).bind('mousedown', function() {
						        return false;
					        });
				    else
					    $(this).attr('unselectable', 'on');
			    });

		} else {

			return this.each(function() {
				    if ($.browser.msie || $.browser.safari)
					    $(this).unbind('selectstart');
				    else if ($.browser.mozilla)
					    $(this).css('MozUserSelect', 'inherit');
				    else if ($.browser.opera)
					    $(this).unbind('mousedown');
				    else
					    $(this).removeAttr('unselectable', 'on');
			    });

		}

	}; // end noSelect

	// 取得options
	$.fn.getOptions = function() {
		var p;
		this.each(function() {
				if (this.grid)
					p = this.p;
		    });
		return p;
	}; // end getOptions
	
	/*
	$.fn.windowResize = function(w, h) {
		var t;
		this.each(function() {
				if (this.grid) {
					this.windowResize(w, h);
				}
		    });
		return p;
	}; // end getOptions
	*/
	
})(jQuery);