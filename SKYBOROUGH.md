# Skyborough：空中主城与玩家家园

这一版在 uSkyBlock 上增加独立的主城登记、浮空主城地形生成和外围家园分配入口。
保留原项目许可证、数据目录与插件标识 `uSkyBlock`，避免破坏既有 API 和数据。

## 开启

在专用虚空家园世界使用，先在 `plugins/uSkyBlock/config.yml` 配置：

```yaml
options:
  general:
    worldName: skyworld
  island:
    height: 150
    distance: 128
    protectionRange: 96
skyborough:
  enabled: true
  city-gap: 32
```

这是需要合并到现有配置中的片段，请勿覆盖整个配置文件。修改后重启。
模式默认关闭，避免给现有 uSkyBlock 服务器自动改变玩法。
启用后：保留背包、盔甲、末影箱、经济余额和挑战授予的权限；禁止旧空岛重置；
关闭原点出生平台管理和地狱配套岛；回主城使用家园世界的出生点。

## 管理员设置主城

管理员需要 `skyborough.admin`（默认 OP）。

- **已有主城**：在主城两个对角位置分别 `/borough pos1`、`/borough pos2`，
  然后站在选区内安全地面上 `/borough register`。只登记水平范围及保护区，不改动建筑。
  当前站立位置成为出生点。保护覆盖整个世界高度。
- **新主城**：在家园世界空旷位置飞行到主城表面应在的高度，使用 `/borough generate 48`。
  半径允许 16–96，默认 48。生成草地、泥土、石头构成的浮空岛地形，供管理员继续装修。
  先检查整个包围盒内无已有方块，再按每 tick 2048 次迭代分批生成。
- **中断恢复**：生成期间关闭服务器会留下未就绪主城，玩家不能申请家园。
  管理员检查并修复地形后，在安全地面执行 `/borough finish`，确认开放并设置出生点。

选区必须在 `options.general.worldName` 世界内，不允许覆盖已有保护区域或世界边界。
已登记主城不能直接移动、覆盖；启用后不要修改世界名、岛间距、保护范围或主城坐标。
WorldGuard 中主城区域名为 `skyborough_city`，禁止普通玩家放置、破坏方块、PVP、
TNT/苦力怕爆炸、火焰蔓延及生物生成。管理员装修需具备 WorldGuard 对应的绕过权限（OP 默认具备）。
生成失败时保留已生成部分与保护区，日志会记录原因，不自动删除已生成内容。

## 玩家操作

- `/borough claim`：申请一座家园，复用 `/is create` 的模板、冷却和归属流程。
- `/borough home`：回家。
- `/borough spawn`：返回主城。
- `/is invite 玩家`：邀请共同装修；沿用原有成员与访客权限。

`/sb` 是 `/borough` 的别名。申请仍遵循原版一人一岛/同一成员组规则。
家园模板沿用 `island-schemes`，可替换为自己的家园蓝图。
主城尚未就绪时，旧 `/is create` 和新申请入口都会拒绝创建。
分配围绕主城中心所在网格，由近到远填充空位，预留完整家园保护范围与 `city-gap`，
避开已有区域、已占岛和世界边界；同一时间的申请使用原有预留锁避免重叠。
最多搜索 10000 个网格，找不到空位会失败并恢复玩家生成标记。

## 目标与验证

目标运行环境：Paper 26.2 / Java 25 / WorldEdit / WorldGuard。
现有编译依赖仍为 Spigot 1.21.10 API，不能仅凭编译成功认定 26.2 运行兼容。
Paper 26.2 的 Adventure 5 与依赖插件版本需要真实服务端验证。

相关官方文档：
- https://docs.papermc.io/paper/getting-started/
- https://papermc.io/news/26-2/

构建：`./gradlew :uSkyBlock-Core:test :uSkyBlock-Plugin:shadowJar`
产物：`uSkyBlock-Plugin/build/libs/uSkyBlock-3.7.0-SNAPSHOT.jar`。
本地另提供 `dist/Skyborough-dev.jar`，内部插件名仍是 uSkyBlock。

当前实现保留原版挑战、排行榜等旧入口；玩家可统一使用 `/borough` 家园入口。
尚未加入房型菜单、可视化预览、主城迁移、自动生成完整主城建筑。


### 本次验证结果

- 核心测试：214 项，211 通过、3 跳过，0 失败、0 错误。
- `shadowJar` 打包成功，`git diff --check` 通过。
- Java 25 已放在服务器 `~/projects/skyborough-tools/`，系统默认 Java 不变。
- `runServer` 默认目标已改为 Paper 26.2 / Java 25；已下载 Paper 26.2 build 134、WorldEdit 7.4.2、WorldGuard 7.0.15。
- 隔离目录 `run/`，仅监听 `127.0.0.1:25575`。已接受 EULA 并继续启动检查。
- 用户已确认接受 Minecraft EULA；首次启动已完成并创建 skyworld。游戏内验收仍未完成。
- 尚需验证：首次主城登记、主城生成、两名玩家连续认领、访客破坏拦截、背包保留、重启后归属持久化。


## 自定义蓝图与错落高度：已确认需求及当前边界

现有 `island-schemes` 支持多个 WorldEdit 蓝图（包括 `.schem`），可以准备不同风格、尺寸的家园结构。
蓝图文件放入 `plugins/uSkyBlock/schematics/`，在 `island-schemes` 下配置对应名称、文件、权限和展示信息。
当前 `/borough claim` 使用默认模板；指定模板可使用 `/is create 模板名`，仍经过主城模式的分配约束。

当前保护范围和网格间距仍为全局值。不同尺寸蓝图必须都能放进同一保护范围；不能把大蓝图直接当作大地块。
当前生成器强制使用全局高度，岛屿数据读取也依赖全局高度，尚未实现每座岛独立高差。

后续实现方向：
- 每个模板记录真实包围盒、蓝图原点偏移、建议装修范围和安全落脚点。
- 高度采用“主城参考高度 + 可配置高度档位”，例如 -24、-12、0、12、24 格。
- 每座岛保存选中模板、实际坐标和高度，避免重启后传送或保护定位错位。
- 检查蓝图完整尺寸、世界高度上下限及与主城/其他家园的间距。
- 当前保护区贯穿世界高度，所以错落布局仍需保持水平分离；同一 X/Z 上下叠放需改造为三维地块模型。

以上是设计方向，并非已存在的配置字段。

最终启动检查：Paper 26.2 build 134 / Java 25 下，WorldEdit、WorldGuard 与 uSkyBlock 均成功启用；修正启动配置后的第二次启动日志无 ERROR/Exception，23.986 秒完成启动。隔离测试服随后正常关闭。尚未连接玩家客户端做游戏内验收。
