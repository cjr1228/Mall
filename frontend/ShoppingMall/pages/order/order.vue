<template>
	<view class="order-container">
		<!-- 订单状态标签 -->
		<scroll-view class="order-tabs" scroll-x>
			<view 
				v-for="(tab, index) in tabs" 
				:key="index"
				:class="['tab-item', activeTab === tab.status ? 'active' : '']"
				@click="switchTab(tab.status)"
			>
				{{tab.title}}
			</view>
		</scroll-view>
		
		<scroll-view 
			class="order-list" 
			scroll-y
			refresher-enabled
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
			@scrolltolower="loadMore"
		>
			<view v-if="orderList.length === 0" class="empty-order">
				<view class="empty-icon">📦</view>
				<text class="empty-text">暂无订单</text>
				<view class="go-shopping" @click="goIndex">去逛逛</view>
			</view>
			
			<view v-else class="orders">
				<view 
					v-for="(order, index) in orderList" 
					:key="index"
					class="order-item"
					@click="viewOrderDetail(order)"
				>
					<view class="order-header">
						<text class="order-no">订单号：{{order.orderNo}}</text>
						<text class="order-status" :style="getStatusStyle(order.status)">
							{{getStatusText(order.status)}}
						</text>
					</view>
					
					<!-- 订单基本信息 -->
					<view class="order-info">
						<view class="info-row">
							<text class="info-label">下单时间：</text>
							<text class="info-value">{{order.orderTime}}</text>
						</view>
						<view class="info-row">
							<text class="info-label">支付方式：</text>
							<text class="info-value">{{order.payType==='1'?'在线支付':'货到付款'}}</text>
						</view>
						<view class="info-row">
							<text class="info-label">配送方式：</text>
							<text class="info-value">{{order.shipType==='1'?'快递配送':'门店自提'}}</text>
						</view>
					</view>
					
					<view class="order-footer">
						<text class="total-amount">¥{{(order.amount).toFixed(2)}}</text>
						
						<view class="order-actions">
							<view 
								v-if="order.status === 0"
								class="action-btn cancel"
								@click.stop="cancelOrder(order)"
							>
								取消订单
							</view>
							<view 
								v-if="order.status === 0"
								class="action-btn pay"
								@click.stop="payOrder(order)"
							>
								立即支付
							</view>
							<!-- <view 
								v-if="order.status === 1"
								class="action-btn confirm"
								@click.stop="confirmOrder(order)"
							>
								确认收货
							</view> -->
							<view 
								v-if="order.status === 3"
								class="action-btn delete"
								@click.stop="deleteOrder(order)"
							>
								删除订单
							</view>
						</view>
					</view>
				</view>
			</view>
			
			<view v-if="loading" class="loading">
				<text>加载中...</text>
			</view>
			<view v-if="noMore" class="no-more">
				<text>没有更多了</text>
			</view>
		</scroll-view>
	</view>
