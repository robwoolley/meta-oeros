# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "OEROS container image running rosbridge_server"
DESCRIPTION = "ros-base plus rosbridge_suite, exposing the graph as JSON over \
a websocket for roslibjs and other browser clients."

require oeros-container-image.inc

inherit ros_distro_${ROS_DISTRO}
inherit ${ROS_DISTRO_TYPE}_image

OCI_BASE_IMAGE = "oeros-container-ros-base"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
    ros-base \
    rosbridge-suite \
"

OCI_IMAGE_PORTS = "9090/tcp"
OCI_IMAGE_CMD = "ros2 launch rosbridge_server rosbridge_websocket_launch.xml"
