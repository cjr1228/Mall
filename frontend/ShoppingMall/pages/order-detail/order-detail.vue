<template>
	<view class="order-detail">
		<!-- 订单状态 -->
		<view class="order-status-section">
			<image :src="getStatusIcon(order.status)" class="status-icon" mode="widthFix"/>
			<text class="status-text">{{getStatusText(order.status)}}</text>
			<text class="status-desc">{{getStatusDesc(order.status)}}</text>
		</view>
		
		<!-- 收货地址 -->
		<view class="address-section" v-if="order.shipAddress">
			<view class="address-header">
				<image src="/static/icon/address.png" class="icon" mode="widthFix"/>
				<text class="title">收货地址</text>
			</view>
			<view class="address-info">
				<view class="address-row">
					<text class="name">{{order.shipAddress.name}}</text>
					<text class="phone">{{order.shipAddress.telno}}</text>
				</view>
				<text class="address">{{order.shipAddress.address}}</text>
			</view>
		</view>
		
		<!-- 商品列表 -->
		<view class="goods-section">
			<view class="section-header">
				<text class="title">商品清单</text>
			</view>
			
			<view 
				v-for="(detail, index) in order.orderDetails"
				:key="index"
				class="goods-item"
				@click="goGoodsDetail(detail)"
			>
				<image 
				  :src="detail.goodsImg || '/static/images/default-goods.png'"  
				  class="goods-img"
				/>
				<view class="goods-info">
					<text class="goods-name">{{detail.goodsName}}</text>
					<text class="goods-spec">规格：默认</text>
					<view class="goods-price">
						<text class="price">¥{{detail.memberPrice.toFixed(2)}}</text>
						<text class="quantity">x {{detail.qty}}</text>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 订单信息 -->
		<view class="order-info">
			<view class="info-item">
				<text class="label">订单编号</text>
				<text class="value">{{order.orderNo}}</text>
			</view>
			<view class="info-item">
				<text class="label">创建时间</text>
				<text class="value">{{formatDate(order.orderTime)}}</text>
			</view>
			<view class="info-item">
				<text class="label">支付方式</text>
				<text class="value">{{order.payType === '1' ? '在线支付' : '货到付款'}}</text>
			</view>
			<view class="info-item">
				<text class="label">配送方式</text>
				<text class="value">{{order.shipType === '1' ? '快递配送' : '门店自提'}}</text>
			</view>
			<view class="info-item">
				<text class="label">订单备注</text>
				<text class="value">无</text>
			</view>
		</view>
		
		<!-- 价格明细 -->
		<view class="price-section">
			<view class="price-item">
				<text class="label">商品金额</text>
				<text class="value">¥{{(order.amount - 5).toFixed(2)}}</text>
			</view>
			<view class="price-item">
				<text class="label">运费</text>
				<text class="value">¥5.00</text>
			</view>
			<view class="price-item total">
				<text class="label">实付款</text>
				<text class="value">¥{{order.amount.toFixed(2)}}</text>
			</view>
		</view>
		
		<!-- 操作按钮 -->
		<view class="action-bar" v-if="order.status === 0">
			<view class="action-btn cancel" @click="cancelOrder">取消订单</view>
			<view class="action-btn pay" @click="payOrder">立即支付</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const order = ref({
	id: 0,
	orderNo: '',
	status: 0,
	amount: 0,
	orderTime: '',
	payType: '1',
	shipType: '1',
	shipAddress: null,
	orderDetails: []
})

onLoad((options) => {
	if (options.orderId) {
		loadOrderDetail(options.orderId)
	}
})

