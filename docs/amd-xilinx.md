# Oeros on AMD/Xilinx adaptive SoCs (wrynose, rel-v2026.2)

This branch (`feature/edgeai-v3`, forked from
`feature/edgeai-intel-robotics-ai-suite`) adds AMD Kria and Versal AI Edge
boards to Oeros. It supersedes the "Deferred to
`feature/edgeai-amd-xilinx-lag`" note in `docs/edgeai.md`: as of AMD's
**rel-v2026.2** release, the AMD board layers declare
`LAYERSERIES_COMPAT = "wrynose"`, so the boards no longer need a lagging
branch.

Everything below was checked against shallow clones of each repository's
`rel-v2026.2` branch (checked 2026-11-13), not against documentation.

## How to build

The AMD boards live in their **own** bitbake-setup configuration,
`conf/registry/configurations/oeros-wrynose-lyrical-amd.conf.json`, not in
`oeros-wrynose-lyrical`:

```
bitbake-setup init \
    meta-oeros/conf/registry/configurations/oeros-wrynose-lyrical-amd.conf.json \
    oeros-wrynose-lyrical-amd distro/oeros oeros/machine-k26-smk-kr-sdt
```

Why a separate config: `meta-xilinx-multimedia/conf/layer.conf` sets
`PREFERRED_VERSION_gstreamer1.0*` to AMD's `1.28.2+amd` fork from the layer
itself. bitbake-setup shares one `bb-layers` list across every machine option
in a config, so adding the AMD layers to the shared lyrical config would swap
GStreamer for the Jetson, i.MX, Qualcomm, TI and Intel boards too. This is the
second per-vendor split after the Intel Jazzy config, and the first instance
of the "Recommended new OE configuration templates" idea in `docs/edgeai.md`.

The AMD machines use AMD's **System Device Tree (SDT)** flow: the machine
`.conf` fetches the board's SDT bundle from `edf.amd.com` and builds FSBL and
PMU firmware from source through BitBake multiconfig. No Vivado/Vitis install
and no `meta-xilinx-tools` are needed.

## Proprietary opt-in (`oeros/amd-proprietary`)

The default AMD build is **open source only**. Unlike
`oeros-edgeai-qcom.inc` and `oeros-edgeai-tegra.inc`, the AMD `.inc` does not
touch `LICENSE_FLAGS_ACCEPTED`. To opt in, enable the fragment after
`bitbake-setup init`:

```
bitbake-config-build enable-fragment oeros/amd-proprietary
```

| What the fragment sets | What it unlocks | License |
|---|---|---|
| `LICENSE_FLAGS_ACCEPTED += "xilinx"` | `pmu-rom-native`, needed only to boot a ZynqMP image under QEMU. It also silences the `xilinx-vars.bbclass` warning. | Proprietary (`LICENSE_FLAGS = "xilinx"`) |
| `LICENSE_FLAGS_ACCEPTED += "commercial"` | `gstreamer1.0-omx`, `-libav` and `-plugins-ugly` from meta-xilinx-multimedia, which the VCU OpenMAX pipeline uses | `LICENSE_FLAGS = "commercial"` |
| `DISTRO_FEATURES += "libmali"` | `libmali-xlnx` instead of Mesa. On Kria (Mali-400) the open default is Lima. On VEK385 (Mali-G78AE) Mesa has no driver, so without this the GPU is unused and rendering falls back to the CPU. | Proprietary |
| `MACHINE_FEATURES += "edgeai-amd-proprietary"` | `packagegroup-oeros-edgeai-xilinx-proprietary`. With the `vcu` feature (K26: KR260/KV260) it adds `kernel-module-vcu`, `vcu-firmware`, `libvcu-omxil`, `gst-plugins-vcu` and `gstreamer1.0-omx`. With `vcu2` (VEK385) it adds the VCU2 equivalents. On Kria it adds the `k26-starter-kits` or `k24-starter-kits` PL firmware bundle. These are listed directly rather than through `packagegroup-kria`, which also needs `packagegroup-security-tpm2` from meta-security. | `vcu-firmware`, `vcu2-firmware`: Proprietary. `kria-base-firmware`: Proprietary & GPL-2.0-only. |

