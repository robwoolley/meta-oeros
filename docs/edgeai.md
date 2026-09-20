# meta-oeros — Edge AI for ROS 2 on Wrynose/Lyrical

Adapted from the `oeros-edgeai-setup.json` bitbake-setup draft for Yocto
**wrynose** / ROS 2 **lyrical**, and integrated into
`conf/registry/configurations/oeros-wrynose-lyrical.conf.json`.

> **This branch is `feature/edgeai-intel-robotics-ai-suite`, built on top of
> `feature/edgeai`.** It adds the Intel Robotics AI Suite (see the dedicated
> section below) via a **second, separate registry config**,
> `conf/registry/configurations/oeros-wrynose-jazzy.conf.json` — the suite
> forced a fork to ROS 2 Jazzy, which cannot share `bb-layers` with the
> lyrical config (see "Why a separate Jazzy config" below).

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
| Intel (robotics) | intel-corei7-64 (**Jazzy variant**) | meta-intel, meta-openvino, meta-oeros's own new `recipes-robotics-ai-suite/` | packagegroup-oeros-edgeai-intel (**+ -robotics-ai-suite**) | OpenVINO + Intel Robotics AI Suite: YOLOv8 perception, Nav2 wandering, ADBSCAN — on the separate `oeros-wrynose-jazzy` config |

Common to all: oe-core, meta-oe/meta-python/meta-multimedia, meta-clang,
meta-ros (meta-ros-common, meta-ros2, **meta-ros2-lyrical**),
meta-tensorflow-lite, meta-onnxruntime → packagegroup-oeros-edgeai-core + -ros.
(The Intel-robotics row above is the one exception: it lives on
`oeros-wrynose-jazzy`, using **meta-ros2-jazzy** instead.)

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

## Intel Robotics AI Suite (`packagegroup-oeros-robotics-ai-suite`)

Source: `open-edge-platform/edge-ai-suites`, path `robotics-ai-suite/`
(a large monorepo bundling many unrelated Intel product suites —
robotics, education, manufacturing, healthcare, etc. — as one repo).
**No Yocto layer exists for this suite anywhere** (checked the
`open-edge-platform` and `intel` GitHub orgs, the Yocto Project Layer
Index, and GitHub-wide code search); its own release engineering targets
Ubuntu/APT (`.deb`s built by Docker + per-component Makefiles). This is
greenfield recipe work, in a new `recipes-robotics-ai-suite/` directory
(plain `recipes-*`, not `dynamic-layers/`, since these are meta-oeros's
own new recipes with no third-party-layer-presence problem to gate on —
`COMPATIBLE_MACHINE` alone satisfies "only available for Intel machines").

**Recipes created with `recipetool create` + manual ROS-ification**, per
project convention: `recipetool` gets `SRC_URI`/`SRCREV`/a first-pass
license scan right, then each recipe was hand-corrected to use the
`ros_distro_${ROS_DISTRO}` / `ros_component` / `ros_${ROS_BUILD_TYPE}`
pattern (recipetool has no ROS-specific knowledge and defaults to plain
`inherit cmake`), with `DEPENDS`/`RDEPENDS` fixed to the real meta-ros
package names (recipetool's CMake `find_package()` guesses are
approximate) and `LICENSE`/`LIC_FILES_CHKSUM` cross-checked against each
component's actual `REUSE.toml`/`LICENSES/` directory.

