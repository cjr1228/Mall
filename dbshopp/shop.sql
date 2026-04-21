SELECT * FROM `member`
CREATE TABLE goods (
    id INT PRIMARY KEY AUTO_INCREMENT,
    no VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    `desc` TEXT,
    img VARCHAR(200),
    price DECIMAL(10,2) NOT NULL,
    qty INT DEFAULT 0,
    discount DECIMAL(3,2) DEFAULT 1.00
);
CREATE TABLE cart (
    id INT PRIMARY KEY AUTO_INCREMENT,
    memberid INT NOT NULL,
    goodsNo VARCHAR(20) NOT NULL,
    quantity INT DEFAULT 1,
    addTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_member_goods (memberid, goodsNo)
);
CREATE TABLE orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    orderNo VARCHAR(20) NOT NULL UNIQUE,
    memberid INT NOT NULL,
    shipAddressId INT,
    shipType VARCHAR(10) DEFAULT '快递',
    payType VARCHAR(10) DEFAULT '在线支付',
    orderTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    amount DECIMAL(10,2) NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0-未支付,1-已支付,2-已取消,3-已完成'
);

-- 5. 订单详情表
CREATE TABLE order_detail (
    id INT PRIMARY KEY AUTO_INCREMENT,
    goodsNo VARCHAR(20) NOT NULL,
    goodsName VARCHAR(100) NOT NULL,
    orderNo VARCHAR(20) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    discount DECIMAL(3,2) DEFAULT 1.00,
    memberPrice DECIMAL(10,2) NOT NULL,
    qty INT NOT NULL,
    allPrice DECIMAL(10,2) NOT NULL
);

-- 6. 收货地址表
CREATE TABLE ship_address (
    id INT PRIMARY KEY AUTO_INCREMENT,
    memberId INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    areald INT DEFAULT 1,
    address VARCHAR(200) NOT NULL,
    zipcode VARCHAR(10),
    telno VARCHAR(20) NOT NULL,
    isDefault TINYINT DEFAULT 0
);

-- 7. 配送区域表
CREATE TABLE ship_area (
    id INT PRIMARY KEY AUTO_INCREMENT,
    area VARCHAR(20) NOT NULL,
    cost DECIMAL(5,2) NOT NULL
);

-- 8. 收藏表
CREATE TABLE collect (
    id INT PRIMARY KEY AUTO_INCREMENT,
    memberid INT NOT NULL,
    goodsNo VARCHAR(20) NOT NULL,
    colTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_member_goods (memberid, goodsNo)
);
INSERT INTO ship_area (area, cost) VALUES 
('本地', 5.00),
('外地', 10.00);

INSERT INTO goods (no, name, `desc`, img, price, qty, discount) VALUES 
('P001', 'iPhone 14', '苹果最新款手机', 'https://ibb.co/dwT8xfXn', 6999.00, 100, 0.95)

- 4. 插入测试地址
INSERT INTO ship_address (memberId, name, areald, address, zipcode, telno, isDefault) VALUES 
(1, '张三', 1, '北京市朝阳区建国路88号', '100020', '13800138001', 1)

-- 5. 插入测试购物车数据
INSERT INTO cart (memberid, goodsNo, quantity) VALUES 
(1, 'P001', 1)

6. 插入测试订单
INSERT INTO orders (orderNo, memberid, shipAddressId, amount, status) VALUES 
('202312010001', 1, 1, 25997.00, 1)

-- 7. 插入测试订单详情
INSERT INTO order_detail (goodsNo, goodsName, orderNo, price, discount, memberPrice, qty, allPrice) VALUES 
('P002', 'MacBook Pro', '202312010001', 12999.00, 0.90, 11699.10, 2, 23398.20)

8. 插入测试收藏数据
INSERT INTO collect (memberid, goodsNo) VALUES 
(1, 'P001')




