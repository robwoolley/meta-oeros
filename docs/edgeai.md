# meta-oeros — Edge AI for ROS 2 on Wrynose/Lyrical

Adapted from the `oeros-edgeai-setup.json` bitbake-setup draft for Yocto
**wrynose** / ROS 2 **lyrical**, and integrated into
`conf/registry/configurations/oeros-wrynose-lyrical.conf.json`.

## Layout

```
meta-oeros/
├── conf/layer.conf                                  # BBFILES_DYNAMIC keyed on vendor BBFILE_COLLECTIONS
├── conf/machine/include/oeros-edgeai-<vendor>.inc    # sets OEROS_EDGEAI_VENDOR + MACHINE_FEATURES
├── conf/fragments/oeros/
│   ├── edgeai.conf                                   # DISTRO_FEATURES += edgeai, IMAGE_INSTALL += packagegroup-oeros-edgeai
│   ├── ros-lyrical.conf                              # ROS_DISTRO = lyrical
│   └── machine-<board>.conf                          # MACHINE=<board>; requires the vendor .inc + the two fragments above
├── recipes-core/packagegroups/
│   ├── packagegroup-oeros-edgeai.bb                  # selector: core + ros + vendor (via OEROS_EDGEAI_VENDOR)
│   └── packagegroup-oeros-edgeai-core.bb             # onnxruntime, tflite, opencv, gstreamer, python
├── recipes-ros/packagegroups/
│   └── packagegroup-oeros-edgeai-ros.bb              # vision_msgs, cv_bridge, image_transport, inference node
└── dynamic-layers/<vendor>/recipes-core/packagegroups/
    └── packagegroup-oeros-edgeai-<vendor>.bb         # only parsed when the vendor layer is in BBLAYERS
```

Selecting a `oeros/machine-<board>` option in the `machine` one-of category is
self-contained: it sets `MACHINE`, pulls in the vendor `.inc`, sets
`ROS_DISTRO = "lyrical"`, and enables the `edgeai` distro feature + packagegroup.

## MACHINE → layers → packagegroup (this branch, `feature/edgeai`)

| Vendor | MACHINE(s) | Vendor layers | Packagegroup | Accelerator path |
|---|---|---|---|---|
| Nvidia | jetson-orin-nano-devkit (Super nvpmodel by default), jetson-agx-orin-devkit | meta-tegra | packagegroup-oeros-edgeai-tegra | CUDA/TensorRT/VPI, Isaac ROS |
| NXP | imx8mp-lpddr4-evk, imx93-11x11-lpddr4x-evk, imx95-19x19-lpddr5-evk | meta-freescale, meta-imx (meta-imx-bsp/-sdk/-ml) | packagegroup-oeros-edgeai-imx | TFLite VX/Ethos-U/Neutron delegates, ORT VSI-NPU EP, NNStreamer |
| Qualcomm | qcs6490-rb3gen2-core-kit, qcs9100-ride-sx | meta-qcom, meta-qcom-distro | packagegroup-oeros-edgeai-qcom (**runtime only**) | QAIRT/QNN on Hexagon, TFLite/ORT QNN EP |
| Qualcomm (robotics) | iq-9075-evk, iq-8275-evk | meta-qcom, meta-qcom-distro, **meta-qcom-robotics-sdk** | packagegroup-oeros-edgeai-qcom (**runtime + robotics**) | QAIRT/QNN on Hexagon + QIR SDK: Nav2/MoveIt2, QRB-ROS nodes |
| TI | am62axx-evk, am68-sk, am69-sk | meta-arm, meta-ti (meta-ti-bsp/-extras), meta-edgeai | packagegroup-oeros-edgeai-ti | TIDL on C7x/MMA, TIOVX, edgeai-gst, robotics kit |
| Intel | intel-corei7-64 | meta-intel, meta-openvino | packagegroup-oeros-edgeai-intel | OpenVINO CPU/iGPU/NPU, ORT OpenVINO EP, compute-runtime |

Common to all: oe-core, meta-oe/meta-python/meta-multimedia, meta-clang,
meta-ros (meta-ros-common, meta-ros2, **meta-ros2-lyrical**),
meta-tensorflow-lite, meta-onnxruntime → packagegroup-oeros-edgeai-core + -ros.

