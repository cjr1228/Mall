<template>
	<view class="address-container">
		<scroll-view class="address-list" scroll-y>
			<view v-if="addressList.length === 0" class="empty-address">
				<view class="empty-icon">📍</view>
				<text class="empty-text">暂无收货地址</text>
				<view class="add-address-btn" @click="addAddress">添加地址</view>
			</view>
			
			<view v-else class="addresses">
				<view 
					v-for="(address, index) in addressList"
					:key="index"
					:class="['address-item', address.isDefault ? 'default' : '', selectMode ? 'selectable' : '']"
					@click="handleAddressClick(address)"
				>
					<view class="address-header">
						<text class="name">{{address.name}}</text>
						<text class="phone">{{address.telno}}</text>
						<text v-if="address.isDefault" class="default-tag">默认</text>
					</view>
					
					<view class="address-content">
						<text class="address-text">{{address.address}}</text>
						<text class="zipcode" v-if="address.zipcode">邮编：{{address.zipcode}}</text>
					</view>
					
					<view v-if="!selectMode" class="address-actions">
						<view class="action-left">
							<view 
								:class="['checkbox', address.isDefault ? 'checked' : '']"
								@click="setDefault(address.id)"
							></view>
							<text>设为默认</text>
						</view>
						
						<view class="action-right">
							<text class="edit-btn" @click="editAddress(address)">编辑</text>
							<text class="delete-btn" @click="deleteAddress(address.id)">删除</text>
						</view>
					</view>
					
					<view v-if="selectMode" class="select-mode">
						<view 
							:class="['select-radio', selectedAddressId === address.id ? 'selected' : '']"
							@click.stop="selectAddress(address)"
						></view>
						<text class="select-text">选择此地址</text>
					</view>
				</view>
			</view>
		</scroll-view>
		
		
		<view v-if="!selectMode" class="add-btn" @click="addAddress">
			<text>+ 添加新地址</text>
		</view>
		
		
		<view v-if="selectMode" class="select-confirm-bar">
			<view class="add-btn" @click="addAddress">
				<text>+ 添加新地址</text>
			</view>
			<view class="confirm-btn" @click="confirmSelection" :class="{disabled: !selectedAddressId}">
				<text>确认选择</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onLoad } from '@dcloudio/uni-app'

const addressList = ref([])
const selectMode = ref(false) // 是否为选择模式
const selectedAddressId = ref(null) // 选择的地址ID
const goodsNo = ref('')
const quantity = ref(1)
const returnUrl = ref('')

onLoad((options) => {
	if (options.selectMode === 'true') {
		selectMode.value = true
		goodsNo.value = options.goodsNo || ''
		quantity.value = options.quantity || 1
		returnUrl.value = options.returnUrl || ''
	}
})

onShow(() => {
	loadAddresses()
})

