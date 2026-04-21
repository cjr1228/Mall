<template>
	<view class="index-container">
		<!-- 顶部搜索栏 -->
		<view class="search-header">
		  <!-- 第1行：Logo（单独一行，可向上移动） -->
		  <view class="logo-container">
		    <!-- 使用 <image> 标签，并绑定 src 属性到你的图片路径 -->
		    <image class="logo-img" :src="'/static/logo2.png'" mode="widthFix"></image>
		  </view>
		  <!-- 第2行：购物车 + 搜索框（横向排列，购物车在左） -->
		  <view class="search-cart-container">
		    <view class="cart-icon" @click="goCart">
		      <!-- 替换为static下的购物车图标（需提前放入对应路径） -->
		      <image class="icon" :src="'/static/tabbar/cart.png'" mode="widthFix"></image>
		      <text v-if="cartCount > 0" class="badge">{{cartCount}}</text>
		    </view>
		    <view class="search-box" @click="goSearch">
		      <!-- 替换为static下的放大镜图标 -->
		      <image class="icon" :src="'/static/icon/search-icon.png'" mode="widthFix"></image>
		      <text class="placeholder">搜索商品</text>
		    </view>
		  </view>
		</view>
		
		<!-- 分类标签 -->
		<scroll-view class="category-tabs" scroll-x="true" scroll-with-animation>
			<view 
				v-for="(tab, index) in tabs" 
				:key="index" 
				:class="['tab-item', activeTab === index ? 'active' : '']"
				@click="switchTab(index)"
			>
				{{tab}}
			</view>
		</scroll-view>
		
		<scroll-view 
			class="scroll-content" 
			scroll-y="true" 
			refresher-enabled
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
			@scrolltolower="loadMore"
		>
			<!-- 轮播图 -->
			<swiper class="banner" indicator-dots autoplay circular interval="3000" duration="500">
				<swiper-item v-for="(item, index) in banners" :key="index">
					<image :src="item.image" mode="aspectFill" class="banner-img" />
				</swiper-item>
			</swiper>
			
			<!-- 分类入口 -->
			<view class="category-grid">
				<view 
					v-for="(item, index) in categories" 
					:key="index" 
					class="category-item"
					@click="handleCategory(item)"
				>
					<view class="icon-box">
						 <image :src="item.icon" mode="widthFix" class="local-icon"></image>
					</view>
					<text class="title">{{item.title}}</text>
				</view>
			</view>
			
			<!-- 热门推荐 -->
			<view class="section">
				<view class="section-header">
					<text class="section-title">热门推荐</text>
					<text class="more" @click="goGoodsList">更多 ></text>
				</view>
				
				<view class="goods-grid">
					<view 
						v-for="item in goodsList" 
						:key="item.id" 
						class="goods-item"
						@click="goGoodsDetail(item)"
					>
						<image :src="item.img || '/static/images/default-goods.png'" class="goods-img" />
						<view class="goods-info">
							<text class="goods-name">{{item.name}}</text>
							<text class="goods-desc" v-if="item.desc">{{item.desc}}</text>
							<view class="goods-price">
								<text class="current-price">
									¥{{(item.price * (item.discount || 1)).toFixed(2)}}
								</text>
								<text v-if="item.discount && item.discount < 1" class="original-price">
									¥{{item.price.toFixed(2)}}
								</text>
							</view>
							<view class="goods-tags">
								<text v-if="item.discount && item.discount < 1" class="tag">折扣</text>
								<text v-if="item.qty > 0" class="tag stock">有货</text>
								<text v-else class="tag out-stock">缺货</text>
							</view>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 加载更多 -->
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
import { ref, onMounted } from 'vue'
import { onShow, onHide } from '@dcloudio/uni-app'

