SUMMARY = "A really FastMapping method from RGB-D sequences"
HOMEPAGE = "https://github.com/open-edge-platform/edge-ai-suites/tree/main/robotics-ai-suite/components/fast-mapping"
LICENSE = "Apache-2.0 & BSD-3-Clause"
# LICENSES/ lives directly under robotics-ai-suite/components/fast-mapping/,
# the same level as S -- not one up. Confirmed via a real do_populate_lic
# failure: `bitbake -c compile` alone never exercises do_populate_lic, so
# a wrong relative path here went undetected until a full `bitbake
# <recipe>` build actually ran it.
LIC_FILES_CHKSUM = " \
    file://LICENSES/Apache-2.0.txt;md5=c846ebb396f8b174b10ded4771514fcc \
    file://LICENSES/BSD-3-Clause.txt;md5=71f739ef75581cae312e8c711bcdab16 \
"
# BSD-3-Clause: src/fast_mapping_lib/se/** (Copyright 2016 Emanuele Vespa,
# Imperial College London) -- explicitly annotated in the component's own
# REUSE.toml, not guessed.

SRC_URI = "git://github.com/open-edge-platform/edge-ai-suites.git;protocol=https;branch=main"
SRCREV = "e67ecbab6ccce6ca89675b22d784bdb8b430ae3e"
PV = "2.3.0+git"

S = "${UNPACKDIR}/${BP}/robotics-ai-suite/components/fast-mapping"

inherit ros_distro_${ROS_DISTRO} pkgconfig
inherit ros_component

ROS_CN = "fast_mapping"
ROS_BPN = "fast_mapping"

DEPENDS += "opencv"

# ament-lint-auto-native/ament-cmake-gtest-native are added here even
# though ROS_TEST_DEPENDS is normally "informational only" in this layer's
# convention (see e.g. adaptive-component's recipe): CMakeLists.txt calls
# find_package(ament_cmake_gtest REQUIRED) and find_package(ament_lint_auto
# REQUIRED) completely unconditionally at top level, NOT gated behind
# if(BUILD_TESTING) like a well-formed ament_cmake package would -- a real
# upstream CMakeLists.txt bug, confirmed via a live `bitbake -c compile`
# failure ("Could not find ... ament_lint_auto") even with BUILD_TESTING=OFF.
ROS_BUILDTOOL_DEPENDS = "ament-cmake-core-native rosidl-default-generators-native ament-lint-auto-native ament-cmake-gtest-native"
# rosbag2-cpp and std-srvs are real, unconditional dependencies of the
# fast_mapping_node target (find_package()'d and ament_target_dependencies()'d
# outside any if(BUILD_TESTING) guard in CMakeLists.txt) but are MISSING from
# package.xml's own build_depend/exec_depend list -- another real upstream
# package.xml/CMakeLists.txt inconsistency, confirmed the same way. Only
# rosbag2-transport and rosbag2-storage (also find_package()'d, redundantly,
# a second time) are genuinely test-only, correctly gated behind
# if(BUILD_TESTING) -- not added here since EXTRA_OECMAKE disables that.
ROS_BUILD_DEPENDS = "rclcpp cv-bridge rclcpp-action rclcpp-lifecycle std-msgs geometry-msgs sensor-msgs visualization-msgs nav-msgs tf2 tf2-ros tf2-geometry-msgs tf2-eigen message-filters rosbag2-cpp std-srvs"
ROS_EXEC_DEPENDS = "rclcpp cv-bridge rclcpp-action rclcpp-lifecycle std-msgs geometry-msgs sensor-msgs visualization-msgs nav-msgs tf2 tf2-ros tf2-geometry-msgs tf2-eigen message-filters rosbag2-cpp std-srvs"
ROS_TEST_DEPENDS = "ament-lint-auto ament-lint-common ament-cmake-gtest"

DEPENDS += "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# safestringlib is documented as a dependency (dependencies/Safestringlib)
# but CMakeLists.txt never actually find_package()s it -- not added to
# DEPENDS.
EXTRA_OECMAKE += "-DBUILD_TESTING=OFF"

ROS_BUILD_TYPE = "ament_cmake"
inherit ros_${ROS_BUILD_TYPE}
