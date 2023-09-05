<template>
	<view class="pageStyle">
		<view class="content">
			<view class="label heri-start-center">角色:</view>
			<view class="heri-start-center" style="width:100%"><span>{{user.roleName}}</span></view>
		</view>
		<view class="content">
			<view class="label heri-start-center">帐户:</view>
			<view class="heri-start-center" style="width:100%"><span>{{user.name}}</span></view>
		</view>
		<view class="content">
			<view class="label heri-start-center">姓名:</view>
			<view class="heri-start-center" style="width:100%"><span>{{user.realName}}</span></view>
		</view>
		<view class="content">
			<view class="label heri-start-center">部门:</view>
			<view class="heri-start-center" style="width:100%"><span>{{deptName}}</span></view>
		</view>

		<view class="content">
			<view class="label heri-start-center">头像:</view>
			<view class="heri-start-center" style="width:100%">
				<view>
					<img :src="photoUrl" style="width: 100px; height: 100px" class="icons" />
				</view>
				<view>
					<u-upload ref="uUpload" :name="fieldName" :action="url.getRecord" max-count="1" :form-data="user"
						:auto-upload="false" :header="{'Authorization': Authorization}" @on-change="onChange"
						@on-success="onSuccess">
					</u-upload>
				</view>
			</view>
		</view>
		<view class="content">
			<view class="label heri-start-center">手机:</view>
			<view class="heri-start-center" style="width:100%"><input type="text" placeholder="请输入地址"
					v-model="user.mobile" /></view>
		</view>
		<view class="content">
			<view class="label heri-start-center">生日:</view>
			<view class="heri-start-center" style="width:100%">
				<picker mode="date" :value="user.birthday" @change="bindDateChange">
					<view>{{ user.birthday }}</view>
				</picker>
			</view>
		</view>
		<view>
			<u-button type="primary" :hover-class="'buttons'"
				:custom-style="{ width: '100%', backgroundColor: '#31E6E1', padding: '20rpx', border: 'none', color: '#fff' }"
				@click="save">
				确定
			</u-button>
		</view>
	</view>
