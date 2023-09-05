import {
	getAction,
	postAction,
	httpAction,
	deleteAction
} from '@/utils/request.js';
import {
	Toast
} from '@/utils/commonHeader.js';
import {
	filterObj
} from '@/utils/util.js';
const listContent = {
	data() {
		return {
			checkAll: false,
			list: [],
			page: 1,
			pageSize: 20,
			totals: 0,
			/* 排序参数 */
			isorter: {
				// orderBy: 'createTime',
				sort: 'desc',
			},
			isDoRefresh: false,
			queryParam: {},
			isNull: false,
			//子列表
			loadHock: false,
			isGet: true, //是否get请求
			isOnReachBottom: true, //是否触底加载
			resultList: [], //已选
			isResult: false, //是否是result数据
			pageSetup: [], //列表块样式
			cols: [], //没有pageSetup时默认显示cols
			conditions: [], //查询条件
			selectList: [], //可下拉选择的查询数组
			selectTime: [], //可下拉选择的查询时间数据
			canAdd: false,
			canDel: false,
			canEdit: false,
		};
	},
	//点击导航栏新增
	onNavigationBarButtonTap(e) {
		uni.navigateTo({
			url: `${this.linkUrls}`
		});
	},
	//上滑动刷新
	onReachBottom() {
		this.getOnReachBottom()
		console.log("上滑")
	},
	computed: {
		/*分页*/
		fenPage() {
			let fenPage = Math.ceil(this.totals / 20);
			return fenPage;
		},
	},
	//新增完之后刷页面
	onShow(e) {
		const that = this;
		let pages = getCurrentPages();
		//刷新页面
		let currPage = pages[pages.length - 1];
		if (currPage.isDoRefresh == true) {
			currPage.isDoRefresh = false;
			that.list = []
			that.page = 1;
			that.getListData()
		}
	},
	onLoad(option) {
		let rows = option.infos
		//用户传递子id时候需要带入请求list里面
		if (rows) {
			const {
				key,
				id
			} = JSON.parse(rows)
			console.log(rows)
			this.queryParam[key] = id
		}
		this.loadHock && this.getListData();
	},
	methods: {
		//触底加载
		getOnReachBottom() {
			if (this.isOnReachBottom) {
				this.page++
				if (this.page > this.fenPage) {
					uni.showToast({
						title: '没有更多数据',
						duration: 2000
					});
					return false;
				} else {
					this.getListData();
				}
			}
		},
		/*获取list*/
		getListData() {
			if (!this.url.list) {
				uni.showToast({
					title: '请设置url.list',
					duration: 2000
				});
				return
			}
			uni.showLoading({
				title: '加载中',
				mask: true
			});
			if (this.page == 1) {
				this.cols = []
				this.pageSetup = []
				this.list = []
				this.selectList = []
				this.conditions = []
				this.selectTime = []
			}
			let params = filterObj({
				page: this.page,
				pageSize: this.pageSize,
				...this.isorter,
				...this.queryParam
			})
			console.log("params", params);
			console.log("isGet", this.isGet);
			if (this.isGet) {
				getAction(this.url.list, params).then((res) => {
					console.log('getAction res', res);
					if (res.code == 200 || res.res == 0) {
						console.log("mixres", res);
						if (res.moduleName) {
							uni.setNavigationBarTitle({ title: res.moduleName });							
						}
						let results = res.result
						this.canAdd = results.canAdd ? results.canAdd : false
						this.canDel = results.canDel ? results.canDel : false
						this.canEdit = results.canEdit ? results.canEdit : false
						this.files = results.files && Array.isArray(results.files) ? results.files : []
						this.conditions = results.conditions && Array.isArray(results.conditions) ? results
							.conditions : []
						if (this.conditions.length > 0) {
							this.selectList = this.conditions && Array.isArray(this.conditions) && this
								.conditions.length > 0 ? this.conditions.filter(item => item.fieldType !=
									7 && item.fieldType != 8) : []
							this.selectTime = this.conditions && Array.isArray(this.conditions) && this
								.conditions.length > 0 ? this.conditions.filter(item => item.fieldType ==
									7 || item.fieldType == 8) : []
						}
						let cols = results.cols && Array.isArray(results.cols) ? results.cols : []
						let pageSetup = results.pageSetup && Array.isArray(results.pageSetup) && results
							.pageSetup.length > 0 ? results.pageSetup : []
						let arrs = results.datas && Array.isArray(results.datas) && results.datas.length >
							0 ? results.datas : [];
						this.cols = [...this.cols, ...cols]
						this.pageSetup = [...this.pageSetup, ...pageSetup]
						arrs.forEach(item => {
							item.show = false
						})
						this.list = [...this.list, ...arrs]
						this.isNull = this.list && this.list.length > 0 ? false : true
						this.$emit("update:selectList", this.selectList)
						this.$emit("update:selectTime", this.selectTime)
						this.$emit("update:list", this.list)
						console.log("list", this.list)
						this.hadSearchData(this.list)
					} else {
						this.list = [];
						this.isNull = true
					}
				}).finally(() => {
					uni.hideLoading()
				})
			} else {
				postAction(this.url.list, params).then((res) => {
					if (res.code == 200 || res.res == 0) {
						console.log("res", res);
						if (res.moduleName) {
							uni.setNavigationBarTitle({ title: res.moduleName });							
						}
						let arrs = res.data.list || [];
						arrs.forEach(item => {
							item.show = false
						})
						this.list = [...this.list, ...arrs]
						this.hadSearchData(this.list)
						console.log("this.list", this.list)
					} else {
						this.list = [];
					}
				}).finally(() => {
					uni.hideLoading()
				})
			}
		},
		//刷新当前页
		getThisPage() {
			this.list = []
			this.getListData()
		},
		//数据调取结束后调用函数，自定义逻辑,在主页面实现
		hadSearchData(list, totals) {},
		changeSelect(e, index) {
			let isCheck = e.target.value.length > 0
			//全选时候
			if (index < 0) {
				this.checkAll = isCheck
				//选中
				if (isCheck) {
					this.list.map(res => res.checked = true)
				} else {
					this.list.map(res => res.checked = false)
				}
				//取消
			} else {
				this.list[index].checked = isCheck
			}
		},
		//详情
		showDetails(data) {
			uni.navigateTo({
				url: `${this.detailUrls}?record=${JSON.stringify(data)}`
			})
		},
		deleteData() {
			const that = this
			uni.showModal({
				title: '确认删除选中项目',
				content: '点击确认删除数据',
				confirmColor: '#098af0',
				success: function(res) {
					if (res.confirm) {
						that.deleteComfirmed();
					} else if (res.cancel) {
						Toast('none', '已取消删除')
					}
				}
			});
		},
		//删除
		deleteComfirmed() {
			//把所有 checked 是true的全部找出来
			let DeleteDatas = this.list.filter(res => res.checked == true);
			if (DeleteDatas.length == 0) {
				Toast('none', "请选择要删除的数据")
				return;
			}
			this.list = this.list.filter(res => res.checked == false);
			//组合批量的id
			let ids = new Array()
			DeleteDatas.map(ret => ids.push(ret.id))
			//id数组转换成字符串拼接
			ids = String(ids)
			httpAction(`${this.url.delete}?ids=${ids}`, {}, 'delete').then(res => {
				if (res.success) {
					//成功掉接口
					this.resetData()
				}
				Toast('none', res.message)
			})
		},
		/*重置*/
		resetData() {
			this.page = 1
			this.list = []
			this.totals = 0
			this.getListData()
		},
		deleteOne(id) {
			deleteAction(this.url.delete, {
				id: id
			}).then((res) => {
				if (res.success) {
					uni.showToast({
						icon: "success",
						mask: true,
						title: res.message,
					});
					this.page = 1
					this.getThisPage();
				} else {
					uni.showToast({
						icon: "error",
						mask: true,
						title: res.message,
					});
				}
			});
		},
	}
}

export default listContent
