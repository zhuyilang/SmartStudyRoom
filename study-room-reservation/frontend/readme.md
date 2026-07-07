# 校园自习室预约系统 · 前端

> Vue 3 + Vite + Element Plus + Pinia + ECharts + Axios

## ✅ 已实现

### 基础框架
- 路由 + 守卫（角色权限）
- Pinia 状态管理（用户信息 / sessionStorage 持久化）
- Axios 封装（统一错误处理、Result 解包、**后端不可达提示**）
- Element Plus 全局图标注册
- Vite 代理 `/api` → `http://localhost:8081`

### 登录
- 走 `POST /api/auth/login`，由后端校验账号密码
- 角色从后端返回的 `user.role` 中读取
- 登录后按角色跳转 `/admin/dashboard` 或 `/student/rooms`

### 管理端（已完整实现）
| 页面 | 文件 | 功能 |
| --- | --- | --- |
| 数据看板 | [Dashboard.vue](src/views/admin/Dashboard.vue) | 4 张统计卡 + 折线 / 饼图 / 热力图 / 横向条形图，5s 自动轮询 |
| 校区管理 | [CampusManage.vue](src/views/admin/CampusManage.vue) | 列表 + 模糊搜索 + 新增 / 编辑 / 删除 |
| 楼栋管理 | [BuildingManage.vue](src/views/admin/BuildingManage.vue) | 校区联动筛选 + CRUD |
| 楼层管理 | [FloorManage.vue](src/views/admin/FloorManage.vue) | 校区 → 楼栋二级联动 + CRUD |
| 自习室管理 | [RoomManage.vue](src/views/admin/RoomManage.vue) | 校区 → 楼栋 → 楼层三级联动 + CRUD + 跳转座位 |
| 座位管理 | [SeatManage.vue](src/views/admin/SeatManage.vue) | 座位图可视化（按行 × 列渲染）+ 状态切换 + 批量添加 |
| 数据报表 | [Report.vue](src/views/admin/Report.vue) | 预约趋势 / 高峰时段 / 校区分布 / 房间 TOP |

### 学生端（占位）
- RoomList / SeatSelect / MyReservation 仍为占位

## 📦 运行

### 1. 安装 Node.js（≥ 18）
[Node.js 官网](https://nodejs.org) 下载安装包。

### 2. 安装依赖
```bash
npm install
```

### 3. 启动 dev server
```bash
npm run dev
```
浏览器打开 `http://localhost:5173`

### 4. 确认后端
- 后端默认地址：`http://localhost:8081`
- Vite 代理配置见 [vite.config.js](vite.config.js) 的 `server.proxy`
- 后端未启动时，登录/页面会弹窗提示「后端未启动或无法连接」

## 🗂 目录结构

```
src/
├── api/             # 接口定义（全部走 axios）
│   ├── auth.js           登录/登出/profile
│   ├── admin.js          校区/楼栋/楼层/自习室/座位 CRUD
│   ├── dashboard.js      看板数据
│   ├── report.js         报表数据
│   ├── student.js        学生端
│   ├── blacklist.js      黑名单
│   └── request.js        axios 实例 + 拦截器
├── constants/       # 枚举常量（座位状态、房间类型、角色）
├── stores/          # Pinia
├── router/          # 路由 + 守卫
├── utils/           # 工具函数
│   └── format.js
└── views/
    ├── admin/           管理端 7 个页面
    └── student/         学生端 3 个页面
```

## 🔌 后端接口约定

所有接口统一返回 `Result<T>`：
```json
{ "code": 200, "msg": "ok", "data": ... }
```

主要端点：
- `POST /api/auth/login` — `{username, password, role}` → `data: User`
- `GET  /api/admin/campus/list` → `data: Campus[]`
- `GET  /api/admin/building/list` → `data: Building[]`
- `GET  /api/admin/floor/list` → `data: Floor[]`
- `GET  /api/admin/room/list` → `data: Room[]`
- `GET  /api/admin/room/{id}/seats` → `data: Seat[]`
- `GET  /api/admin/dashboard/overall` → `data: {today, weekTrend, ...}`
- `GET  /api/admin/dashboard/today` → `data: {hourly[], statusDist}`
- `GET  /api/admin/dashboard/seat-status` → `data: {roomId, roomName, total, occupied, rate}[]`
- `GET  /api/admin/dashboard/heatmap/{roomId}` → `data: {rows, cols, data[days][row][col], days}`

完整的列表见 [src/api/admin.js](src/api/admin.js) / [dashboard.js](src/api/dashboard.js) / [report.js](src/api/report.js)。

## 🛠 技术栈版本

| 库 | 版本 |
| --- | --- |
| vue | ^3.3.13 |
| vue-router | ^4.2.5 |
| pinia | ^2.1.7 |
| element-plus | ^2.4.3 |
| @element-plus/icons-vue | ^2.3.1 |
| echarts | ^5.4.3 |
| axios | ^1.6.2 |
| vite | ^5.0.8 |
