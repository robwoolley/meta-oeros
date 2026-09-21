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

# A long list of ordinary packages -- confirmed so far: iptables (both its
# own RRECOMMENDS:${PN} and an extra ~14 kernel-module-* tokens appended by
# meta-virtualization/recipes-extended/iptables/iptables-meta-virtualization.inc,
# pulled in because this layer inherits image-oci from that same layer),
# systemd, util-linux -- carry RRECOMMENDS on kernel-module-* packages, which
# linux-yocto's kernel-module package split genuinely provides, so
# do_rootfs[recrdeptask] conservatively builds the entire kernel (a fresh
# git-mirror clone of the whole upstream git.yoctoproject.org/linux-yocto
# history, then a real build) to have those recommendations available,
# regardless of KERNELDEPMODDEPEND = "" above (see oeros-container-image.inc)
# or NO_RECOMMENDATIONS/BAD_RECOMMENDATIONS (both install-time-only, and so
# incapable of affecting this build-time task graph at all).
#
# Removing the RRECOMMENDS tokens package by package -- confirmed to actually
# work once the override syntax was corrected (VARIABLE:override:operation,
# not VARIABLE:operation:override; the packaging-variable rename from
# RRECOMMENDS:${PN} silently ignores the reversed form) -- does not scale:
# every additional image in this series (perception/simulation/desktop pull
# in hundreds more packages) is likely to surface more of them.
#
# openembedded-core already ships the purpose-built answer:
# recipes-kernel/linux/linux-dummy.bb, "to be selected as the preferred
# provider for virtual/kernel to satisfy dependencies for situations where
# you wish to build the kernel externally from the build system." Its
# PACKAGES_DYNAMIC += "^kernel-module-.*" (plus kernel-image-*,
# kernel-firmware-*) means it satisfies ANY kernel-module-* package name any
# recipe ever asks for, present or future, without enumerating them -- and
# every one of its tasks is a no-op ("do_compile() { : }" and so on), so it
# costs nothing to build. This is a strictly better fix than BBMASKing
# linux-yocto out: masking left 'Nothing PROVIDES virtual/kernel' fatal
# errors for anything with a genuine hard (not RRECOMMENDS) dependency on
# the kernel, where linux-dummy satisfies those too, by actually providing
# virtual/kernel rather than making it unresolvable.
PREFERRED_PROVIDER_virtual/kernel = "linux-dummy"
