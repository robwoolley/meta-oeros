# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "OEROS container image with the ROS 2 desktop-full variant, ready for rocker"
DESCRIPTION = "The top of the OEROS container stack.  Built as six layers in \
REP-2001 dependency order -- ros-core, ros-base, perception, simulation, \
desktop, desktop-full -- so that a registry stores each variant once and a \
client pulling this image reuses whatever it already has from the smaller \
ones.  Adds the packages osrf/rocker needs in a base image, so that \
    rocker --x11 --user --home <image> rviz2 \
works without a derived Dockerfile of your own."

require oeros-container-image.inc

inherit ros_distro_${ROS_DISTRO}
inherit ${ROS_DISTRO_TYPE}_image

OCI_BASE_IMAGE = "oeros-container-desktop"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
    perception \
    simulation \
    desktop \
    desktop-full \
    packagegroup-oeros-rocker \
"

# rocker's --user extension creates an account matching the invoking user and
# expects to land in its home directory.  Leaving OCI_IMAGE_RUNTIME_UID unset
# keeps the image running as root by default, which is what rocker's generated
# Dockerfile assumes when it runs its useradd snippet.
OCI_IMAGE_WORKINGDIR = "/root"

OCI_IMAGE_LABELS:append = " io.oeros.rocker-ready=true"

# rocker --x11 sets DISPLAY itself, but QT_X11_NO_MITSHM avoids the shared
# memory failure Qt hits when the container and the X server do not share an
# IPC namespace -- rviz2 in a container is the standard case for it.
OCI_IMAGE_ENV_VARS:append = " QT_X11_NO_MITSHM=1"
