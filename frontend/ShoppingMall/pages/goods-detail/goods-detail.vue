<template>
	<view class="goods-detail">
		<!-- 商品图片轮播 -->
		<swiper class="goods-swiper" indicator-dots circular>
			<swiper-item v-for="(img, index) in goodsImages" :key="index">
				<image :src="img" mode="aspectFill" class="swiper-img" />
			</swiper-item>
		</swiper>
		
		<!-- 商品信息 -->
		<view class="goods-info">
			<view class="goods-price">
				<text class="current-price">¥{{currentPrice.toFixed(2)}}</text>
				<text v-if="goods.discount < 1" class="original-price">¥{{goods.price.toFixed(2)}}</text>
				<text v-if="goods.discount < 1" class="discount">{{(goods.discount * 10).toFixed(1)}}折</text>
			</view>
			
			<view class="goods-name">{{goods.name}}</view>
			<view class="goods-desc">{{goods.desc}}</view>
			
			<view class="goods-meta">
				<view class="meta-item">
					<text class="label">库存</text>
					<text class="value">{{goods.qty || 0}}件</text>
				</view>
				<view class="meta-item">
					<text class="label">销量</text>
					<text class="value">0件</text>
				</view>
				<view class="meta-item">
					<text class="label">收藏</text>
					<text class="value">0人</text>
				</view>
			</view>
		</view>
		
		<!-- 操作按钮 -->
		<view class="action-bar">
			<view class="action-btn">
				<view class="btn-icon" @click="toggleCollect">
					 <image  :src="isCollected ? '/static/icon/collect.png' : '/static/tabbar/collect.png'"  class="icon-img" mode="widthFix"  />
					<text class="text">{{isCollected ? '已收藏' : '收藏'}}</text>
				</view>
				<view class="btn-icon" @click="goCart">
					<image src="/static/tabbar/cart.png"  class="icon-img" mode="widthFix"/>
					<text class="text">购物车</text>
				</view>
			</view>
			<view class="action-buttons">
				<view class="add-cart" @click="addToCart">加入购物车</view>
				<view class="buy-now" @click="buyNow">立即购买</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { onLoad, onShow } from '@dcloudio/uni-app'
import { ref, computed } from 'vue'

const goods = ref({
	id: 0,
	no: '',
	name: '',
	desc: '',
	img: '',
	price: 0,
	discount: 1,
	qty: 0
})
const goodsImages = ref([])
const isCollected = ref(false)
const quantity = ref(1)
const selectedAddressId = ref(null) // 存储从地址页面返回的地址ID

const currentPrice = computed(() => {
	return goods.value.price * (goods.value.discount || 1)
})

onLoad((options) => {
	console.log('商品详情页面接收到的参数:', options)
	if (options.goodsId) {
		console.log('使用goodsId加载商品:', options.goodsId)
		loadGoodsDetail(options.goodsId)
	} else if (options.goodsNo) {
		console.log('使用goodsNo加载商品:', options.goodsNo)
		loadGoodsDetailByNo(options.goodsNo)
	}
})

onShow(() => {
	// 检查是否有从地址页面返回的地址ID
	const addressId = uni.getStorageSync('temp_address_id')
	if (addressId) {
		selectedAddressId.value = addressId
		uni.removeStorageSync('temp_address_id')
		
		// 如果有地址ID，则使用这个地址创建订单
		if (selectedAddressId.value) {
			createOrderWithAddress(selectedAddressId.value)
		}
	}
})

