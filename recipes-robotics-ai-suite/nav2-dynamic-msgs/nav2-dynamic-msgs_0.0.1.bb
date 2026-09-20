SUMMARY = "Msg and srv for nav2_dynamic_obstacle"
DESCRIPTION = "ROS 2 message/service definitions for Nav2's dynamic obstacle \
detection, tracking, and processing pipelines. Needed by adbscan-ros2, which \
is not otherwise available in meta-ros."
HOMEPAGE = "https://github.com/ros-navigation/navigation2_dynamic"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

SRC_URI = "git://github.com/ros-navigation/navigation2_dynamic.git;protocol=https;branch=master"
SRCREV = "e1b0d920fa309c7eb072524b00de7ee12e21076e"
PV = "0.0.1+git"

S = "${UNPACKDIR}/${BP}/nav2_dynamic_msgs"

inherit ros_distro_${ROS_DISTRO} pkgconfig
inherit ros_component

ROS_CN = "nav2_dynamic_msgs"
ROS_BPN = "nav2_dynamic_msgs"

ROS_BUILDTOOL_DEPENDS = "ament-cmake-native rosidl-default-generators-native"
ROS_BUILD_DEPENDS = "sensor-msgs std-msgs unique-identifier-msgs"
ROS_EXEC_DEPENDS = "sensor-msgs std-msgs unique-identifier-msgs"
ROS_TEST_DEPENDS = "ament-lint-auto ament-lint-common"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

ROS_BUILD_TYPE = "ament_cmake"
inherit ros_${ROS_BUILD_TYPE}

