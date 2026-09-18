# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "OEROS container image with the ROS 2 simulation variant"
DESCRIPTION = "REP-2001 simulation: perception plus the Gazebo bridge and \
the simulation interfaces.  This is the layer where the build gets expensive \
-- gz-sim-vendor and its Ogre/Qt dependencies dominate the wall clock time \
for the whole stack."

require oeros-container-image.inc

inherit ros_distro_${ROS_DISTRO}
inherit ${ROS_DISTRO_TYPE}_image

OCI_BASE_IMAGE = "oeros-container-perception"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
    perception \
    simulation \
"
