<template>
	<view class="pageStyle">
		<view class="form_conter">
			<uni-forms ref="form" :modelValue="model" :label-width="108">
				<template v-for="(item, index) in fields">
					<uni-forms-item :label="item.title" :key="index">
						<view v-if="item.type == 'text'"><u-input v-model="item.value" type="text" input-align="left" placeholder="无" disabled /></view>
						<view v-else-if="item.type == 'macro'" class="ver-item-center viewItem">
							<view v-if="item.macroCode == 'macro_raty'">
								<u-rate active-color="#FFCA2A" inactive-color="#D9D9D9" v-model="item.value" :disabled="setDisabled"></u-rate>
							</view>
							<view v-else-if="item.macroCode == 'macro_attachment'">
								<view style="margin-bottom:10rpx" v-for="(fi, fin) in fileList" :key="fin" @click="viewFile(fi, fin)">{{ fi.name }}</view>
							</view>
							<view v-else-if="item.macroCode == 'module_field_select'">
								<u-input v-model="item.value" type="text" input-align="left" placeholder="无" disabled />
							</view>
							<view v-else><u-input v-model="item.value" type="text" input-align="left" placeholder="无" disabled /></view>
						</view>
						<view v-else-if="item.type == 'DATE'"><u-input v-model="item.value" type="text" input-align="left" placeholder="无" disabled /></view>
						<view v-else-if="item.type == 'checkbox'" class="ver-item-center viewItem">
							<u-checkbox-group><u-checkbox disabled v-model="item.value"></u-checkbox></u-checkbox-group>
						</view>
						<view v-else-if="item.type == 'textarea'"><u-input v-model="item.value" type="textarea" input-align="left" placeholder="无" disabled /></view>
						<view v-else-if="item.type == 'select'"><u-input v-model="item.value" type="text" input-align="left" placeholder="无" disabled /></view>

						<view v-else-if="item.type == 'DATE_TIME'"><u-input v-model="item.value" type="text" input-align="left" placeholder="无" disabled /></view>

						<view v-else><u-input v-model="item.value" type="text" input-align="left" placeholder="无" disabled /></view>
					</uni-forms-item>
				</template>
			</uni-forms>
		</view>
		<!-- <view class="body_bottom" @click="handleSave" v-if="!setDisabled">
      保存
    </view> -->
		<!-- <view class="body_top">
      <u-section title="五小区整改通知单" line-color="#1c6ec4" color="#191919" :arrow="false" :right="false">
      </u-section>
      <view class="body_top_bottom">
        <view class="color7d bottom_info">
          2021年9月18日 16：00
        </view>
      </view>
    </view>
    <u-divider :margin-top="0" :margin-bottom="0" half-width="45%" :use-slot="false"></u-divider>
    <view class="body_center">
      <view class="body_center_top">
        <view class="sub_title">
          <u-section title="整改内容:" line-color="#1c6ec4" :right="false" :show-line="false" :font-size="24"></u-section>
        </view>
        <view class="sub_con">
          盼望着，盼望着，东风来了，春天的脚步近了。
          一切都像刚睡醒的样子，欣欣然张开了眼。山朗润起来了，水涨起来了，太阳的脸红起来了。
          小草偷偷地从土里钻出来，嫩嫩的，绿绿的。园子里，田野里，瞧去，一大片一大片满是的。坐着，躺着，打两个滚，踢几脚球，赛几趟跑，捉几回迷藏。风轻悄悄的，草软绵绵的。
          桃树、杏树、梨树，你不让我，我不让你，都开满了花赶趟儿。红的像火，粉的像霞，白的像雪。花里带着甜味儿；闭了眼，树上仿佛已经满是桃儿、杏儿、梨儿。花下成千成百的蜜蜂嗡嗡地闹着，大小的蝴蝶飞来飞去。野花遍地是：杂样儿，有名字的，没名字的，散在草丛里，像眼睛，像星星，还眨呀眨的。
          “吹面不寒杨柳风”，不错的，像母亲的手抚摸着你。风里带来些新翻的泥土的气息，混着青草味儿，还有各种花的香，都在微微润湿的空气里酝酿。鸟儿将窠巢安在繁花嫩叶当中，高兴起来了，呼朋引伴地卖弄清脆的喉咙，唱出宛转的曲子，与轻风流水应和着。牛背上牧童的短笛，这时候也成天在嘹亮地响。
          雨是最寻常的，一下就是三两天。可别恼。看，像牛毛，像花针，像细丝，密密地斜织着，人家屋顶上全笼着一层薄烟。树叶子却绿得发亮，小草也青得逼你的眼。傍晚时候，上灯了，一点点黄晕的光，烘托出一片安静而和平的夜。乡下去，小路上，石桥边，有撑起伞慢慢走着的人；还有地里工作的农夫，披着蓑，戴着笠的。他们的草屋，稀稀疏疏的，在雨里静默着。
          天上风筝渐渐多了，地上孩子也多了。城里乡下，家家户户，老老小小，他们也赶趟儿似的，一个个都出来了。舒活舒活筋骨，抖擞抖擞精神，各做各的一份事去。“一年之计在于春”，刚起头儿，有的是工夫，有的是希望。
          春天像刚落地的娃娃，从头到脚都是新的，他生长着。
          春天像小姑娘，花枝招展的，笑着，走着。
          春天像健壮的青年，有铁一般的胳膊和腰脚，他领着我们上前去。
        </view>
      </view>
      <u-divider :margin-top="0" :margin-bottom="0" half-width="50%" :use-slot="false"></u-divider>
      <view class="body_center_bottom">
        <view class="sub_title">
          <u-section title="整改意见:" line-color="#1c6ec4" :right="false" :show-line="false" :font-size="24"></u-section>
        </view>
        <view class="sub_con">
          整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见整改意见
        </view>
      </view>
      <u-divider :margin-top="0" :margin-bottom="0" half-width="50%" :use-slot="false"></u-divider>
      <view class="body_center_bottom" style="padding-left:10rpx">
        <view style="margin-bottom:10rpx">
          <text style="color:#7d7d7d;font-size:22rpx">接收人：</text>
          <text style="font-size:22rpx">五小区业主</text>
        </view>
        <view>
          <text style="color:#7d7d7d;font-size:22rpx">签发人：</text>
          <text style="font-size:22rpx">张三</text>
        </view>
      </view>
    </view> -->
	</view>