## Deferred to `feature/edgeai-amd-xilinx-lag`

As of 2026-09-19, these vendor layers have no branch past `scarthgap`
(3-4 Yocto releases behind `wrynose`) and are **not** included on this branch:

- **AMD/Xilinx**: `meta-xilinx-tools`, `meta-vitis-ai`, `meta-kria` — versioned by
  Xilinx tool release (`rel-v2026.1`) rather than Yocto codename. `meta-xilinx`
  (core BSP) *does* have a `wrynose` branch, but it does not define the
  `k26-smk-kr` / `k24-smk-kd` / `vek280-versal` MACHINEs — those live in
  `meta-kria`/`meta-vitis-ai`, so the Kria/Versal targets cannot be built on
  wrynose at all yet. `packagegroup-oeros-edgeai-xilinx.bb` and the
  corresponding machine fragments/`.inc` live on the follow-up branch.
- **Qualcomm QIM SDK**: `meta-qcom-hwe`, `meta-qcom-qim-product-sdk` — also
  stuck at `scarthgap`. The QCS6490/QCS9100 *MACHINE* definitions and the raw
  QNN/QAIRT/SNPE runtime are unaffected (they live in `meta-qcom` /
  `meta-qcom-distro`, both wrynose-ready), so those machines **stay on this
  branch** with a **trimmed** `packagegroup-oeros-edgeai-qcom` (`-runtime`
  only). The QIM GStreamer AI pipeline (`-pipeline`) and QRB ROS 2 offload
  nodes (`-ros`) are deferred with the rest of the QIM SDK.
  **Update 2026-09-19**: `meta-qcom-hwe`'s `main` branch README now states
  its development branch has been merged into `meta-qcom` and it is "no
  longer open for development" — its `kirkstone`/`scarthgap` branches are
  now frozen QLI 1.x snapshots, so it will likely never get a `wrynose`
  branch of its own. Confirmed a large chunk of its content (FastCV, CamX
  camera stack, Adreno/Mesa/Wayland graphics, GStreamer) is now already
  present directly in `meta-qcom`'s `wrynose` branch under
  `recipes-multimedia/`, `recipes-graphics/`, and a new `recipes-ml/`
  (which now ships a real `qairt-sdk` recipe) plus a `dynamic-layers/ai/`
  collection with a real `onnxruntime-qnn` recipe. **This was not
  re-audited against `packagegroup-oeros-edgeai-qcom`'s `-runtime` list in
  this pass** — the package names there (`qnn-sdk`, `qairt-runtime`,
  `onnxruntime-qnn-ep`) should be re-verified against `meta-qcom`'s actual
  `recipes-ml/qairt` and `dynamic-layers/ai/recipes-ml/onnxruntime-qnn`
  recipes (real PNs look like `qairt-sdk` and `onnxruntime-qnn`, not
  `qairt-runtime`/`onnxruntime-qnn-ep`) as a follow-up. `meta-qcom-qim-
  product-sdk` itself is unaffected by the hwe merge and remains at
  `scarthgap`.

## Qualcomm Intelligent Robotics SDK (`meta-qcom-robotics-sdk`)

