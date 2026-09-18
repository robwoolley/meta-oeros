# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "Minimal OEROS container base image"
DESCRIPTION = "The bottom of the OEROS container stack: a shell, an account \
database, name resolution and a CA bundle, with no init system and no kernel. \
Every other image here layers onto this one, which is what lets a registry \
share a single copy of the OS beneath all of them."

require oeros-container-image.inc

# Nothing below this image, so no OCI_BASE_IMAGE: image-oci starts from an
# empty layout.
OCI_BASE_IMAGE = ""

# Recommendations are dropped only here.  Further up the stack the ROS
# packages rely on them to pull in their Python and plugin dependencies.
NO_RECOMMENDATIONS = "1"

IMAGE_INSTALL = " \
    packagegroup-oeros-container-base \
    ${CONTAINER_SHELL} \
"

# There is no ROS in this image, so the entrypoint's setup.sh test always
# fails and it degrades to a plain exec.  It is installed anyway so that the
# entrypoint is part of the shared base layer instead of being duplicated into
# every ROS image above it.
OCI_IMAGE_CMD = "/bin/sh"
