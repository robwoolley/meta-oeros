# Copyright (c) 2026 Wind River Systems, Inc.
#
# Common definitions for every OEROS container image.
#
# The images in this directory form a chain: each one is a complete rootfs, and
# each names its predecessor in OCI_BASE_IMAGE.  image-oci.bbclass unpacks the
# parent's OCI layout, rsyncs this image's IMAGE_ROOTFS over it and calls
# "umoci repack", which diffs against the unpacked parent -- so the layer that
# actually gets added contains only what this image adds.  That is why every
# recipe here installs its full contents rather than just its delta.

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

IMAGE_FSTYPES = "container oci"

inherit image
inherit image-oci

IMAGE_FEATURES = ""
IMAGE_LINGUAS = ""

# No kernel is built for these images, so the usual image-to-kernel dependency
# must not be asserted.
IMAGE_CONTAINER_NO_DUMMY = "1"

# image-oci finds the parent's layout at
#     ${DEPLOY_DIR_IMAGE}/${OCI_BASE_IMAGE}-${OCI_BASE_IMAGE_TAG}-oci
# and the parent deploys that symlink as
#     ${IMAGE_BASENAME}-${OCI_IMAGE_TAG}-oci
# so the chain only resolves while IMAGE_BASENAME equals PN and parent and
# child agree on the tag.  ros_image.bbclass appends "-${ROS_DISTRO}" to
# IMAGE_BASENAME by default, which would silently break the lookup with a
# "Base image not found at expected path" abort; suppress it.
ROS_IMAGE_BASENAME_APPEND = ""

OEROS_CONTAINER_TAG ?= "${ROS_DISTRO}-${MACHINE}"
OCI_IMAGE_TAG ?= "${OEROS_CONTAINER_TAG}"
OCI_BASE_IMAGE_TAG ?= "${OEROS_CONTAINER_TAG}"

OCI_IMAGE_AUTHOR ?= "OpenEmbedded ROS"
OCI_IMAGE_AUTHOR_EMAIL ?= "${MAINTAINER}"

# Mirrors the convention osrf/ros established: ENTRYPOINT sources the ROS
# environment and execs whatever it is handed, CMD is an interactive shell that
# a "docker run <image> ros2 topic list" replaces.  Keeping this identical to
# the Docker Hub images means existing tutorials and rocker invocations work
# against these images unchanged.
OCI_IMAGE_ENTRYPOINT ?= "/ros_entrypoint.sh"
OCI_IMAGE_CMD ?= "/bin/bash"

# Space-separated key=value: values may not themselves contain spaces.
OCI_IMAGE_ENV_VARS ?= " \
    ROS_DISTRO=${ROS_DISTRO} \
    ROS_VERSION=2 \
    LANG=C.UTF-8 \
"

OCI_IMAGE_LABELS ?= " \
    org.opencontainers.image.vendor=openembedded-ros \
    org.opencontainers.image.title=${PN} \
    io.oeros.ros-distro=${ROS_DISTRO} \
    io.oeros.machine=${MACHINE} \
"

# base-files and base-passwd carry postinstalls that invoke /bin/sh, so the
# rootfs needs either a real shell or the explicit dummy provider.
CONTAINER_SHELL ?= "${@bb.utils.contains('PACKAGE_EXTRA_ARCHS', 'container-dummy-provides', 'container-dummy-provides', 'busybox', d)}"

# Nothing boots in a container, so the volatile directories that would normally
# be created by the init scripts have to be created here instead.  The :remove
# is required because empty_var_volatile runs later and would undo this.
ROOTFS_POSTPROCESS_COMMAND += "oeros_container_fixup_var_volatile ; "
ROOTFS_POSTPROCESS_COMMAND:remove = "empty_var_volatile"

oeros_container_fixup_var_volatile () {
    install -m 1777 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/tmp
    install -m 755 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/log
}
