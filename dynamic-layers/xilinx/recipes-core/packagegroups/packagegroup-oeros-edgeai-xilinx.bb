SUMMARY = "AMD/Xilinx Kria + Versal: XRT/zocl, DFX, AI Engine, ros2_control, TSN"
DESCRIPTION = "Edge AI / robotics stack for AMD adaptive SoCs on wrynose \
(meta-xilinx, meta-kria, meta-amd-adaptive-socs rel-v2026.2). Open source \
by default; proprietary firmware/drivers and Vitis AI are opt-in via \
MACHINE_FEATURES (see docs/amd-xilinx.md)."
LICENSE = "MIT"
inherit packagegroup
PACKAGE_ARCH = "${MACHINE_ARCH}"
# zynqmp covers the Kria K26/K24 SOMs; versal covers VEK280 (and
# versal-2ve-2vm, i.e. VEK385, matches the same substring)
COMPATIBLE_MACHINE = "(zynqmp|versal)"

PACKAGES = " \
    ${PN} ${PN}-runtime ${PN}-pipeline ${PN}-ros ${PN}-industrial \
    ${PN}-kria ${PN}-openamp ${PN}-proprietary ${PN}-vitis-ai ${PN}-dev \
"
RDEPENDS:${PN} = " \
    ${PN}-runtime ${PN}-pipeline ${PN}-ros ${PN}-industrial \
    ${@'${PN}-kria' if 'kria' in d.getVar('MACHINEOVERRIDES').split(':') else ''} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'edgeai-openamp', '${PN}-openamp', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'edgeai-amd-proprietary', '${PN}-proprietary', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'edgeai-vitis-ai', '${PN}-vitis-ai', '', d)} \
"

# All open source (meta-xilinx-core). XRT + the zocl kernel driver are the
# runtime for anything loaded into the PL (HLS kernels, DPU-style IP);
# dfx-mgr handles partial-reconfiguration "accelerator app" loading;
# libxaiengine/aiefal drive the Versal AI Engine array.
# packagegroup-vitis-aiml (meta-vitis) is AMD's own list of the open-source
# libraries Vitis AI applications build against (fmt, glog, hdf5, spdlog,
# pybind11, ...): no proprietary content.
RDEPENDS:${PN}-runtime = " \
    xrt \
    kernel-module-zocl \
    dfx-mgr \
    fpga-manager-script \
    ${@bb.utils.contains('MACHINE_FEATURES', 'edgeai-aie', 'ai-engine-driver aiefal', '', d)} \
    ${@bb.utils.contains('BBFILE_COLLECTIONS', 'vitis-ai', 'packagegroup-vitis-aiml', '', d)} \
"

# AMD's GStreamer 1.28 fork (meta-xilinx-multimedia) + base/good/bad plugins.
# The VCU hardware codec plugins are proprietary-firmware-backed and live in
# -proprietary instead.
RDEPENDS:${PN}-pipeline = "packagegroup-xilinx-gstreamer"

# KR260 (robotics) and KD240 (motor drives) are ros2_control targets; the
# hardware_interface/controller_manager pair is what a PL motor controller
# or an EtherCAT/CAN drive plugs into. Names checked against
# meta-ros2-lyrical/generated-recipes/ros2-control{,lers}.
RDEPENDS:${PN}-ros = " \
    ros2-control \
    controller-manager \
    controller-interface \
    hardware-interface \
    transmission-interface \
    ros2-controllers \
    ros2controlcli \
"

# Deterministic networking and fieldbus for industrial/automotive robots.
# packagegroup-tsn (meta-xilinx-tsn: gPTP, tsn-utils, openlldp) only when
# that layer is configured.
RDEPENDS:${PN}-industrial = " \
    linuxptp \
    can-utils \
    ${@bb.utils.contains('BBFILE_COLLECTIONS', 'xilinx-tsn', 'packagegroup-tsn', '', d)} \
"

# Kria SOM utilities from meta-kria-core, all Apache-2.0/MIT, plus Oeros'
# port of the Kria robotics apps (dynamic-layers/xilinx/recipes-kria-apps/, packagegroup-oeros-kria-apps): xmutil (load
# and unload accelerator apps), platform stats, AXI/DDR QoS tuning.
# packagegroup-kria itself is NOT used: it RDEPENDS on the proprietary
# k26-/k24-starter-kits firmware (those go in -proprietary instead) and on
# packagegroup-security-tpm2 from meta-security.
# Set only under the 'kria' MACHINEOVERRIDE: xmutil is COMPATIBLE_MACHINE
# zynqmp/versal/versal-net only, so listing it unconditionally makes the
# whole packagegroup unbuildable on VEK385 (versal-2ve-2vm).
# (MACHINEOVERRIDES is ':'-separated, so bb.utils.contains() can't test it.)
RDEPENDS:${PN}-kria = ""
RDEPENDS:${PN}-kria:kria = " \
    packagegroup-oeros-kria-apps \
    xmutil \
    xlnx-platformstats \
    axi-qos \
    ddr-qos \
"

