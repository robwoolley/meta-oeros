# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "OEROS container image with the Intel RealSense ROS 2 driver"
DESCRIPTION = "ros-base plus librealsense2 and realsense2_camera.  Needs the \
camera's USB device passed in at run time (--device, or -v /dev/bus/usb)."

require oeros-container-image.inc

inherit ros_distro_${ROS_DISTRO}
inherit ${ROS_DISTRO_TYPE}_image

OCI_BASE_IMAGE = "oeros-container-ros-base"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
    ros-base \
    librealsense2 \
    realsense2-camera \
    realsense2-description \
"

OCI_IMAGE_CMD = "ros2 launch realsense2_camera rs_launch.py"
