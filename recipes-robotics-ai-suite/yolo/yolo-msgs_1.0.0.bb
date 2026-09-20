SUMMARY = "ROS 2 message definitions for the Intel Robotics AI Suite YOLOv8 node"
HOMEPAGE = "https://github.com/open-edge-platform/edge-ai-suites/tree/main/robotics-ai-suite/components/object-detection"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://../../../LICENSES/Apache-2.0.txt;md5=c846ebb396f8b174b10ded4771514fcc"

SRC_URI = "git://github.com/open-edge-platform/edge-ai-suites.git;protocol=https;branch=main"
SRCREV = "e67ecbab6ccce6ca89675b22d784bdb8b430ae3e"
PV = "1.0.0+git"

S = "${UNPACKDIR}/${BP}/robotics-ai-suite/components/object-detection/yolov8/src/yolo_msgs"

inherit ros_distro_${ROS_DISTRO} pkgconfig
inherit ros_component

ROS_CN = "yolo_msgs"
ROS_BPN = "yolo_msgs"

ROS_BUILDTOOL_DEPENDS = "ament-cmake-native rosidl-default-generators-native"
ROS_BUILD_DEPENDS = "geometry-msgs sensor-msgs std-msgs tf2-msgs"
ROS_EXEC_DEPENDS = "geometry-msgs sensor-msgs std-msgs tf2-msgs"
ROS_TEST_DEPENDS = "ament-lint-auto ament-lint-common"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

ROS_BUILD_TYPE = "ament_cmake"
inherit ros_${ROS_BUILD_TYPE}