# AMP/multidomain machines only (MACHINE_FEATURE edgeai-openamp, set by the
# oeros/machine-*-multidomain fragments along with DISTRO_FEATURES openamp).
# packagegroup-openamp (meta-xilinx-core, openamp-layer dynamic layer):
# libmetal, open-amp, rpmsg char/ctrl modules and the echo/matrix/proxy
# demos. On KR260/KV260 also the prebuilt Cortex-R5 Zephyr OpenAMP demo
# firmware from meta-kria (BSD-3-Clause). AMD ships equivalent VEK280/VEK385
# firmware recipes only for the EDF "common" machines
# (amd-cortexa72-common, amd-cortexa78-*-common), so none is added there.
RDEPENDS:${PN}-openamp = "${@bb.utils.contains('MACHINE_FEATURES', 'edgeai-openamp', 'packagegroup-openamp', '', d)}"
RDEPENDS:${PN}-openamp:append:k26-smk-kr-sdt-multidomain = " k26-smk-kr-sdt-multidomain-openamp-fw-examples"
RDEPENDS:${PN}-openamp:append:k26-smk-kv-sdt-multidomain = " k26-smk-kv-sdt-multidomain-openamp-fw-examples"

# Opt-in only (MACHINE_FEATURE edgeai-amd-proprietary, set by the
# oeros/amd-proprietary fragment together with the matching
# LICENSE_FLAGS_ACCEPTED and DISTRO_FEATURES "libmali").
#  - vcu  (K26 / KV260 / KR260): H.264/H.265 VCU, firmware is "Proprietary"
#  - vcu2 (VEK385): VCU2, firmware is "Proprietary"
#  - kria: k26-/k24-starter-kits PL firmware (Proprietary). Listed directly
#    rather than via packagegroup-kria, which also RDEPENDS on
#    packagegroup-security-tpm2 (meta-security, not in this config).
# Like -vitis-ai, the whole list is gated (not just ${PN}'s RDEPENDS on
# it): gstreamer1.0-omx is skipped without 'commercial' in
# LICENSE_FLAGS_ACCEPTED, which would otherwise break the packagegroup.
OEROS_XILINX_PROPRIETARY = " \
    ${@bb.utils.contains('MACHINE_FEATURES', 'vcu', 'kernel-module-vcu vcu-firmware libvcu-omxil gst-plugins-vcu gstreamer1.0-omx', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'vcu2', 'kernel-module-vcu2 vcu2-firmware libvcu2-omxil gst-plugins-vcu2', '', d)} \
    ${@'k26-starter-kits' if 'k26-smk' in d.getVar('MACHINEOVERRIDES').split(':') else ''} \
    ${@'k24-starter-kits' if 'k24-smk' in d.getVar('MACHINEOVERRIDES').split(':') else ''} \
"
RDEPENDS:${PN}-proprietary = "${@bb.utils.contains('MACHINE_FEATURES', 'edgeai-amd-proprietary', d.getVar('OEROS_XILINX_PROPRIETARY'), '', d)}"

# Opt-in only (MACHINE_FEATURE edgeai-vitis-ai), and NOT satisfiable from
# any wrynose layer today: VART / VART-ML / VART-X ship only as
# LICENSE = "CLOSED" recipes inside amd/Vitis-AI 6.3's per-board reference
# designs (versal_ve/.../vek280, versal_2ve/.../vek385), whose meta-vitis-ai
# layer declares LAYERSERIES_COMPAT = "scarthgap". Kria/ZynqMP DPU is not
# supported by Vitis AI 6.x at all. Kept as a documented hook so a user who
# ports that layer can switch it on without editing this recipe.
# The RDEPENDS themselves must be conditional too: bitbake resolves every
# sub-package of a packagegroup, so an unconditional list would make the
# whole recipe unbuildable ("Nothing RPROVIDES 'vart-ml'").
RDEPENDS:${PN}-vitis-ai = "${@bb.utils.contains('MACHINE_FEATURES', 'edgeai-vitis-ai', 'vart vart-ml vart-x', '', d)}"

RDEPENDS:${PN}-dev = " \
    xrt-dev \
    opencl-headers-dev \
    opencl-clhpp-dev \
    ${@bb.utils.contains('BBFILE_COLLECTIONS', 'vitis-ai', 'packagegroup-vitis-aiml-dev', '', d)} \
"
