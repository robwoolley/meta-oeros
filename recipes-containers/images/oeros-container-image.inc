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

# NOT setting IMAGE_CONTAINER_NO_DUMMY here is deliberate. "container" in
# IMAGE_FSTYPES (above) makes image.bbclass auto-inherit oe-core's
# image-container.bbclass, whose whole job is a parse-time guard: fail with
# a clear message if PREFERRED_PROVIDER_virtual/kernel isn't "linux-dummy",
# unless IMAGE_CONTAINER_NO_DUMMY = "1" says that's intentional. An earlier
# version of this file set NO_DUMMY = "1" -- copied from meta-virtualization's
# container-base.bb, where it belongs because that recipe sometimes *does*
# want a real kernel bound -- which silenced that exact guard for images that
# never do, for the whole time it took to separately rediscover, the hard
# way, that PREFERRED_PROVIDER_virtual/kernel needed to be linux-dummy (now
# set in conf/include/oeros-container-mc.inc). Leaving the opt-out unset
# keeps the guard live: if some future image here ever stops inheriting that
# multiconfig include, or something overrides the provider back, parsing
# fails immediately with an actionable message instead of silently
# scheduling a full kernel build again.

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

# image.bbclass unconditionally adds do_rootfs[depends] +=
# "${KERNELDEPMODDEPEND}", which defaults to "virtual/kernel:do_packagedata"
# -- there to populate depmod data in PKGDATA_DIR for images that install
# kernel modules. None of these images do (no init system, no kernel at
# all), but the dependency is unconditional: on genericx86-64/genericarm64
# it resolved virtual/kernel to linux-yocto and pulled a full do_fetch/
# do_compile of the upstream kernel (a fresh git-mirror clone of the entire
# git.yoctoproject.org/linux-yocto history, then a real kernel build) into
# what should be a kernel-free container image. image.bbclass's own comment
# on KERNELDEPMODDEPEND names this exact case ("if you're building small
# initramfs image ... you probably don't want this dependency") and the
# empty override below is its documented way out.
#
# KERNELDEPMODDEPEND alone was not enough: iptables (still reaching these
# images through a source not yet traced) independently pulls linux-yocto
# back in through its own RRECOMMENDS:${PN} on iptables-modules and a dozen
# kernel-module-* packages, in openembedded-core's iptables_1.8.13.bb.
#
# Neither NO_RECOMMENDATIONS nor BAD_RECOMMENDATIONS touches this, and for
# the same reason: both are consumed only in
# oe/package_manager/{ipk,rpm}/*.py and rootfs_deb.bbclass -- the
# install-time code that decides what the package manager actually writes
# into the final rootfs. Neither one appears anywhere in lib/oe/rootfs.py,
# which is what computes do_rootfs[recrdeptask]'s build set; by the time
# either flag is consulted, BitBake has already conservatively built
# everything reachable through RDEPENDS and RRECOMMENDS regardless. A
# dry run inspects exactly that build-time task graph, which is why both
# looked like they should work and neither did.
#
# The actual, working fix is in conf/include/oeros-container-mc.inc:
# RRECOMMENDS:iptables:remove, which changes the RRECOMMENDS value itself
# at parse time -- the only place upstream of do_rootfs[recrdeptask] that
# can keep a recommended package's build out of the graph in the first
# place, rather than merely out of the finished image.
KERNELDEPMODDEPEND = ""

# Nothing boots in a container, so the volatile directories that would normally
# be created by the init scripts have to be created here instead.  The :remove
# is required because empty_var_volatile runs later and would undo this.
ROOTFS_POSTPROCESS_COMMAND += "oeros_container_fixup_var_volatile ; "
ROOTFS_POSTPROCESS_COMMAND:remove = "empty_var_volatile"

oeros_container_fixup_var_volatile () {
    install -m 1777 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/tmp
    install -m 755 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/log
}
