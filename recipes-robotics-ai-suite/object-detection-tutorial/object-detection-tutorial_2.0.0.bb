SUMMARY = "Tutorial for Object detection using OpenVINO"
DESCRIPTION = "Launch/rviz/param bundle demonstrating the yolo OpenVINO ROS 2 \
node, part of the Intel Robotics AI Suite. No compiled code."
HOMEPAGE = "https://github.com/open-edge-platform/edge-ai-suites/tree/main/robotics-ai-suite/components/object-detection"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://../LICENSES/Apache-2.0.txt;md5=c846ebb396f8b174b10ded4771514fcc"

SRC_URI = "git://github.com/open-edge-platform/edge-ai-suites.git;protocol=https;branch=main"
SRCREV = "e67ecbab6ccce6ca89675b22d784bdb8b430ae3e"
PV = "2.0.0+git"

S = "${UNPACKDIR}/${BP}/robotics-ai-suite/components/object-detection/object_detection_tutorial"

inherit ros_distro_${ROS_DISTRO} pkgconfig
inherit ros_component

ROS_CN = "object_detection_tutorial"
ROS_BPN = "object_detection_tutorial"

ROS_BUILDTOOL_DEPENDS = "ament-cmake-native"
# openvino-node: NOT verified to exist anywhere in this distro's layers (no
# ros2-openvino-toolkit/openvino_node package found in meta-ros or
# meta-openvino as of this pass). packagegroup-oeros-edgeai-intel.bb's own
# -ros sub-package RDEPENDS on "ros2-openvino-toolkit" -- same unverified
# assumption, now confirmed absent. Left in ROS_EXEC_DEPENDS so this is a
# real, resolvable-later gap rather than silently dropped; expect a missing
# -provides error until that package is sourced.
ROS_EXEC_DEPENDS = "launch launch-ros openvino-node ament-index-python python3-pyyaml"
ROS_TEST_DEPENDS = "ament-lint-auto ament-lint-common"

DEPENDS = "${ROS_BUILDTOOL_DEPENDS}"
RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

EXTRA_OECMAKE += "-DBUILD_TESTING=OFF"

ROS_BUILD_TYPE = "ament_cmake"
inherit ros_${ROS_BUILD_TYPE}
