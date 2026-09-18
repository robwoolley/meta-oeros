# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "OEROS container image for use as a VS Code dev container"
DESCRIPTION = "The debugging image plus what the VS Code server needs to \
attach to a running container: a real bash, tar, gzip and a writable home. \
Usable both as a devcontainer image and under rocker."

require oeros-container-image.inc

inherit ros_distro_${ROS_DISTRO}
inherit ${ROS_DISTRO_TYPE}_image

OCI_BASE_IMAGE = "oeros-container-ros-dev"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
    ros-base \
    packagegroup-oeros-ros-tools \
    packagegroup-oeros-ros-dev \
    packagegroup-oeros-ros-ci \
    packagegroup-oeros-rocker \
    tar \
    gzip \
    libstdc++ \
"

IMAGE_FEATURES += "dbg-pkgs"

OCI_IMAGE_WORKINGDIR = "/workspace"
OCI_IMAGE_LABELS:append = " io.oeros.rocker-ready=true devcontainer.metadata=oeros"

ROOTFS_POSTPROCESS_COMMAND += "oeros_container_devcontainer_workspace ; "
oeros_container_devcontainer_workspace () {
    install -m 0755 -d ${IMAGE_ROOTFS}/workspace
}
