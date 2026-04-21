<template>
	<view class="cart-container">
		<!-- 购物车为空 -->
		<view v-if="cartList.length === 0" class="empty-cart">
			<view class="empty-icon">🛒</view>
			<text class="empty-text">购物车空空如也</text>
			<view class="go-shopping" @click="goIndex">去逛逛</view>
		</view>
		
		<!-- 购物车列表 -->
		<scroll-view v-else class="cart-list" scroll-y>
			<view class="cart-items">
				<view 
					v-for="(item, index) in cartList" 
					:key="index" 
					class="cart-item"
				>
					<view class="item-select">
						<view 
							:class="['checkbox', item.selected ? 'checked' : '']"
							@click="toggleSelect(index)"
						></view>
					</view>
					
					<image 
						:src="item.goodsImg || '/static/images/default-goods.png'" 
						class="item-img"
						@click="goGoodsDetail(item)"
					/>
					
					<view class="item-info">
						<view class="item-name" @click="goGoodsDetail(item)">
							{{item.goodsName}}
						</view>
						<view class="item-price">¥{{item.memberPrice.toFixed(2)}}</view>
						
						<view class="item-actions">
							<view class="quantity-control">
								<view 
									class="btn minus" 
									@click="updateQuantity(index, item.quantity - 1)"
									:class="{ disabled: item.quantity <= 1 }"
								>-</view>
								<input 
									type="number" 
									v-model="item.quantity"
									class="quantity-input"
									@blur="validateQuantity(index)"
								/>
								<view 
									class="btn plus" 
									@click="updateQuantity(index, item.quantity + 1)"
									:class="{ disabled: item.quantity >= item.stock }"
								>+</view>
							</view>
							
							<view class="delete-btn" @click="removeItem(index)">
								<text>删除</text>
							</view>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>
		
		<!-- 结算栏 -->
		<view v-if="cartList.length > 0" class="settle-bar">
			<view class="select-all">
				<view 
					:class="['checkbox', allSelected ? 'checked' : '']"
					@click="toggleAllSelect"
				></view>
				<text>全选</text>
			</view>
			
			<view class="total">
				<text class="label">合计：</text>
				<text class="amount">¥{{totalAmount.toFixed(2)}}</text>
			</view>
			
			<view class="settle-btn" @click="goSettle">
				结算({{selectedCount}})
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'

const cartList = ref([])

onShow(() => {
	loadCartList()
})

const allSelected = computed(() => {
	return cartList.value.length > 0 && cartList.value.every(item => item.selected)
})

const selectedCount = computed(() => {
	return cartList.value.filter(item => item.selected).length
})

const totalAmount = computed(() => {
	return cartList.value
		.filter(item => item.selected)
		.reduce((sum, item) => sum + item.memberPrice * item.quantity, 0)
})

