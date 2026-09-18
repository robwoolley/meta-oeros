# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "OEROS container image with the full TurtleBot 3 stack"
DESCRIPTION = "turtlebot3-core plus the workstation-side packages: \
navigation, SLAM, teleoperation and the simulation worlds.  Builds in the \
desktop multiconfig because the extended packagegroup reaches into the \
graphical and simulation variants."

require oeros-container-image.inc

inherit ros_distro_${ROS_DISTRO}
inherit ${ROS_DISTRO_TYPE}_image

OCI_BASE_IMAGE = "oeros-container-desktop"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
    perception \
    simulation \
    desktop \
    packagegroup-ros-turtlebot3-core \
    packagegroup-ros-turtlebot3-extended \
    packagegroup-oeros-rocker \
"

OCI_IMAGE_ENV_VARS:append = " QT_X11_NO_MITSHM=1 TURTLEBOT3_MODEL=burger"
OCI_IMAGE_LABELS:append = " io.oeros.rocker-ready=true"
