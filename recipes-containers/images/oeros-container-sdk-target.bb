# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "OEROS container image with an on-target ROS 2 build environment"
DESCRIPTION = "The on-target counterpart to oeros-container-sdk-cross: a \
container of the target's own architecture carrying a compiler that runs on \
that same architecture, the development headers and the ament/rosidl build \
tooling, so that colcon build works inside the container on the target \
itself (or under qemu-user).\n\
\n\
Built from the same package set the ros2-image-sdktest SDK exposes -- \
ROS_SDK_TARGET_PACKAGES (a global conf-level variable) plus the -dev/-staticdev \
packages that recipe lists under ROS_SDK_EXTRA_INSTALL (mirrored below as \
OEROS_SDK_TARGET_EXTRA_INSTALL, since ROS_SDK_EXTRA_INSTALL is set inside \
ros2-image-sdktest.bb's own recipe file rather than a shared .inc, and so is \
not visible to any other recipe) -- installed directly rather than unpacked \
from a relocatable SDK tarball.  Installing the packages keeps the result \
layered on oeros-container-ros-base, so it shares every layer below it with \
the runtime images instead of carrying a second private copy of ROS.\n\
\n\
Named 'sdk-target', not 'sdk-native': in BitBake '-native' specifically means \
a recipe that builds for and runs on the build host (cmake-native, and so \
on), which is the opposite of what this image is -- its rootfs and compiler \
both match MACHINE, not the build host. 'target' is the word OE itself uses \
for that side of the native/nativesdk/target triad."

require oeros-container-image.inc

inherit ros_distro_${ROS_DISTRO}
inherit ${ROS_DISTRO_TYPE}_image

OCI_BASE_IMAGE = "oeros-container-ros-base"

# dev-pkgs adds the -dev package for everything else installed in the image,
# which is what makes the ROS libraries below linkable. There is no
# "tools-sdk" IMAGE_FEATURES here (unlike core-image-based recipes) because
# that feature is registered by core-image.bbclass, which this image does not
# inherit; packagegroup-core-buildessential below is the same on-target
# toolchain (gcc, g++, binutils, make, libstdc++-dev, autoconf, automake,
# libtool, pkgconfig) that FEATURE_PACKAGES_tools-sdk would have pulled in,
# without also dragging in packagegroup-core-standalone-sdk-target, which is
# for a relocatable SDK sysroot rather than a plain on-target install.
IMAGE_FEATURES += "dev-pkgs"

# Kept identical to ros2-image-sdktest.bb's ROS_SDK_EXTRA_INSTALL -- see the
# note above on why that variable can't just be referenced here. If the SDK
# recipe's list changes, update this one to match.
OEROS_SDK_TARGET_EXTRA_INSTALL = " \
    boost \
    bullet \
    eigen3-cmake-module \
    libeigen \
    libstdc++-staticdev \
    opencv-staticdev \
    orocos-kdl \
    pcl-dev \
    pybind11-vendor \
    python-cmake-module \
    python3-numpy-staticdev \
    python3-opencv \
    python3-pykdl \
    qhull-staticdev \
    rttest \
    tlsf-staticdev \
    tlsf-cpp \
    tinyxml-vendor \
    yaml-cpp-vendor \
"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
    ros-base \
    packagegroup-oeros-ros-tools \
    packagegroup-core-buildessential \
    ${OEROS_SDK_TARGET_EXTRA_INSTALL} \
    ${ROS_SDK_TARGET_PACKAGES} \
    cmake \
    git \
    python3-colcon-common-extensions \
"

OCI_IMAGE_WORKINGDIR = "/workspace"
OCI_IMAGE_LABELS:append = " io.oeros.sdk=target io.oeros.sdk-host-arch=${TARGET_ARCH}"

ROOTFS_POSTPROCESS_COMMAND += "oeros_container_sdk_workspace ; "
oeros_container_sdk_workspace () {
    install -m 0755 -d ${IMAGE_ROOTFS}/workspace
}
