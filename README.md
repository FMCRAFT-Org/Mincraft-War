# War Plugin

一个 Minecraft Spigot 插件，提供玩家挑战与战利品系统。

## 功能特性

- **挑战系统** - 玩家可以发起公开挑战或向指定玩家发起私人挑战
- **战利品系统** - 战斗前双方可放置奖励物品，胜者获得所有战利品
- **自动传送** - 自动传送至战利品放置区和战场
- **完整流程** - 从挑战到战斗的完整流程管理
- **管理员工具** - 提供丰富的管理命令

## 工作流程

1. 玩家使用 `/war challenge` 发起公开挑战或 `/war challenge <玩家>` 发起私人挑战
2. 被挑战者使用 `/war accept` 接受挑战
3. 双方被传送至战利品放置区，在箱子中放置奖励物品
4. 使用 `/war done` 完成放置，使用 `/war confirm` 确认战利品
5. 双方确认后传送至战场开始战斗
6. 战斗至一方死亡或退出，胜者获得所有战利品
7. 使用 `/war back` 返回之前的位置

## 命令

### 玩家命令

| 命令                    | 说明        |
| --------------------- | --------- |
| `/war challenge`      | 发起公开挑战    |
| `/war challenge <玩家>` | 向指定玩家发起挑战 |
| `/war accept`         | 接受挑战      |
| `/war decline`        | 拒绝挑战      |
| `/war done`           | 完成战利品放置   |
| `/war confirm`        | 确认战利品     |
| `/war stop`           | 放弃比赛      |
| `/war back`           | 返回之前位置    |
| `/war help`           | 查看帮助      |

### 管理员命令

| 命令                    | 说明        |
| --------------------- | --------- |
| `/war admin box`      | 设置战利品箱子位置 |
| `/war admin field`    | 设置战场位置    |
| `/war admin stop`     | 强制结束挑战    |
| `/war admin win <玩家>` | 强制指定获胜者   |

## 权限

| 权限              | 说明      | 默认  |
| --------------- | ------- | --- |
| `war.use`       | 使用基础命令  | 所有人 |
| `war.challenge` | 发起挑战    | 所有人 |
| `war.admin`     | 使用管理员命令 | OP  |

## 安装

1. 确保服务器运行 Spigot/Paper 1.21 或更高版本
2. 将插件 JAR 文件放入 `plugins` 文件夹
3. 启动服务器，插件会自动生成配置文件
4. 使用 `/war admin box` 设置战利品箱子位置
5. 使用 `/war admin field` 设置战场位置

## 配置

### config.yml

```yaml
chest:
  world: world
  x: 0
  y: 0
  z: 0
  yaw: 0
  pitch: 0

field:
  world: world
  x: 0
  y: 0
  z: 0
  yaw: 0
  pitch: 0
```

### Language.yml

支持完整的中文消息自定义，包括挑战、接受、拒绝、传送、战利品、战斗等所有消息。

## 构建

```bash
mvn clean package
```

构建产物位于 `target/War-1.0.0.jar`

## 技术要求

- Java 21+
- Spigot/Paper 1.21+
- Maven 3.6+

## 许可证

MIT License
