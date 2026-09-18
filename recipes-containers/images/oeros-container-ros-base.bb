# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "OEROS container image with the ROS 2 ros-base variant"
DESCRIPTION = "REP-2001 ros_base: ros_core plus the TF2 stack, urdf, \
kdl_parser and the other packages a robot runtime is normally built against. \
The usual starting point for a deployed node."

require oeros-container-image.inc

inherit ros_distro_${ROS_DISTRO}
inherit ${ROS_DISTRO_TYPE}_image

OCI_BASE_IMAGE = "oeros-container-ros-core"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
    ros-base \
"
