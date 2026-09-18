# Copyright (c) 2026 Wind River Systems, Inc.
#
# Settings shared by every OEROS container multiconfig.
#
# Required (not just included) by conf/multiconfig/oeros-*.conf, each of which
# sets MACHINE -- and, for the SDK configurations, SDKMACHINE -- before pulling
# this in.  bitbake.conf requires conf/multiconfig/${BB_CURRENT_MC}.conf after
# site.conf, auto.conf, local.conf and the builtin machine/distro fragments, so
# anything set here wins over the top-level build configuration.

# Every multiconfig needs its own TMPDIR.  Two of the configurations below
# share a MACHINE and differ only in SDKMACHINE or LICENSE_FLAGS_ACCEPTED, so
# keying on MACHINE alone would let them collide.
TMPDIR = "${TOPDIR}/tmp-${BB_CURRENT_MC}"

# Layers are installed as ipk: the OCI multi-layer code path drives the package
# manager once per layer, and opkg is substantially cheaper to stand up
# repeatedly than dnf.
PACKAGE_CLASSES = "package_ipk"

# genericarm64 and genericx86-64 both add qemuboot to IMAGE_CLASSES, whose
# do_write_qemuboot_conf depends on virtual/kernel:do_deploy.  A container
# image has no kernel, so leaving this in place would drag a full kernel build
# behind every image here for a file nothing consumes.  ':remove' is applied at
# expansion time, so it does not matter that the machine conf is parsed later.
IMAGE_CLASSES:remove = "qemuboot"

# Same reasoning for the initramfs genericarm64 asks for.
INITRAMFS_IMAGE = ""
INITRAMFS_IMAGE_BUNDLE = "0"

# The OCI layering in meta-virtualization's image-oci.bbclass only works with
# the umoci backend; sloci-image raises a hard error on OCI_BASE_IMAGE.
OCI_IMAGE_BACKEND = "umoci"

# Tag applied to every image built in this multiconfig, and the tag each image
# looks for in the OCI layout of its parent.  Keeping these equal is what makes
# OCI_BASE_IMAGE chaining resolve -- see recipes-containers/images/oeros-container-image.inc.
OEROS_CONTAINER_TAG ?= "${ROS_DISTRO}-${MACHINE}"
