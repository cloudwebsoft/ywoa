<template>
	<view class="uni-cursor-point ">
		<view v-if="(popMenu && (leftBottom||rightBottom||leftTop||rightTop) && 0 < content.length) && showMenu" 
			:class="{
			  'lc-fab-touch--leftBottom': leftBottom,
			  'lc-fab-touch--rightBottom': rightBottom,
			  'lc-fab-touch--leftTop': leftTop,
			  'lc-fab-touch--rightTop': rightTop
			}"
		 class="lc-fab-touch " :style="{top : contTop2+'px',left :contLeft2}" style="height: 35px;border: 0px solid green;">
			<view :class="{
	      'lc-fab-touch__content--left': horizontal2 === 'left',
	      'lc-fab-touch__content--right': horizontal2 === 'right',
	      'lc-fab-touch__content--flexDirection': direction === 'vertical',
	      'lc-fab-touch__content--flexDirectionStart': flexDirectionStart,
	      'lc-fab-touch__content--flexDirectionEnd': flexDirectionEnd,
		  'lc-fab-touch__content--other-platform': !isAndroidNvue
	    }"
			 :style="{ width: boxWidth, height: boxHeight, backgroundColor: styles.backgroundColor}" class="lc-fab-touch__content"
			 elevation="5" style="border: 0px solid red;">
				<view v-if="flexDirectionStart || horizontalLeft" class="lc-fab-touch__item lc-fab-touch__item--first" />
				<view v-for="(item, index) in content" :key="index" :class="{ 'lc-fab-touch__item--active': isShow }" class="lc-fab-touch__item"
				 @click="_onItemClick(index, item)">
					<image :src="item.active ? item.selectedIconPath : item.iconPath" class="lc-fab-touch__item-image" mode="widthFix" />
					<text class="lc-fab-touch__item-text" :style="{ color: item.active ? styles.selectedColor : styles.color }">{{ item.text }}</text>
				</view>
				<view v-if="flexDirectionEnd || horizontalRight" class="lc-fab-touch__item lc-fab-touch__item--first" />
			</view>
		</view>
		
		<view 
			:class="[{
			  'lc-fab-touch__circle--leftBottom': leftBottom,
			  'lc-fab-touch__circle--rightBottom': rightBottom,
			  'lc-fab-touch__circle--leftTop': leftTop,
			  'lc-fab-touch__circle--rightTop': rightTop,
			  'lc-fab-touch__content--other-platform': !isAndroidNvue,
			  'active':!dragMoveSatus
			}]"
		 class="lc-fab-touch__circle lc-fab-touch__plus drag-ball-box" :style="{ 'background-color': styles.buttonColor,left:left+'px',top :top+'px'} " 
		 @click="_onClick" id="drag-ball" @touchstart="dragStart" @touchmove="dragMove" @touchend="dragEnd">
			<view class="fab-circle-v"  :class="{'lc-fab-touch__plus--active': isShow && content.length > 0}"></view>
			<view class="fab-circle-h" :class="{'lc-fab-touch__plus--active': isShow  && content.length > 0}"></view>
		</view>
	</view>
</template>

