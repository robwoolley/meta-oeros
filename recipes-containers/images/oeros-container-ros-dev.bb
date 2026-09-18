# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "OEROS container image for interactive debugging"
DESCRIPTION = "ros-base plus gdb, strace, valgrind, tcpdump and the rest of \
the tools you want when you exec into a container to work out why a node is \
misbehaving.  Not intended for deployment."

require oeros-container-image.inc

inherit ros_distro_${ROS_DISTRO}
inherit ${ROS_DISTRO_TYPE}_image

OCI_BASE_IMAGE = "oeros-container-ros-base"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
    ros-base \
    packagegroup-oeros-ros-tools \
    packagegroup-oeros-ros-dev \
"

# Debug symbols are the point of this image.
IMAGE_FEATURES += "dbg-pkgs"