const loadOrderDetail = (orderId) => {
	uni.showLoading({ title: '加载中...' })
	
	// 从本地存储获取Token
	const token = uni.getStorageSync('auth_token')
	if (!token) {
		uni.hideLoading()
		uni.showModal({
			title: '提示',
			content: '请先登录查看订单详情',
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
	
	uni.request({
		url: '/api/order/detail',
		method: 'GET',
		data: { orderId },
		header: {
			'Authorization': `Bearer ${token}`
		},
		success: (res) => {
			uni.hideLoading()
			console.log('订单详情:',res);
			if (res.data.code === 1) {
				order.value = res.data.order
				
				
			} else if (res.data.code === -1) {
				// Token失效的情况
				uni.removeStorageSync('auth_token')
				uni.removeStorageSync('user_info')
				uni.showModal({
					title: '提示',
					content: '登录已过期，请重新登录',
					success: (res2) => {
						if (res2.confirm) {
							uni.navigateTo({
								url: '/pages/login/login'
							})
						}
					}
				})
			} else {
				uni.showToast({
					title: res.data.message || '订单不存在',
					icon: 'none'
				})
				setTimeout(() => {
					uni.navigateBack()
				}, 1500)
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

const getStatusIcon = (status) => {
	switch (status) {
			case 0: return '/static/icon/wait-pay-white.png'  
			case 1: return '/static/icon/wait-send-white.png' 
			case 2: return '/static/icon/wait-receive-white.png'  
			case 3: return '/static/icon/completed-white.png' 
			case 4: return '/static/icon/canceled-white.png'  
			
		}
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

const getStatusDesc = (status) => {
	switch (status) {
		case 0: return '请尽快完成支付，超时订单将自动取消'
		case 1: return '商家正在处理您的订单'
		case 2: return '商品正在运输途中'
		case 3: return '订单已完成'
		case 4: return '订单已取消'
		default: return ''
	}
}

const formatDate = (dateString) => {
	if (!dateString) return ''
	const date = new Date(dateString)
	return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const goGoodsDetail = (detail) => {
	uni.navigateTo({
		url: `/pages/goods-detail/goods-detail?goodsNo=${detail.goodsNo}`
	})
}

const cancelOrder = () => {
	uni.showModal({
		title: '提示',
		content: '确定要取消这个订单吗？',
		success: (res) => {
			if (res.confirm) {
				// 获取Token
				const token = uni.getStorageSync('auth_token')
				if (!token) {
					uni.showModal({
						title: '提示',
						content: '请先登录',
						success: (loginRes) => {
							if (loginRes.confirm) {
								uni.navigateTo({
									url: '/pages/login/login'
								})
							}
						}
					})
					return
				}
				
				uni.showLoading({ title: '取消中...' })
				
				uni.request({
					url: '/api/order/cancel',
					method: 'POST',
					data: {
						orderId: order.value.id
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
							// 重新加载订单详情
							setTimeout(() => {
								loadOrderDetail(order.value.id)
							}, 1500)
						} else if (res2.data.code === -1) {
							// Token失效
							uni.removeStorageSync('auth_token')
							uni.removeStorageSync('user_info')
							uni.showModal({
								title: '提示',
								content: '登录已过期，请重新登录',
								success: () => {
									uni.navigateTo({
										url: '/pages/login/login'
									})
								}
							})
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

const payOrder = () => {
	// 获取Token
	const token = uni.getStorageSync('auth_token')
	if (!token) {
		uni.showModal({
			title: '提示',
			content: '请先登录',
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
	
	uni.showLoading({ title: '支付中...' })
	
	uni.request({
		url: '/api/order/pay',
		method: 'POST',
		data: {
			orderNo: order.value.orderNo
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
				// 重新加载订单详情
				setTimeout(() => {
					loadOrderDetail(order.value.id)
				}, 1500)
			} else if (res.data.code === -1) {
				// Token失效
				uni.removeStorageSync('auth_token')
				uni.removeStorageSync('user_info')
				uni.showModal({
					title: '提示',
					content: '登录已过期，请重新登录',
					success: () => {
						uni.navigateTo({
							url: '/pages/login/login'
						})
					}
				})
			} else {
				uni.showToast({
					title: res.data.message || '支付失败',
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
</script>

<style scoped>
.order-detail {
	padding-bottom: 120rpx;
	min-height: 100vh;
	background: #f5f5f5;
}

/* 订单状态 */
.order-status-section {
	background: linear-gradient(135deg, #e93b3d 0%, #c82519 100%);
	padding: 60rpx 30rpx;
	color: #fff;
	text-align: center;
}

.status-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
}

.status-text {
	display: block;
	font-size: 40rpx;
	font-weight: bold;
	margin-bottom: 15rpx;
}

.status-desc {
	font-size: 28rpx;
	opacity: 0.9;
}

/* 地址部分 */
.address-section {
	background: #fff;
	margin: 20rpx 30rpx;
	border-radius: 20rpx;
	padding: 30rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.address-header {
	display: flex;
	align-items: center;
	margin-bottom: 30rpx;
	padding-bottom: 20rpx;
	border-bottom: 2rpx solid #f5f5f5;
}

.address-header .icon {
	font-size: 36rpx;
	margin-right: 15rpx;
}

.address-header .title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.address-row {
	display: flex;
	justify-content: space-between;
	margin-bottom: 15rpx;
}

.address-row .name {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.address-row .phone {
	font-size: 28rpx;
	color: #666;
}

.address {
	font-size: 28rpx;
	color: #666;
	line-height: 1.6;
}

/* 商品部分 */
.goods-section {
	background: #fff;
	margin: 20rpx 30rpx;
	border-radius: 20rpx;
	padding: 30rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.section-header {
	margin-bottom: 30rpx;
	padding-bottom: 20rpx;
	border-bottom: 2rpx solid #f5f5f5;
}

.section-header .title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.goods-item {
	display: flex;
	margin-bottom: 30rpx;
	padding-bottom: 30rpx;
	border-bottom: 2rpx solid #f5f5f5;
}

.goods-item:last-child {
	margin-bottom: 0;
	padding-bottom: 0;
	border-bottom: none;
}

.goods-img {
	width: 180rpx;
	height: 180rpx;
	border-radius: 15rpx;
	background: #f8f8f8;
	margin-right: 30rpx;
}

.goods-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.goods-name {
	font-size: 30rpx;
	color: #333;
	line-height: 1.4;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	overflow: hidden;
}

.goods-spec {
	font-size: 26rpx;
	color: #999;
	margin: 10rpx 0;
}

.goods-price {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.goods-price .price {
	font-size: 36rpx;
	color: #e93b3d;
	font-weight: bold;
}

.goods-price .quantity {
	font-size: 28rpx;
	color: #666;
}

/* 订单信息 */
.order-info {
	background: #fff;
	margin: 20rpx 30rpx;
	border-radius: 20rpx;
	padding: 30rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.info-item {
	display: flex;
	justify-content: space-between;
	margin-bottom: 25rpx;
}

.info-item:last-child {
	margin-bottom: 0;
}

.info-item .label {
	font-size: 28rpx;
	color: #999;
}

.info-item .value {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

/* 价格明细 */
.price-section {
	background: #fff;
	margin: 20rpx 30rpx;
	border-radius: 20rpx;
	padding: 30rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.price-item {
	display: flex;
	justify-content: space-between;
	margin-bottom: 25rpx;
}

.price-item:last-child {
	margin-bottom: 0;
}

.price-item .label {
	font-size: 28rpx;
	color: #666;
}

.price-item .value {
	font-size: 28rpx;
	color: #333;
}

.price-item.total {
	margin-top: 30rpx;
	padding-top: 30rpx;
	border-top: 2rpx solid #f5f5f5;
}

.price-item.total .label {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.price-item.total .value {
	font-size: 40rpx;
	font-weight: bold;
	color: #e93b3d;
}

/* 操作按钮 */
.action-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: #fff;
	border-top: 2rpx solid #f5f5f5;
	padding: 20rpx 30rpx;
	display: flex;
	justify-content: flex-end;
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.action-btn {
	padding: 20rpx 40rpx;
	border-radius: 30rpx;
	font-size: 30rpx;
	font-weight: 500;
	margin-left: 20rpx;
}

.action-btn.cancel {
	background: #fff;
	color: #666;
	border: 2rpx solid #ddd;
}

.action-btn.pay {
	background: linear-gradient(135deg, #e93b3d 0%, #c82519 100%);
	color: #fff;
}

/* 订单状态图标样式调整 */
.status-icon {
	width: 80rpx;  /* 根据实际图片大小调整 */
	height: 80rpx; /* 根据实际图片大小调整 */
	margin-bottom: 20rpx;
}

/* 地址图标样式调整 */
.address-header .icon {
	width: 36rpx;  /* 根据实际图片大小调整 */
	height: 36rpx; /* 根据实际图片大小调整 */
	margin-right: 15rpx;
}

</style>