<script>
	let platform = 'other'
	// #ifdef APP-NVUE
	platform = uni.getSystemInfoSync().platform
	// #endif

	/**
	 * Fab 悬浮按钮
	 * @description 点击可展开一个图形按钮菜单
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=144
	 * @property {Object} pattern 可选样式配置项
	 * @property {Object} horizontal = [left | right] 水平对齐方式
	 * 	@value left 左对齐
	 * 	@value right 右对齐
	 * @property {Object} vertical = [bottom | top] 垂直对齐方式
	 * 	@value bottom 下对齐
	 * 	@value top 上对齐
	 * @property {Object} direction = [horizontal | vertical] 展开菜单显示方式
	 * 	@value horizontal 水平显示
	 * 	@value vertical 垂直显示
	 * @property {Array} content 展开菜单内容配置项
	 * @property {Boolean} popMenu 是否使用弹出菜单
	 * @event {Function} trigger 展开菜单点击事件，返回点击信息
	 * @event {Function} fabClick 悬浮按钮点击事件
	 */
	export default {
		name: 'UniFab',
		props: {
			pattern: {
				type: Object,
				default () {
					return {}
				}
			},
			horizontal: {
				type: String,
				default: 'right'
			},
			vertical: {
				type: String,
				default: 'bottom'
			},
			direction: {
				type: String,
				default: 'horizontal'
			},
			content: {
				type: Array,
				default () {
					return []
				}
			},
			show: {
				type: Boolean,
				default: false
			},
			popMenu: {
				type: Boolean,
				default: true
			}
		},
		data() {
			return {
				fabShow: false,
				isShow: false,
				isAndroidNvue: platform === 'android',
				styles: {
					color: '#3c3e49',
					selectedColor: '#007AFF',
					backgroundColor: '#fff',
					buttonColor: '#007AFF'
				},
				
				showMenu: false,
				horizontal2: 'left',
				vertical2: 'bottom',
				
				orientation: 'portrait',
				safeArea: {
				  minTop: 0,
				  maxTop: 0,
				  minLeft: 0,
				  maxLeft: 0,
				  windowWidth: 0,
				  windowHeight: 0
				},
				dragMoveSatus: true,
				dragEndInfo: {
				  type: null,
				  coordinate: null
				},
				width: 0,
				height: 0,
				left: 0,
				top: 0
			}
		},
		computed: {
			contLeft2() {
				if(this.horizontal2 == 'right') {
					// console.log('left'+this.left)
					if(this.left == this.safeArea.maxLeft || this.left >= this.safeArea.maxLeft-20 ) {
						return this.left+30 + 'px';
					} else {
						if(this.direction == 'vertical') { // 垂直
							return this.left + 'px';
						} else {
							return this.left - (this.content.length) * 55-10 + 'px';
						}
						return 'unset';
					}
				} else {
					return this.left - 10+'px';
				}
			},
			contTop2() {
				if(this.direction == 'vertical') {
					if(this.vertical2 == 'top') {
						return this.top + ((this.content.length) * 30);
					}
					return this.top - ((this.content.length) * 30);
				}
				return this.top;
			},
			contentWidth(e) {
				return (this.content.length + 1) * 55 + 10 + 'px'
			},
			contentWidthMin() {
				return 55 + 'px'
			},
			// 动态计算宽度
			boxWidth() {
				return this.getPosition(3, 'horizontal')
			},
			// 动态计算高度
			boxHeight() {
				return this.getPosition(3, 'vertical')
			},
			// 计算左下位置
			leftBottom() {
				return this.getPosition(0, 'left', 'bottom')
			},
			// 计算右下位置
			rightBottom() {
				return this.getPosition(0, 'right', 'bottom')
			},
			// 计算左上位置
			leftTop() {
				return this.getPosition(0, 'left', 'top')
			},
			rightTop() {
				return this.getPosition(0, 'right', 'top')
			},
			flexDirectionStart() {
				return this.getPosition(1, 'vertical', 'top')
			},
			flexDirectionEnd() {
				return this.getPosition(1, 'vertical', 'bottom')
			},
			horizontalLeft() {
				return this.getPosition(2, 'horizontal', 'left')
			},
			horizontalRight() {
				return this.getPosition(2, 'horizontal', 'right')
			}
		},
		watch: {
			pattern(newValue, oldValue) {
				//console.log(JSON.stringify(newValue))
				this.styles = Object.assign({}, this.styles, newValue)
			}
		},
		created() {
			this.isShow = this.show;
			if (this.top === 0) {
				this.fabShow = true
			}
			// 初始化样式
			this.styles = Object.assign({}, this.styles, this.pattern);
			this.horizontal2 = this.horizontal;
			this.vertical2 = this.vertical;
		},
		mounted: function() {
		  this.initDragBall();
		},
		methods: {
			_onClick() {
				this.$emit('fabClick')
				if (!this.popMenu) {
					return
				}
				
				if(this.showMenu) {
					this.showMenu = false;
				} else {
					this.showMenu = true;
				}
				this.isShow = !this.isShow;
				
				// linc 20210714 当展开菜单时不自动隐藏
				if(!this.showMenu) {
					// 隐藏
					setTimeout(function(that) {
					  if(!that.dragMoveSatus && !that.showMenu){
					    that[that.dragEndInfo.type] = that.dragEndInfo.coordinate;
					  }
					}, 1500, this);
				}
				if(this.left >= this.safeArea.maxLeft-40) {
					this.horizontal2 ='right';
					// this.horizontal ='right';
				}
				if(this.left <= this.safeArea.minLeft+30) {
					this.horizontal2 ='left';
					// this.horizontal ='left';
				}
			},
			open() {
				this.isShow = true;
			},
			close() {
				this.isShow = false;
			},
			/**
			 * 按钮点击事件
			 */
			_onItemClick(index, item) {
				this._onClick();
				if(item.type == 'type') {
					uni.pageScrollTo({
						scrollTop: 0,
						duration: 300
					});
				} else {
					this.$emit('trigger', {
						index,
						item
					})
				}
			},
			/**
			 * 获取 位置信息
			 */
			getPosition(types, paramA, paramB) {
				if (types === 0) {
					return this.horizontal2 === paramA && this.vertical2 === paramB
				} else if (types === 1) {
					return this.direction === paramA && this.vertical2 === paramB
				} else if (types === 2) {
					return this.direction === paramA && this.horizontal2 === paramB
				} else {
					return this.isShow && this.direction === paramA ? this.contentWidth : this.contentWidthMin
				}
			},
			
			// 计算悬浮球初始位置
			initDragBall(){
			  console.log('initDragBall');
			  let that = this;
			  try {
			    uni.createSelectorQuery().in(this).select('#drag-ball').fields({
			      size: true
			    }, dragBallSize => {
			      that.width = dragBallSize.width;
			      that.height = dragBallSize.height;
			      uni.getSystemInfo({
			        success: function(res) {
			          that.safeArea.windowWidth = res.windowWidth;
			          that.safeArea.windowHeight = res.windowHeight;
			          if (that.orientation === 'landscape') {
			            that.safeArea.minTop = -parseInt(dragBallSize.height / 2);
			            that.safeArea.maxTop = res.windowHeight - parseInt(dragBallSize.height / 2);
			            that.safeArea.minLeft = parseInt(0.06 * res.windowWidth);
			            that.safeArea.maxLeft = parseInt(0.94 * res.windowWidth) - dragBallSize.width;
			            that.left = that.safeArea.minLeft;
			            that.top = that.safeArea.minTop;
			            // that.left = that.safeArea.maxLeft;
			            // that.top = that.safeArea.maxTop;
			          } else {
			            that.safeArea.minTop = parseInt(0.06 * res.windowHeight);
			            that.safeArea.maxTop = parseInt(0.94 * res.windowHeight) - dragBallSize.height;
			            that.safeArea.minLeft = -parseInt(dragBallSize.width / 2);
			            that.safeArea.maxLeft = res.windowWidth - parseInt(dragBallSize.width / 2);
			            that.left = that.safeArea.maxLeft;
			            that.top = that.safeArea.minTop;
						if(that.vertical2 == 'bottom') {
							that.top = that.safeArea.maxTop / 1.2;
						}
			            // that.left = that.safeArea.minLeft;
			            // that.top = that.safeArea.maxTop;
			          }
			          that.showDragBall();
			        }
			      });
			    }).exec();
			  } catch (e) {
			    //TODO handle the exception
			    console.log(e);
			  }
			},
			showDragBall() {
			  setTimeout(function(that) {
			    that.dragMoveSatus = false;
			  }, 1000, this);
			},
			// 手指触摸悬浮球动作开始
			dragStart(event) {
			  if (this.orientation === 'landscape') {
			    if (this.top === this.safeArea.minTop) {
			      this.top += parseInt(this.height / 2); // 上面
			    } else if (this.top === this.safeArea.maxTop) {
			      this.top -= parseInt(this.height / 2); // 下面
			    }
			  } else {
			    if (this.left === this.safeArea.minLeft) {
			      this.left += parseInt(this.width / 2); // 左面
			    } else if (this.left === this.safeArea.maxLeft) {
			      this.left -= parseInt(this.width / 2); // 右面
			    }
			  }
			},
			// 手指触摸后移动
			dragMove(event) {
			  if (!this.dragMoveSatus) {
			    this.dragMoveSatus = true;
			  }
			  // 定位触摸焦点及移动位置
			  this.top = event.touches[0].clientY - parseInt(this.height / 2);
			  this.left = event.touches[0].clientX - parseInt(this.width / 2);
			},
			// 手指触摸悬浮球动作结束，贴边并延迟隐藏
			dragEnd(event) {
			  this.dragMoveSatus = false;
			  if (this.orientation === 'landscape') {
			    let halfWindowHeight = parseInt(this.safeArea.windowHeight / 2),
			      halfDragBallHeight = parseInt(this.height / 2);
			    this.dragEndInfo.type = 'top';
			    if (this.top < halfWindowHeight) {
			      this.dragEndInfo.coordinate = this.safeArea.minTop;
			      this.top = this.safeArea.minTop + halfDragBallHeight;
				  this.vertical2 = 'top'; // 根据拖拽后高度自动适配展示模式
			    } else {
			      this.dragEndInfo.coordinate = this.safeArea.maxTop;
			      this.top = this.safeArea.maxTop - halfDragBallHeight;
				  this.vertical2 = 'bottom'; // 根据拖拽后高度自动适配展示模式
			    }
			  } else {
			    let halfWindowWidth = parseInt(this.safeArea.windowWidth / 2),
			      halfDragBallWidth = parseInt(this.width / 2);
			    this.dragEndInfo.type = 'left';
			    if (this.left < halfWindowWidth) {
			      this.dragEndInfo.coordinate = this.safeArea.minLeft;
			      this.left = this.safeArea.minLeft + halfDragBallWidth;
				  this.horizontal2 = 'left'; // 根据拖拽后高度自动适配展示模式
			    } else {
			      this.dragEndInfo.coordinate = this.safeArea.maxLeft;
			      this.left = this.safeArea.maxLeft - halfDragBallWidth;
				  this.horizontal2 = 'right'; // 根据拖拽后高度自动适配展示模式
			    }
				
				let halfWindowHeight = parseInt(this.safeArea.windowHeight / 2);
				if (this.top < halfWindowHeight) {
				  this.vertical2 = 'top';
				} else {
				  this.vertical2 = 'bottom';
				}
			  }
			
			  // 位置容错计算
			  if (this.top < this.safeArea.minTop) {
			    this.top = this.safeArea.minTop;
			  } else if (this.top > this.safeArea.maxTop) {
				this.top = this.safeArea.maxTop;
			  }
			  if (this.left < this.safeArea.minLeft) {
			    this.left = this.safeArea.minLeft;
				console.log(this.left + 'right');
			  } else if (this.left > this.safeArea.maxLeft) {
			    this.left = this.safeArea.maxLeft;
				console.log(this.left + 'left');
			  }
			  // console.log(this.vertical2 + this.horizontal2);
			}
		}
	}
