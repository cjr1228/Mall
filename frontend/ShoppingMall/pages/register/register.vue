<template>
	<view class="register-container">
		<view class="register-header">
			<image class="logo" src="/static/logo-red.png" mode="widthFix"></image>
			<text class="slogan">创建新账号</text>
		</view>
		
		<view class="register-form">
			<view class="form-item">
				<text class="label">邮箱</text>
				<input 
					v-model="form.email"
					type="text" 
					placeholder="请输入邮箱"
					placeholder-class="placeholder"
					class="input"
				/>
			</view>
			
			<view class="form-item">
				<text class="label">昵称</text>
				<input 
					v-model="form.nick"
					type="text" 
					placeholder="请输入昵称"
					placeholder-class="placeholder"
					class="input"
				/>
			</view>
			
			<view class="form-item">
				<text class="label">密码</text>
				<input 
					v-model="form.password"
					type="password" 
					placeholder="请输入密码"
					placeholder-class="placeholder"
					class="input"
				/>
			</view>
			
			<view class="form-item">
				<text class="label">确认密码</text>
				<input 
					v-model="form.confirmPassword"
					type="password" 
					placeholder="请再次输入密码"
					placeholder-class="placeholder"
					class="input"
				/>
			</view>
			
			<view class="register-btn" @click="handleRegister">
				<text>注册</text>
			</view>
			
			<view class="login-link">
				<text>已有账号？</text>
				<text class="link" @click="goLogin">立即登录</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'

const form = ref({
	email: '',
	nick: '',
	password: '',
	confirmPassword: ''
})

const handleRegister = () => {
	if (!form.value.email || !form.value.password || !form.value.confirmPassword) {
		uni.showToast({
			title: '请输入完整信息',
			icon: 'none'
		})
		return
	}
	
	if (form.value.password !== form.value.confirmPassword) {
		uni.showToast({
			title: '两次密码不一致',
			icon: 'none'
		})
		return
	}
	
	if (form.value.password.length < 6) {
		uni.showToast({
			title: '密码长度不能少于6位',
			icon: 'none'
		})
		return
	}
	
	uni.showLoading({
		title: '注册中...'
	})
	const params = new URLSearchParams();
	params.append('email',form.value.email);
	params.append('nick',form.value.nick);
	params.append('password',form.value.password);
	params.append('confirmPassword',form.value.confirmPassword);
	
	uni.request({
		url: '/api/member/register',
		method: 'POST',
		header:{
			'Content-Type':'application/x-www-form-urlencoded'
		},
		data: params.toString(),
		success: (res) => {
			uni.hideLoading()
			if (res.data.code === 1) {
				uni.showToast({
					title: '注册成功',
					icon: 'success'
				})
				// 注册成功后跳转到登录页
				setTimeout(() => {
					uni.navigateTo({
						url: '/pages/login/login'
					})
				}, 1500)
			} else {
				uni.showToast({
					title: res.data.message || '注册失败',
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

const goLogin = () => {
	uni.navigateTo({
		url: '/pages/login/login'
	})
}
</script>

<style scoped>
.register-container {
	padding: 80rpx 60rpx;
	min-height: 100vh;
	background: linear-gradient(135deg, #f9f9f9 0%, #ffffff 100%);
}

.register-header {
	text-align: center;
	margin-bottom: 80rpx;
}

.logo {
	width: 300rpx;  /* 根据你的logo尺寸调整 */
    height: auto;
	margin: 0 auto 20rpx;
	display: block;
	}
.slogan {
	font-size: 32rpx;
	color: #999;
}

.register-form {
	background: #fff;
	border-radius: 30rpx;
	padding: 60rpx 50rpx;
	box-shadow: 0 10rpx 40rpx rgba(0, 0, 0, 0.1);
}

.form-item {
	margin-bottom: 40rpx;
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
	height: 90rpx;
	border: 2rpx solid #eee;
	border-radius: 15rpx;
	padding: 0 30rpx;
	font-size: 28rpx;
	box-sizing: border-box;
}

.input:focus {
	border-color: #e93b3d;
}

.placeholder {
	color: #ccc;
	font-size: 28rpx;
}

.register-btn {
	width: 100%;
	height: 90rpx;
	background: linear-gradient(135deg, #e93b3d 0%, #c82519 100%);
	border-radius: 45rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	color: #fff;
	font-size: 34rpx;
	font-weight: 500;
	margin-top: 60rpx;
	margin-bottom: 40rpx;
}

.register-btn:active {
	opacity: 0.9;
}

.login-link {
	text-align: center;
	font-size: 28rpx;
	color: #666;
}

.link {
	color: #e93b3d;
	margin-left: 10rpx;
	font-weight: 500;
}
</style>