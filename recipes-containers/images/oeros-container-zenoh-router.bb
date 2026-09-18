# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "OEROS container image running the zenoh router"
DESCRIPTION = "A standalone zenohd.  rmw_zenoh needs a router to bridge \
sessions that cannot discover each other directly, which is exactly the case \
once ROS 2 nodes are split across containers or hosts.  Layered on the OS \
base rather than on ros-core: zenohd is not a ROS node and needs none of it."

require oeros-container-image.inc

OCI_BASE_IMAGE = "oeros-container-base"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
    zenoh \
"

# zenohd's default listener.  Change with -e or a mounted config rather than
# rebuilding.
OCI_IMAGE_PORTS = "7447/tcp 7447/udp 8000/tcp"

# Run the router directly: there is no ROS environment to source here, so the
# shared entrypoint would only add a failed test.
OCI_IMAGE_ENTRYPOINT = "/usr/bin/zenohd"
OCI_IMAGE_CMD = ""
