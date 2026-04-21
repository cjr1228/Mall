<template>
	<view class="collect-container">
		<scroll-view 
			class="collect-list" 
			scroll-y
			refresher-enabled
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
			@scrolltolower="loadMore"
		>
			<view v-if="collectList.length === 0" class="empty-collect">
				<view class="empty-icon">❤️</view>
				<text class="empty-text">还没有收藏商品</text>
				<view class="go-shopping" @click="goIndex">去逛逛</view>
			</view>
			
			<view v-else class="collects">
				<view v-for="(item, index) in collectList" :key="index" class="collect-item">
					<!-- 修复：使用商品图片，如果没有则显示默认图片 -->
					<image 
						:src="item.goods && item.goods.img ? item.goods.img : '/static/images/default-goods.png'" 
						class="goods-img"
						@click="goGoodsDetail(item)"
						mode="aspectFill"
					/>
					
					<view class="goods-info">
						<text class="goods-name" @click="goGoodsDetail(item)">
							{{item.goods && item.goods.name ? item.goods.name : '商品'}}
						</text>
						<!-- 修复：显示商品价格 -->
						<text class="goods-price">¥{{getGoodsPrice(item)}}</text>
						<text class="collect-time">收藏于：{{formatDate(item.colTime)}}</text>
						
						<view class="item-actions">
							<view class="action-btn remove" @click="removeCollect(item.goodsNo, index)">
								取消收藏
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
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'

const collectList = ref([])
const loading = ref(false)
const noMore = ref(false)
const refreshing = ref(false)
const currentPage = ref(1)

onShow(() => {
	loadCollects()
})

const onRefresh = () => {
	refreshing.value = true
	collectList.value = []
	currentPage.value = 1
	noMore.value = false
	setTimeout(() => {
		loadCollects()
		refreshing.value = false
	}, 1000)
}

const loadMore = () => {
	if (loading.value || noMore.value) return
	currentPage.value++
	loadCollects()
}

const loadCollects = async () => {
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
	
	loading.value = true
	
	try {
		const response = await uni.request({
			url: '/api/collect/list',
			method: 'GET',
			data: {
				page: currentPage.value,
				pageSize: 10
			},
			header: {
				'Authorization': `Bearer ${token}`,
				'Content-Type': 'application/json'
			}
		})
		
		console.log('收藏列表响应:', response)
		
		const data = response.data || {}
		if (data.code === 1) {
			const newCollects = data.list || []
			
			if (newCollects.length === 0) {
				noMore.value = true
			} else {
				if (currentPage.value === 1) {
					collectList.value = newCollects
				} else {
					collectList.value = [...collectList.value, ...newCollects]
				}
			}
		} else if (data.code === -1) {
			uni.removeStorageSync('auth_token')
			uni.removeStorageSync('user_info')
			uni.showModal({
				title: '提示',
				content: '登录已过期，请重新登录',
				success: (res) => {
					if (res.confirm) {
						uni.navigateTo({
							url: '/pages/login/login'
						})
					}
				}
			})
		} else {
			uni.showToast({
				title: data.message || '加载失败',
				icon: 'none'
			})
		}
	} catch (error) {
		console.error('加载收藏失败:', error)
		uni.showToast({
			title: '网络异常',
			icon: 'none'
		})
	} finally {
		loading.value = false
	}
}

const getGoodsPrice = (item) => {
	if (item.goods) {
		const price = item.goods.price || 0
		const discount = item.goods.discount || 1
		return (price * discount).toFixed(2)
	}
	return '0.00'
}

const formatDate = (dateString) => {
	if (!dateString) return ''
	const date = new Date(dateString)
	return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const goGoodsDetail = (item) => {
	if (item.goodsNo) {
		uni.navigateTo({
			url: `/pages/goods-detail/goods-detail?goodsNo=${item.goodsNo}`
		})
	} else {
		uni.showToast({
			title: '商品信息错误',
			icon: 'none'
		})
	}
}

const removeCollect = (goodsNo, index) => {
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
	
	uni.showModal({
		title: '提示',
		content: '确定要取消收藏吗？',
		success: (res) => {
			if (res.confirm) {
				uni.request({
					url: '/api/collect/remove',
					method: 'POST',
					data: {
						goodsNo: goodsNo
					},
					header: {
						'Authorization': `Bearer ${token}`,
						'Content-Type': 'application/x-www-form-urlencoded'
					},
					success: (res2) => {
						const data = res2.data || {}
						if (data.code === 1) {
							collectList.value.splice(index, 1)
							uni.showToast({
								title: '取消收藏成功',
								icon: 'success'
							})
							
							// 如果列表为空，重新加载第一页
							if (collectList.value.length === 0 && currentPage.value > 1) {
								currentPage.value = 1
								loadCollects()
							}
						} else if (data.code === -1) {
							uni.removeStorageSync('auth_token')
							uni.removeStorageSync('user_info')
							uni.showModal({
								title: '提示',
								content: '登录已过期，请重新登录',
								success: (res) => {
									if (res.confirm) {
										uni.navigateTo({
											url: '/pages/login/login'
										})
									}
								}
							})
						} else {
							uni.showToast({
								title: data.message || '操作失败',
								icon: 'none'
							})
						}
					},
					fail: () => {
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

const goIndex = () => {
	uni.switchTab({
		url: '/pages/index/index'
	})
}
</script>


<style scoped>
.collect-container {
	height: 100vh;
	background: #f5f5f5;
}

.collect-list {
	height: 100%;
}

.empty-collect {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	height: 70vh;
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

/* 收藏列表 */
.collects {
	padding: 30rpx;
}

.collect-item {
	display: flex;
	background: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.goods-img {
	width: 220rpx;
	height: 220rpx;
	border-radius: 15rpx;
	background: #f8f8f8;
	margin-right: 30rpx;
}

.goods-info {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.goods-name {
	font-size: 30rpx;
	color: #333;
	line-height: 1.4;
	margin-bottom: 15rpx;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	overflow: hidden;
}

.goods-price {
	font-size: 36rpx;
	color: #e93b3d;
	font-weight: bold;
	margin-bottom: 15rpx;
}

.collect-time {
	font-size: 26rpx;
	color: #999;
	margin-bottom: 30rpx;
}

.item-actions {
	margin-top: auto;
}

.action-btn {
	width: 100%;
	padding: 20rpx 0;
	text-align: center;
	border: 2rpx solid #ddd;
	border-radius: 25rpx;
	font-size: 28rpx;
	color: #666;
}

.action-btn.remove {
	background: #ffe7eb;
	color: #e93b3d;
	border-color: #ffe7eb;
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