# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "OEROS container image for running colcon test in CI"
DESCRIPTION = "ros-base plus colcon, the ament linters and coverage tooling, \
so that a CI job exercises the same binaries that ship in the runtime images \
instead of a separately built copy."

require oeros-container-image.inc

inherit ros_distro_${ROS_DISTRO}
inherit ${ROS_DISTRO_TYPE}_image

OCI_BASE_IMAGE = "oeros-container-ros-base"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
    ros-base \
    packagegroup-oeros-ros-tools \
    packagegroup-oeros-ros-ci \
"

# CI runs a command and exits rather than dropping into a shell.
OCI_IMAGE_CMD = "/bin/bash"