**Vitis AI** has a separate, documented hook: `MACHINE_FEATURES +=
"edgeai-vitis-ai"` adds `packagegroup-oeros-edgeai-xilinx-vitis-ai` (`vart`,
`vart-ml`, `vart-x`). **No wrynose layer can satisfy it today.** No fragment
sets it, and nothing turns it on by default:

- `meta-vitis` on rel-v2026.2 (it was `meta-vitis-ai`; GitHub serves the
  same commit under both names) now contains only `packagegroup-vitis-aiml`
  (open-source build dependencies) and `h5pp`. The Vitis AI 3.5
  VART/DPU recipes were removed in rel-v2025.1.
- Vitis AI **6.3** ([amd/Vitis-AI](https://github.com/amd/Vitis-AI))
  supports only Versal AI Edge (VEK280) and Versal AI Edge Gen 2 (VEK385).
  Its Yocto recipes sit inside each board's reference design
  (`versal_ve/reference_design/vek280/sw/yocto/meta-vitis-ai`). That layer
  declares `LAYERSERIES_COMPAT = "scarthgap"` (EDF 2026.1), and `vart`,
  `vart-ml`, `vart-x`, `hip`, `pvt` and `cert-ve2` are `LICENSE = "CLOSED"`.
- Kria/ZynqMP DPU is not supported by Vitis AI 6.x at all.

## All machines in meta-xilinx rel-v2026.2 and the AMD board layers

"Relevant" means a plausible ROS 2 target for robotics, including automotive,
medical and industrial embedded designs. **Added** means it has an
`oeros/machine-*` fragment on this branch.

### meta-xilinx (core repository)

| MACHINE | Sub-layer | Relevant | Added | Why |
|---|---|---|---|---|
| `zynqmp-generic` | meta-xilinx-core | Indirectly | No | The SoC family behind Kria and ZCU boards, but its own description says it is "required by board- and SKU-specific" machines, not built standalone. |
| `versal-generic` | meta-xilinx-core | Indirectly | No | Base for VEK280/VCK190 etc.; not a standalone target. |
| `versal-2ve-2vm-generic` | meta-xilinx-core | Indirectly | No | Base for VEK385 (Versal Gen 2: Cortex-A78 APU, Cortex-R52 RPU); not standalone. |
| `versal-net-generic` | meta-xilinx-core | No | No | Versal NET is a networking/data-center family. |
| `versal-2vp-generic` | meta-xilinx-core | No | No | Versal Premium Gen 2 targets networking and data center. |
| `zynq-generic` | meta-xilinx-core | No | No | Zynq-7000 is a 32-bit Cortex-A9 with no AI acceleration. It is a legacy part for a ROS 2 Edge AI distro. |
| `microblaze-v-generic` | meta-xilinx-core | No | No | MicroBlaze-V soft RISC-V core inside the FPGA fabric; not a Linux application processor for ROS 2. |
| `mbv32` | meta-amd-zephyr-bsp | No | No | Builds Zephyr RTOS, not Linux. It could host micro-ROS someday, but that is outside Oeros. |
| `aarch32-tc`, `aarch64-tc`, `arm-rm-tc`, `microblaze-tc`, `riscv-tc` | meta-vitis-tc | No | No | Toolchain-only machines for the Vitis IDE; "not intended to boot a Linux image". |

### meta-kria (Kria SOMs and starter kits)

| MACHINE | Board | Relevant | Added | Why |
|---|---|---|---|---|
| `k26-smk-kr-sdt` | **KR260 Robotics Starter Kit** (K26, ZU5EV) | **Yes** | **Yes** | AMD's robotics SKU: 4x GigE (TSN-capable), Pmod and Raspberry Pi headers, camera interfaces, and the target of AMD's Kria Robotics Stack (KRS). It has a Mali-400 and an H.264/H.265 VCU (VCU is opt-in). |
| `k26-smk-kv-sdt` | **KV260 Vision AI Starter Kit** (K26) | **Yes** | **Yes** | The perception/smart-camera kit: MIPI camera input, ISP and VCU. It covers robot vision, machine-vision inspection and medical-imaging prototypes. |
| `k24-smk-kd-sdt` | **KD240 Drives Starter Kit** (K24, ZU3EG) | **Yes** | **Yes** | The industrial motor-control kit: encoder/QEI, CAN-FD and motor drive. It is the natural `ros2_control` hardware-interface target. It has no VCU or GPU. |
| `k26-smk-kr-sdt-multidomain`, `k26-smk-kv-sdt-multidomain`, `k24-smk-kd-sdt-multidomain` | Same kits, AMP | Yes | No | Linux plus FreeRTOS/bare metal on the Cortex-R5F, for hard real-time control loops and safety monitoring. The OpenAMP firmware wiring still needs designing, so this is follow-up work (see the parity list). |
| `k26-sm-sdt` | K26 SOM alone | Yes (production) | No | The production module behind KR260/KV260. A product needs its own carrier-board machine, so it is not a bootable target as is. |
| `k24c-sm-sdt`, `k24i-sm-sdt` | K24 SOM, commercial / **industrial** grade | Yes (production) | No | Same as above. The industrial-temperature `k24i` is the one a real industrial or medical drive would ship on. |

### meta-amd-adaptive-socs (evaluation kits)

Each board has an `-sdt-seg` machine (Linux plus PMC/PSM firmware) and a
`-multidomain` machine (AMP with the RPU). The table lists boards; the
fragments use the `-sdt-seg` variant.

| Board (MACHINE prefix) | Family | Relevant | Added | Why |
|---|---|---|---|---|
| **VEK280** (`versal-vek280-*`) | Versal AI Edge (AIE-ML) | **Yes** | **Yes** (`-sdt-seg`) | AMD's Edge AI flagship and a Vitis AI 6.3 target. AI Edge is aimed at ADAS/automotive, industrial and robotics perception. |
| **VEK385 Rev B** (`versal-2ve-2vm-vek385-revb-*`) | Versal AI Edge Gen 2 (AIE-MLv2) | **Yes** | **Yes** (`-sdt-seg`) | Cortex-A78 APU plus Cortex-R52 RPU, VCU2 and Mali-G78AE. It is the Vitis AI 6.3 Rev B reference platform, positioned for automotive, industrial and robotics. |
| VEK385 Rev A (`versal-2ve-2vm-vek385-*`) | Versal AI Edge Gen 2 | Yes | No | Early-access revision. Add a fragment only if a Rev A board is actually on hand. |
| VEK386 (`versal-2ve-2vm-vek386-*`) | Versal Gen 2 | Possibly | No | The machine description only says "Versal Gen 2 VEK386 evaluation kit". I could not confirm the device or its positioning, so I am not guessing. |
| VMK365 (`versal-2ve-2vm-vmk365-*`) | Versal Prime Gen 2 | Possibly | No | General-purpose embedded with no AI Engine. It may suit industrial or medical control, but nothing is robotics-specific. |
| VCK190 (`versal-vck190-*`) | Versal AI Core (AIE) | Secondary | No | High-power AI compute aimed at data center and 5G. Vitis AI 6.x does not support AI Core, so it would have no AI runtime path. VEK280 covers the edge use case. |
| VMK180 (`versal-vmk180-*`) | Versal Prime | No | No | General-purpose eval kit with no AI Engine. |
| VM-P-M1369 on X-PRC-01 (`versal-vm-p-m1369-00-reva-x-prc-01-reva-*`) | Versal SOM + carrier | Unknown | No | Not publicly documented enough to classify. |
| VPK120, VPK180 (`versal-vpk*`), VPK360 (`versal-2vp-vpk360-*`) | Versal Premium | No | No | Networking, data center and test equipment. |
| VHK158 (`versal-vhk158-*`) | Versal HBM | No | No | Memory-bandwidth/data-center part. |
| VRK160, VRK165 (+ Rev B) (`versal-vrk*`) | Versal RF | No | No | Direct-RF radio/wireless. |
| ZCU102 (`zynqmp-zcu102-*`) | Zynq US+ ZU9EG | Secondary | No | The classic ZynqMP eval board, historically used for ROS 2 bring-up. It has no VCU, and Kria is the better robotics target today. |
| ZCU104 (`zynqmp-zcu104-*`) | Zynq US+ ZU7EV | Secondary | No | Embedded-vision eval board and the old Vitis AI 3.5 DPU reference, which is gone on wrynose. KV260 covers the same EV-class silicon. |
| ZCU106 (`zynqmp-zcu106-*`) | Zynq US+ ZU7EV | No | No | Video-codec eval board. |
| ZCU111, ZCU208, ZCU216 (`zynqmp-zcu1*`, `zynqmp-zcu2*`) | Zynq US+ RFSoC | No | No | RF data converters for radio and radar front ends. |
| ZC702, ZC706 (`zynq-zc70*`) | Zynq-7000 | No | No | 32-bit Cortex-A9; legacy. |
| `qemu-microblaze-v-64` | QEMU | No | No | Emulation of a soft core. |
| `mbv32-scu200-zephyr-sdt` | Zephyr | No | No | RTOS, not Linux. |

### Not wrynose, so not in the table

- `meta-xilinx-tools` (`master`: `LAYERSERIES_COMPAT = "styhead walnascar"`;
  `rel-v2026.2-scarthgap`: scarthgap). Its `zcu102-zynqmp`,
  `vek280-versal` and similar XSCT-based machines are superseded by the SDT
  machines above.
- `meta-amd-edf-bsp` is wrynose, but its machines are the EDF "common"
  machines (`amd-cortexa53-mali-common`, `amd-cortexa78-mali-common`, ...),
  one image per CPU class, plus Alveo/Embedded+ management controllers. The
  generic common machines are an alternative to per-board machines. Vitis AI
  6.3's VEK385 build uses `amd-cortexa78-mali-common`. I did not adopt them
  because the other vendors in Oeros all use per-board machines.

## Related AMD layers: where, who publishes them, wrynose status

The `Xilinx` GitHub organization is AMD's (Xilinx was acquired by AMD in
2022). The layers' maintainers all have `@amd.com` addresses and the
READMEs say "AMD Xilinx", so every layer below is published by AMD. Most
still live under the legacy Xilinx organization; Vitis AI is under the
newer `amd` organization.

| Layer | Where | Org | Wrynose? | In the Oeros AMD config | Notes |
|---|---|---|---|---|---|
| meta-xilinx | [github.com/Xilinx/meta-xilinx](https://github.com/Xilinx/meta-xilinx) (also [git.yoctoproject.org/meta-xilinx](https://git.yoctoproject.org/meta-xilinx)) | Xilinx (AMD) | **Yes**: `rel-v2026.2`, plus `wrynose`/`wrynose-next` | Yes (core, standalone, standalone-sdt, microblaze, multimedia, mali400, gpu-malig78ae) | XRT/zocl, dfx-mgr, AI Engine driver, the AMD GStreamer fork, and `packagegroup-xilinx-ros` (dynamic layer on meta-ros) |
| meta-kria | [github.com/Xilinx/meta-kria](https://github.com/Xilinx/meta-kria) | Xilinx (AMD) | **Yes**: `rel-v2026.2` | Yes (`meta-kria-core`, `meta-kria-bsp`) | `Xilinx/meta-som` serves the identical commit; it is the old name |
| meta-amd-adaptive-socs | [github.com/Xilinx/meta-amd-adaptive-socs](https://github.com/Xilinx/meta-amd-adaptive-socs) | Xilinx (AMD) | **Yes**: `rel-v2026.2` | Yes (core, bsp) | Versal/ZCU eval kit machines |
| meta-vitis | [github.com/Xilinx/meta-vitis](https://github.com/Xilinx/meta-vitis) (`meta-vitis-ai` serves the same commit) | Xilinx (AMD) | **Yes**: `rel-v2026.2` | Yes | Only `packagegroup-vitis-aiml` + `h5pp`; no VART/DPU |
| Vitis AI 6.3 | [github.com/amd/Vitis-AI](https://github.com/amd/Vitis-AI) | **amd** | **No**: its in-tree `meta-vitis-ai` is scarthgap | No (opt-in hook only) | VEK280/VEK385 only; VART recipes are CLOSED |
| meta-openamp | [github.com/Xilinx/meta-openamp](https://github.com/Xilinx/meta-openamp) | Xilinx (AMD) | **Yes**: `rel-v2026.2` (`blacksail wrynose scarthgap`) | Yes | RPU (R5F/R52) communication, used by the multidomain machines |
| meta-xilinx-tsn | [github.com/Xilinx/meta-xilinx-tsn](https://github.com/Xilinx/meta-xilinx-tsn) | Xilinx (AMD) | **Yes**: `rel-v2026.2` | Yes | `packagegroup-tsn` (gPTP, tsn-utils, openlldp) |
| meta-jupyter | [github.com/Xilinx/meta-jupyter](https://github.com/Xilinx/meta-jupyter) | Xilinx (AMD) | **Yes**: `rel-v2026.2` | No | Only needed for `kria-dashboard`/notebooks |
| meta-amd-edf, meta-amd-edf-demos, meta-amd-hybrid | [Xilinx/meta-amd-edf](https://github.com/Xilinx/meta-amd-edf), [-edf-demos](https://github.com/Xilinx/meta-amd-edf-demos), [-hybrid](https://github.com/Xilinx/meta-amd-hybrid) | Xilinx (AMD) | **Yes**: `rel-v2026.2` | No | AMD's EDF distro and x86+FPGA hybrid platforms; Oeros is its own distro |
| meta-kria-apps | [github.com/Xilinx/meta-kria-apps](https://github.com/Xilinx/meta-kria-apps) | Xilinx (AMD) | **No**: `main` and `rel-v2026.1` are scarthgap | No | KD240 FOC motor control, `ros-tsn-pubsub`, KV260 AP1302 ISP firmware, BIST. This is the most robotics-relevant content AMD has, and it is not on wrynose. |
| meta-xilinx-tools | [github.com/Xilinx/meta-xilinx-tools](https://github.com/Xilinx/meta-xilinx-tools) | Xilinx (AMD) | **No** (`master`: styhead/walnascar) | No | Not needed with the SDT flow |
| meta-petalinux | [github.com/Xilinx/meta-petalinux](https://github.com/Xilinx/meta-petalinux) | Xilinx (AMD) | **No** (latest is `rel-v2026.2-scarthgap`) | No | PetaLinux distro |
| Xilinx/meta-ros | [github.com/Xilinx/meta-ros](https://github.com/Xilinx/meta-ros) | Xilinx (AMD) | Yes: `rel-v2026.2` | No | AMD's fork of meta-ros; Oeros uses upstream `ros/meta-ros` `wrynose` |
| **meta-ros-kria** | does not exist | n/a | n/a | n/a | No repository by that name on GitHub. The nearest things are [Xilinx/KRS](https://github.com/Xilinx/KRS) (Kria Robotics Stack: a ROS 2 colcon workspace, not a Yocto layer), [ros-acceleration/ros2_kria](https://github.com/ros-acceleration/ros2_kria) (last push 2022) and [Xilinx/kria_ros_perception](https://github.com/Xilinx/kria_ros_perception) (last push 2023). |

## What the branch adds

| File | Purpose |
|---|---|
| `conf/machine/include/oeros-edgeai-xilinx.inc` | `OEROS_EDGEAI_VENDOR = "xilinx"`, `MACHINE_FEATURES += edgeai edgeai-fpga edgeai-xrt` (+ `edgeai-aie` on Versal). Accepts no license flags. |
| `conf/fragments/machine-{k26-smk-kr-sdt,k26-smk-kv-sdt,k24-smk-kd-sdt,versal-vek280-sdt-seg,versal-2ve-2vm-vek385-revb-sdt-seg}.conf` | One-of machine options, same shape as the other vendors |
| `conf/fragments/amd-proprietary.conf` | The documented opt-in above |
| `dynamic-layers/xilinx/recipes-core/packagegroups/packagegroup-oeros-edgeai-xilinx.bb` | `-runtime` (XRT, zocl, dfx-mgr, AIE driver, packagegroup-vitis-aiml), `-pipeline` (packagegroup-xilinx-gstreamer), `-ros` (ros2_control), `-industrial` (linuxptp, can-utils, packagegroup-tsn), `-kria` (xmutil, platformstats, QoS tools), `-proprietary`, `-vitis-ai`, `-dev` |
| `dynamic-layers/xilinx-multimedia/recipes-multimedia/gstreamer/gstreamer1.0-plugins-bad_%.bbappend` | AMD's plugins-bad fork unconditionally appends the `faac`/`faad` PACKAGECONFIGs (both `LICENSE_FLAGS = "commercial"`), which breaks the open-source default. This drops them unless `commercial` is accepted. |
| `conf/layer.conf` | `BBFILES_DYNAMIC` keys `xilinx` (meta-xilinx-core's collection) and `xilinx-multimedia` |
| `conf/registry/configurations/oeros-wrynose-lyrical-amd.conf.json` | The separate AMD bitbake-setup config. It includes `meta-jupyter` because `meta-xilinx-mali400` carries `python3-qtconsole`/`python3-qtpy` bbappends (its `meta-qt6` dynamic layer) whose recipes live in meta-jupyter; without it, parsing fails with "No recipes available for". |

## Comparison with the other vendors in Oeros

| Capability | Nvidia | NXP | Qualcomm | TI | Intel | **AMD (this branch)** |
|---|---|---|---|---|---|---|
| Vendor `.inc` + machine fragments | Yes | Yes | Yes | Yes | Yes | **Yes** |
| bitbake-setup config | shared lyrical | shared lyrical | shared lyrical | shared lyrical | shared lyrical + own Jazzy config | **own lyrical config** (GStreamer override) |
| Accelerated inference runtime on wrynose | CUDA/TensorRT, ORT TensorRT EP | TFLite VX/Ethos-U/Neutron, ORT VSI EP | QAIRT/QNN, ORT QNN EP | TIDL on C7x/MMA | OpenVINO, ORT OpenVINO EP | **Gap**: XRT/AIE driver only. No Vitis AI runtime or ORT Vitis AI EP on wrynose, so inference runs on the CPU (ORT/TFLite). |
| Vendor AI video pipeline | DeepStream, nvv4l2 | NNStreamer | QIM (deferred) | edgeai-gst-apps, TIOVX | (OpenVINO GStreamer not used) | **Partial**: AMD GStreamer fork; VCU encode/decode opt-in; VVAS no longer shipped |
| Vendor ROS 2 nodes | Isaac ROS | none | QIR SDK: QRB-ROS, Nav2/MoveIt2 | edgeai-robotics-kit | Robotics AI Suite (Oeros' own recipes) | **Gap**: only ros2_control from meta-ros. AMD's ROS 2 content (KRS, `ros-tsn-pubsub`, `foc-motor-ctrl`) is not on wrynose. |
| Real-time / industrial networking | n/a | n/a | n/a | (R5F available, not wired up) | n/a | **TSN, gPTP, CAN** in `-industrial`; AMP machines not yet added |
| Proprietary content | `commercial_tegra` accepted unconditionally in the `.inc` | none in the `.inc` | `commercial` accepted unconditionally in the `.inc` | none | none | **Explicit opt-in fragment** |
| Reuses the vendor's own packagegroups | No | could (`packagegroup-imx-ml`) | Yes (QIR) | could (`packagegroup-edgeai-tisdk-addons`) | No | **Yes**: `packagegroup-xilinx-gstreamer`, `packagegroup-vitis-aiml`, `packagegroup-tsn` (`packagegroup-kria` is not reusable as is: it mixes proprietary firmware with a meta-security dependency) |

### Suggested additions for parity

1. **Inference acceleration (the biggest gap).** Port the Vitis AI 6.3
   reference-design `meta-vitis-ai` (VART, VART-ML, VART-X and the ONNX
   Runtime Vitis AI EP) from scarthgap to wrynose as a separate, opt-in
   layer for VEK280/VEK385. The `-vitis-ai` hook is ready for it. Before
   redistributing anything, check whether AMD's CLOSED license permits
   redistribution, or wait for AMD to publish a wrynose layer. For Kria
   there is no Vitis AI 6.x path, so the realistic option is XRT plus
   HLS/Vitis Libraries kernels loaded through `dfx-mgr`/`xmutil`.
2. **AMD ROS 2 nodes, for parity with Qualcomm's QRB-ROS and TI's robotics
   kit.** Port `ros-tsn-pubsub` and `foc-motor-ctrl` from `meta-kria-apps`
   (scarthgap) into Oeros recipes, in the same style as the Intel Robotics
   AI Suite. `foc-motor-ctrl` plus a `ros2_control` hardware interface would
   make the KD240 a complete motor-control demo. Evaluate `Xilinx/KRS` for
   any packages still maintained.
3. **Real-time AMP machines.** Add fragments for the `*-multidomain`
   variants of KR260/KD240/VEK280 with OpenAMP (`meta-openamp` is already in
   the config) so control loops can run on the R5F/R52. TI's AM6x R5F would
   benefit from the same pattern.
4. **Camera bring-up on KV260.** Package the AP1302 ISP firmware (in
   `meta-kria-apps`, scarthgap, proprietary, so it would go behind the
   opt-in) so that `v4l2-camera`/`gscam` in `packagegroup-oeros-edgeai-ros`
   work out of the box.
5. **Use the same opt-in pattern for everyone.** Move `LICENSE_FLAGS_ACCEPTED`
   out of `oeros-edgeai-qcom.inc` and `oeros-edgeai-tegra.inc` into
   `oeros/qcom-proprietary` and `oeros/nvidia-proprietary` fragments, so
   every vendor's proprietary content is an explicit choice.
6. **Finish the per-vendor config split** that `docs/edgeai.md` already
   recommends. With AMD, two of six vendors now have their own config.

## Verification (2026-11-14)

I ran `bitbake-setup init` with `oeros-wrynose-lyrical-amd.conf.json`, using
`-L meta-oeros` to point at this checkout, and then ran
`bitbake -g packagegroup-oeros-edgeai-xilinx` for each case. This resolves
the dependency graph only; nothing was built.

| Case | Result | Proprietary packages in the graph |
|---|---|---|
| KR260 (`k26-smk-kr-sdt`) | resolves | none |
| KV260 (`k26-smk-kv-sdt`) | resolves | none |
| KD240 (`k24-smk-kd-sdt`) | resolves | none |
| VEK280 (`versal-vek280-sdt-seg`) | resolves | none |
| VEK385 Rev B (`versal-2ve-2vm-vek385-revb-sdt-seg`) | resolves | none |
| KR260 + `oeros/amd-proprietary` | resolves | `k26-starter-kits`, `vcu-firmware`, `libmali-xlnx`, `gstreamer1.0-omx` |
| KD240 + `oeros/amd-proprietary` | resolves | `k24-starter-kits`, `libmali-xlnx` |
| VEK385 + `oeros/amd-proprietary` | resolves | `vcu2-firmware`, `libmali-xlnx` |

Not covered here: the image-level `packagegroup-oeros-edgeai` fails on every
AMD board. The cause is in the shared groups, not the AMD one:
`packagegroup-oeros-edgeai-core` asks for `python3-onnxruntime`,
`onnxruntime-tools` and `tensorflow-lite`, none of which meta-onnxruntime or
meta-tensorflow-lite provides. `packagegroup-oeros-edgeai-ros` asks for
`oeros-inference-node`, which has no recipe anywhere. This was already true
on the base branch and affects every vendor.

Also already present on the base branch: `oeros.conf` sets
`DISTRO_FEATURES ?= "${OEROS_DEFAULT_DISTRO_FEATURES}"`, but nothing defines
that variable, so every recipe prints an "Unexpanded variable" warning
(about 3,000 per parse).