// 数据定义
const tabs = ref(['首页', '新品', '智能', '穿搭', '笔记本', '家电', '美妆', '运动'])
const activeTab = ref(0)
const banners = ref([
    { image: '/static/swiper/01.jpg' },
    { image: '/static/swiper/02.jpg' },
    { image: '/static/swiper/03.jpg' },
	{ image: '/static/swiper/04.jpg' },
	{ image: '/static/swiper/05.jpg' }
	
])
const categories = ref([
    { icon: '/static/icon/清.png', title: '手机数码' },    
     { icon: '/static/icon/仓.png', title: '电脑办公' },  
     { icon: '/static/icon/甩.png', title: '食品新鲜' },      
     { icon: '/static/icon/卖.png', title: '以旧换新' },      
     { icon: '/static/icon/惠.png', title: '美妆护肤' }, 
     { icon: '/static/icon/及.png', title: '运动户外' },   
     { icon: '/static/icon/全.png', title: '服装服饰' },   
     { icon: '/static/icon/城.png', title: '更多' } 
])
const goodsList = ref([])
const loading = ref(false)
const noMore = ref(false)
const refreshing = ref(false)
const currentPage = ref(1)
const cartCount = ref(0)
const cartLoading = ref(false)

// 定时器
let cartRefreshTimer = null

// 生命周期
onMounted(() => {
    loadGoods()
})

onShow(() => {
    // 每次显示时刷新购物车数量
    loadCartCount()
    // 每30秒刷新一次购物车数量（可选）
    cartRefreshTimer = setInterval(() => {
        loadCartCount()
    }, 30000)
})

onHide(() => {
    // 页面隐藏时清除定时器
    if (cartRefreshTimer) {
        clearInterval(cartRefreshTimer)
        cartRefreshTimer = null
    }
})

// 方法定义
const switchTab = (index) => {
    activeTab.value = index
    // 清空数据重新加载
    goodsList.value = []
    currentPage.value = 1
    noMore.value = false
    loadGoods()
}

const goSearch = () => {
    uni.showToast({
        title: '搜索功能开发中',
        icon: 'none'
    })
}

