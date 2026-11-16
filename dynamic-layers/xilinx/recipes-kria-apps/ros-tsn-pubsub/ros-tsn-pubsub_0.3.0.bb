# Ported from Xilinx/meta-kria-apps (main, LAYERSERIES_COMPAT scarthgap; MIT,
# Copyright (C) 2026 Advanced Micro Devices, Inc.) to wrynose / Oeros:
#   - S moved from ${WORKDIR}/git to ${UNPACKDIR}/${BP} (wrynose unpack layout)
#   - ros_distro_jazzy -> ros_distro_${ROS_DISTRO} (Oeros builds lyrical)
#   - COMPATIBLE_MACHINE limited to the Kria SOMs (KR260/KD240 TSN demo)

SUMMARY = "ROS 2 publisher/subscriber example for the TSN multi-node demo"
DESCRIPTION = "Builds the xlnx-pubsub ament package from Xilinx/ros-tsn-pubsub. \
Provides the talker (pub-control) and listener (sub-all) executables used to \
exercise the ROS 2 multi-node-communication-via-TSN application on Kria \
KR260/KD240, the FastRTPS DDS talker/listener profiles and the \
start_ros_test.sh launch helper."
SECTION = "apps"

HOMEPAGE = "https://github.com/Xilinx/ros-tsn-pubsub"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=41bcdd6af856cc34f13217eb319564a3"

SRC_BRANCH = "main"
SRC_URI = "git://github.com/Xilinx/ros-tsn-pubsub.git;branch=${SRC_BRANCH};protocol=https"
# v0.3 release (commit 898ab27). SRCREV is pinned to the immutable commit
# for reproducibility; the tag is noted here for readability.
SRCREV = "898ab27af754b0c9c6c0e1b814c826ce99d5a009"

PV = "0.3.0+git"
COMPATIBLE_MACHINE = "^$"
COMPATIBLE_MACHINE:kria = "${MACHINE}"

S = "${UNPACKDIR}/${BP}"

inherit ros_distro_${ROS_DISTRO}
inherit ros_component

ROS_BPN = "xlnx-pubsub"

ROS_BUILD_DEPENDS = " \
    rclcpp \
    std-msgs \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = ""
ROS_BUILDTOOL_EXPORT_DEPENDS = ""

# start_ros_test.sh drives `ros2 run xlnx-pubsub {talker,listener}`, so the
# ros2 CLI run verb has to be on the target alongside the executables.
# ros-workspace ships the top-level ROS 2 environment (/opt/ros/${ROS_DISTRO}/
# setup.bash) that the operator sources before launching the app; it is not
# pulled in transitively by rclcpp/ros2run, so pull it explicitly here.
ROS_EXEC_DEPENDS = " \
    rclcpp \
    ros2run \
    ros-workspace \
    std-msgs \
"

DEPENDS = " \
    ${ROS_BUILD_DEPENDS} \
    ${ROS_BUILDTOOL_DEPENDS} \
    ${ROS_BUILDTOOL_EXPORT_DEPENDS} \
    ${ROS_EXPORT_DEPENDS} \
"

RDEPENDS:${PN} += "bash ${ROS_EXEC_DEPENDS}"

ROS_BUILD_TYPE = "ament_cmake"
inherit ros_${ROS_BUILD_TYPE}

# The example CMakeLists installs start_ros_test.sh under SCRIPT_PREFIX
# (/opt/xilinx/ros-tsn-example/bin), which sits outside ${ros_prefix}, so the
# base ros_component FILES do not cover it. Package it explicitly.
FILES:${PN} += "/opt/xilinx/ros-tsn-example"