</template>
<script>
	import {
		Api
	} from "@/utils/Api.js";
	import {
		postAction,
		getAction,
		postFormAction,
		fileAction,
	} from "@/utils/request.js";
	import {
		getToken,
		Toast
	} from '@/utils/commonHeader.js';
	export default {
		data() {
			return {
				deptName: '',
				user: {
					mobile: '', // 手机号
					birthday: null, // 生日
				},
				url: {
					getRecord: Api.baseUrl + "/user/updateMyInfo",
				},
				Authorization: getToken(),
				// contentType: "application/json;charset=UTF-8",
				// contentType: 'multipart/form-data; boundary=----WebKitFormBoundaryAOihCT2p5Bt9Kjm3',
				fieldName: 'photo',
				photoUrl: '',
			};
		},
		onLoad(options) {
			this.loadUser();
		},
		methods: {
			async loadUser() {
				let params = {};
				await postFormAction("/user/editUser", params)
					.then((res) => {
						let result = res;
						console.log("result", result);
						if (result == null) {
							uni.showToast({
								icon: "none",
								title: "网络连接失败",
							});
							return;
						}
						if (result.code && result.code == 500) {
							uni.showToast({
								icon: "none",
								title: "获取用户信息失败",
							});
						} else {
							this.user = {
								...result.data.user
							};
							this.deptName = result.data.deptName;
							if (this.user.photo != null && this.user.photo != '') {
								// 不加public在企业微信手机端中会报“非法访问”，而PC端则不会
								this.photoUrl = Api.baseUrl + '/public/showImg.do?path=' + this.user.photo;
							}
							console.log('loadUser', this.user);
						}
					})
					.catch((e) => {
						uni.showToast({
							icon: "none",
							title: e.message,
						});
					});
			},
			save() {
				let that = this;
				console.log("this.$refs.uUpload.lists", this.$refs.uUpload.lists)
				// 去除所有为null的值，以免LocalDateTime类型的字段报typeMismatch
				Object.keys(that.user).forEach(function(key) {
					if (that.user[key] == null) {
						console.log('key is null:', key);
						delete(that.user[key]);
					}
				});

				if (this.$refs.uUpload.lists.length == 0) {
					console.log('that.user', that.user);
					// 去除photo，以免后端报：Field error in object 'userVO' on field 'photo': rejected value [null]; codes [typeMismatch.userVO.photo,typeMismatch.photo,typeMismatch.org.springframework.web.multipart.MultipartFile,typeMismatch]; 
					// arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [userVO.photo,photo]; arguments []; default message [photo]]; default message [Failed to convert property value of type 'java.lang.String' to required type 'org.springframework.web.multipart.MultipartFile' for property 'photo'; nested exception is java.lang.IllegalStateException: Cannot convert value of type 'java.lang.String' to required type 'org.springframework.web.multipart.MultipartFile' for property 'photo': no matching editors or conversion strategy found]
					delete(that.user['photo']);

					uni.showModal({
						title: "提示",
						content: "是否确定提交",
						success: (res) => {
							if (res.confirm) {
								fileAction("/user/updateMyInfo", that.user)
									.then((res) => {
										console.log("res", res);
										if (res == null) {
											uni.showToast({
												icon: "none",
												title: "网络连接失败",
											});
											return;
										}
										console.log('res.msg', res.msg);
										uni.showToast({
											icon: "none",
											title: res.msg,
										});
									})
									.catch((e) => {
										console.log("请求失败", e);
										uni.showToast({
											icon: "none",
											title: e.message,
										});
									});
							} else if (res.cancel) {
								console.log("点击取消");
							}
						},
					});
				} else {
					uni.showModal({
						title: "提示",
						content: "是否确定提交",
						success: (res) => {
							if (res.confirm) {
								that.doSubmit();
							} else if (res.cancel) {
								console.log("点击取消");
							}
						},
					});
				}
			},
			bindDateChange: function(e) {
				this.user.birthday = e.detail.value;
				console.log(this.user.birthday);
			},
			async doSubmit() {
				console.log('doSubmit user', this.user);
				await this.$refs.uUpload.upload();
				// 清空所选的图片
				this.$refs.uUpload.lists = [];
			},
			onSuccess() {

			},
			// 上传完成之后的回调
			async onChange(res, index, lists, name) {
				await this.loadUser().then((res) => {
					console.log('user.photo', this.user.photo);
					let info = uni.getStorageSync("loginInfo");
					info.photo = this.user.photo;
					console.log('info', info);
					info.loginInfo.photo = 'showImg.do?path=' + this.user.photo;
					console.log('info.loginInfo', info.loginInfo);
					this.photoUrl = Api.baseUrl + '/public/showImg.do?path=' + this.user.photo;

					uni.setStorage({
						key: "loginInfo",
						data: {
							...info,
						},
					});
				});

				console.log("res, index, lists, name", res, index, lists, name)
				let data = JSON.parse(res.data);
				if (res.statusCode == 200) {
					uni.showToast({
						icon: "none",
						mask: false,
						title: data.msg,
					});
					if (data.res == 0) {
						this.callBack();
					}
				} else {
					uni.showToast({
						icon: "none",
						mask: false,
						title: data.msg,
					});
				}
			},
			/*返回上一页*/
			callBack() {
				uni.navigateBack();
			},
		}
	};
</script>
<style lang="scss" scoped>
	.pageStyle {
		width: 100%;
		height: 100%;
		padding: 0 20px;
		font-size: 16px;

		.content {
			display: flex;
		}

		.label {
			height: 120rpx;
			width: 100px;
		}

		uni-input {
			height: 30px;
			width: 100%;
		}

		.icons {
			width: 120upx;
			height: 120upx;
			border-radius: 50%;
		}

		::v-deep .uni-input-input {
			border-bottom: 1px solid #cbced8;
		}

		::v-deep .uni-input-placeholder {
			font-size: 16px;
		}
	}
</style>