const goCart = () => {
    // 优化：先检查登录状态
    const token = uni.getStorageSync('auth_token')
    if (!token) {
        uni.showModal({
            title: '提示',
            content: '请先登录查看购物车',
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
        url: '/pages/cart/cart'
    })
}

const goGoodsList = () => {
    uni.showToast({
        title: '商品列表开发中',
        icon: 'none'
    })
}

const goGoodsDetail = (goods) => {
    uni.navigateTo({
        url: `/pages/goods-detail/goods-detail?goodsId=${goods.id}&goodsNo=${goods.no}`
    })
}

const handleCategory = (item) => {
    uni.showToast({
        title: `进入${item.title}`,
        icon: 'none'
    })
}

const onRefresh = () => {
    refreshing.value = true
    goodsList.value = []
    currentPage.value = 1
    noMore.value = false
    setTimeout(() => {
        loadGoods()
        refreshing.value = false
    }, 1000)
}

const loadMore = () => {
    if (loading.value || noMore.value) return
    currentPage.value++
    loadGoods()
}

const loadCartCount = () => {
    cartLoading.value = true
    const token = uni.getStorageSync('auth_token')
    
    if (!token) {
        cartCount.value = 0
        cartLoading.value = false
        return
    }
    
    uni.request({
        url: '/api/cart/count',
        method: 'GET',
        header: {
            'Authorization': `Bearer ${token}`
        },
        success: (res) => {
            if (res.data.code === 1) {
                cartCount.value = res.data.count || 0
            } else if (res.data.code === -999) { // Token失效错误码
                // Token失效，清理本地存储
                uni.removeStorageSync('auth_token')
                uni.removeStorageSync('user_info')
                cartCount.value = 0
                console.log('登录已过期，请重新登录')
            } else {
                cartCount.value = 0
            }
        },
        fail: () => {
            cartCount.value = 0
        },
        complete: () => {
            cartLoading.value = false
        }
    })
}

const loadGoods = async () => {
    loading.value = true
    
    try {
        const response = await uni.request({
            url: '/api/goods/recommend',
            method: 'GET',
            data: { limit: 28 },
            header: { 'Content-Type': 'application/json' }
        })
        
        if (response.statusCode !== 200) {
            throw new Error(`请求失败，状态码：${response.statusCode}`)
        }
        
        const data = response.data || {}
        
        if (data.code === 1) {
            const newGoods = data.list || []
            
            if (newGoods.length === 0) {
                noMore.value = true
            } else {
                if (currentPage.value === 1) {
                    goodsList.value = newGoods
                } else {
                    goodsList.value = [...goodsList.value, ...newGoods]
                }
            }
        } else {
            uni.showToast({
                title: data.message || '获取商品失败',
                icon: 'none',
                duration: 2000
            })
        }
    } catch (error) {
        console.error('加载商品失败:', error)
        uni.showToast({
            title: '网络异常，加载本地数据',
            icon: 'none',
            duration: 2000
        })
    } finally {
        loading.value = false
    }
}
</script>
<style scoped>
.index-container {
	height: 100vh;
	display: flex;
	flex-direction: column;
	background-color: #f5f5f5;
}

/* 搜索栏样式 */
/* 搜索栏整体容器：纵向排列，控制背景图高度和内边距 */
.search-header {
  display: flex;
  flex-direction: column; /* 改为纵向排列（logo在上，搜索+购物车在下） */
  padding: 0rpx 30rpx; /* 整体上下内边距，可调整控制整体位置 */
  padding-top: calc(10rpx + var(--status-bar-height)); /* 适配状态栏，避免遮挡 */
  height: calc(275rpx + var(--status-bar-height)); /* 增大背景图高度（根据需求调整） */
  /* 背景图片配置（保持不变） */
  background: url('/static/header-bg.jpg') no-repeat;
  background-size: cover;
  background-position: center;
  color: #fff;
  box-sizing: border-box;
}

/* 第1行：Logo容器（控制logo上移，通过margin-bottom减少与下方距离） */
.logo-container {
	 margin-top: -70rpx;
  margin-bottom: 0rpx; /* 减少logo与下方搜索栏的间距，实现“上移”效果（值越小越靠上） */
}

.logo-img {
  /* 设置一个合适的宽度，它会自动根据宽度调整高度 */
  width: 300rpx; 
  
  display: block;
}

/* 第2行：购物车 + 搜索框 容器（横向排列，控制整体下移） */
.search-cart-container {
  display: flex;
  align-items: center;
  margin-top:-50rpx; /* 增加与上方logo的间距，实现“下移”效果（值越大越靠下） */
}

/* 购物车：在搜索框左侧，控制大小和间距 */
.cart-icon {
  position: relative;
  margin-right: 20rpx; /* 购物车与搜索框的间距 */
  width: 60rpx; /* 购物车图标容器宽度 */
  height: 60rpx; /* 购物车图标容器高度 */
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 自定义图标样式（适配图片大小，避免变形） */
.cart-icon .icon, 
.search-box .icon {
  width: 40rpx; /* 图标宽度（根据你的图标尺寸调整） */
  height: 40rpx; /* 图标高度（与宽度一致，保持正方形） */
  object-fit: contain; /* 保持图标比例，不拉伸 */
}

/* 购物车数字徽章（位置微调，避免遮挡图标） */
.cart-icon .badge {
  position: absolute;
  top: 0;
  right: 0; /* 右对齐，贴合图标右上角 */
  background: #ff4757;
  color: #fff;
  font-size: 20rpx;
  width: 30rpx;
  height: 30rpx;
  border-radius: 15rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 搜索框：在购物车右侧，占满剩余宽度 */
.search-box {
  flex: 1; /* 占满剩余宽度，适配不同屏幕 */
  height: 70rpx;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 35rpx;
  display: flex;
  align-items: center;
  padding: 0 30rpx;
}

/* 搜索框提示文字（保持不变，确保可读性） */
.search-box .placeholder {
  color: #999;
  font-size: 28rpx;
  margin-left: 15rpx; /* 与放大镜图标的间距 */
}

/* 分类标签样式 */
.category-tabs {
	white-space: nowrap;
	background: #fff;
	padding: 20rpx 30rpx;
	border-bottom: 2rpx solid #f0f0f0;
}

.tab-item {
	display: inline-block;
	padding: 15rpx 30rpx;
	margin-right: 40rpx;
	font-size: 30rpx;
	color: #666;
	position: relative;
}

.tab-item.active {
	color: #e93b3d;
	font-weight: bold;
}

.tab-item.active::after {
	content: '';
	position: absolute;
	bottom: 0;
	left: 50%;
	transform: translateX(-50%);
	width: 60rpx;
	height: 6rpx;
	background: #e93b3d;
	border-radius: 3rpx;
}

/* 滚动内容区域 */
.scroll-content {
	flex: 1;
	height: 0;
}

/* 轮播图样式 */
.banner {
	width: 100%;
	height: 360rpx;
	background: #fff;
}

.banner-img {
	width: 100%;
	height: 100%;
}

/* 分类入口样式 */
.category-grid {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	background: #fff;
	padding: 30rpx 0;
	margin-bottom: 20rpx;
}

.category-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 20rpx 0;
}

.icon-box {
	width: 100rpx;
	height: 100rpx;
	border-radius: 50%;
	background: linear-gradient(135deg, #ffecf1 0%, #ffe7eb 100%);
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 20rpx;
}

.icon-box .icon {
	font-size: 48rpx;
}

.title {
	font-size: 26rpx;
	color: #333;
}

.local-icon {
  width: 50rpx;  /* 根据你的图标大小调整 */
  height: 50rpx; /* 根据你的图标大小调整 */
  object-fit: contain; /* 保持图标比例 */
}

/* 商品区样式 */
.section {
	background: #fff;
	padding: 30rpx;
	margin-bottom: 20rpx;
	border-radius: 20rpx;
}

.section-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30rpx;
}

.section-title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
	position: relative;
	padding-left: 20rpx;
}

.section-title::before {
	content: '';
	position: absolute;
	left: 0;
	top: 50%;
	transform: translateY(-50%);
	width: 8rpx;
	height: 36rpx;
	background: #e93b3d;
	border-radius: 4rpx;
}

.more {
	font-size: 28rpx;
	color: #999;
}

.goods-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 20rpx;
}