Separate from (and not blocked like) the QIM SDK above: **QIR SDK**
(`meta-qcom-robotics-sdk`) has its own `wrynose` branch and is added on
this branch for its two officially-supported machines, `iq-9075-evk`
(QCS9100 family) and `iq-8275-evk` (QCS8300 family) — matching the boards
in the SDK's own `ci/*.yml` kas configs. Wired in as
`packagegroup-oeros-edgeai-qcom`'s new `-robotics` sub-package, gated on
an `edgeai-robotics-sdk` `MACHINE_FEATURE` those two machines' fragments
set (so it doesn't affect `qcs6490-rb3gen2-core-kit`/`qcs9100-ride-sx`,
which the SDK doesn't officially support).

Packagegroups consumed directly from `meta-qcom-robotics-sdk` (RDEPENDS,
not re-derived):

| Packagegroup | Contents |
|---|---|
| `packagegroup-qcom-ros2` | Core ROS 2 + Nav2 (`navigation2`, `nav2-common`, `nav2-msgs`) + MoveIt2 (`moveit-runtime`, `moveit-planners-chomp`/`-ompl`) + `cv-bridge`/`vision-msgs`/`foxglove-bridge` + demo/example nodes |
| `packagegroup-robotics-opensource` | QRB-ROS open-source nodes (`qrb-ros-robot-base`, `qrb-ros-amr`, `qrb-ros-follow-path`, `qrb-ros-nn-inference`, `qrb-ros-benchmark`, `qrb-ros-system-monitor`, SLAM/AMR msg types) + community packages (`rplidar-ros2`, `orbbec-camera`, `xsens-mti-ros2-driver`, `cartographer`/`cartographer-ros`, `nav2-bringup`) + `ros-gst-bridge` |
| `packagegroup-oss-with-prop-deps` | QRB-ROS nodes needing the proprietary QNN/camera stack (`qrb-ros-camera`, `qrb-ros-video`, `qrb-ros-audio-service`, `qrb-ros-colorspace-convert`) + perception sample apps (`sample-hand-detection`, `sample-object-detection`, `sample-object-segmentation`, `sample-resnet101`, `sample-apriltag`, `sample-depth-estimation`, `sample-hrnet-pose-estimation`, `sample-face-detection`) |
| `packagegroup-robotics-proprietary` | Currently an empty scaffold upstream (no packages listed) — not RDEPENDed on here since it contributes nothing |

Notes:
- `qrb-ros-nn-inference` (the same package the original draft's
  hand-rolled `-ros` component guessed at) is real here, and depends on
  `qairt-sdk` + `tensorflow-lite` at build time — consistent with the
  `-runtime` packages already in `packagegroup-oeros-edgeai-qcom`.
- `COMPATIBLE_MACHINE` on `packagegroup-oeros-edgeai-qcom.bb` had to be
  widened: `qcs6490-rb3gen2-core-kit`/`qcs9100-ride-sx` embed their SOC
  family in the MACHINE name so the old regex substring-matched them, but
  `iq-9075-evk`/`iq-8275-evk` don't — they're now listed explicitly.
- Full device/kernel bring-up (kas `linux-qcom-6.18.yml` etc.) wasn't
  cross-checked against `meta-qcom`'s own `iq-9075-evk.conf`/
  `iq-8275-evk.conf`; both machines already exist in `meta-qcom` itself
  (`recipes-bsp/packagegroups/packagegroup-iq-9075-evk.bb` etc.), so the
  robotics SDK layers on top of an already-working MACHINE rather than
  introducing a new one.

## Things to verify before first build

- `meta-tensorflow-lite` and `meta-onnxruntime`: the draft config pointed at
  `nxp-imx/meta-tensorflow-lite` and `xerox-ai/meta-onnxruntime`, **neither of
  which exists**. The real upstream is `NobuoTsukamoto/meta-tensorflow-lite`
  and `NobuoTsukamoto/meta-onnxruntime`, but both only have a `walnascar`
  branch (one release behind wrynose). Forked to
  `robwoolley/meta-tensorflow-lite` and `robwoolley/meta-onnxruntime`, each
  with a `wrynose` branch cut from `walnascar` that backports
  `LAYERSERIES_COMPAT += "whinlatter wrynose"`. Re-point at upstream once it
  cuts a native wrynose branch.
- `meta-imx` versions by kernel/BSP release, not Yocto codename; pinned to
  `wrynose-6.18.37-2.1.0` (the newer of the two available wrynose-era trains,
  the other being `wrynose-6.18.20-2.0.0`). Its `layer.conf` declares
  `LAYERSERIES_COMPAT` through `wrynose` explicitly, so this is a real (not
  backported) compatibility claim.
- The draft's i.MX 95 MACHINE, `imx95-19x19-evk`, doesn't exist upstream at
  all (checked both `meta-freescale` and `meta-imx-bsp` on their wrynose-era
  branches). The real NXP i.MX 95 19x19 EVK machine is
  `imx95-19x19-lpddr5-evk` (defined in `meta-imx-bsp`) — fixed here.
- Likewise `jetson-orin-nano-devkit-super` doesn't exist as a separate
  MACHINE in `meta-tegra`. On the current wrynose branch,
  `jetson-orin-nano-devkit.conf` itself already defaults `NVPMODEL` and
  `KERNEL_DEVICETREE` to the Super variant (`nvpmodel_p3767_0003_super`,
  `...nv-super.dtb`) — fixed to just `jetson-orin-nano-devkit` here.
- BBFILES_DYNAMIC keys must match each vendor layer's BBFILE_COLLECTIONS. The
  original draft had `imx-ml`; the real `meta-imx-ml` collection name is
  `imx-machine-learning` — fixed in `conf/layer.conf`. Re-verify against each
  layer's `conf/layer.conf` on every vendor branch bump.
- Nvidia/Qualcomm/NXP blobs need `LICENSE_FLAGS_ACCEPTED` / EULA vars (set in
  the machine `.inc` files) and, for Jetson, a devnet mirror
  (`NVIDIA_DEVNET_MIRROR`, passed through via `bb-env-passthrough-additions`).
- Vendor package names in the `-<vendor>` groups follow the vendor layers'
  current recipe names as of 2026-09-19; expect renames between releases.
- `packagegroup-oeros-edgeai-qcom`'s `-runtime` RDEPENDS on a plain
  `hexagon-dsp-binaries`, but `meta-qcom`'s own
  `packagegroup-rb3gen2.bb` only ever RDEPENDS on the board-specific
  `hexagon-dsp-binaries-thundercomm-rb3gen2-adsp` /
  `-cdsp`. `hexagon-dsp-binaries` as a bare name was not found in
  `meta-qcom`; verify the real PN before building QCS6490.

## Upstream packagegroups worth reusing (found while integrating)

Several vendor layers already ship their own aggregate packagegroups that
overlap with what `packagegroup-oeros-edgeai-<vendor>.bb` hand-rolls. Where
noted, consider RDEPENDS on the upstream packagegroup instead of
re-deriving the same package list (less drift as vendor recipes get
renamed):

| Layer | Packagegroup | What it does | Relation to oeros |
|---|---|---|---|
| `meta-imx-ml` | `packagegroup-imx-ml` | Per-SoC-family (`mx8mp`/`mx93`/`mx943`/`mx95`) selection of TFLite/PyTorch/ONNXRuntime, NNStreamer, and the correct NPU delegate (VX/Ethos-U/Neutron) via `MACHINE_FEATURES`/override logic | `packagegroup-oeros-edgeai-imx.bb` re-derives this delegate selection by hand; RDEPENDS on `packagegroup-imx-ml` directly instead and it stays correct as NXP adds SoCs |
| `meta-vitis-ai` | `packagegroup-vitis-aiml` | Base ML dependency libs (fmt, glog, gsl, hdf5, eigen, spdlog, xtensor, pybind11) needed to build/run Vitis-AI apps | Good `-dev`/runtime dependency for `packagegroup-oeros-edgeai-xilinx` on `feature/edgeai-amd-xilinx-lag` |
| `meta-kria` | `packagegroup-kria` | Kria SOM essentials: firmware, `kria-dashboard`, `xmutil`, Jupyter, TPM2 security, board-id data | `packagegroup-oeros-edgeai-xilinx.bb` already lists `xmutil`/`dfx-mgr` individually; RDEPENDS on `packagegroup-kria` as a whole would be more maintainable |
| `meta-qcom` | `packagegroup-rb3gen2` | RB3 Gen2-specific firmware + the real Hexagon DSP binary package names (see the bug noted above) | Use directly rather than guessing PNs |
| `meta-qcom` (formerly `meta-qcom-hwe`, now merged — see "Update 2026-09-19" above) | `packagegroup-qcom-fastcv`, `packagegroup-qcom-camera`, `packagegroup-qcom-opencv` | SOC_FAMILY-aware (qcm6490/qcs615/qcs9100/qcs8300) camera/FastCV/OpenCV stacks | Now potentially usable directly from `meta-qcom` on this (non-lag) branch — not yet re-audited into `packagegroup-oeros-edgeai-qcom.bb`'s `-runtime` |
| `meta-qcom-robotics-sdk` | `packagegroup-qcom-ros2`, `packagegroup-robotics-opensource`, `packagegroup-oss-with-prop-deps` | Nav2/MoveIt2 + QRB-ROS nodes + community robotics packages | Consumed directly by `packagegroup-oeros-edgeai-qcom.bb`'s `-robotics` sub-package (see the QIR SDK section above) |
| `meta-edgeai` (TI) | `packagegroup-edgeai-tisdk-addons` | Build/runtime deps for the edgeai/ADAS SDK: cmake, boost, opencv(-dev), numpy, pybind11, meson/ninja | `packagegroup-oeros-edgeai-ti.bb` could RDEPEND on this instead of implicitly assuming these land via `packagegroup-oeros-edgeai-core` |

`meta-tegra` was checked too but exposes only a `nativesdk-packagegroup-cuda-sdk-host`
(host-side SDK, not a target aggregate) — no upstream target packagegroup to
reuse there; the individual CUDA/TensorRT/VPI recipe names in
`packagegroup-oeros-edgeai-tegra.bb` are the right level of granularity.

## Recommended additional machines for robotics

Surveying each vendor layer's `conf/machine/` for boards not yet in this
config, marketed at or commonly used for robotics:

- **Qualcomm (meta-qcom)**: `iq-9075-evk`/`iq-8275-evk` are now added
  (`meta-qcom-robotics-sdk`'s own officially-supported machines — see the
  QIR SDK section above). Still not added: `qcm6490-idp.conf` and the
  `rb5`/`rb2`/`qrb2210-rb1-core-kit` family, Qualcomm's older **Robotics
  RB-series** boards (`packagegroup-rb1.bb`, `-rb2.bb`, `-rb5.bb` all exist
  in `meta-qcom`) — these are more ROS-community-established (many
  existing third-party QRB ROS 2 packages target RB5 specifically) than
  the newer devkits, but `meta-qcom-robotics-sdk`'s own CI doesn't build
  for them, so it's unverified whether the QIR SDK packagegroups apply
  cleanly there.
- **AMD/Xilinx (meta-kria)**: alongside `k26-smk-kr` (KR260, robotics
  starter kit), `k26-smk-kv` (**KV260**, the Vision AI starter kit) is
  arguably more relevant to a perception/inference-focused Edge AI
  config than `k24-smk-kd`, which the original draft's README table
  listed but which meta-kria doesn't clearly distinguish for vision use.
  Add once the AMD/Xilinx branch is unblocked.
- **Nvidia (meta-tegra)**: `jetson-agx-thor-devkit` (next-gen AGX, wrynose
  branch already has machine confs for it) is worth tracking as a future
  high-end robotics target once Isaac ROS / CUDA support matures on it;
  not added now since it's very new and unvalidated.
- **TI (meta-ti + meta-edgeai)**: the existing AM62A/AM68/AM69 selection is
  already TI's edge-AI/robotics-flavored SK line; no gaps found.
- **NXP (meta-imx)**: `imx8mp-lpddr4-frdm`/`imx8mp-navq` (NavQPlus, a
  ROS-2-oriented carrier board built specifically for robotics on i.MX 8M
  Plus) is a notable gap versus the generic EVK already selected — worth
  adding as a dedicated robotics-carrier option alongside the EVK.

## Recommended new OE configuration templates

The single `oeros-wrynose-lyrical` configuration now unconditionally lists
every vendor's BSP/tools layers in `bb-layers` (Nvidia, NXP, Qualcomm, TI,
Intel, and — on the follow-up branch — AMD) even though a given build only
ever uses one vendor's `MACHINE`. That's a real cost: every vendor layer's
own recipes still get parsed on every build regardless of which
`oeros/machine-*` option is picked. Recommend splitting into focused,
per-vendor `bitbake-setup` configuration templates once this stabilizes,
e.g.:

- `oeros-wrynose-lyrical-nvidia.conf.json`
- `oeros-wrynose-lyrical-nxp.conf.json`
- `oeros-wrynose-lyrical-qualcomm.conf.json`
- `oeros-wrynose-lyrical-ti.conf.json`
- `oeros-wrynose-lyrical-intel.conf.json`
- `oeros-wrynose-lyrical-amd-xilinx.conf.json` (once unblocked)

Each would set `"extends": "oeros-wrynose-lyrical"` (mirroring how the
original `oeros-edgeai-setup.json` draft used `"extends": "oeros-base"`)
and add only that vendor's `bb-layers`, keeping the base config's
`oeros/machine-*` options but scoped to just its own boards. This is the
concrete mechanism for "selectively enable functionality from the vendor
layers" that parse-time bloat in the single monolithic config works
against today.
