# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "OEROS container image with the ROS 2 perception variant"
DESCRIPTION = "REP-2001 perception: ros_base plus the image pipeline, \
laser geometry, PCL and vision_opencv.  Built in the desktop multiconfig \
because some of its dependencies carry commercial license flags."

require oeros-container-image.inc

inherit ros_distro_${ROS_DISTRO}
inherit ${ROS_DISTRO_TYPE}_image

OCI_BASE_IMAGE = "oeros-container-ros-base"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
    perception \
"