.goods-item {
	background: #fff;
	border-radius: 20rpx;
	overflow: hidden;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.goods-img {
	width: 100%;
	height: 320rpx;
	background: #f8f8f8;
}

.goods-info {
	padding: 20rpx;
}

.goods-name {
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	overflow: hidden;
	font-size: 28rpx;
	color: #333;
	line-height: 1.4;
	height: 78rpx;
	margin-bottom: 10rpx;
}

.goods-desc {
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 1;
	overflow: hidden;
	font-size: 24rpx;
	color: #999;
	margin-bottom: 20rpx;
	line-height: 1.4;
}

.goods-price {
	display: flex;
	align-items: baseline;
	margin-bottom: 15rpx;
}

.current-price {
	font-size: 34rpx;
	font-weight: bold;
	color: #e93b3d;
	margin-right: 15rpx;
}

.original-price {
	font-size: 24rpx;
	color: #999;
	text-decoration: line-through;
}

.goods-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
}

.tag {
	padding: 6rpx 12rpx;
	border-radius: 6rpx;
	font-size: 22rpx;
	background: #ffe7eb;
	color: #e93b3d;
}

.tag.stock {
	background: #e8f7f0;
	color: #00b894;
}

.tag.out-stock {
	background: #f5f5f5;
	color: #999;
}

/* 加载状态样式 */
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