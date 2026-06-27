# Argon2 配置 (ewa_conf.xml)

在 `ewa_conf.xml` 中添加 `<argon2>` 节点可覆盖默认参数。

## 节点位置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ewa_confs>
  <!-- 其他配置 ... -->

  <argon2 memory="1024" iterations="3" />

  <!-- 其他配置 ... -->
</ewa_confs>
```

## 参数

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `memory` | int | 1024 | 内存占用 (KB)，OwASP 建议 ≥ 46MB (47104)。1G 服务器建议 1024-32768 |
| `iterations` | int | 3 | 迭代次数，与 memory 成反比 |

## 推荐配置

### 1G 内存服务器

```xml
<argon2 memory="1024" iterations="3" />
```
单次耗时 ~5ms，10 并发内存 10MB。

### 生产环境 (≥4G 内存)

```xml
<argon2 memory="65536" iterations="2" />
```
单次耗时 ~300ms，暴力破解成本提升 2000 倍。

## 生效时机

- `UPath.initPath()` 首次加载或 `ewa_conf.xml` 变更后触发
- 已持久化的密码哈希不受影响（参数存在哈希字符串中）
- 仅影响新调用 `UArgon2.hashPwd()` 生成的哈希

## 验证

启动时日志输出：
```
Argon2 config: memory=1024KB, iterations=3
```
