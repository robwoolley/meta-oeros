# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "OEROS container image running the Foxglove bridge"
DESCRIPTION = "ros-base plus foxglove_bridge, which exposes the ROS 2 graph \
over a websocket for the Foxglove app.  On a headless target this is usually \
a better answer than forwarding X11 for rviz2: nothing graphical has to exist \
in the image at all."

require oeros-container-image.inc

inherit ros_distro_${ROS_DISTRO}
inherit ${ROS_DISTRO_TYPE}_image

OCI_BASE_IMAGE = "oeros-container-ros-base"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
    ros-base \
    foxglove-bridge \
"

OCI_IMAGE_PORTS = "8765/tcp"
OCI_IMAGE_CMD = "ros2 launch foxglove_bridge foxglove_bridge_launch.xml"
