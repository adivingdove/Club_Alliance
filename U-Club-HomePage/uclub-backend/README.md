# 社团管理系统后端

这是一个基于Spring Boot的社团管理系统后端，提供完整的社团管理功能，包括社团信息管理、成员管理、文件上传下载、活动管理等。

## 功能特性

### 🏢 社团管理
- 社团信息的增删改查
- 社团分类管理
- 社团状态管理（活跃、非活跃、暂停）
- 社团搜索功能

### 👥 成员管理
- 成员信息的增删改查
- 成员角色管理（社长、副社长、秘书、成员）
- 成员状态管理
- 成员搜索功能
- 自动更新社团成员数量

### 📁 文件管理
- 文件上传下载
- 文件分类管理
- 文件搜索功能
- 支持多种文件格式
- 文件大小限制

### 🎯 活动管理
- 活动信息的增删改查
- 活动状态管理（计划中、进行中、已完成、已取消）
- 活动类型管理
- 活动时间管理
- 参与人数管理

## 技术栈

- **框架**: Spring Boot 3.5.3
- **数据库**: MySQL 8.0
- **ORM**: Spring Data JPA
- **构建工具**: Maven
- **Java版本**: 17

## 项目结构

```
src/main/java/com/example/summer/
├── controller/          # 控制器层
│   ├── ClubController.java
│   ├── MemberController.java
│   ├── FileController.java
│   └── ActivityController.java
├── service/            # 服务层
│   ├── ClubService.java
│   ├── MemberService.java
│   ├── FileService.java
│   └── ActivityService.java
├── repository/         # 数据访问层
│   ├── ClubRepository.java
│   ├── MemberRepository.java
│   ├── ClubFileRepository.java
│   └── ActivityRepository.java
├── entity/            # 实体类
│   ├── Club.java
│   ├── Member.java
│   ├── ClubFile.java
│   └── Activity.java
├── vo/               # 视图对象
│   ├── Result.java
│   └── ClubVO.java
├── util/             # 工具类
│   ├── FileUtil.java
│   └── GlobalExceptionHandler.java
└── SummerApplication.java
```

## 快速开始

### 1. 环境要求
- JDK 17+
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库配置
1. 创建MySQL数据库
2. 执行 `src/main/resources/sql/init.sql` 初始化数据库
3. 修改 `application.properties` 中的数据库连接信息

### 3. 运行项目
```bash
# 克隆项目
git clone <repository-url>
cd club-backend

# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run
```

### 4. 访问API
项目启动后，API文档可通过以下地址访问：
- 基础URL: `http://localhost:8080`
- API前缀: `/api`

## API接口文档

### 社团管理接口

#### 获取所有社团
```
GET /api/clubs
```

#### 获取活跃社团
```
GET /api/clubs/active
```

#### 获取可加入社团
```
GET /api/clubs/available
```

#### 根据分类获取社团
```
GET /api/clubs/category/{category}
```

#### 搜索社团
```
GET /api/clubs/search?keyword={keyword}
```

#### 获取社团详情
```
GET /api/clubs/{id}
```

#### 创建社团
```
POST /api/clubs
Content-Type: application/json

{
  "name": "社团名称",
  "description": "社团描述",
  "category": "技术类",
  "maxMembers": 50,
  "presidentName": "社长姓名",
  "presidentPhone": "13800138001",
  "presidentEmail": "president@example.com",
  "advisorName": "指导老师",
  "meetingTime": "每周三晚上7点",
  "meetingLocation": "教学楼A101"
}
```

#### 更新社团
```
PUT /api/clubs/{id}
Content-Type: application/json

{
  // 同创建社团的请求体
}
```

#### 删除社团
```
DELETE /api/clubs/{id}
```

### 成员管理接口

#### 获取社团成员
```
GET /api/members/club/{clubId}
```

#### 获取活跃成员
```
GET /api/members/club/{clubId}/active
```

#### 添加成员
```
POST /api/members
Content-Type: application/json

{
  "clubId": 1,
  "studentId": "2021001",
  "studentName": "张三",
  "phone": "13800138001",
  "email": "zhangsan@example.com",
  "major": "计算机科学与技术",
  "grade": "大三",
  "role": "MEMBER"
}
```

#### 更新成员
```
PUT /api/members/{id}
Content-Type: application/json

{
  // 同添加成员的请求体
}
```

#### 删除成员
```
DELETE /api/members/{id}
```

### 文件管理接口

#### 上传文件
```
POST /api/files/upload
Content-Type: multipart/form-data

file: 文件
clubId: 社团ID
description: 文件描述（可选）
category: 文件分类（可选）
uploaderId: 上传者ID（可选）
uploaderName: 上传者姓名（可选）
```

#### 获取社团文件
```
GET /api/files/club/{clubId}
```

#### 下载文件
```
GET /api/files/download/{id}
```

#### 删除文件
```
DELETE /api/files/{id}
```

### 活动管理接口

#### 获取社团活动
```
GET /api/activities/club/{clubId}
```

#### 创建活动
```
POST /api/activities
Content-Type: application/json

{
  "clubId": 1,
  "title": "活动标题",
  "description": "活动描述",
  "activityType": "比赛",
  "startTime": "2024-01-15T09:00:00",
  "endTime": "2024-01-15T17:00:00",
  "location": "活动地点",
  "maxParticipants": 30,
  "organizerName": "组织者姓名",
  "organizerPhone": "13800138001",
  "budget": 1000.00
}
```

#### 更新活动
```
PUT /api/activities/{id}
Content-Type: application/json

{
  // 同创建活动的请求体
}
```

#### 删除活动
```
DELETE /api/activities/{id}
```

## 配置说明

### 数据库配置
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/club_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=123456
```

### 文件上传配置
```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
file.upload.path=uploads/
file.max.size=10485760
```

## 注意事项

1. **文件上传**: 文件会保存在 `uploads/` 目录下，按社团ID和日期分类存储
2. **数据库**: 确保MySQL服务正在运行，并且数据库连接信息正确
3. **跨域**: 已配置跨域支持，前端可以直接调用API
4. **文件大小**: 默认最大文件大小为10MB，可在配置文件中修改

## 开发说明

### 添加新功能
1. 在 `entity` 包中创建实体类
2. 在 `repository` 包中创建Repository接口
3. 在 `service` 包中创建Service类
4. 在 `controller` 包中创建Controller类
5. 更新数据库表结构

### 代码规范
- 使用Lombok简化代码
- 统一使用Result类作为API响应格式
- 异常处理使用GlobalExceptionHandler
- 文件操作使用FileUtil工具类

## 许可证

MIT License 