</script>

<style lang="scss" scoped>
	.lc-fab-touch {
		position: fixed;
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		justify-content: center;
		align-items: center;
		z-index: 10;
	}
	
	.uni-cursor-point {
		/* #ifdef H5 */
		cursor: pointer;
		/* #endif */
	}
	
	.lc-fab-touch--active {
		opacity: 1;
	}
	
	.lc-fab-touch--leftBottom {
		left: 5px;
		bottom: 20px;
		/* #ifdef H5 */
		left: calc(5px + var(--window-left));
		bottom: calc(20px + var(--window-bottom));
		/* #endif */
		padding: 10px;
	}
	
	.lc-fab-touch--leftTop {
		left: 5px;
		top: 30px;
		/* #ifdef H5 */
		left: calc(5px + var(--window-left));
		top: calc(30px + var(--window-top));
		/* #endif */
		padding: 10px;
	}
	
	.lc-fab-touch--rightBottom {
		right: 0px;
		bottom: 20px;
		/* #ifdef H5 */
		// right: calc(5px + var(--window-right));
		bottom: calc(20px + var(--window-bottom));
		/* #endif */
		padding: 10px 0;
	}
	
	.lc-fab-touch--rightTop {
		// right: 0px;
		top: 30px;
		/* #ifdef H5 */
		// right: calc(5px + var(--window-right));
		top: calc(30px + var(--window-top));
		/* #endif */
		padding: 0;
		padding-top: 10px;
	}
	
	.lc-fab-touch__circle {
		position: fixed;
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		justify-content: center;
		align-items: center;
		width: 55px;
		height: 55px;
		background-color: #3c3e49;
		border-radius: 55px;
		z-index: 11;
	}
	
	.lc-fab-touch__circle--leftBottom {
		left: 15px;
		bottom: 30px;
		/* #ifdef H5 */
		left: calc(15px + var(--window-left));
		bottom: calc(30px + var(--window-bottom));
		/* #endif */
	}
	
	.lc-fab-touch__circle--leftTop {
		left: 15px;
		top: 40px;
		/* #ifdef H5 */
		left: calc(15px + var(--window-left));
		top: calc(40px + var(--window-top));
		/* #endif */
	}
	
	.lc-fab-touch__circle--rightBottom {
		right: 15px;
		bottom: 30px;
		/* #ifdef H5 */
		right: calc(15px + var(--window-right));
		bottom: calc(30px + var(--window-bottom));
		/* #endif */
	}
	
	.lc-fab-touch__circle--rightTop {
		right: 15px;
		top: 40px;
		/* #ifdef H5 */
		right: calc(15px + var(--window-right));
		top: calc(40px + var(--window-top));
		/* #endif */
	}
	
	.lc-fab-touch__circle--left {
		left: 0;
	}
	
	.lc-fab-touch__circle--right {
		right: 0;
	}
	
	.lc-fab-touch__circle--top {
		top: 0;
	}
	
	.lc-fab-touch__circle--bottom {
		bottom: 0;
	}
	
	.lc-fab-touch__plus {
		font-weight: bold;
	}
	
	.fab-circle-v {
		position: absolute;
		width: 3px;
		height: 31px;
		left: 26px;
		top: 12px;
		background-color: white;
		transform: rotate(0deg);
		transition: transform 0.3s;
	}
	
	.fab-circle-h {
		position: absolute;
		width: 31px;
		height: 3px;
		left: 12px;
		top: 26px;
		background-color: white;
		transform: rotate(0deg);
		transition: transform 0.3s;
	}
	
	.lc-fab-touch__plus--active {
		transform: rotate(135deg);
	}
	
	.lc-fab-touch__content {
		/* #ifndef APP-NVUE */
		box-sizing: border-box;
		display: flex;
		/* #endif */
		flex-direction: row;
		border-radius: 55px;
		overflow: hidden;
		transition-property: width, height;
		transition-duration: 0.2s;
		width: 55px;
		border-color: #DDDDDD;
		border-width: 1rpx;
		border-style: solid;
	}
	
	.lc-fab-touch__content--other-platform {
		border-width: 0px;
		box-shadow: 0 0 5px 2px rgba(0, 0, 0, 0.2);
	}
	
	.lc-fab-touch__content--left {
		justify-content: flex-start;
	}
	
	.lc-fab-touch__content--right {
		justify-content: flex-end;
	}
	
	.lc-fab-touch__content--flexDirection {
		flex-direction: column;
		justify-content: flex-end;
	}
	
	.lc-fab-touch__content--flexDirectionStart {
		flex-direction: column;
		justify-content: flex-start;
	}
	
	.lc-fab-touch__content--flexDirectionEnd {
		flex-direction: column;
		justify-content: flex-end;
	}
	
	.lc-fab-touch__item {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: column;
		justify-content: center;
		align-items: center;
		width: 55px;
		height: 55px;
		opacity: 0;
		transition: opacity 0.2s;
	}
	
	.lc-fab-touch__item--active {
		opacity: 1;
	}
	
	.lc-fab-touch__item-image {
		width: 25px;
		height: 25px;
		margin-bottom: 3px;
	}
	
	.lc-fab-touch__item-text {
		color: #FFFFFF;
		font-size: 12px;
	}
	
	.lc-fab-touch__item--first {
		width: 55px;
	}
	
	.drag-ball-box {
	  position: fixed;
	  overflow: hidden;
	  border-radius: 50%;
	  z-index: 109;
	
	  &.portrait {
	    width: 55px;
	    height: 55px;
	  }
	
	  &.landscape {
	    width: 40rpx;
	    height: 40rpx;
	  }
	
	  &.active {
	    transition: .3s;
	  }
	
	  .item-group {
	    width: 100%;
	    height: 100%;
	
	    .item-image {
	      width: 100%;
	      height: 100%;
	    }
	  }
	}
</style>
