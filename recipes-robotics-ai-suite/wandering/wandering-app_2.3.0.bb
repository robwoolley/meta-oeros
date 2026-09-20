SUMMARY = "Wandering application"
DESCRIPTION = "Nav2-based autonomous wandering demo, part of the Intel \
Robotics AI Suite -- validates the Nav2 dependency chain end to end."
HOMEPAGE = "https://github.com/open-edge-platform/edge-ai-suites/tree/main/robotics-ai-suite/components/wandering"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://../LICENSES/Apache-2.0.txt;md5=c846ebb396f8b174b10ded4771514fcc"

SRC_URI = "git://github.com/open-edge-platform/edge-ai-suites.git;protocol=https;branch=main"
SRCREV = "e67ecbab6ccce6ca89675b22d784bdb8b430ae3e"
PV = "2.3.0+git"

S = "${UNPACKDIR}/${BP}/robotics-ai-suite/components/wandering/wandering"

inherit ros_distro_${ROS_DISTRO} pkgconfig
inherit ros_component

ROS_CN = "wandering_app"
ROS_BPN = "wandering_app"

ROS_BUILDTOOL_DEPENDS = "ament-cmake-native"
ROS_BUILD_DEPENDS = "nav2-msgs nav2-util geometry-msgs rclcpp-action action-msgs nav2-costmap-2d ament-index-cpp rclcpp std-msgs nav-msgs"
ROS_EXEC_DEPENDS = "nav2-msgs nav2-util geometry-msgs rclcpp-action action-msgs nav2-costmap-2d ament-index-cpp rclcpp std-msgs"
ROS_TEST_DEPENDS = "ament-lint-auto ament-lint-common ament-cmake-gtest"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

EXTRA_OECMAKE += "-DBUILD_TESTING=OFF"

ROS_BUILD_TYPE = "ament_cmake"
inherit ros_${ROS_BUILD_TYPE}
