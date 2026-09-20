SUMMARY = "Efficient groundfloor segmentation for 3D pointclouds"
HOMEPAGE = "https://github.com/open-edge-platform/edge-ai-suites/tree/main/robotics-ai-suite/components/groundfloor"
LICENSE = "Apache-2.0 & BSD-3-Clause"
LIC_FILES_CHKSUM = " \
    file://../../LICENSES/Apache-2.0.txt;md5=c846ebb396f8b174b10ded4771514fcc \
    file://../../LICENSES/BSD-3-Clause.txt;md5=85c5d57f6d7c8862ba09a3961201419c \
"
# BSD-3-Clause: vendored cmake/CodeCoverage.cmake (Lars Bilke,
# github.com/bilke/cmake-modules) -- build-tooling only, not linked into
# any shipped binary, but still part of the fetched source tree.

SRC_URI = "git://github.com/open-edge-platform/edge-ai-suites.git;protocol=https;branch=main"
SRCREV = "e67ecbab6ccce6ca89675b22d784bdb8b430ae3e"
PV = "2.1.0+git"

S = "${UNPACKDIR}/${BP}/robotics-ai-suite/components/groundfloor/pointcloud_groundfloor_segmentation"

inherit ros_distro_${ROS_DISTRO} pkgconfig
inherit ros_component

ROS_CN = "pointcloud_groundfloor_segmentation"
ROS_BPN = "pointcloud_groundfloor_segmentation"

ROS_BUILDTOOL_DEPENDS = "ament-cmake-native"
ROS_BUILD_DEPENDS = "rclcpp pcl-conversions sensor-msgs depth-image-proc tf2-ros tf2-eigen"
ROS_EXEC_DEPENDS = "rclcpp pcl-conversions sensor-msgs depth-image-proc tf2-ros tf2-eigen"
ROS_TEST_DEPENDS = "ament-lint-auto ament-lint-common"

# package.xml's <exec_depend>nav2_bringup</exec_depend> (launch-time-only,
# used by the demo launch files, not linked into the compiled node) is
# omitted. CORRECTED REASONING (an earlier pass here wrongly concluded
# nav2-bringup's deps were simply missing from meta-ros2-jazzy, from an
# unscoped `find`+`head -1` across all meta-ros2-<distro> dirs that
# silently matched a different distro's recipe -- they are NOT missing,
# every one exists directly under meta-ros2-jazzy at real jazzy-pinned
# versions). The real reason: `bitbake-layers show-recipes nav2-bringup`
# shows it's SKIPPED, because it transitively RDEPENDS on slam-toolbox ->
# rviz-common -> rviz-ogre-vendor, whose own package.xml (ROS_BUILD/EXEC_
# DEPENDS in rviz-ogre-vendor's recipe) directly requires libx11, libxaw,
# and libxrandr -- Ogre's GLX render-window/input backend calling Xlib
# APIs directly, not a generic "needs *a* display server" stand-in.
# Those X11 lib recipes `inherit features_check` with
# REQUIRED_DISTRO_FEATURES = "x11" (a hard AND-style gate, via the shared
# xorg-lib-common.inc), and this distro (conf/distro/oeros.conf) is
# deliberately Wayland-only (`wayland opengl vulkan polkit`, no `x11` in
# DISTRO_FEATURES). Adding `wayland` would NOT unskip this: unlike e.g.
# vulkan-tools (which uses ANY_OF_DISTRO_FEATURES = "x11 wayland" since
# Vulkan surface creation genuinely supports either), this vendored Ogre
# build has no Wayland/EGL render-window path wired in -- it's X11-only
# code, not an either-protocol-works choice. This is a distro-architecture
# incompatibility, not a ROS-distro-availability gap, and would affect any
# RViz-dependent ROS 2 package on this distro, not just nav2-bringup.
# See docs/edgeai.md.

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

EXTRA_OECMAKE += "-DBUILD_TESTING=OFF"

ROS_BUILD_TYPE = "ament_cmake"
inherit ros_${ROS_BUILD_TYPE}
