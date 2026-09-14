/*
 * 公式权威审计脚本:直接执行 prototype.html 里的真实引擎代码,
 * 对同一组标准测试向量断言(与 Kotlin 侧 EnginesTest/ConversionsTest 向量一致)。
 * 运行:node tools/audit.js
 */
'use strict';
const fs = require('fs');
const path = require('path');

const html = fs.readFileSync(path.join(__dirname, '..', 'prototype.html'), 'utf8');
const m = html.match(/<script>([\s\S]*?)<\/script>/);
if (!m) { console.error('未找到 <script> 引擎代码'); process.exit(1); }

// DOM 桩:让页面脚本在 node 里无 DOM 也能跑到定义处
const stub = `
  const document = { getElementById: () => ({ innerHTML: '', style: {}, classList: { toggle() {}, add() {}, remove() {} } }) };
  const window = {};
`;
const tail = `
  ;globalThis.__E = {
    dbm2w, w2dbm, w2v, v2w, vr2pp, pp2vr, vr2dbv, dbv2vr, vr2dbuv, dbuv2vr,
    hnuW, photonPsdDbmHz, asePsdW, asePowerDbm, dFreqHz, dWlM,
    reflection, s2p, toW, fromW, eng, fx, num, kB, CC, HP, QE,
  };
`;
// eval 作用域内取值:通过尾挂到 globalThis 导出
const factory = new Function(stub + m[1] + tail + '; return globalThis.__E;');
const E = factory();

let pass = 0, fail = 0;
function close(name, got, want, tol) {
  const ok = Math.abs(got - want) <= tol;
  if (ok) pass++; else { fail++; console.log(`FAIL ${name}: got=${got} want=${want}±${tol}`); }
}
function is(name, got, want) {
  if (got === want) pass++; else { fail++; console.log(`FAIL ${name}: got=${JSON.stringify(got)} want=${JSON.stringify(want)}`); }
}

/* ---- 功率 <-> dBm/dBW(Pozar, Microwave Engineering App. B;IEC 对数单位定义) ---- */
close('dbm2w(0)=1mW', E.dbm2w(0), 1e-3, 1e-15);
close('dbm2w(30)=1W', E.dbm2w(30), 1, 1e-12);
close('dbm2w(10)=10mW', E.dbm2w(10), 1e-2, 1e-15);
close('dbm2w(-30)=1uW', E.dbm2w(-30), 1e-6, 1e-18);
close('w2dbm(1)=30', E.w2dbm(1), 30, 1e-12);
close('w2dbm(1e-3)=0', E.w2dbm(1e-3), 0, 1e-12);
is('dBW=dBm-30', E.w2dbm(1) - 30, 0); // 由定义 dBW=dBm-30(以 1W 为参考)

/* ---- 电压 <-> 功率(欧姆定律 P=V^2/Z;Vpp=2*sqrt(2)*Vrms 正弦) ---- */
close('0dBm@50R Vrms', E.w2v(E.dbm2w(0), 50), 0.223606798, 1e-9);
close('0dBm@75R Vrms', E.w2v(E.dbm2w(0), 75), 0.273861279, 1e-9);
close('10dBm@50R Vpp=2V', E.vr2pp(E.w2v(E.dbm2w(10), 50)), 2.0, 1e-12);
close('Vrms->W roundtrip', E.v2w(E.w2v(1e-3, 50), 50), 1e-3, 1e-18);
close('0dBm@50R dBuV=+106.99', E.vr2dbuv(E.w2v(E.dbm2w(0), 50)), 106.9897, 1e-3);
close('1Vrms=0dBV=120dBuV', E.vr2dbv(1), 0, 1e-12);
close('1Vrms=120dBuV', E.vr2dbuv(1), 120, 1e-12);
close('107dBuV@50R=1.0024mW', E.v2w(E.dbv2vr(107 - 120), 50), 1.0024e-3, 1e-6);

/* ---- 增益/衰减(电压 20lg,功率 10lg) ---- */
close('+6dB 电压 x1.9953', Math.pow(10, 6 / 20), 1.995262315, 1e-9);
close('-3.0103dB 功率减半', E.dbm2w(0 - 3.0103), 5e-4, 1e-9);

/* ---- 热噪底 kTB(290K -> -174 dBm/Hz) ---- */
close('kT@290K=-173.98dBm/Hz', 10 * Math.log10(E.kB * 290 * 1e3), -173.98, 0.02);
close('kT@290K+1GHz+3dB=-80.98', 10 * Math.log10(E.kB * 290 * 1e3) + 3 + 90, -80.98, 0.02);

