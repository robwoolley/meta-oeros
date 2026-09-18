# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "OEROS container image running Gazebo without a GUI"
DESCRIPTION = "The simulation variant with the Gazebo server but none of the \
GUI client.  CI jobs and build farms want to run a world headless, and \
bundling that capability into the desktop image would force an X stack into \
jobs that have no display at all."

require oeros-container-image.inc

inherit ros_distro_${ROS_DISTRO}
inherit ${ROS_DISTRO_TYPE}_image

OCI_BASE_IMAGE = "oeros-container-ros-base"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
    ros-base \
    simulation \
    ros-gz-sim \
    ros-gz-bridge \
"

# gz-sim still links its rendering libraries in a headless build; it is the
# GUI process that is not started.  ogre2 picks EGL when there is no display.
OCI_IMAGE_ENV_VARS:append = " GZ_HEADLESS=1"
OCI_IMAGE_CMD = "ros2 launch ros_gz_sim gz_sim.launch.py"
