# Ported from Xilinx/meta-kria-apps (main, LAYERSERIES_COMPAT scarthgap; MIT,
# Copyright (C) 2026 Advanced Micro Devices, Inc.) to wrynose / Oeros:
#   - S moved from ${WORKDIR}/git to ${UNPACKDIR}/${BP} (wrynose unpack layout)
#   - ros_distro_jazzy -> ros_distro_${ROS_DISTRO} (Oeros builds lyrical)
#   - velocity-controllers dropped from ROS_EXEC_DEPENDS: ros2_controllers no longer ships it on lyrical (no meta-ros2-lyrical recipe); forward-command-controller covers velocity commands
#   - COMPATIBLE_MACHINE limited to the Kria SOMs

SUMMARY = "ROS 2 CANopen client for the Kria FOC motor-control server"
DESCRIPTION = "Builds the kria_motor_control ament package from \
foc-motor-ctrl/test/ros2_canopen. The package provides the ros2_control / \
ros2_canopen configuration and launch files used on KR260 to drive the FOC \
motor server running on KD240 over CAN."
SECTION = "devel"

# Pulls in HOMEPAGE, LICENSE, SRC_URI, SRCREV so this recipe and
# foc-motor-ctrl.bb stay in lockstep against the same SRCREV.
require foc-motor-ctrl-src.inc

PV = "0.6+git"
COMPATIBLE_MACHINE = "^$"
COMPATIBLE_MACHINE:kria = "${MACHINE}"

# S points two levels into the upstream tree (test/ros2_canopen),
# so the LICENSE file lives at ../../LICENSE relative to S.
LIC_FILES_CHKSUM = "file://../../LICENSE;md5=370aacba9d9e1a362c89a2b36dc0608d"

inherit ros_distro_${ROS_DISTRO}
inherit ros_component

ROS_BPN = "kria_motor_control"

ROS_BUILD_DEPENDS = " \
    canopen \
    lely-core-libraries \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = ""
ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    canopen-402-driver \
    canopen-core \
    canopen-master-driver \
    canopen-ros2-control \
    canopen-ros2-controllers \
    controller-manager \
    forward-command-controller \
    joint-state-broadcaster \
    joint-trajectory-controller \
    robot-state-publisher \
    xacro \
"

DEPENDS = " \
    ${ROS_BUILD_DEPENDS} \
    ${ROS_BUILDTOOL_DEPENDS} \
    ${ROS_BUILDTOOL_EXPORT_DEPENDS} \
    ${ROS_EXPORT_DEPENDS} \
    lely-core-libraries-native \
    lely-core-native \
"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

S = "${UNPACKDIR}/${BP}/test/ros2_canopen"

ROS_BUILD_TYPE = "ament_cmake"
inherit ros_${ROS_BUILD_TYPE}

do_compile:prepend() {
    export PATH="${RECIPE_SYSROOT_NATIVE}${ros_bindir}:$PATH"
}

# config/**/foc-mc.eds is shipped as a relative symlink pointing outside the
# ament install tree (../../../../apps/foc-mc.eds). After install the target
# does not exist, leaving dangling symlinks. Replace each link with the real
# EDS file from the source tree.
do_install:append() {
    for f in $(find ${D}${ros_datadir}/${ROS_BPN}/config -name "foc-mc.eds" -type l 2>/dev/null); do
        rm -f "$f"
        install -m 0644 ${UNPACKDIR}/${BP}/apps/foc-mc.eds "$f"
    done
}
