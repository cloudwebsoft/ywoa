<template>
	<view class="pageStyle">
		<view class="select_value">
			<u-button type="primary" :custom-style="{ backgroundColor: '#31E6E1' }" :hover-class="{ backgroundColor: '#31E6E1' }" @click="show = true">选择角色</u-button>
		</view>

		<u-select
			v-model="show"
			mode="single-column"
			:mask-close-able="false"
			label-name="name"
			value-name="code"
			:list="dataSource"
			@confirm="confirmSelect"
			@cancel="cancelSelect"
		></u-select>
	</view>
</template>
<script>
export default {
	data() {
		return {
			model: {},
			show: true,
			dataSource: [],
			selectValue: {}
		};
	},
	onLoad(options) {
		this.dataSource = options.roleList ? JSON.parse(options.roleList) : [];
	},
	methods: {
		confirmSelect(e) {
			console.log('e', e);
			let record = e[0];
			this.selectValue = record;
			let curRoleCode = record.value;

			uni.setStorage({
				key: 'curRoleCode',
				data: curRoleCode,
				success() {
					//跳转
					setTimeout(() => {
						uni.reLaunch({
							url: '/pages/index/index'
						});
					});
				}
			});
		},
		//取消选择
		cancelSelect(e) {
			console.log('取消选择', e);
		}
	}
};
</script>
<style lang="scss" scoped>
.pageStyle {
	width: 100%;
	height: 100%;
	.select_value {
		width: 100%;
		height: 100%;
		display: flex;
		justify-content: center;
		align-items: center;
	}
}
</style>