</template>
<script>
import { getAction, postAction } from '@/utils/request.js';
import { Api } from '@/utils/Api.js';
export default {
	components: {},
	data() {
		return {
			describe: '通用详情',
			model: {},
			url: {
				show: '/modular/show',
				visit: '/modular/visit',
				edit: '/modular/edit'
			},
			queryParams: {
				moduleCode: '',
				id: ''
			},
			fields: [],
			listRecord: {},
			setDisabled: false,
			fileList: []
		};
	},
	onLoad(options) {
		this.model = JSON.parse(options.record);
		this.listRecord = options.listRecord ? JSON.parse(options.listRecord) : {};
		this.queryParams.moduleCode = this.model.code ? this.model.code : '';
		this.queryParams.id = this.model.id ? this.model.id : '';

		if (this.listRecord.mName) {
			//重定义头部
			uni.setNavigationBarTitle({
				title: this.listRecord.mName
			});
		}
		this.search();
	},
	methods: {
		search() {
			uni.showLoading({
				title: '加载中',
				mask: true
			});
			this.setDisabled = false;
			getAction(this.url.show, this.queryParams)
				.then(res => {
					if (res.res == 0) {
						this.fields = res.fields.filter(el => el.code != 'cws_id');
						this.fields.forEach(item => {
							if (item.type == 'checkbox') {
								item.value = item.value == 1 ? true : false;
								this.setDisabled = item.value == 1 ? true : false;
							}
						});
						this.fileList = res.files && res.files.length > 0 ? res.files : [];
					} else {
						uni.showToast({
							icon: 'none',
							mask: false,
							title: '接口请求失败'
						});
					}
				})
				.finally(() => {
					uni.hideLoading();
				});
		},
		processingProgress() {
			uni.navigateTo({
				url: `/application/chilPage/chilPage/processingProgress?record=${JSON.stringify(this.model)}`
			});
		},
		//view文件
		viewFile(record) {
			let filePath = Api.baseUrl + record.url;
			let fileType = record.name.split('.')[1].toLowerCase();
			if (!fileType) {
				return;
			}
			uni.showLoading({
				title: '文件下载中'
			});
			uni.downloadFile({
				url: encodeURI(filePath), //注意中文文件名的网络地址需要encodeURI
				success(res) {
					uni.hideLoading();
					//预览图片或打开文件
					if (fileType == 'png' || fileType == 'jpg' || fileType == 'jpeg') {
						// 图片的话直接预览
						uni.previewImage({
							urls: [res.tempFilePath]
						});
						uni.hideLoading();
					} else {
						uni.openDocument({
							filePath: res.tempFilePath,
							success() {},
							fail(e) {
								uni.hideLoading();
								uni.showToast({
									icon: 'none',
									title: '文件打开失败！'
								});
							}
						});
					}
				},
				fail() {
					uni.hideLoading();
					uni.showToast({
						icon: 'none',
						title: '下载出错！'
					});
				},
				complete() {
					uni.hideLoading();
				}
			});
		},
		handleSave() {
			let param = {
				moduleCode: this.queryParams.moduleCode,
				id: this.queryParams.id,
				fields: this.fields
			};
			postAction(this.url.edit, param).then(res => {
				if (res.res == 0) {
					this.callBack();
				} else {
					uni.showToast({
						icon: 'none',
						mask: false,
						title: res.msg
					});
				}
			});
		},
		/*返回上一页*/
		callBack() {
			var pages = getCurrentPages();
			// var currPage = pages[pages.length - 1]; //当前页面
			var prevPage = pages[pages.length - 2]; //上一个页面
			//直接调用上一个页面的setData()方法，把数据存到上一个页面中去
			prevPage.isDoRefresh = true;
			this.model = {};
			uni.navigateBack();
			// prevPage.isDoRefresh = true;
		}
	}
};
</script>
<style lang="scss" scoped>
.pageStyle {
	padding: 20rpx 0;
	background-color: #f9f9f9;
	height: 100%;
	.viewItem {
		height: 100%;
	}
	.form_conter {
		padding: 0 20rpx;
		background-color: #ffffff;

		/deep/ .uni-forms-item {
			border-bottom: 1px #eee solid;
		}
		/deep/ .uni-forms-item__inner {
			padding: 20rpx 0;
		}
	}

	.color7d {
		color: #7d7d7d;
	}
	.colorblack {
		color: #000;
	}
	.body_top {
		background-color: #ffffff;
		padding: 20rpx;
		.body_top_bottom {
			font-size: 24rpx;
			padding: 20rpx;
			.bottom_info {
				width: 100;
				text-align: right;
			}
		}
	}
	.body_center {
		padding: 20rpx;
		background-color: #ffffff;
		.body_center_top {
			padding: 20rpx 0;
			.sub_title {
				padding-bottom: 20rpx;
			}
			.sub_con {
				font-size: 22rpx;
				font-weight: 400;
				color: #7d7d7d;
				line-height: 30rpx;
			}
		}
		.body_center_bottom {
			padding: 20rpx 0;
			.sub_title {
				padding-bottom: 20rpx;
			}
			.sub_con {
				font-size: 22rpx;
				font-weight: 400;
				color: #7d7d7d;
				line-height: 30rpx;
			}
		}
	}
	.body_bottom {
		position: fixed;
		bottom: 0;
		height: 100rpx;
		width: 100%;
		display: flex;
		align-items: center;
		justify-content: center;
		color: #31e6e1;
		background-color: #ffffff;
	}
}
</style>