const loadGoodsDetail = (goodsId) => {
	uni.showLoading({ title: '加载中...' })
	
	uni.request({
		url: '/api/goods/detail',
		method: 'GET',
		data: { goodsId },
		success: (res) => {
			uni.hideLoading()
			if (res.data.code === 1) {
				goods.value = res.data.goods
				goodsImages.value = [goods.value.img || '/static/images/default-goods.png']
			} else {
				uni.showToast({
					title: res.data.message,
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

const loadGoodsDetailByNo = (goodsNo) => {
	uni.showLoading({ title: '加载中...' })
	
	uni.request({
		url: '/api/goods/detail',
		method: 'GET',
		data: { goodsNo },
		success: (res) => {
			uni.hideLoading()
			console.log('商品详情请求响应:', res)
			console.log('响应数据:', res.data)
			if (res.data.code === 1) {
				goods.value = res.data.goods
				console.log('赋值后的goods:', goods.value)
				goodsImages.value = [goods.value.img || '/static/images/default-goods.png']
				checkCollectStatus()
			} else {
				uni.showToast({
					title: res.data.message,
					icon: 'none'
				})
			}
		},
		fail: () => {
			uni.hideLoading()
		}
	})
}

const checkCollectStatus = () => {
	const token = uni.getStorageSync('auth_token')
	
	if (!token) {
		isCollected.value = false
		return
	}
	
	uni.request({
		url: '/api/collect/check',
		method: 'GET',
		data: { 
			goodsNo: goods.value.no 
		},
		header: {
			'Authorization': `Bearer ${token}`,
			'Content-Type': 'application/json'
		},
		success: (res) => {
			console.log(res);
			const data = res.data || {}
			if (data.code === 1) {
				isCollected.value = data.isCollected || false
			} else if (data.code === -1) {
				isCollected.value = false
			}
		},
		fail: () => {
			isCollected.value = false
		}
	})
}

const toggleCollect = () => {
	if (isCollected.value) {
		removeCollect()
	} else {
		addCollect()
	}
}

const addCollect = () => {
	// 从本地存储获取Token
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
	
	uni.request({
		url: '/api/collect/add',
		method: 'POST',
		data: { goodsNo: goods.value.no },
		header: {
			'Authorization': `Bearer ${token}`,
			'Content-Type': 'application/x-www-form-urlencoded'
		},
		success: (res) => {
			if (res.data.code === 1) {
				isCollected.value = true
				uni.showToast({
					title: '收藏成功',
					icon: 'success'
				})
			} else {
				uni.showToast({
					title: res.data.message || '收藏失败',
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

const removeCollect = () => {
	// 从本地存储获取Token
	const token = uni.getStorageSync('auth_token')
	if (!token) return
	
	uni.request({
		url: '/api/collect/remove',
		method: 'POST',
		data: { goodsNo: goods.value.no },
		header: {
			'Authorization': `Bearer ${token}`,
			'Content-Type': 'application/x-www-form-urlencoded'
		},
		success: (res) => {
			if (res.data.code === 1) {
				isCollected.value = false
				uni.showToast({
					title: '取消收藏',
					icon: 'success'
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

const addToCart = () => {
	// 从本地存储获取Token
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
	
	uni.request({
		url: '/api/cart/add',
		method: 'POST',
		data: {
			goodsNo: goods.value.no,
			quantity: quantity.value
		},
		header: {
			'Authorization': `Bearer ${token}`,
			'Content-Type': 'application/x-www-form-urlencoded'
		},
		success: (res) => {
			if (res.data.code === 1) {
				uni.showToast({
					title: '加入购物车成功',
					icon: 'success'
				})
			} else {
				uni.showToast({
					title: res.data.message || '加入购物车失败',
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

const buyNow = async () => {
	// 检查是否登录
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
	
	// 1. 检查库存
	const stockCheck = await checkStock()
	if (!stockCheck) return
	
	// 2. 获取默认地址
	const defaultAddress = await getDefaultAddress()
	if (!defaultAddress) {
		// 没有默认地址，跳转到地址列表页面选择地址
		uni.showModal({
			title: '提示',
			content: '请先选择收货地址',
			showCancel: false,
			success: (res) => {
				if (res.confirm) {
					// 跳转到地址列表页面，并传递商品信息
					uni.navigateTo({
						url: `/pages/address/address?selectMode=true&goodsNo=${goods.value.no}&quantity=${quantity.value}&returnUrl=${encodeURIComponent('/pages/goods-detail/goods-detail?goodsNo=' + goods.value.no)}`
					})
				}
			}
		})
	} else {
		// 有默认地址，直接创建订单
		createOrderWithAddress(defaultAddress.id)
	}
}

const checkStock = () => {
	return new Promise((resolve) => {
		uni.request({
			url: '/api/goods/checkStock',
			method: 'GET',
			data: {
				goodsNo: goods.value.no,
				quantity: quantity.value
			},
			success: (res) => {
				if (res.data.code === 1 && res.data.inStock) {
					resolve(true)
				} else {
					uni.showToast({
						title: res.data.message || '库存不足',
						icon: 'none'
					})
					resolve(false)
				}
			},
			fail: () => {
				uni.showToast({
					title: '网络错误',
					icon: 'none'
				})
				resolve(false)
			}
		})
	})
}

const getDefaultAddress = () => {
	return new Promise((resolve) => {
		const token = uni.getStorageSync('auth_token')
		uni.request({
			url: '/api/address/default',
			method: 'GET',
			header: {
				'Authorization': `Bearer ${token}`
			},
			success: (res) => {
				if (res.data.code === 1 && res.data.address) {
					resolve(res.data.address)
				} else {
					resolve(null)
				}
			},
			fail: () => {
				resolve(null)
			}
		})
	})
}

const createOrderWithAddress = (addressId) => {
	const token = uni.getStorageSync('auth_token')
	
	uni.showLoading({ title: '创建订单中...' })
	
	uni.request({
		url: '/api/order/createNow',
		method: 'POST',
		data: {
			goodsNo: goods.value.no,
			quantity: quantity.value,
			addressId: addressId,
			shipType: '1',
			payType: '1'
		},
		header: {
			'Authorization': `Bearer ${token}`,
			'Content-Type': 'application/x-www-form-urlencoded'
		},
		success: (res) => {
			uni.hideLoading()
			const data = res.data || {}
			
			if (data.code === 1) {
				uni.showToast({
					title: '订单创建成功',
					icon: 'success'
				})
				
				// 跳转到订单详情页面
				setTimeout(() => {
					uni.navigateTo({
						url: `/pages/order-detail/order-detail?orderId=${data.orderId}`
					})
				}, 1500)
			} else {
				uni.showToast({
					title: data.message || '创建订单失败',
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

const goCart = () => {
	uni.switchTab({
		url: '/pages/cart/cart'
	})
}
</script>

<style scoped>
/* 样式保持不变 */
.goods-detail {
	padding-bottom: 120rpx;
	min-height: 100vh;
	background: #f5f5f5;
}

.goods-swiper {
	width: 100%;
	height: 750rpx;
	background: #fff;
}

.swiper-img {
	width: 100%;
	height: 100%;
}

.goods-info {
	background: #fff;
	padding: 30rpx;
	margin-top: 20rpx;
	border-radius: 20rpx;
}

.goods-price {
	display: flex;
	align-items: baseline;
	margin-bottom: 30rpx;
}

.current-price {
	font-size: 48rpx;
	font-weight: bold;
	color: #e93b3d;
	margin-right: 20rpx;
}

.original-price {
	font-size: 28rpx;
	color: #999;
	text-decoration: line-through;
	margin-right: 20rpx;
}

.discount {
	padding: 6rpx 16rpx;
	background: #ffe7eb;
	color: #e93b3d;
	border-radius: 8rpx;
	font-size: 24rpx;
}

.goods-name {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 20rpx;
	line-height: 1.4;
}

.goods-desc {
	font-size: 28rpx;
	color: #666;
	line-height: 1.6;
	margin-bottom: 30rpx;
}

.goods-meta {
	display: flex;
	justify-content: space-around;
	padding: 20rpx 0;
	border-top: 2rpx solid #f5f5f5;
}

.meta-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.meta-item .label {
	font-size: 24rpx;
	color: #999;
	margin-bottom: 10rpx;
}

.meta-item .value {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

/* 操作栏 */
.action-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: #fff;
	border-top: 2rpx solid #f5f5f5;
	padding: 20rpx 30rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.action-btn {
	display: flex;
	align-items: center;
}

.btn-icon {
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-right: 40rpx;
}

.btn-icon .icon {
	font-size: 40rpx;
	margin-bottom: 8rpx;
}

.btn-icon .text {
	font-size: 22rpx;
	color: #666;
}

.action-buttons {
	display: flex;
	flex: 1;
}

.add-cart, .buy-now {
	flex: 1;
	height: 80rpx;
	border-radius: 40rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 30rpx;
	font-weight: 500;
	margin-left: 20rpx;
}

.add-cart {
	background: linear-gradient(135deg, #ff9500 0%, #ff7300 100%);
	color: #fff;
}

.buy-now {
	background: linear-gradient(135deg, #e93b3d 0%, #c82519 100%);
	color: #fff;
}

.add-cart:active, .buy-now:active {
	opacity: 0.9;
}

.icon-img {
  width: 40rpx; /* 与原emoji大小保持一致 */
  height: auto;
  margin-bottom: 8rpx;
}
</style>