INSERT INTO goods (no, name, `desc`, img, price, qty, discount) VALUES 
('P001', 'iPhone 17 Pro', '苹果最新旗舰手机，A16芯片，4800万像素主摄', 'https://i.ibb.co/zh06KF2Y/i-Phone-17-Pro.png', 7999.00, 50, 0.95),
('P002', 'MacBook Air M2', '轻薄笔记本电脑，M2芯片，13.6英寸视网膜屏', 'https://i.ibb.co/d4kGLnPw/Mac-Book-Air-M2.jpg', 9499.00, 30, 0.92),
('P003','雪中飞（SNOW FLYING）','雪中飞羽绒服女短款马卡龙色连帽面包服2025冬新款廓形显瘦保暖 冰川蓝|5338 L /165/88A','https://i.ibb.co/7xgHKcKy/image.jpg',395.00,25,0.95),
('P004','中号大嘴巴贴片','中号大嘴巴贴片手机壳diy水杯冰箱贴PVC洞洞鞋扣鞋花软胶鞋包配件','https://i.ibb.co/hFS5pxK0/image.jpg',1.51,10,0.99),
('POO5','活力28洗衣液','活力28薰衣洗衣液2kg袋装4斤衣物清洁补充液持久留香','https://i.ibb.co/r230PjnM/28.png',24.85,20,0.92),
('P006', 'iPad Pro 12.9', '专业级平板电脑，M2芯片，Mini-LED显示屏', 'https://i.ibb.co/r2T9qvMf/i-Pad-Pro-12-9.jpg', 8999.00, 25, 0.90),
('P007', 'AirPods Pro 2', '第二代降噪耳机，自适应通透模式，空间音频', 'https://i.ibb.co/ZzkDsSsJ/Air-Pods-Pro-2.jpg', 1899.00, 100, 0.88),
('P008','花知晓贝壳珍珠系列礼盒','花知晓礼盒贝壳珍珠系列唇蜜腮红粉饼眼影妆前乳','https://i.ibb.co/NgqSRL79/image.jpg',177.75,20,0.95),
('P009','卡通小夜灯','网红卡通小夜灯卧室可爱兔子氛围灯装饰摆件精致生日礼物','https://i.ibb.co/Jjw1MDNY/image.jpg',3.27,10,0.99),
('P010', '索尼A7M4', '全画幅微单相机，3300万像素，4K 60p视频', 'https://i.ibb.co/WWrsZHWr/A7M4.jpg', 16999.00, 15, 0.90),
('P011','冻干苹果脆片','冻干苹果脆片烟台无糖精无添加剂厚切原味官方旗舰店果干特产零食','https://i.ibb.co/S7rS0fLN/image.jpg',5.88,10,0.99),
('P012','可爱毛绒背包','水豚背包卡皮巴拉可爱毛绒玩具单肩包丑萌水豚鼠公仔双肩斜跨包L','https://i.ibb.co/0yN1SYxn/image.jpg',12.91,15,0.95),

('P013', 'Apple Watch Ultra', '专业运动手表，双频GPS，49毫米钛金属表壳', 'https://i.ibb.co/0pRXqvKw/Apple-Watch-Ultra.jpg', 6299.00, 40, 0.93),
('P014', '小米13 Pro', '徕卡专业光学镜头，骁龙8 Gen 2，120W快充', 'https://i.ibb.co/fd4wr2pf/13-Pro.png', 4999.00, 80, 0.87),
('P015','卡其色裤子','美式复古卡其色牛仔裤女2025新款秋季垂感宽松阔腿直筒高腰长裤子','https://i.ibb.co/k62RkpKR/image.jpg',67.91,25,0.90),
('P016', '华为MateBook X Pro', '3.1K触控全面屏，第12代酷睿处理器', 'https://i.ibb.co/JF8HFsHH/Mate-Book-X-Pro.jpg', 8999.00, 35, 0.89),

('P017','AKF闪粉','akf散粉afk定装粉扑正品秋冬干皮适合老牌国货持久保湿大容量','https://i.ibb.co/5WJ27VFL/AKF.jpg',6.99,10,0.99),

('P018','联想拯救者Y9000P', '游戏本，i9处理器，RTX 4060显卡', 'https://i.ibb.co/wrjBqJXB/Y9000-P.jpg', 10999.00, 20, 0.85),

('P019', '佳能EOS R6', '全画幅微单，2010万像素，机身防抖', 'https://i.ibb.co/7NnwGYvK/EOS-R6.jpg', 15999.00, 18, 0.91),
('P020','手机支架','2024新款手机支架桌面懒人平板支撑架可旋转折叠手机架ipad支撑架子航空合金金属720旋转便携','https://i.ibb.co/f3Zbkx9/image.jpg',2.1,5,0.95),
('P021','凡士林','凡士林倍护特润修护润肤露清香芦荟秋冬男女士密集滋润保湿身体乳','https://i.ibb.co/vCh4t9ky/image.jpg',49.99,18,0.90),
('P022', '三星S23 Ultra', '2亿像素主摄，SPen，骁龙8 Gen 2', 'https://i.ibb.co/F41N460r/S23-Ultra.jpg', 8999.00, 45, 0.86),
('P023', '戴尔XPS 13', '轻薄本，13.4英寸触控屏，第13代处理器', 'https://i.ibb.co/gMmCNRbb/XPS-13.jpg', 8999.00, 28, 0.92),
('P024','潘婷洗发水','潘婷丝质顺滑洗发水家庭装乳液修护护发素干枯毛躁洗头膏洗发露','https://i.ibb.co/NBqX7zD/image.jpg',79.99,25,0.92),
('P025', 'Bose QuietComfort', '降噪耳机，24小时续航，通透模式', 'https://i.ibb.co/Q383nwFY/Bose-Quiet-Comfort.jpg', 2299.00, 60, 0.85),
('P026','美式复古牛仔背带裤女','美式复古牛仔背带裤女2025新款春秋宽松蝴蝶结烫钻夏季减龄直筒裤','https://i.ibb.co/RpqKZMc5/image.jpg',91.75,30,0.85),
('P027', '索尼WH-1000XM5', '头戴式降噪耳机，30小时续航，AI降噪', 'https://i.ibb.co/5hVZtZ51/WH-1000-XM5.jpg', 2999.00, 55, 0.88),
('P028', '任天堂Switch OLED', '7英寸OLED屏，64GB存储，掌机/电视模式', 'https://i.ibb.co/03hZWrx/Switch-OLED.jpg', 2499.00, 70, 0.82);


TRUNCATE TABLE goods;