</template>
<script setup>
import {onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'

const tabs = ref([
	{ title: '全部', status: '-1' },
	{ title: '待支付', status: '0' },
	{ title: '待发货', status: '1' },
	{ title: '待收货', status: '2' },
	{ title: '已完成', status: '3' },
	{ title: '已取消', status: '4' }
])

const activeTab = ref('-1')
const orderList = ref([])
const loading = ref(false)
const noMore = ref(false)
const refreshing = ref(false)
const currentPage = ref(1)

onShow(() => {
	loadOrders()
})

const switchTab = (status) => {
	activeTab.value = status
	orderList.value = []
	currentPage.value = 1
	noMore.value = false
	loadOrders()
}

const onRefresh = () => {
	refreshing.value = true
	orderList.value = []
	currentPage.value = 1
	noMore.value = false
	setTimeout(() => {
		loadOrders()
		refreshing.value = false
	}, 1000)
}

const loadMore = () => {
	if (loading.value || noMore.value) return
	currentPage.value++
	loadOrders()
}

const loadOrders = async () => {
	// 从本地存储获取Token
	const token = uni.getStorageSync('auth_token')
	
	if (!token) {
		uni.showModal({
			title: '提示',
			content: '请先登录查看订单',
			success: (res) => {
				if (res.confirm) {
					uni.navigateTo({
						url: '/pages/login/login'
					})
				}
			}
		})
		return
	}
	
	loading.value = true
	
	try {
		const response = await uni.request({
			url: '/api/order/list',
			method: 'GET',
			data: {
				status: activeTab.value,
				page: currentPage.value,
				pageSize: 10
			},
			header: {
				'Authorization': `Bearer ${token}`,
				'Content-Type': 'application/json'
			}
		})
		console.log('订单列表:',response);
		
		
		
		if (response.statusCode !== 200) {
			throw new Error(`请求失败，状态码: ${res.statusCode}`)
		}
		
		const data = response.data
		
		if (data.code === 1) {
			const newOrders = data.list || []
			
			const processedOrders = newOrders.map(order => ({
				...order,
				
			}))
			
			if (newOrders.length === 0) {
				noMore.value = true
			} else {
				if (currentPage.value === 1) {
					orderList.value = processedOrders
				} else {
					orderList.value = [...orderList.value, ...processedOrders]
				}
			}
		} else if (data.code === -1) {
			// Token失效或未登录
			handleTokenInvalid(data.message)
		} else {
			uni.showToast({
				title: data.message || '加载订单失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('加载订单失败:', error)
		uni.showToast({
			title: error.message || '网络异常，请稍后重试',
			icon: 'none'
		})
	} finally {
		loading.value = false
	}
}

// 处理Token失效
const handleTokenInvalid = (message) => {
	uni.removeStorageSync('auth_token')
	uni.removeStorageSync('user_info')
	
	uni.showModal({
		title: '提示',
		content: message || '登录已过期，请重新登录',
		showCancel: false,
		success: (res) => {
			if (res.confirm) {
				uni.navigateTo({
					url: '/pages/login/login'
				})
			}
		}
	})
}

const getStatusText = (status) => {
	switch (status) {
		case 0: return '待支付'
		case 1: return '待发货'
		case 2: return '待收货'
		case 3: return '已完成'
		case 4: return '已取消'
		default: return '未知状态'
	}
}

const getStatusStyle = (status) => {
	switch (status) {
		case 0: return 'color: #ff9500; background: #fff5e6;'
		case 1: return 'color: #007aff; background: #e6f2ff;'
		case 2: return 'color: #34c759; background: #e6f7ec;'
		case 3: return 'color: #999; background: #f5f5f5;'
		case 4: return 'color: #ff3b30; background: #ffe6e6;'
		default: return ''
	}
}

const viewOrderDetail = (order) => {
	uni.navigateTo({
		url: `/pages/order-detail/order-detail?orderId=${order.id}`
	})
}

const payOrder = (order) => {
	const token = uni.getStorageSync('auth_token')
	if (!token) {
		handleTokenInvalid('请先登录')
		return
	}
	
	uni.showLoading({ title: '支付中...' })
	
	uni.request({
		url: '/api/order/pay',
		method: 'POST',
		data: {
			orderNo: order.orderNo
		},
		header: {
			'Authorization': `Bearer ${token}`,
			'Content-Type': 'application/x-www-form-urlencoded'
		},
		success: (res) => {
			uni.hideLoading()
			
			if (res.data.code === 1) {
				uni.showToast({
					title: '支付成功',
					icon: 'success'
				})
				setTimeout(() => {
					onRefresh()
				}, 1500)
			} else if (res.data.code === -1) {
				handleTokenInvalid(res.data.message)
			} else {
				uni.showToast({
					title: res.data.message || '支付失败',
					icon: 'none'
				})
			}
		},
		fail: (err) => {
			uni.hideLoading()
			uni.showToast({
				title: '网络错误',
				icon: 'none'
			})
		}
	})
}

const cancelOrder = (order) => {
	const token = uni.getStorageSync('auth_token')
	if (!token) {
		handleTokenInvalid('请先登录')
		return
	}
	
	uni.showModal({
		title: '提示',
		content: '确定要取消这个订单吗？',
		success: (res) => {
			if (res.confirm) {
				uni.showLoading({ title: '取消中...' })
				
				uni.request({
					url: '/api/order/cancel',
					method: 'POST',
					data: {
						orderId: order.id
					},
					header: {
						'Authorization': `Bearer ${token}`,
						'Content-Type': 'application/x-www-form-urlencoded'
					},
					success: (res2) => {
						uni.hideLoading()
						
						if (res2.data.code === 1) {
							uni.showToast({
								title: '订单取消成功',
								icon: 'success'
							})
							setTimeout(() => {
								onRefresh()
							}, 1500)
						} else if (res2.data.code === -1) {
							handleTokenInvalid(res2.data.message)
						} else {
							uni.showToast({
								title: res2.data.message || '取消失败',
								icon: 'none'
							})
						}
					},
					fail: () => {
						uni.hideLoading()
						uni.showToast({
							title: '网络错误',
							icon: 'none'
						})
					}
				})
			}
		}
	})
}

// const confirmOrder = (order) => {
// 	uni.showToast({
// 		title: '功能开发中',
// 		icon: 'none'
// 	})
// }

// const deleteOrder = (order) => {
// 	uni.showToast({
// 		title: '功能开发中',
// 		icon: 'none'
// 	})
// }

const goIndex = () => {
	uni.switchTab({
		url: '/pages/index/index'
	})
}
</script>

<style scoped>
.order-container {
	height: 100vh;
	display: flex;
	flex-direction: column;
	background-color: #f5f5f5;
}

.order-tabs {
	white-space: nowrap;
	background: #fff;
	padding: 20rpx 30rpx;
	border-bottom: 2rpx solid #f0f0f0;
}

.tab-item {
	display: inline-block;
	padding: 15rpx 30rpx;
	margin-right: 40rpx;
	font-size: 30rpx;
	color: #666;
	position: relative;
}

.tab-item.active {
	color: #e93b3d;
	font-weight: bold;
}

.tab-item.active::after {
	content: '';
	position: absolute;
	bottom: 0;
	left: 50%;
	transform: translateX(-50%);
	width: 60rpx;
	height: 6rpx;
	background: #e93b3d;
	border-radius: 3rpx;
}

.order-list {
	flex: 1;
	height: 0;
}

.empty-order {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	height: 60vh;
}

.empty-icon {
	font-size: 120rpx;
	color: #ccc;
	margin-bottom: 40rpx;
}

.empty-text {
	font-size: 32rpx;
	color: #999;
	margin-bottom: 60rpx;
}

.go-shopping {
	width: 300rpx;
	height: 80rpx;
	background: linear-gradient(135deg, #e93b3d 0%, #c82519 100%);
	border-radius: 40rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	color: #fff;
	font-size: 32rpx;
	font-weight: 500;
}

/* 订单列表 */
.orders {
	padding: 30rpx;
}

.order-item {
	background: #fff;
	border-radius: 20rpx;
	margin-bottom: 20rpx;
	overflow: hidden;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.order-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 30rpx;
	border-bottom: 2rpx solid #f5f5f5;
}

.order-no {
	font-size: 28rpx;
	color: #666;
}

.order-status {
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
}

.order-info {
	padding: 30rpx;
	border-bottom: 2rpx solid #f5f5f5;
}

.info-row {
	display: flex;
	margin-bottom: 20rpx;
	font-size: 28rpx;
}

.info-row:last-child {
	margin-bottom: 0;
}

.info-label {
	color: #999;
	width: 150rpx;
	flex-shrink: 0;
}

.info-value {
	color: #333;
	flex: 1;
}

.order-footer {
	padding: 30rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.total-amount {
	font-size: 36rpx;
	color: #e93b3d;
	font-weight: bold;
}

.order-actions {
	display: flex;
	justify-content: flex-end;
}

.action-btn {
	padding: 15rpx 30rpx;
	border-radius: 25rpx;
	font-size: 28rpx;
	font-weight: 500;
	margin-left: 20rpx;
	border: 2rpx solid #ddd;
}

.action-btn.cancel {
	color: #666;
	background: #fff;
}

.action-btn.pay {
	background: linear-gradient(135deg, #e93b3d 0%, #c82519 100%);
	color: #fff;
	border: none;
}

.action-btn.confirm {
	background: #34c759;
	color: #fff;
	border: none;
}

.action-btn.delete {
	color: #ff3b30;
	background: #fff;
	border-color: #ff3b30;
}

.loading, .no-more {
	text-align: center;
	padding: 40rpx;
	color: #999;
	font-size: 28rpx;
}

.loading {
	animation: loading 1s infinite;
}

@keyframes loading {
	0% { opacity: 0.5; }
	50% { opacity: 1; }
	100% { opacity: 0.5; }
}
</style>