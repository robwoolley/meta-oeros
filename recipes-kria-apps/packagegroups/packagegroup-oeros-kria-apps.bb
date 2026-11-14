SUMMARY = "AMD Kria robotics apps: ROS 2 TSN pub/sub, FOC motor control, ros2_canopen client"
DESCRIPTION = "Oeros port of the ROS 2 and robotics applications from \
Xilinx/meta-kria-apps (scarthgap) for KR260 and KD240. See docs/amd-xilinx.md, \
'AMD Kria robotics apps'."
LICENSE = "MIT"
inherit packagegroup
PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "^$"
COMPATIBLE_MACHINE:kria = "${MACHINE}"

PACKAGES = "${PN} ${PN}-tsn ${PN}-motor-control ${PN}-motor-control-client ${PN}-proprietary"
RDEPENDS:${PN} = " \
    ${PN}-tsn \
    ${PN}-motor-control \
    ${PN}-motor-control-client \
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

# Opt-in only (oeros/amd-proprietary): the PL firmware (bitstream + device
# tree overlay) these apps load through dfx-mgr / xmutil. Licensed
# "Advanced-Micro-Devices-Xilinx-binaries". Without it the apps install
# but have no PL design to talk to. Gated here too because bitbake resolves
# every sub-package of a packagegroup.
OEROS_KRIA_APPS_FW = ""
OEROS_KRIA_APPS_FW:k24-smk = "kd240-motor-ctrl-qei-fw"
OEROS_KRIA_APPS_FW:k26-smk-kr-sdt = "kr260-tsn-rs485pmod-fw"
OEROS_KRIA_APPS_FW:k26-smk-kr-sdt-multidomain = "kr260-tsn-rs485pmod-fw"
RDEPENDS:${PN}-proprietary = "${@bb.utils.contains('MACHINE_FEATURES', 'edgeai-amd-proprietary', d.getVar('OEROS_KRIA_APPS_FW'), '', d)}"
