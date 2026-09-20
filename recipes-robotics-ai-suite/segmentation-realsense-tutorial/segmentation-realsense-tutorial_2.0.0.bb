SUMMARY = "Tutorial for Sematic Segmentation with Realsense Topic"
DESCRIPTION = "Launch/rviz/param bundle demonstrating semantic segmentation \
against an Intel RealSense camera topic, part of the Intel Robotics AI \
Suite. No compiled code, no runtime dependencies beyond the RealSense \
camera nodes (meta-intel-realsense, already unconditional in this distro) \
launched separately."
HOMEPAGE = "https://github.com/open-edge-platform/edge-ai-suites/tree/main/robotics-ai-suite/components/object-detection"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://../LICENSES/Apache-2.0.txt;md5=c846ebb396f8b174b10ded4771514fcc"

SRC_URI = "git://github.com/open-edge-platform/edge-ai-suites.git;protocol=https;branch=main"
SRCREV = "e67ecbab6ccce6ca89675b22d784bdb8b430ae3e"
PV = "2.0.0+git"

S = "${UNPACKDIR}/${BP}/robotics-ai-suite/components/object-detection/segmentation_realsense_tutorial"

inherit ros_distro_${ROS_DISTRO} pkgconfig
inherit ros_component

ROS_CN = "segmentation_realsense_tutorial"
ROS_BPN = "segmentation_realsense_tutorial"

ROS_BUILDTOOL_DEPENDS = "ament-cmake-native"
ROS_TEST_DEPENDS = "ament-lint-auto ament-lint-common"

DEPENDS = "${ROS_BUILDTOOL_DEPENDS}"

EXTRA_OECMAKE += "-DBUILD_TESTING=OFF"

ROS_BUILD_TYPE = "ament_cmake"
inherit ros_${ROS_BUILD_TYPE}
