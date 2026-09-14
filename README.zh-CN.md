# 微波光子计算器(PhotonCalc)

[English](README.md) | 中文说明

面向微波光子 / 光通信实验室日常的安卓功率计算器。Kotlin + Jetpack Compose 原生开发,Material 3,深浅色自适应,完全离线、无网络权限。

![预览](docs/preview.png)

## 功能(7 个页面)

| 页面 | 功能 |
|------|------|
| 换算 | W / mW / μW / dBm / dBW / V rms / V pp / dBV / dBμV 九单位互转,输入任一实时出全部;50 / 75 / 1 / 自定义阻抗 |
| 链路 | 输入电平(dBm / Vrms / Vpp)+ 多级增益/衰减(dB)级联,输出全套单位与总增益 |
| 探测 | 光功率(dBm / mW)+ 响应度(A/W)→ 光电流、负载电压、耗散功率 |
| 噪声 | 电域热噪底(kTB + NF);光放大器 ASE 谱密度 / 总功率 / OSNR(带宽支持 nm 与 GHz) |
| 阻抗 | 反射系数 Γ、VSWR、回波损耗、失配损耗、反射功率;串联 ↔ 并联等效变换(Q 变换) |
| 调制 | 马赫-曾德尔调制器:半波电压、推挽/单臂、正交偏置、小信号调制深度 m、相位摆幅 |
| 工具 | 光波长 ↔ 频率 ↔ 光子能量;波长间隔 ↔ 频率间隔(1550 nm 处 1 nm ≈ 124.8 GHz);运行诊断 |

另附**免安装网页原型** `prototype.html`:与安卓版同一套计算引擎,浏览器(含手机浏览器)直接打开即用。

## 公式正确性

所有换算关系经双实现交叉审计:Kotlin 引擎 33 项单元测试 + 对网页引擎直接执行的 74 项标准向量断言,全部通过。
公式出处(Pozar《Microwave Engineering》、Agrawal《Fiber-Optic Communication Systems》、SI 常数定义等)与逐条推导见
[tools/公式审计报告.md](tools/公式审计报告.md),审计脚本见 [tools/audit.js](tools/audit.js)。

关键数值抽查:

- 0 dBm = 1 mW = 223.6 mV rms = 632 mV pp = +107 dBμV(50 Ω);10 dBm = 2 V pp(50 Ω)
- kTB @ 290 K = −174 dBm/Hz;1 GHz、NF 3 dB → −80.98 dBm
- EDFA(G 20 dB、NF 5 dB)ASE @ 0.1 nm、1550 nm → −33 dBm,与经验速算 G + NF − 58 一致
- 75 Ω 负载接 50 Ω 系统:Γ = 0.2、VSWR = 1.5、回损 13.98 dB、失配 0.18 dB

## 构建

要求:JDK 17、Android SDK(compileSdk 35)。

- **Android Studio**:Open 打开本仓库根目录,等待 Gradle 同步后直接运行。
- **命令行**:

```bash
./gradlew assembleDebug        # 产物: app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest    # 公式单元测试
```

提示:中国大陆网络可将 `gradle/wrapper/gradle-wrapper.properties` 中的 `distributionUrl`
替换为 `https://mirrors.cloud.tencent.com/gradle/gradle-8.9-bin.zip` 加速下载;
依赖仓库已内置阿里云/腾讯镜像优先(见 `settings.gradle.kts`)。

## 工程化

- **崩溃捕获**:未捕获异常写入应用私有目录(保留最近 5 份),工具页"运行诊断"可查看/清除,无第三方 SDK
- **性能**:debug 构建内置 StrictMode 与掉帧监测;release 零开销
- **发布**:R8 混淆 + 资源压缩(release ≈ 1.1 MB);签名密钥不入库(`keystore.properties` 已被 .gitignore 排除),克隆后如需出签名包请在 `keystore.properties` 配置自己的密钥
- **CI**:GitHub Actions 自动跑单元测试并产出 debug APK(见 `.github/workflows/android.yml`)

## 免责说明

本工具面向工程估算,结果为理想模型下的标称值;用于测量与设计决策时请按器件手册与系统实际口径复核。

## License

[MIT](LICENSE)
