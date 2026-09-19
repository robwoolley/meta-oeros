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
| Nvidia | jetson-orin-nano-devkit-super, jetson-agx-orin-devkit | meta-tegra | packagegroup-oeros-edgeai-tegra | CUDA/TensorRT/VPI, Isaac ROS |
| NXP | imx8mp-lpddr4-evk, imx93-11x11-lpddr4x-evk, imx95-19x19-lpddr5-evk | meta-freescale, meta-imx (meta-imx-bsp/-sdk/-ml) | packagegroup-oeros-edgeai-imx | TFLite VX/Ethos-U/Neutron delegates, ORT VSI-NPU EP, NNStreamer |
| Qualcomm | qcs6490-rb3gen2-core-kit, qcs9100-ride-sx | meta-qcom, meta-qcom-distro | packagegroup-oeros-edgeai-qcom (**runtime only**) | QAIRT/QNN on Hexagon, TFLite/ORT QNN EP |
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
- BBFILES_DYNAMIC keys must match each vendor layer's BBFILE_COLLECTIONS. The
  original draft had `imx-ml`; the real `meta-imx-ml` collection name is
  `imx-machine-learning` — fixed in `conf/layer.conf`. Re-verify against each
  layer's `conf/layer.conf` on every vendor branch bump.
- Nvidia/Qualcomm/NXP blobs need `LICENSE_FLAGS_ACCEPTED` / EULA vars (set in
  the machine `.inc` files) and, for Jetson, a devnet mirror
  (`NVIDIA_DEVNET_MIRROR`, passed through via `bb-env-passthrough-additions`).
- Vendor package names in the `-<vendor>` groups follow the vendor layers'
  current recipe names as of 2026-09-19; expect renames between releases.