**Why a separate Jazzy config.** The suite officially validates ROS 2
Jazzy Jalisco / Ubuntu 24.04; this whole distro's default is ROS 2
"lyrical" (newer than Jazzy in meta-ros's lineage — `navigation2` is
1.5.1 on lyrical vs. 1.3.13 on jazzy, a real version gap). Rather than
build the suite against an unverified newer ROS_DISTRO, this integration
forks to Jazzy — but `bitbake-setup` configs share ONE `bb-layers` list
across every machine option in a config file, so `meta-ros/meta-ros2-jazzy`
cannot simply be added alongside `meta-ros/meta-ros2-lyrical` in
`oeros-wrynose-lyrical.conf.json` without both ROS-distro package sets
(near-certainly colliding on recipe names like `navigation2`, `rclcpp`)
landing in the same `BBLAYERS` for every machine in that config, not just
Intel's. The fix is the new, separate `oeros-wrynose-jazzy.conf.json`,
scoped to just this Intel path — the first concrete instance of the
"Recommended new OE configuration templates" idea already in this file.
Its `oeros/machine-intel-corei7-64-robotics-ai-suite` fragment is also
where the `edgeai-robotics-ai-suite` `MACHINE_FEATURE` gets set (fragment-
level gating, same pattern as the Qualcomm QIR SDK above) — the existing
lyrical `machine-intel-corei7-64.conf` is untouched.

### Initial slice (verified against live upstream source, not just docs)

