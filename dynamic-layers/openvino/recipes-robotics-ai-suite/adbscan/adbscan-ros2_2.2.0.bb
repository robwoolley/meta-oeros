SUMMARY = "Intel-patented unsupervised clustering algorithm"
DESCRIPTION = "ADBSCAN adaptive DBSCAN clustering for ROS 2 point-cloud/object \
localization, part of the Intel Robotics AI Suite. \"Intel-patented\" refers \
to the algorithm's patent status and does not affect its Apache-2.0 licensing."
HOMEPAGE = "https://github.com/open-edge-platform/edge-ai-suites/tree/main/robotics-ai-suite/components/adbscan"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://../LICENSES/Apache-2.0.txt;md5=c846ebb396f8b174b10ded4771514fcc"

SRC_URI = "git://github.com/open-edge-platform/edge-ai-suites.git;protocol=https;branch=main"
SRCREV = "e67ecbab6ccce6ca89675b22d784bdb8b430ae3e"
PV = "2.2.0+git"

S = "${UNPACKDIR}/${BP}/robotics-ai-suite/components/adbscan/ROS2_node"

inherit ros_distro_${ROS_DISTRO} pkgconfig
inherit ros_component

ROS_CN = "adbscan_ros2"
ROS_BPN = "adbscan_ros2"

ROS_BUILDTOOL_DEPENDS = "ament-cmake-native"
ROS_BUILD_DEPENDS = "rclcpp geometry-msgs sensor-msgs visualization-msgs nav2-dynamic-msgs pcl-conversions"
ROS_EXEC_DEPENDS = "rclcpp geometry-msgs sensor-msgs visualization-msgs nav2-dynamic-msgs pcl-conversions"
ROS_TEST_DEPENDS = "ament-cmake-gtest"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# CMakeLists.txt find_package(TBB REQUIRED)s and links TBB::tbb directly
# for the adaptive/parallel clustering implementation -- a real, genuine
# dependency (not a ROS package, hence not in package.xml or ROS_*_DEPENDS;
# provided by meta-openembedded's tbb recipe).
DEPENDS += "tbb"

EXTRA_OECMAKE += "-DBUILD_TESTING=OFF"

ROS_BUILD_TYPE = "ament_cmake"
inherit ros_${ROS_BUILD_TYPE}
