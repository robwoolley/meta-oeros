# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "OEROS container image with the ROS 2 command line tools"
DESCRIPTION = "ros-core plus the ros2 CLI and rosbag2.  Small enough to run \
alongside a deployed system to inspect or record its graph without shipping a \
second copy of ros-base."

require oeros-container-image.inc

inherit ros_distro_${ROS_DISTRO}
inherit ${ROS_DISTRO_TYPE}_image

OCI_BASE_IMAGE = "oeros-container-ros-core"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
    ros-core \
    packagegroup-oeros-ros-tools \
"