| Recipe | Upstream path | Status |
|---|---|---|
| `nav2-dynamic-msgs_0.0.1.bb` | `ros-navigation/navigation2_dynamic` (separate small repo, Apache-2.0, active) | Clean. Needed because `adbscan_ros2` depends on `nav2_dynamic_msgs`, which doesn't exist in meta-ros at all; its own deps (`sensor_msgs`, `std_msgs`, `unique_identifier_msgs`) are all present in `meta-ros2-jazzy`. |
| `yolo-msgs_1.0.0.bb` | `.../object-detection/yolov8/src/yolo_msgs` | Clean. |
| `yolo_1.0.0.bb` | `.../object-detection/yolov8/src/yolo` | **Required a real patch**: `CMakeLists.txt` unconditionally does `FetchContent_Declare/MakeAvailable(googletest)` from GitHub at configure time and unconditionally builds/runs a `tests` target against it — neither gated behind `BUILD_TESTING` despite the section header implying it should be. This is a live network fetch during `do_configure`, which fails under BitBake's offline build. Patched (`files/0001-tests-gate-FetchContent-googletest-behind-BUILD_TES.patch`, generated via real `git format-patch` — a hand-rolled pseudo-git-am header without `diff --git`/`index` lines fails silently with `quilt`'s "can't find file to patch", confusing since `patch -p1` alone accepts it fine) to wrap both blocks in `if(BUILD_TESTING)`, plus `EXTRA_OECMAKE += "-DBUILD_TESTING=OFF"`. Verified: `bitbake -c patch yolo` applies cleanly and the unpacked `CMakeLists.txt` shows the gated blocks. `DEPENDS` needed `openvino-inference-engine` (not `openvino` — `bitbake -e` caught this immediately with "Nothing PROVIDES"). Two vendored single-file headers (`toml.hpp` MIT, `CLI11.hpp` BSD-3-Clause) attributed in `LICENSE`/`LIC_FILES_CHKSUM` alongside the component's own Apache-2.0. |
| `adbscan-ros2_2.2.0.bb` | `.../adbscan/ROS2_node` | Clean once `nav2-dynamic-msgs` existed. Apache-2.0; upstream description literally says "Intel-patented" (patent status doesn't affect OSS licensing, just unusual phrasing to see in redistributed code). |
| `wandering-app_2.3.0.bb` | `.../wandering/wandering/wandering` | Clean — zero gaps, all deps (`nav2-msgs`, `nav2-util`, `nav2-costmap-2d`, etc.) individually confirmed present in `meta-ros2-jazzy`. Best recipe to validate the whole pattern with first. |
| `segmentation-realsense-tutorial_2.0.0.bb` | `.../object-detection/segmentation_realsense_tutorial` | Clean — launch/rviz/param bundle, no compiled code, zero `exec_depend`s at all in its `package.xml`. |
| `object-detection-tutorial_2.0.0.bb` | `.../object-detection/object_detection_tutorial` | **BLOCKED, real gap, not a bug in this recipe**: its `package.xml` declares `<exec_depend>openvino-node</exec_depend>`. This package does not exist anywhere in this distro's layers, **and it does not exist in the real upstream `intel/ros2_openvino_toolkit` repo either** (checked both its `master` and `ros2` branches — the actual packages there are `openvino_wrapper_lib`, `openvino_msgs`, `openvino_param_lib`, `openvino_people_msgs`; no `openvino_node`). This looks like a genuine naming-drift bug in Intel's own `package.xml`, not something resolvable by further searching — flagged here rather than guessed at. `bitbake -e object-detection-tutorial` fails with `Nothing RPROVIDES 'openvino-node'`, confirming the recipe faithfully reflects a currently-unsatisfiable real dependency. Isolated into its own `-tutorials` sub-package in `packagegroup-oeros-robotics-ai-suite` so this gap doesn't block `-perception`/`-navigation`. |

All seven verified individually with `bitbake -e <recipe>` (parses cleanly,
`DEPENDS`/`RDEPENDS` resolve) except the documented `object-detection-tutorial`
gap. **A related, previously-undetected pre-existing bug surfaced trying to
resolve the whole `packagegroup-oeros-edgeai-intel` for the first time**:
its existing (not new) `-runtime` sub-package RDEPENDS on
`openvino-model-optimizer`, which doesn't exist — modern OpenVINO (2025.x)
removed the standalone Model Optimizer tool in favor of `ovc` (visible in
`openvino-inference-engine`'s own `-python3` sub-package,
`${bindir}/ovc`). Its `-ros` sub-package has the same class of bug:
`ros2-openvino-toolkit` is the *repo name*, not any package within it.
**Both are out of scope for this pass** (pre-existing, unrelated to the
suite) — flagged here rather than silently fixed, since fixing them
properly means sourcing new recipes for `intel/ros2_openvino_toolkit`'s
real packages, a separate body of work.

**A note on `recipetool` and this specific monorepo**: `edge-ai-suites`
has `.gitmodules` at its root, so `recipetool` auto-upgrades every fetch
to `gitsm://` and recursively clones *all* submodules repo-wide —
including entirely unrelated products bundled in the same monorepo (e.g.
`education-ai-suite/ai-teaching-assistant/.../datumaro`,
`pipelines/gr00t-n1d7-ov/isaac-gr00t/.../LIBERO`, `.../ManiSkill2_real2sim`
several directories deep in an unrelated humanoid-robot-learning pipeline).
`--src-subdir` only scopes where `recipetool` *looks* for build files
afterward, not what it fetches. This made `object-detection-tutorial`'s
and `segmentation-realsense-tutorial`'s `recipetool create` runs balloon
to 10+ minutes fetching gigabytes of irrelevant ML framework submodules,
and one run had to be killed (own local subprocess only) after it started
cloning `LIBERO`; `segmentation-realsense-tutorial` was hand-written
instead once its trivial `package.xml` (zero dependencies) was already
known. Worth keeping in mind for any future component pulled from this
same monorepo.

### Tier B slice (added later, same convention)

`groundfloor`, `fast-mapping`, and `ros-kpi` — the three Tier B blockers
from the original planning pass — were resolved and added as recipes.
`its-planner` remains excluded (see below); its blocker is architectural,
not a verification gap, and needs an explicit decision before proceeding.

| Recipe | Upstream path | Status |
|---|---|---|
| `pointcloud-groundfloor-segmentation_2.1.0.bb` | `.../groundfloor/pointcloud_groundfloor_segmentation` | Its original Tier B blocker ("BSD-3-Clause file not reflected in `REUSE.toml`") was wrong — the file is `cmake/CodeCoverage.cmake`, the well-known `bilke/cmake-modules` (Lars Bilke) file, confirmed via GitHub code search; attributed in `LIC_FILES_CHKSUM` as build-tooling-only, not linked into any shipped binary. **Real, more interesting blocker found instead**: `package.xml`'s `<exec_depend>nav2_bringup</exec_depend>` is omitted from `ROS_EXEC_DEPENDS` — not because it's unavailable for Jazzy (an earlier investigation pass here wrongly concluded that, from an unscoped `find \| head -1` across all `meta-ros2-<distro>` sibling dirs that silently matched a different distro's recipe; corrected after direct user challenge — every one of `nav2-bringup`'s deps genuinely exists under `meta-ros2-jazzy`), but because BitBake **skips** `nav2-bringup` outright: it transitively RDEPENDS on `slam-toolbox` → `rviz-common` → `rviz-ogre-vendor`, whose own `package.xml` directly requires `libx11`/`libxaw`/`libxrandr` (Ogre's GLX render-window/input backend calling Xlib APIs directly — not a generic "needs *a* display server" stand-in). Those X11 lib recipes `inherit features_check` with `REQUIRED_DISTRO_FEATURES = "x11"` (a hard AND-gate via the shared `xorg-lib-common.inc`), and `oeros` (`conf/distro/oeros.conf`) is deliberately Wayland-only (`wayland opengl vulkan polkit`, no `x11`). Adding `wayland` would **not** fix this — unlike `vulkan-tools` (`ANY_OF_DISTRO_FEATURES = "x11 wayland"`, since Vulkan surface creation genuinely supports either), this vendored Ogre build has no Wayland/EGL path wired in; it's X11-only code, not an either-protocol choice. **This is a distro-architecture incompatibility that will affect any future RViz-dependent ROS 2 package on `oeros`, not just this one** — worth remembering broadly, not just here. Verified clean via `bitbake -e` after excluding `nav2-bringup`. |
| `fast-mapping_2.3.0.bb` | `.../fast-mapping` | Both original Tier B blockers resolved: `safestringlib` is documented in upstream's `dependencies/Safestringlib` but `CMakeLists.txt` never actually `find_package()`s it — omitted from `DEPENDS`, not silently added. Its `BSD-3-Clause` (`src/fast_mapping_lib/se/**`, Copyright Emanuele Vespa/Imperial College London) *is* properly annotated in the component's own `REUSE.toml` — the earlier "not reflected in REUSE.toml" blocker description was itself imprecise. **A real `do_configure` failure surfaced on the first actual compile attempt**, not caught by `bitbake -e`'s dependency-resolution check alone: `CMakeLists.txt` calls `find_package(ament_cmake_gtest REQUIRED)` and `find_package(ament_lint_auto REQUIRED)` completely unconditionally at top level (not gated behind `if(BUILD_TESTING)` like a well-formed ament_cmake package — a real upstream CMakeLists.txt bug), so `-DBUILD_TESTING=OFF` didn't prevent the failure; fixed by adding `ament-lint-auto-native`/`ament-cmake-gtest-native` to `ROS_BUILDTOOL_DEPENDS` despite `ROS_TEST_DEPENDS` normally being "informational only" in this layer's convention. Also found `rosbag2-cpp` and `std-srvs` are real, unconditional link-time dependencies of `fast_mapping_node` (found and `ament_target_dependencies()`'d outside any `BUILD_TESTING` guard) that are simply missing from `package.xml`'s own `build_depend`/`exec_depend` list — the reverse of the usual gap, a real package.xml/CMakeLists.txt inconsistency in upstream, not a packaging mistake here; added to `ROS_BUILD_DEPENDS`/`ROS_EXEC_DEPENDS`. (`rosbag2-transport`/`rosbag2-storage` genuinely are test-only and correctly `BUILD_TESTING`-gated — not added.) Verified via `bitbake -e` and a full `bitbake -c compile` (succeeded after these fixes). |
| `ros-kpi_0.2.6.bb` | `.../ros-kpi` | Different recipe idiom confirmed and handled: `python_hatchling` (git-fetched monorepo subdirectory, not a standalone PyPI package, so no `pypi` bbclass). **Real, unresolved scoping question flagged in the recipe's own `DESCRIPTION`**: this package's `pyproject.toml` mixes on-target ROS 2 topic monitors/exporters (`rclpy`-based, plausibly meant to run on the robot) with clearly desktop-oriented tools (`matplotlib` GUI popups, `Tkinter`, `openpyxl` Excel report generation) and a `docker-compose`-based Grafana/Prometheus bundle (packaged as wheel data). Packaged as upstream defines it — whether the desktop pieces belong on an embedded robot image vs. a developer workstation companion tool is not decided here. Needed one new supporting recipe, `python3-prometheus-client_0.26.0.bb` (standard PyPI package, didn't exist in any layer; its own license is `Apache-2.0 & BSD-2-Clause` since it bundles `decorator` 4.0.10 in-tree, per its own `NOTICE`). Also caught a wrong OE package-name assumption in upstream's own code comment (`python3-tk`, which doesn't exist in OE) — the real name is `python3-tkinter`, added as `RRECOMMENDS` since it's only needed for interactive graph-click popups. Verified via `bitbake -e` and a full `bitbake -c compile` (succeeded, including the non-default `packages = ["src"]` hatchling layout). |

### Explicitly out of scope (documented, not silently dropped)

- **Tier B remaining**: `its-planner` — vendors its own `navigation2` as a
  git submodule instead of using the distro's, an architecturally
  different and higher-risk case than the three resolved above; needs an
  explicit decision (vendor vs. distro `navigation2`) before starting.
- **Tier C** (not evaluated this pass): `collaborative-slam` (bundles
  third-party SLAM libraries directly, higher licensing-audit burden),
  `multicam-demo` (Docker-only, no ROS/CMake packaging exists to
  convert), `simulations` (Gazebo tutorials, not a deployable runtime
  component), `robot-vision-control` (Docker/devcontainer-only,
  pre-release, no `package.xml`/`CMakeLists.txt` anywhere), and all 15
  `pipelines/*` dirs (`act-sample`, `diffusion-policy-ov`,
  `fast-lio2-demo`, `fast-livo2-demo`, `gr00t-n1d7-ov`, `gr00t-wbc`,
  `idp3-ov`, `llm-robotics-demo`, `mpc-demo`, `openclaw-agenticros-demo`,
  `orb-slam3-sample`, `pi05-rtc-ov`, `point-lio-demo`, `rdt-ov`,
  `vla-pi0.5-openvino` — vendor large third-party ML/SLAM/robot-learning
  repos as submodules, GPLv3 ORB-SLAM3 among them, none licensed or
  evaluated here).
- Intel oneAPI Base Toolkit: no existing Yocto layer provides it, and the
  initial-slice components don't need it (`yolo` uses plain
  `find_package(OpenVINO)`/`find_package(OpenCV)`, no oneAPI compiler) —
  not added as a new layer dependency.

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
- (this branch) `object-detection-tutorial`'s `openvino-node` dependency
  doesn't exist anywhere, including in the real upstream
  `intel/ros2_openvino_toolkit` project — see the Intel Robotics AI Suite
  section for the full investigation.
- (this branch) `packagegroup-oeros-edgeai-intel`'s pre-existing `-runtime`
  and `-ros` sub-packages RDEPEND on `openvino-model-optimizer` (removed
  from modern OpenVINO in favor of `ovc`) and `ros2-openvino-toolkit`
  (a repo name, not a package) respectively — both confirmed broken while
  verifying this branch's new `-robotics-ai-suite` addition, both
  pre-existing and out of scope to fix here.
- `bitbake-setup`'s `DL_DIR`/`SSTATE_DIR` `bb-env-passthrough-additions`
  only take effect if those env vars are exported in the shell that
  actually runs `bitbake` (not just at `bitbake-setup init` time) —
  otherwise `site.conf`'s `?=` defaults win and everything lands under
  `bitbake-builds/.bitbake-setup-downloads`/`.sstate-cache` instead of the
  shared `/opt/yocto/downloads`/`/opt/yocto/sstate-cache`. Set them
  directly in `site.conf` instead of relying on env export per-invocation.
- `recipetool create` against a monorepo with `.gitmodules` at its root
  (like `edge-ai-suites`) will recursively clone *every* submodule
  repo-wide, not just ones under `--src-subdir` — budget for this or
  hand-write trivially-simple recipes instead once the real
  `package.xml`/`CMakeLists.txt` content is already known.

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