const loadCartList = () => {
	// 从本地存储获取Token
	const token = uni.getStorageSync('auth_token')
	
	if (!token) {
		// 没有Token，未登录状态
		cartList.value = []
		uni.showModal({
			title: '提示',
			content: '请先登录',
			success: (modalRes) => {
				if (modalRes.confirm) {
					uni.navigateTo({
						url: '/pages/login/login'
					})
				}
			}
		})
		return
	}
	
	uni.showLoading({ title: '加载中...' })
	
	uni.request({
		url: '/api/cart/list',
		method: 'GET',
		header: {
			'Authorization': `Bearer ${token}`
		},
		success: (res) => {
			uni.hideLoading()
			if (res.data.code === 1) {
				const list = res.data.list || []
				cartList.value = list.map(item => ({
					...item,
					selected: false,
					stock: item.stock || 99
				}))
			} else if (res.data.code === -1) {
				// Token失效，清理本地存储
				uni.removeStorageSync('auth_token')
				uni.removeStorageSync('user_info')
				uni.showModal({
					title: '提示',
					content: '登录已过期，请重新登录',
					success: (modalRes) => {
						if (modalRes.confirm) {
							uni.navigateTo({
								url: '/pages/login/login'
							})
						}
					}
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

const toggleSelect = (index) => {
	cartList.value[index].selected = !cartList.value[index].selected
}

const toggleAllSelect = () => {
	const newSelected = !allSelected.value
	cartList.value.forEach(item => {
		item.selected = newSelected
	})
}

const updateQuantity = async (index, newQuantity) => {
	if (newQuantity < 1) newQuantity = 1
	if (newQuantity > cartList.value[index].stock) {
		uni.showToast({
			title: '库存不足',
			icon: 'none'
		})
		return
	}
	
	cartList.value[index].quantity = newQuantity
	
	// 从本地存储获取Token
	const token = uni.getStorageSync('auth_token')
	if (!token) {
		uni.showModal({
			title: '提示',
			content: '请先登录',
			success: (modalRes) => {
				if (modalRes.confirm) {
					uni.navigateTo({
						url: '/pages/login/login'
					})
				}
			}
		})
		return
	}
	
	// 更新服务器
	uni.request({
		url: '/api/cart/update',
		method: 'POST',
		header: {
			'Authorization': `Bearer ${token}`,
			'Content-Type': 'application/x-www-form-urlencoded'
		},
		data: {
			goodsNo: cartList.value[index].goodsNo,
			quantity: newQuantity
		},
		success: (res) => {
			if (res.data.code !== 1) {
				// 更新失败，恢复原数量
				loadCartList()
				uni.showToast({
					title: res.data.message || '更新失败',
					icon: 'none'
				})
			}
		}
	})
}

const validateQuantity = (index) => {
	if (cartList.value[index].quantity < 1) {
		cartList.value[index].quantity = 1
		updateQuantity(index, 1)
	}
}

const removeItem = (index) => {
	const item = cartList.value[index]
	
	uni.showModal({
		title: '提示',
		content: '确定删除该商品吗？',
		success: (res) => {
			if (res.confirm) {
				// 从本地存储获取Token
				const token = uni.getStorageSync('auth_token')
				if (!token) {
					uni.showModal({
						title: '提示',
						content: '请先登录',
						success: (modalRes) => {
							if (modalRes.confirm) {
								uni.navigateTo({
									url: '/pages/login/login'
								})
							}
						}
					})
					return
				}
				
				uni.request({
					url: '/api/cart/remove',
					method: 'POST',
					header: {
						'Authorization': `Bearer ${token}`,
						'Content-Type': 'application/x-www-form-urlencoded'
					},
					data: {
						goodsNo: item.goodsNo
					},
					success: (res) => {
						if (res.data.code === 1) {
							cartList.value.splice(index, 1)
							uni.showToast({
								title: '删除成功',
								icon: 'success'
							})
						} else {
							uni.showToast({
								title: res.data.message || '删除失败',
								icon: 'none'
							})
						}
					}
				})
			}
		}
	})
}

const goGoodsDetail = (item) => {
	uni.navigateTo({
		url: `/pages/goods-detail/goods-detail?goodsNo=${item.goodsNo}`
	})
}

const goIndex = () => {
	uni.switchTab({
		url: '/pages/index/index'
	})
}

const goSettle = () => {
	const selectedItems = cartList.value.filter(item => item.selected)
	if (selectedItems.length === 0) {
		uni.showToast({
			title: '请选择商品',
			icon: 'none'
		})
		return
	}
	
	// 从本地存储获取Token
	const token = uni.getStorageSync('auth_token')
	if (!token) {
		uni.showModal({
			title: '提示',
			content: '请先登录',
			success: (modalRes) => {
				if (modalRes.confirm) {
					uni.navigateTo({
						url: '/pages/login/login'
					})
				}
			}
		})
		return
	}
	
	// 先检查库存
	uni.request({
		url: '/api/cart/checkStock',
		method: 'GET',
		header: {
			'Authorization': `Bearer ${token}`
		},
		success: (res) => {
			if (res.data.code === 1 && res.data.inStock) {
				uni.navigateTo({
					url: '/pages/order/order?fromCart=true'
				})
			} else {
				uni.showToast({
					title: '部分商品库存不足',
					icon: 'none'
				})
			}
		}
	})
}
</script>

<style scoped>
.cart-container {
	min-height: 100vh;
	background: #f5f5f5;
}

/* 空购物车 */
.empty-cart {
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

/* 购物车列表 */
.cart-list {
	height: calc(100vh - 120rpx);
}

.cart-items {
	padding: 30rpx;
}

.cart-item {
	display: flex;
	align-items: center;
	background: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.item-select {
	margin-right: 20rpx;
}

.checkbox {
	width: 40rpx;
	height: 40rpx;
	border: 2rpx solid #ddd;
	border-radius: 50%;
	position: relative;
}

.checkbox.checked {
	background: #e93b3d;
	border-color: #e93b3d;
}

.checkbox.checked::after {
	content: '✓';
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	color: #fff;
	font-size: 24rpx;
	font-weight: bold;
}

.item-img {
	width: 180rpx;
	height: 180rpx;
	border-radius: 15rpx;
	margin-right: 30rpx;
	background: #f8f8f8;
}

.item-info {
	flex: 1;
}

.item-name {
	font-size: 30rpx;
	color: #333;
	margin-bottom: 20rpx;
	line-height: 1.4;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	overflow: hidden;
}

.item-price {
	font-size: 36rpx;
	font-weight: bold;
	color: #e93b3d;
	margin-bottom: 30rpx;
}

.item-actions {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.quantity-control {
	display: flex;
	align-items: center;
}

.quantity-control .btn {
	width: 50rpx;
	height: 50rpx;
	border: 2rpx solid #ddd;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 32rpx;
	color: #666;
	background: #fff;
}

.quantity-control .btn.disabled {
	background: #f5f5f5;
	color: #ccc;
	border-color: #f5f5f5;
}

.quantity-input {
	width: 80rpx;
	height: 50rpx;
	margin: 0 15rpx;
	text-align: center;
	font-size: 30rpx;
	color: #333;
	border: 2rpx solid #eee;
	border-radius: 10rpx;
	background: #fff;
}

.delete-btn {
	padding: 10rpx 20rpx;
	background: #ffe7eb;
	color: #e93b3d;
	border-radius: 25rpx;
	font-size: 26rpx;
}

/* 结算栏 */
.settle-bar {
	position: fixed;
	bottom: 90rpx;
	left: 0;
	right: 0;
	height: 120rpx;
	background: #fff;
	border-top: 2rpx solid #f5f5f5;
	display: flex;
	align-items: center;
	padding: 0 30rpx;
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.select-all {
	display: flex;
	align-items: center;
	margin-right: 30rpx;
}

.select-all text {
	font-size: 28rpx;
	color: #333;
	margin-left: 15rpx;
}

.total {
	flex: 1;
	display: flex;
	align-items: baseline;
}

.total .label {
	font-size: 28rpx;
	color: #333;
}

.total .amount {
	font-size: 36rpx;
	font-weight: bold;
	color: #e93b3d;
}

.settle-btn {
	width: 220rpx;
	height: 80rpx;
	background: linear-gradient(135deg, #e93b3d 0%, #c82519 100%);
	border-radius: 40rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	color: #fff;
	font-size: 32rpx;
	font-weight: 500;
	margin-left: 20rpx;
}

.settle-btn:active {
	opacity: 0.9;
}
</style>