const loadAddresses = () => {
	// 检查Token
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
	
	uni.showLoading({ title: '加载中...' })
	
	uni.request({
		url: '/api/address/list',
		method: 'GET',
		header: {
			'Authorization': `Bearer ${token}`
		},
		success: (res) => {
			uni.hideLoading()
			const data = res.data || {}
			if (data.code === 1) {
				addressList.value = data.list || []
			} else if (data.code === -1) {
				// Token失效处理
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
					title: data.message || '加载失败',
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

const handleAddressClick = (address) => {
	if (selectMode.value) {
		selectAddress(address)
	}
}

const selectAddress = (address) => {
	if (!selectMode.value) return
	selectedAddressId.value = address.id
}

const confirmSelection = () => {
	if (!selectedAddressId.value) {
		uni.showToast({
			title: '请选择一个地址',
			icon: 'none'
		})
		return
	}
	
	// 将选择的地址ID存储到本地，然后返回到商品详情页面
	uni.setStorageSync('temp_address_id', selectedAddressId.value)
	
	// 返回到商品详情页面
	if (returnUrl.value) {
		uni.navigateTo({
			url: returnUrl.value
		})
	} else {
		uni.navigateBack()
	}
}

const addAddress = () => {
	if (selectMode.value) {
		// 选择模式下，跳转到地址编辑页面，并传递参数
		uni.navigateTo({
			url: `/pages/address-edit/address-edit?mode=add&selectMode=true&goodsNo=${goodsNo.value}&quantity=${quantity.value}&returnUrl=${encodeURIComponent('/pages/address/address?selectMode=true&goodsNo=' + goodsNo.value + '&quantity=' + quantity.value + '&returnUrl=' + returnUrl.value)}`
		})
	} else {
		uni.navigateTo({
			url: '/pages/address-edit/address-edit?mode=add'
		})
	}
}

const editAddress = (address) => {
	if (selectMode.value) {
		uni.navigateTo({
			url: `/pages/address-edit/address-edit?mode=edit&id=${address.id}&selectMode=true&goodsNo=${goodsNo.value}&quantity=${quantity.value}&returnUrl=${encodeURIComponent('/pages/address/address?selectMode=true&goodsNo=' + goodsNo.value + '&quantity=' + quantity.value + '&returnUrl=' + returnUrl.value)}`
		})
	} else {
		uni.navigateTo({
			url: `/pages/address-edit/address-edit?mode=edit&id=${address.id}`
		})
	}
}

const setDefault = (addressId) => {
	// 检查Token
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
	
	uni.showLoading({ title: '设置中...' })
	
	uni.request({
		url: '/api/address/setDefault',
		method: 'POST',
		data: { id: addressId },
		header: {
			'Authorization': `Bearer ${token}`,
			'Content-Type': 'application/x-www-form-urlencoded'
		},
		success: (res) => {
			uni.hideLoading()
			const data = res.data || {}
			if (data.code === 1) {
				uni.showToast({
					title: '设置成功',
					icon: 'success'
				})
				// 重新加载地址列表
				loadAddresses()
			} else if (data.code === -1) {
				// Token失效处理
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
					title: data.message || '设置失败',
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

const deleteAddress = (addressId) => {
	// 检查Token
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
		content: '确定要删除这个地址吗？',
		success: (res) => {
			if (res.confirm) {
				uni.showLoading({ title: '删除中...' })
				
				uni.request({
					url: '/api/address/delete',
					method: 'POST',
					data: { id: addressId },
					header: {
						'Authorization': `Bearer ${token}`,
						'Content-Type': 'application/x-www-form-urlencoded'
					},
					success: (res2) => {
						uni.hideLoading()
						const data = res2.data || {}
						if (data.code === 1) {
							uni.showToast({
								title: '删除成功',
								icon: 'success'
							})
							// 重新加载地址列表
							loadAddresses()
						} else if (data.code === -1) {
							// Token失效处理
							uni.removeStorageSync('auth_token')
							uni.removeStorageSync('user_info')
							uni.showModal({
								title: '提示',
								content: '登录已过期，请重新登录',
								success: (res3) => {
									if (res3.confirm) {
										uni.navigateTo({
											url: '/pages/login/login'
										})
									}
								}
							})
						} else {
							uni.showToast({
								title: data.message || '删除失败',
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
</script>

<style scoped>
.address-container {
	height: 100vh;
	display: flex;
	flex-direction: column;
	background-color: #f5f5f5;
}

.address-list {
	flex: 1;
	height: 0;
}

.empty-address {
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
	margin-bottom: 40rpx;
}

.add-address-btn {
	padding: 20rpx 60rpx;
	background: linear-gradient(135deg, #e93b3d 0%, #c82519 100%);
	color: #fff;
	border-radius: 40rpx;
	font-size: 30rpx;
}

/* 地址列表 */
.addresses {
	padding: 30rpx;
}

.address-item {
	background: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
	border: 2rpx solid transparent;
}

.address-item.default {
	border-color: #e93b3d;
	background: #fff5f5;
}

.address-item.selectable {
	cursor: pointer;
}

.address-header {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.name {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-right: 30rpx;
}

.phone {
	font-size: 28rpx;
	color: #666;
	flex: 1;
}

.default-tag {
	padding: 6rpx 16rpx;
	background: #e93b3d;
	color: #fff;
	border-radius: 15rpx;
	font-size: 24rpx;
}

.address-content {
	margin-bottom: 30rpx;
}

.address-text {
	display: block;
	font-size: 28rpx;
	color: #666;
	line-height: 1.6;
	margin-bottom: 10rpx;
}

.zipcode {
	font-size: 26rpx;
	color: #999;
}

.address-actions {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-top: 20rpx;
	border-top: 2rpx solid #f5f5f5;
}

.action-left {
	display: flex;
	align-items: center;
}

.checkbox {
	width: 36rpx;
	height: 36rpx;
	border: 2rpx solid #ddd;
	border-radius: 50%;
	margin-right: 15rpx;
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
	font-size: 20rpx;
	font-weight: bold;
}

.action-left text {
	font-size: 28rpx;
	color: #666;
}

.action-right {
	display: flex;
	align-items: center;
}

.edit-btn, .delete-btn {
	font-size: 28rpx;
	padding: 10rpx 20rpx;
	border-radius: 20rpx;
	margin-left: 20rpx;
}

.edit-btn {
	color: #e93b3d;
	background: #ffe7eb;
}

.delete-btn {
	color: #666;
	background: #f5f5f5;
}

/* 选择模式样式 */
.select-mode {
	display: flex;
	align-items: center;
	padding-top: 20rpx;
	border-top: 2rpx solid #f5f5f5;
}

.select-radio {
	width: 36rpx;
	height: 36rpx;
	border: 2rpx solid #ddd;
	border-radius: 50%;
	margin-right: 15rpx;
	position: relative;
}

.select-radio.selected {
	background: #e93b3d;
	border-color: #e93b3d;
}

.select-radio.selected::after {
	content: '✓';
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	color: #fff;
	font-size: 20rpx;
	font-weight: bold;
}

.select-text {
	font-size: 28rpx;
	color: #333;
}

/* 添加按钮 */
.add-btn {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	height: 100rpx;
	background: linear-gradient(135deg, #e93b3d 0%, #c82519 100%);
	display: flex;
	align-items: center;
	justify-content: center;
	color: #fff;
	font-size: 32rpx;
	font-weight: 500;
}

.add-btn:active {
	opacity: 0.9;
}

/* 选择模式下的确认栏 */
.select-confirm-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	height: 100rpx;
	display: flex;
	background: #fff;
	border-top: 2rpx solid #f5f5f5;
}

.select-confirm-bar .add-btn {
	position: relative;
	flex: 1;
	border-radius: 0;
}

.select-confirm-bar .confirm-btn {
	flex: 1;
	background: linear-gradient(135deg, #34c759 0%, #2db34a 100%);
	display: flex;
	align-items: center;
	justify-content: center;
	color: #fff;
	font-size: 32rpx;
	font-weight: 500;
}

.select-confirm-bar .confirm-btn.disabled {
	background: #ccc;
	pointer-events: none;
}

.select-confirm-bar .confirm-btn:active {
	opacity: 0.9;
}
</style>