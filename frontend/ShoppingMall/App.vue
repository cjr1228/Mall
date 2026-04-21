<template>
	<view></view>
</template>

<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'

onLaunch(() => {
	console.log('App Launch')
})

onShow(() => {
	console.log('App Show')
})

onHide(() => {
	console.log('App Hide')
})
</script>

<style>
/* 全局样式 */
page {
	background-color: #f8f8f8;
}

/* 按钮样式 */
.btn-primary {
	background: linear-gradient(135deg, #e93b3d 0%, #c82519 100%);
	color: #fff;
	border-radius: 50rpx;
	font-size: 32rpx;
	height: 88rpx;
	line-height: 88rpx;
	text-align: center;
}

.btn-primary:active {
	opacity: 0.8;
}

/* 卡片样式 */
.card {
	background: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	margin: 20rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

/* 价格样式 */
.price {
	color: #e93b3d;
	font-size: 36rpx;
	font-weight: bold;
}

.price::before {
	content: "¥";
	font-size: 24rpx;
}

/* 标签样式 */
.tag {
	display: inline-block;
	padding: 8rpx 16rpx;
	border-radius: 8rpx;
	font-size: 22rpx;
	background-color: #e93b3d;
	color: #fff;
}
</style>