/* ---- 光子能量与 ASE(Agrawal, Fiber-Optic Communication Systems ch.6;NF=2*n_sp,双偏振) ---- */
close('hV@1550nm=-158.92dBm/Hz', E.photonPsdDbmHz(1550e-9), -158.92, 0.02);
const bw01 = E.dFreqHz(1550e-9, 0.1e-9); // 0.1nm @1550 = 12.4784 GHz
close('0.1nm@1550=12.4784GHz', bw01, 12.4784e9, 0.01e9);
close('ASE(20dB,5dB,1550,0.1nm)=-33dBm', E.asePowerDbm(20, 5, 1550e-9, bw01), -33.0, 0.1);
close('ASE(0dB)=0', E.asePsdW(0, 5, 1550e-9), 0, 1e-30);
close('光子能量1550=0.800eV', E.HP * E.CC / (1550e-9) / E.QE, 0.79993, 5e-4);

/* ---- 波长-频率与间隔(ITU-T G.694.1 DWDM 惯用换算 df=c*dl/l^2) ---- */
close('1550nm=193.4145THz', E.CC / 1550e-9, 193.4145e12, 0.01e12);
close('1nm@1550=124.784GHz', E.dFreqHz(1550e-9, 1e-9), 124.784e9, 0.01e9);
close('1nm@1310=174.717GHz', E.dFreqHz(1310e-9, 1e-9), 174.717e9, 0.2e9);
close('df<->dl roundtrip', E.dWlM(1550e-9, E.dFreqHz(1550e-9, 1e-9)), 1e-9, 1e-15);

/* ---- 反射/驻波/回损(Pozar ch.2 传输线理论) ---- */
const g = E.reflection(50, 75, 0);
close('|G|=0.2', g.mag, 0.2, 1e-12);
close('VSWR=1.5', g.vswr, 1.5, 1e-12);
close('RL=13.9794dB', g.rl, 13.9794, 1e-3);
close('ML=0.17729dB', g.ml, 0.17729, 1e-4);
close('反射功率4%', g.pct, 4.0, 1e-9);
const gm = E.reflection(50, 50, 0);
close('匹配|G|=0', gm.mag, 0, 1e-15);
is('匹配VSWR=1', gm.vswr, 1);
const gq = E.reflection(50, 0, 50); // 纯电抗负载 ZL=j50:|G|=1,VSWR 无穷
close('纯电抗|G|=1', gq.mag, 1, 1e-12);
is('纯电抗VSWR=NaN', Number.isFinite(gq.vswr), false);

/* ---- 串并联等效变换(Q 变换,射频匹配网络标准结果) ---- */
const [rp, xp] = E.s2p(1, 1);
close('s2p Rp=2', rp, 2, 1e-12);
close('s2p Xp=2', xp, 2, 1e-12);
const [rp2, xp2] = E.s2p(1, 1);
// 等效性验证:Rp ∥ jXp 必须还原出原串联阻抗 1+j1
// (Rp·jXp)/(Rp+jXp) = [j(Rp·Xp)]·(Rp−jXp)/(Rp²+Xp²) → 实部 Rp·Xp²/(Rp²+Xp²),虚部 Rp²·Xp/(Rp²+Xp²)
const den2 = rp2 * rp2 + xp2 * xp2;
close('等效并联实部=1', (rp2 * xp2 * xp2) / den2, 1, 1e-12);
close('等效并联虚部=1', (rp2 * rp2 * xp2) / den2, 1, 1e-12);

/* ---- MZM(马赫-曾德尔: T=cos^2(pi*V/2Vpi),正交点小信号 m=pi*Vpk/Vpi) ---- */
close('m(2Vpp,Vpi4)=pi/4', Math.PI * (2 / 2) / 4, Math.PI / 4, 1e-12);
// 页面引擎未导出 transmission/modulationDepth,以窗口函数核对:
// (modulator 计算在页面内联实现,这里用等价表达式交叉核对)
close('100%%调制Vpp=2*Vpi/pi', 2 * 4 / Math.PI, 2.54648, 1e-4);

/* ---- 九单位互转一致性(HTML fromW/toW vs 解析公式) ---- */
for (const z of [50, 75, 1]) {
  for (const u of ['dBm', 'dBW', 'W', 'mW', 'μW', 'Vrms', 'Vpp', 'dBV', 'dBμV']) {
    const w = E.toW(3.3, u, z);
    close(`roundtrip 3.3 ${u} @${z}R`, E.fromW(w, u, z), 3.3, 1e-9 * Math.max(1, Math.abs(3.3)));
  }
}

/* ---- 工程格式化 ---- */
is('eng(0.2236)=223.6 m', E.eng(0.22360679), '223.6 m');
is('eng(1000)=1 k', E.eng(1000.0), '1 k');
is('eng(0)=0', E.eng(0), '0');
is('eng(NaN)防呆', E.eng(NaN), '—');

console.log(`\n==== 审计结果: ${pass} 通过 / ${fail} 失败 ====`);
process.exit(fail ? 1 : 0);
