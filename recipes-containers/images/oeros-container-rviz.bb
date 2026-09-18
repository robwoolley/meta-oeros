# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "OEROS container image with rviz2 only"
DESCRIPTION = "ros-base plus rviz2 and the rocker prerequisites, without the \
demos, tutorials, rqt or Gazebo that come with the desktop variant.  The \
image to reach for when all you want is to look at a remote graph:\n\
    rocker --x11 --user oeros/rviz:lyrical-genericx86-64 rviz2"

require oeros-container-image.inc

inherit ros_distro_${ROS_DISTRO}
inherit ${ROS_DISTRO_TYPE}_image

OCI_BASE_IMAGE = "oeros-container-ros-base"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
    ros-base \
    rviz2 \
    rviz-default-plugins \
    packagegroup-oeros-rocker \
"

OCI_IMAGE_CMD = "rviz2"
OCI_IMAGE_ENV_VARS:append = " QT_X11_NO_MITSHM=1"
OCI_IMAGE_LABELS:append = " io.oeros.rocker-ready=true"
