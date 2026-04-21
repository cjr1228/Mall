<template>
	<view class="address-edit">
		<view class="form-container">
			<view class="form-item">
				<text class="label">收货人</text>
				<input 
					v-model="form.name"
					type="text" 
					placeholder="请输入收货人姓名"
					placeholder-class="placeholder"
					class="input"
				/>
			</view>
			
			<view class="form-item">
				<text class="label">手机号码</text>
				<input 
					v-model="form.telno"
					type="number" 
					placeholder="请输入手机号码"
					placeholder-class="placeholder"
					class="input"
				/>
			</view>
			
			<view class="form-item">
				<text class="label">所在地区</text>
				<view class="picker" @click="showAreaPicker = true">
					<text v-if="selectedArea" class="area-text">{{selectedArea}}</text>
					<text v-else class="placeholder">请选择所在地区</text>
					<text class="arrow">›</text>
				</view>
			</view>
			
			<view class="form-item">
				<text class="label">详细地址</text>
				<input 
					v-model="form.address"
					type="text" 
					placeholder="请输入详细地址"
					placeholder-class="placeholder"
					class="input"
				/>
			</view>
			
			<view class="form-item">
				<text class="label">邮政编码</text>
				<input 
					v-model="form.zipcode"
					type="number" 
					placeholder="请输入邮政编码（选填）"
					placeholder-class="placeholder"
					class="input"
				/>
			</view>
		</view>
		
		<!-- 保存按钮 -->
		<view class="save-btn" @click="saveAddress">
			<text>保存地址</text>
		</view>
		
		<!-- 地区选择器 -->
		<view v-if="showAreaPicker" class="area-picker-modal">
			<view class="picker-header">
				<text class="cancel" @click="showAreaPicker = false">取消</text>
				<text class="title">选择地区</text>
				<text class="confirm" @click="confirmArea">确定</text>
			</view>
			<view class="area-list">
				<view 
					v-for="(area, index) in areaList"
					:key="index"
					:class="['area-item', form.areald === area.id ? 'selected' : '']"
					@click="selectArea(area)"
				>
					<text class="area-name">{{area.area}}</text>
					<text v-if="form.areald === area.id" class="check-icon">✓</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { onLoad } from '@dcloudio/uni-app'
import { ref, computed } from 'vue'

const form = ref({
	id: null,
	name: '',
	telno: '',
	areald: 1,
	address: '',
	zipcode: '',
	isDefault: 0
})

const areaList = ref([])
const showAreaPicker = ref(false)
const mode = ref('add') // add/edit
const selectMode = ref(false) // 是否为选择模式
const goodsNo = ref('')
const quantity = ref(1)
const returnUrl = ref('') // 返回地址

// 获取Token的函数
const getAuthToken = () => {
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
		return null
	}
	return token
}

onLoad((options) => {
	// 页面加载时检查登录状态
	const token = getAuthToken()
	if (!token) return
	
	mode.value = options.mode || 'add'
	
	if (mode.value === 'edit' && options.id) {
		loadAddressDetail(options.id)
	}
	
	if (options.selectMode === 'true') {
		selectMode.value = true
		goodsNo.value = options.goodsNo || ''
		quantity.value = options.quantity || 1
		returnUrl.value = options.returnUrl || ''
	}
	
	loadAreaList()
})

