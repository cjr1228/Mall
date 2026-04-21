<template>
	<view class="my-container">
		<!-- 用户信息 -->
		<view class="user-info" @click="goLogin" v-if="!isLogin">
			<view class="avatar">
				<image class="avatar-img" src="/static/icon/user.png" mode="widthFix"></image>
			</view>
			<view class="user-detail">
				<text class="welcome">点击登录</text>
				<text class="tip">登录后享受更多服务</text>
			</view>
			<text class="arrow">›</text>
		</view>
		
		<view class="user-info" v-else @click="goUserInfo">
			<view class="avatar">
				<image class="avatar-img" src="/static/icon/user.png" mode="widthFix"></image>
			</view>
			<view class="user-detail">
				<text class="name">{{userInfo.nick || userInfo.email}}</text>
				<text class="email">{{userInfo.email}}</text>
			</view>
			<text class="arrow">›</text>
		</view>
		
		<!-- 订单状态 -->
		<view class="order-status">
			<view class="status-header" @click="goOrderList('-1')">
				<text class="title">我的订单</text>
				<text class="more">查看全部 ›</text>
			</view>
			
			<view class="status-list">
				<view class="status-item" @click="goOrderList('0')">
					<image class="icon-img" src="/static/icon/wait-pay.png" mode="widthFix"></image>
					<text class="text">待支付</text>
				</view>
				<view class="status-item" @click="goOrderList('1')">
					<image class="icon-img" src="/static/icon/wait-send.png" mode="widthFix"></image>
					<text class="text">待发货</text>
				</view>
				<view class="status-item" @click="goOrderList('2')">
					<image class="icon-img" src="/static/icon/wait-receive.png" mode="widthFix"></image>
					<text class="text">待收货</text>
				</view>
				<view class="status-item" @click="goOrderList('3')">
					<image class="icon-img" src="/static/icon/completed.png" mode="widthFix"></image>
					<text class="text">已完成</text>
				</view>
				<view class="status-item" @click="goOrderList('4')">
					<image class="icon-img" src="/static/icon/canceled.png" mode="widthFix"></image>
					<text class="text">已取消</text>
				</view>
			</view>
		</view>
		
		<!-- 功能列表 -->
		<view class="function-list">
			<view class="list-section">
				<view class="list-title">我的服务</view>
				<view class="list-items">
					<view class="list-item" @click="goCollect">
						<image class="icon-img" src="/static/icon/collect.png" mode="widthFix"></image>
						<text class="text">我的收藏</text>
						<text class="arrow">›</text>
					</view>
					<view class="list-item" @click="goAddress">
						<image class="icon-img" src="/static/icon/address.png" mode="widthFix"></image>
						<text class="text">收货地址</text>
						<text class="arrow">›</text>
					</view>
					<view class="list-item" @click="goCart">
						<image class="icon-img" src="/static/tabbar/cart.png" mode="widthFix"></image>
						<text class="text">购物车</text>
						<text class="arrow">›</text>
					</view>
				</view>
			</view>
			
			<view class="list-section">
				<view class="list-title">设置与帮助</view>
				<view class="list-items">
					<view class="list-item" @click="goSettings">
						<image class="icon-img" src="/static/icon/settings.png" mode="widthFix"></image>
						<text class="text">设置</text>
						<text class="arrow">›</text>
					</view>
					<view class="list-item" @click="goHelp">
						<image class="icon-img" src="/static/icon/help.png" mode="widthFix"></image>
						<text class="text">帮助中心</text>
						<text class="arrow">›</text>
					</view>
					<view class="list-item" @click="goAbout">
						<image class="icon-img" src="/static/icon/about.png" mode="widthFix"></image>
						<text class="text">关于我们</text>
						<text class="arrow">›</text>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 退出登录 -->
		<view class="logout-btn" v-if="isLogin" @click="logout">
			退出登录
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'

const isLogin = ref(false)
const userInfo = ref({})

onShow(() => {
	checkLoginStatus()
})

const checkLoginStatus = () => {
	// 从本地存储获取Token
	const token = uni.getStorageSync('auth_token')
	
	if (!token) {
		isLogin.value = false
		userInfo.value = {}
		return
	}
	
	uni.request({
		url: '/api/member/checkLogin',
		method: 'GET',
		header: {
			'Authorization': `Bearer ${token}`
		},
		// 注意：不再需要 withCredentials: true
		success: (res) => {
			if (res.data.code === 1 && res.data.isLogin) {
				isLogin.value = true
				userInfo.value = res.data.member || {}
				// 更新本地存储的用户信息
				uni.setStorageSync('user_info', res.data.member)
			} else {
				isLogin.value = false
				userInfo.value = {}
				// Token失效，清理本地存储
				uni.removeStorageSync('auth_token')
				uni.removeStorageSync('user_info')
			}
		},
		fail: () => {
			isLogin.value = false
			userInfo.value = {}
		}
	})
}

const goLogin = () => {
	if (!isLogin.value) {
		uni.navigateTo({
			url: '/pages/login/login'
		})
	}
}

const goUserInfo = () => {
	if (isLogin.value) {
		uni.showToast({
			title: '功能开发中',
			icon: 'none'
		})
	}
}

