# Sophisticated AE Addons

一个面向 Minecraft 1.21.1 NeoForge 的联动模组，为 Applied Energistics 2 与 Sophisticated 系列存储提供额外集成功能。

## 主要功能

### 玩家接口

- 方块放置后绑定玩家 UUID。
- 向 AE2 存储总线暴露玩家主背包、盔甲栏、副手和末影箱。
- 接口本身不主动接入 AE 网络，需要使用 AE2 存储总线连接。

### 精妙背包接口

- 使用精妙背包与接口交互后绑定背包内容 UUID。
- 通过 Sophisticated Backpacks 的持久化数据直接访问对应库存，不依赖背包当前所在位置。
- 定期检测库存变化并刷新对外能力。

### AE 背包与存储升级

- 分别用于 Sophisticated Backpacks 和 Sophisticated Storage。
- 可通过 AE2 无线访问点绑定网络。
- 为精妙存储中的功能类升级提供 AE 网络物品访问能力。

### 精妙工具

- 提供 9 个仅允许放置 Sophisticated 升级物品的内部槽位。
- 可在 AE2 与 Sophisticated 容器界面中访问额外槽位。
- 支持右键打开独立存储界面。

### 精确优先级卡

- 可安装在 AE2 存储总线升级槽中。
- 在 AE2 原生优先级界面中配置多行条件。
- 支持 `AND`、`OR`、`XOR`、`NAND`、`NOR`、`XNOR` 与逐行取反。
- 支持按 AE Key 类型、物品最大堆叠数或数量进行判断。
- 条件命中的存储总线优先于普通存储，同层继续遵循 AE2 静态数字优先级。
- 提取保持 AE2 原生逻辑，条件仅作用于插入。
- 提供 `OFF`、`ON`、`FORCE` 三种迁移模式。

## 环境

- Minecraft `1.21.1`
- NeoForge `21.1.192`
- Java `21`
- Applied Energistics 2 `19.2.17`
- Sophisticated Core `1.21.1-1.4.80.2194`
- Sophisticated Backpacks `1.21.1-3.25.73.2020`
- Sophisticated Storage `1.21.1-1.5.83.2017`

Sophisticated Core 与 AE2 是运行必需依赖。Sophisticated Backpacks 和 Sophisticated Storage 按实际使用的联动功能安装。

## 构建

Windows：

```powershell
.\gradlew.bat build
```

Linux 或 macOS：

```bash
./gradlew build
```

构建产物位于 `build/libs`。

## 开发运行

启动开发客户端：

```powershell
.\gradlew.bat runClient
```

启动开发服务端：

```powershell
.\gradlew.bat runServer
```

## 项目信息

- Mod ID：`sophisticated_ae_addons`
- Java 包名：`cn.yhzcake.sophisticatedaeaddons`
- 仓库：<https://github.com/yhzcake/Sophisticated-AE-Addons>
- 许可证：MIT

## 状态

项目仍处于开发阶段，功能行为与兼容范围可能继续调整。提交问题时请附带 Minecraft、NeoForge、AE2、Sophisticated 系列模组版本以及相关日志。
