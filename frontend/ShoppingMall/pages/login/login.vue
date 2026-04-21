<template>
	<view class="login-container">
		<view class="login-header">
			<image class="logo" src="/static/logo-red.png" mode="widthFix"></image>
			<text class="slogan">欢迎回来</text>
		</view>

		<view class="login-form">
			<view class="form-item">
				<text class="label">邮箱</text>
				<input v-model="form.email" type="text" placeholder="请输入邮箱" placeholder-class="placeholder"
					class="input" />
			</view>

			<view class="form-item">
				<text class="label">密码</text>
				<input v-model="form.password" type="password" placeholder="请输入密码" placeholder-class="placeholder"
					class="input" />
			</view>

			<view class="login-btn" @click="handleLogin">
				<text>登录</text>
			</view>

			<view class="register-link">
				<text>还没有账号？</text>
				<text class="link" @click="goRegister">立即注册</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue'
	import {
		onLoad
	} from '@dcloudio/uni-app'

	const form = ref({
		email: '',
		password: ''
	})

	const handleLogin = () => {
		if (!form.value.email || !form.value.password) {
			uni.showToast({
				title: '请输入完整信息',
				icon: 'none'
			})
			return
		}

		uni.showLoading({
			title: '登录中...'
		})

		const params = new URLSearchParams();
		params.append('email', form.value.email); // 参数名必须和后端一致
		params.append('password', form.value.password);

		uni.request({
			url: '/api/member/login',
			method: 'POST',
			header: {
				// 关键：请求头设为 urlencoded（与后端接收规则匹配）
				'Content-Type': 'application/x-www-form-urlencoded'
			},
			data: params.toString(), // 传 urlencoded 字符串

			success: (res) => {
				uni.hideLoading()
				if (res.data.code === 1) {
					// 保存Token和用户信息到本地存储
					uni.setStorageSync('auth_token', res.data.token)
					uni.setStorageSync('user_info', res.data.member)
					uni.showToast({
						title: '登录成功',
						icon: 'success'
					})
					// 跳转到首页
					setTimeout(() => {
						uni.switchTab({
							url: '/pages/index/index'
						})
					}, 1500)
				} else {
					uni.showToast({
						title: res.data.message || '登录失败',
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

	const goRegister = () => {
		uni.navigateTo({
			url: '/pages/register/register'
		})
	}
</script>

<style scoped>
	.login-container {
		padding: 80rpx 60rpx;
		min-height: 100vh;
		background: linear-gradient(135deg, #f9f9f9 0%, #ffffff 100%);
	}

	.login-header {
		text-align: center;
		margin-bottom: 100rpx;
	}

	.logo {
			/* 移除原文字样式，添加图片样式 */
			width: 300rpx;  /* 根据你的logo尺寸调整 */
			height: auto;   /* 自动保持宽高比 */
			margin: 0 auto 20rpx;  /* 居中显示 */
			display: block;
		}
	.slogan {
		font-size: 32rpx;
		color: #999;
	}

	.login-form {
		background: #fff;
		border-radius: 30rpx;
		padding: 60rpx 50rpx;
		box-shadow: 0 10rpx 40rpx rgba(0, 0, 0, 0.1);
	}

	.form-item {
		margin-bottom: 50rpx;
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

	.login-btn {
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

	.login-btn:active {
		opacity: 0.9;
	}

	.register-link {
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