const goOrderList = (status) => {
	if (!isLogin.value) {
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
	
	uni.navigateTo({
		url: `/pages/order/order?status=${status}`
	})
}

const goCollect = () => {
	if (!isLogin.value) {
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
	
	uni.switchTab({
		url: '/pages/collect/collect'
	})
}

const goAddress = () => {
	if (!isLogin.value) {
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
	
	uni.navigateTo({
		url: '/pages/address/address'
	})
}

const goCart = () => {
	uni.switchTab({
		url: '/pages/cart/cart'
	})
}

const goSettings = () => {
	uni.showToast({
		title: '功能开发中',
		icon: 'none'
	})
}

const goHelp = () => {
	uni.showToast({
		title: '功能开发中',
		icon: 'none'
	})
}

const goAbout = () => {
	uni.showToast({
		title: '功能开发中',
		icon: 'none'
	})
}

const logout = () => {
	uni.showModal({
		title: '提示',
		content: '确定要退出登录吗？',
		success: (res) => {
			if (res.confirm) {
				const token = uni.getStorageSync('auth_token')
				
				// 调用后端退出接口（可选）
				uni.request({
					url: '/api/member/logout',
					method: 'POST',
					header: {
						'Authorization': `Bearer ${token}`
					},
					success: () => {
						// 清理本地存储
						uni.removeStorageSync('auth_token')
						uni.removeStorageSync('user_info')
						
						isLogin.value = false
						userInfo.value = {}
						
						uni.showToast({
							title: '退出成功',
							icon: 'success'
						})
					},
					fail: () => {
						// 即使后端调用失败，也清理前端状态
						uni.removeStorageSync('auth_token')
						uni.removeStorageSync('user_info')
						isLogin.value = false
						userInfo.value = {}
					}
				})
			}
		}
	})
}
</script>

<style scoped>
.my-container {
	min-height: 100vh;
	background: #f5f5f5;
	padding-bottom: 100rpx;
}

/* 用户信息 */
.user-info {
	display: flex;
	align-items: center;
	background: linear-gradient(135deg, #e93b3d 0%, #c82519 100%);
	padding: 60rpx 40rpx;
	color: #fff;
}

.avatar {
	width: 120rpx;
	height: 120rpx;
	border-radius: 60rpx;
	background: rgba(255, 255, 255, 0.2);
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 30rpx;
}

.avatar-icon {
	font-size: 60rpx;
}

.user-detail {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.welcome {
	font-size: 40rpx;
	font-weight: bold;
	margin-bottom: 10rpx;
}

.tip {
	font-size: 28rpx;
	opacity: 0.8;
}

.name {
	font-size: 40rpx;
	font-weight: bold;
	margin-bottom: 10rpx;
}

.email {
	font-size: 28rpx;
	opacity: 0.8;
}

.arrow {
	font-size: 50rpx;
	opacity: 0.8;
	transform: rotate(90deg);
}

/* 订单状态 */
.order-status {
	background: #fff;
	margin: 20rpx;
	border-radius: 20rpx;
	padding: 30rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.status-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 40rpx;
	padding-bottom: 20rpx;
	border-bottom: 2rpx solid #f5f5f5;
}

.status-header .title {
	font-size: 34rpx;
	font-weight: bold;
	color: #333;
}

.status-header .more {
	font-size: 26rpx;
	color: #999;
}

.status-list {
	display: flex;
	justify-content: space-around;
}

.status-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.status-item .icon {
	font-size: 50rpx;
	margin-bottom: 15rpx;
}

.status-item .text {
	font-size: 26rpx;
	color: #666;
}

/* 功能列表 */
.function-list {
	margin: 20rpx;
}

.list-section {
	background: #fff;
	border-radius: 20rpx;
	margin-bottom: 20rpx;
	padding: 30rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.list-title {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 30rpx;
	padding-bottom: 20rpx;
	border-bottom: 2rpx solid #f5f5f5;
}

.list-items {
	
}

.list-item {
	display: flex;
	align-items: center;
	padding: 30rpx 0;
	border-bottom: 2rpx solid #f5f5f5;
}

.list-item:last-child {
	border-bottom: none;
}

.list-item .icon {
	font-size: 40rpx;
	margin-right: 30rpx;
	width: 60rpx;
	text-align: center;
}

.list-item .text {
	flex: 1;
	font-size: 30rpx;
	color: #333;
}

.list-item .arrow {
	font-size: 40rpx;
	color: #999;
	transform: rotate(90deg);
}

.avatar-img {
	width: 100rpx;
	height: 100rpx;
}

.icon-img {
	width: 40rpx;
	height: 40rpx;
	margin-bottom: 15rpx; /* 订单状态图标底部间距 */
}

/* 功能列表图标调整 */
.list-item .icon-img {
	margin-right: 30rpx;
	width: 40rpx;
	margin-top: 15rpx;
}

/* 退出登录 */
.logout-btn {
	margin: 40rpx 30rpx;
	background: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	text-align: center;
	font-size: 32rpx;
	color: #e93b3d;
	font-weight: 500;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}
</style>