const loadAddressDetail = (addressId) => {
	const token = getAuthToken()
	if (!token) return
	
	uni.showLoading({ title: '加载中...' })
	
	uni.request({
		url: '/api/address/detail',
		method: 'GET',
		data: { id: addressId },
		header: {
			'Authorization': `Bearer ${token}`
		},
		success: (res) => {
			uni.hideLoading()
			if (res.data.code === 1) {
				form.value = res.data.address
			} else if (res.data.code === -1) {
				// Token失效，跳转到登录页
				handleTokenExpired(res.data.message)
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

const loadAreaList = () => {
	const token = getAuthToken()
	if (!token) return
	
	uni.request({
		url: '/api/address/areas',
		method: 'GET',
		header: {
			'Authorization': `Bearer ${token}`
		},
		success: (res) => {
			if (res.data.code === 1) {
				areaList.value = res.data.list || []
			} else if (res.data.code === -1) {
				handleTokenExpired(res.data.message)
			}
		}
	})
}

const selectedArea = computed(() => {
	const area = areaList.value.find(item => item.id === form.value.areald)
	return area ? area.area : ''
})

const selectArea = (area) => {
	form.value.areald = area.id
}

const confirmArea = () => {
	showAreaPicker.value = false
}

// Token失效处理函数
const handleTokenExpired = (message) => {
	// 清除本地存储的Token
	uni.removeStorageSync('auth_token')
	uni.removeStorageSync('user_info')
	
	uni.showModal({
		title: '提示',
		content: message || '登录已过期，请重新登录',
		showCancel: false,
		success: () => {
			uni.navigateTo({
				url: '/pages/login/login'
			})
		}
	})
}

const saveAddress = () => {
	const token = getAuthToken()
	if (!token) return
	
	// 表单验证
	if (!form.value.name || !form.value.telno || !form.value.address) {
		uni.showToast({
			title: '请填写完整信息',
			icon: 'none'
		})
		return
	}
	
	if (!/^1[3-9]\d{9}$/.test(form.value.telno)) {
		uni.showToast({
			title: '请输入正确的手机号码',
			icon: 'none'
		})
		return
	}
	
	const url = mode.value === 'add' ? '/api/address/add' : '/api/address/update'
	
	uni.showLoading({ title: '保存中...' })
	
	// 设置新地址为默认地址
	const requestData = {
		...form.value,
		isDefault: 1 // 设置为默认地址
	}
	
	// 如果是编辑模式，确保传递id
	if (mode.value === 'edit' && !requestData.id) {
		uni.hideLoading()
		uni.showToast({
			title: '地址ID缺失',
			icon: 'none'
		})
		return
	}
	
	uni.request({
		url,
		method: 'POST',
		data: requestData,
		header: {
			'Authorization': `Bearer ${token}`,
			'Content-Type': 'application/x-www-form-urlencoded'
		},
		success: (res) => {
			uni.hideLoading()
			if (res.data.code === 1) {
				uni.showToast({
					title: '保存成功',
					icon: 'success'
				})
				setTimeout(() => {
					if (selectMode.value) {
						// 选择模式下，返回地址列表页面
						if (returnUrl.value) {
							uni.navigateTo({
								url: returnUrl.value
							})
						} else {
							uni.navigateTo({
								url: `/pages/address/address?selectMode=true&goodsNo=${goodsNo.value}&quantity=${quantity.value}`
							})
						}
					} else {
						uni.navigateBack()
					}
				}, 1500)
			} else if (res.data.code === -1) {
				// Token失效
				handleTokenExpired(res.data.message)
			} else {
				uni.showToast({
					title: res.data.message || '保存失败',
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
.address-edit {
	min-height: 100vh;
	background: #f5f5f5;
	padding-bottom: 120rpx;
}

.form-container {
	background: #fff;
	margin: 20rpx 30rpx;
	border-radius: 20rpx;
	padding: 0 30rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.form-item {
	padding: 30rpx 0;
	border-bottom: 2rpx solid #f5f5f5;
}

.form-item:last-child {
	border-bottom: none;
}

.label {
	display: block;
	font-size: 30rpx;
	color: #333;
	margin-bottom: 20rpx;
	font-weight: 500;
}

.input {
	width: 100%;
	height: 70rpx;
	font-size: 28rpx;
	color: #333;
	border: none;
	background: transparent;
}

.picker {
	display: flex;
	justify-content: space-between;
	align-items: center;
	height: 70rpx;
	font-size: 28rpx;
}

.area-text {
	color: #333;
}

.placeholder {
	color: #ccc;
}

.arrow {
	font-size: 40rpx;
	color: #999;
	transform: rotate(90deg);
}

/* 保存按钮 */
.save-btn {
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

.save-btn:active {
	opacity: 0.9;
}

/* 地区选择器 */
.area-picker-modal {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.5);
	display: flex;
	flex-direction: column;
}

.picker-header {
	background: #fff;
	height: 100rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 30rpx;
	border-bottom: 2rpx solid #f5f5f5;
}

.cancel, .confirm {
	font-size: 30rpx;
	color: #e93b3d;
}

.title {
	font-size: 34rpx;
	font-weight: bold;
	color: #333;
}

.area-list {
	flex: 1;
	background: #fff;
	overflow-y: auto;
}

.area-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 30rpx;
	border-bottom: 2rpx solid #f5f5f5;
}

.area-item:last-child {
	border-bottom: none;
}

.area-name {
	font-size: 30rpx;
	color: #333;
}

.check-icon {
	color: #e93b3d;
	font-size: 36rpx;
	font-weight: bold;
}
</style>