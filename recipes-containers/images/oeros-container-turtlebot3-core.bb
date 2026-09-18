# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "OEROS container image with the TurtleBot 3 on-robot packages"
DESCRIPTION = "The packages meta-ros groups as turtlebot3-core -- the ones \
that run on the robot itself.  A known-good workload for validating that the \
whole container chain actually works end to end."

require oeros-container-image.inc

inherit ros_distro_${ROS_DISTRO}
inherit ${ROS_DISTRO_TYPE}_image

OCI_BASE_IMAGE = "oeros-container-ros-base"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
    ros-base \
    packagegroup-ros-turtlebot3-core \
"
