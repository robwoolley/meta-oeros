# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "OEROS container image with the ROS 2 ros-core variant"
DESCRIPTION = "REP-2001 ros_core: the ROS 2 middleware, rclcpp/rclpy, the \
core message packages and the ros2 CLI.  The smallest image that can take \
part in a ROS 2 graph, and the base layer for every other ROS image here."

require oeros-container-image.inc

inherit ros_distro_${ROS_DISTRO}
inherit ${ROS_DISTRO_TYPE}_image

OCI_BASE_IMAGE = "oeros-container-base"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
    ros-core \
"
