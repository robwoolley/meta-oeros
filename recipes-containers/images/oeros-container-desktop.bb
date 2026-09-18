# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "OEROS container image with the ROS 2 desktop variant"
DESCRIPTION = "REP-2001 desktop: ros_base plus rviz2, the demos, the \
tutorials and rqt.  Layered on simulation here so that the desktop-full chain \
ends up with one layer per variant in REP-2001 dependency order."

require oeros-container-image.inc

inherit ros_distro_${ROS_DISTRO}
inherit ${ROS_DISTRO_TYPE}_image

OCI_BASE_IMAGE = "oeros-container-simulation"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
    perception \
    simulation \
    desktop \
"
