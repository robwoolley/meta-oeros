SUMMARY = "AMD Kria robotics apps: ROS 2 TSN pub/sub, FOC motor control, ros2_canopen client, KV260 camera"
DESCRIPTION = "Oeros port of the ROS 2 and robotics applications from \
Xilinx/meta-kria-apps (scarthgap) for KR260 and KD240. See docs/amd-xilinx.md, \
'AMD Kria robotics apps'."
LICENSE = "MIT"
inherit packagegroup
PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "^$"
COMPATIBLE_MACHINE:kria = "${MACHINE}"

PACKAGES = "${PN} ${PN}-tsn ${PN}-motor-control ${PN}-motor-control-client ${PN}-camera ${PN}-proprietary"
RDEPENDS:${PN} = " \
    ${PN}-tsn \
    ${PN}-motor-control \
    ${PN}-motor-control-client \
    ${PN}-camera \
    ${@bb.utils.contains('MACHINE_FEATURES', 'edgeai-amd-proprietary', '${PN}-proprietary', '', d)} \
"

# ROS 2 multi-node communication over TSN (KR260 and KD240): talker/listener
# with FastRTPS profiles, plus the TSN/gPTP tools it is deployed with.
RDEPENDS:${PN}-tsn = " \
    ros-tsn-pubsub \
    ${@bb.utils.contains('BBFILE_COLLECTIONS', 'xilinx-tsn', 'packagegroup-tsn', '', d)} \
    ethtool \
"

# KD240: field-oriented-control motor server (CANopen) + dashboard and the
# out-of-tree IIO drivers for the motor-control PL IP. KR260 doesn't carry
# the motor-control PL, so this is K24 starter kit only.
RDEPENDS:${PN}-motor-control = ""
RDEPENDS:${PN}-motor-control:k24-smk = " \
    foc-motor-ctrl \
    foc-motor-ctrl-server \
    foc-motor-ctrl-dashboard \
    motor-control-iio-modules \
"

# KR260 (MACHINE-name overrides: there is no KR-only override in meta-kria): the ros2_canopen / ros2_control client that drives a KD240 motor
# server over CAN.
RDEPENDS:${PN}-motor-control-client = ""
RDEPENDS:${PN}-motor-control-client:k26-smk-kr-sdt = "kria-motor-control"
RDEPENDS:${PN}-motor-control-client:k26-smk-kr-sdt-multidomain = "kria-motor-control"

# KV260 camera bring-up (AR1335 13 MP sensor behind the onsemi AP1302 ISP on
# the KV260 carrier, MIPI CSI-2 into the PL). Open part: the out-of-tree
# AR1335 V4L2 subdev driver and the V4L2 user-space tools; the ROS 2 side
# (v4l2-camera, gscam, image-pipeline) is already in
# packagegroup-oeros-edgeai-ros. The capture pipeline itself needs the
# kv260-bist PL design and the AP1302 ISP firmware, which are proprietary
# and come with the opt-in below (kv260-bist-fw also pulls the in-tree
# kernel-module-ap1302 and kernel-module-xlnx-vcu).
RDEPENDS:${PN}-camera = ""
RDEPENDS:${PN}-camera:k26-smk-kv-sdt = "ar1335-module v4l-utils media-ctl yavta"
RDEPENDS:${PN}-camera:k26-smk-kv-sdt-multidomain = "ar1335-module v4l-utils media-ctl yavta"

# Opt-in only (oeros/amd-proprietary): the PL firmware (bitstream + device
# tree overlay) these apps load through dfx-mgr / xmutil. Licensed
# "Advanced-Micro-Devices-Xilinx-binaries". Without it the apps install
# but have no PL design to talk to. Gated here too because bitbake resolves
# every sub-package of a packagegroup.
OEROS_KRIA_APPS_FW = ""
OEROS_KRIA_APPS_FW:k24-smk = "kd240-motor-ctrl-qei-fw"
OEROS_KRIA_APPS_FW:k26-smk-kr-sdt = "kr260-tsn-rs485pmod-fw"
OEROS_KRIA_APPS_FW:k26-smk-kr-sdt-multidomain = "kr260-tsn-rs485pmod-fw"
OEROS_KRIA_APPS_FW:k26-smk-kv-sdt = "kv260-bist-fw ap1302-firmware"
OEROS_KRIA_APPS_FW:k26-smk-kv-sdt-multidomain = "kv260-bist-fw ap1302-firmware"
RDEPENDS:${PN}-proprietary = "${@bb.utils.contains('MACHINE_FEATURES', 'edgeai-amd-proprietary', d.getVar('OEROS_KRIA_APPS_FW'), '', d)}"
