SUMMARY = "Openvino Yolov8 ROS2 tool"
HOMEPAGE = "https://github.com/open-edge-platform/edge-ai-suites/tree/main/robotics-ai-suite/components/object-detection"
LICENSE = "Apache-2.0 & MIT & BSD-3-Clause"
LIC_FILES_CHKSUM = " \
    file://../../../LICENSES/Apache-2.0.txt;md5=c846ebb396f8b174b10ded4771514fcc \
    file://../../../LICENSES/MIT.txt;md5=3912d958d00bac4a6b550f75d7c806bb \
    file://../../../LICENSES/BSD-3-Clause.txt;md5=71f739ef75581cae312e8c711bcdab16 \
"
# MIT: vendored src/toml.hpp (Mark Gillard)
# BSD-3-Clause: vendored src/CLI11.hpp (University of Cincinnati / NSF)

DEPENDS = "openvino-inference-engine opencv"

SRC_URI = " \
    git://github.com/open-edge-platform/edge-ai-suites.git;protocol=https;branch=main \
    file://0001-tests-gate-FetchContent-googletest-behind-BUILD_TES.patch;striplevel=1 \
"
SRCREV = "e67ecbab6ccce6ca89675b22d784bdb8b430ae3e"
PV = "1.0.0+git"

S = "${UNPACKDIR}/${BP}/robotics-ai-suite/components/object-detection/yolov8/src/yolo"

inherit ros_distro_${ROS_DISTRO} pkgconfig
inherit ros_component

ROS_CN = "yolo"
ROS_BPN = "yolo"

ROS_BUILDTOOL_DEPENDS = "ament-cmake-native"
ROS_BUILD_DEPENDS = "geometry-msgs sensor-msgs std-msgs yolo-msgs rclcpp tf2 tf2-ros tf2-msgs visualization-msgs"
ROS_EXEC_DEPENDS = "geometry-msgs sensor-msgs std-msgs yolo-msgs rclcpp tf2 tf2-ros tf2-msgs visualization-msgs"

DEPENDS += "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

EXTRA_OECMAKE += "-DBUILD_TESTING=OFF"

ROS_BUILD_TYPE = "ament_cmake"
inherit ros_${ROS_BUILD_TYPE}
