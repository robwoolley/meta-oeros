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

# linux-dummy's PACKAGES_DYNAMIC only satisfies the *wildcard* kernel-module-*
# family. It has no effect on a package that is itself a real, specifically-
# named out-of-tree kernel module recipe -- do_rootfs[recrdeptask] still
# schedules a genuine build for one of those the moment anything reachable
# RDEPENDS/RRECOMMENDS on it by name.
#
# ROS 2's tracetools (ros2_tracing) RDEPENDS:${PN} on lttng-tools for runtime
# LTTng userspace tracing -- correctly: lttng-tools itself is pure userspace
# and builds fine here. But openembedded-core's lttng-tools recipe also sets
# RDEPENDS:${PN}-ptest += "... ${LTTNGMODULES} ...", where LTTNGMODULES
# (recipes-kernel/lttng/lttng-platforms.inc) expands to "lttng-modules" -- a
# real recipe that inherits module-base.bbclass and needs a fully configured
# kernel source tree at STAGING_KERNEL_DIR (work-shared/<machine>/
# kernel-source) to actually compile against, which linux-dummy never
# creates. IMAGE_FEATURES here has no "ptest-pkgs", so lttng-tools-ptest is
# never installed -- but the same conservative recrdeptask closure that
# ignored KERNELDEPMODDEPEND (see oeros-container-image.inc) still builds
# every package a reachable recipe produces, ptest included, so it gets
# scheduled anyway and make-mod-scripts:do_configure fails looking for a
# kernel tree that was never going to exist.
RDEPENDS:lttng-tools-ptest:remove = "lttng-modules"

# meta-ros's recipe generator (superflore) resolves each rosdep key against
# rosdep's own per-distro package lists; when a key has no "openembedded:"
# mapping there, it emits the literal marker
# "${ROS_UNRESOLVED_DEP-<name>}" as a bare guess rather than a real lookup.
# python3-osrf-pycommon is one such key (see meta-ros2-lyrical/generated-
# recipes/launch-ros/launch-ros_0.29.8-1.bb ROS_EXEC_DEPENDS): opkg then
# looks for an ipk package literally named "python3-osrf-pycommon", but the
# recipe that actually builds it (generated-recipes/osrf-pycommon/
# osrf-pycommon_2.1.7-3.bb) packages itself as plain "osrf-pycommon" --
# ROS_CN/ROS_BPN "osrf_pycommon" with ROS_BUILD_TYPE "ament_python" never
# gets a python3- prefix. Rather than hand-edit a "DO NOT EDIT" generated
# recipe that superflore will just regenerate, alias the name it actually
# produces to the one launch-ros asks for. (Other ROS_UNRESOLVED_DEP names
# exist in this layer -- see `grep -rl "ROS_UNRESOLVED_DEP-"
# layers/meta-ros/meta-ros2-lyrical/generated-recipes/` -- but they belong
# to packages nothing in base/ros-core reaches; fix each as it actually
# surfaces rather than guessing all of them up front.)
RPROVIDES:osrf-pycommon += "python3-osrf-pycommon"

# Mirror image of the osrf-pycommon case above, hit during the simulation
# image: ros-gz-sim's ROS_EXEC_DEPENDS legitimately lists "cli11" (there is
# a real openembedded rosdep mapping this time, no ROS_UNRESOLVED_DEP
# marker), but meta-openembedded's cli11 recipe deliberately packages
# itself the correct OE way for a header-only C++ library -- "cli11 is a
# header only C++ library, so the main package will be empty" (its own
# comment; RDEPENDS:${PN}-dev = "" and everything actually installable
# lives in cli11-dev). rosdep's source-package naming convention and OE's
# empty-main-package-for-header-only-libs convention just don't line up.
# Alias the package that actually has content to the name ros-gz-sim asks
# for, same reasoning as osrf-pycommon: don't hand-edit the generated recipe.
RPROVIDES:cli11-dev += "cli11"

# oeros-container-sdk-cross.bb's do_rootfs[depends] on
# ros2-image-sdktest:do_populate_sdk (meta-ros, not ours) pulls that image
# recipe's own dependency graph in too, which includes oe-core's
# packagegroup-core-boot -- a normal, expected thing for a genuine bootable
# image, which ros2-image-sdktest otherwise is. packagegroup-core-boot
# RDEPENDS on the literal runtime package name "kernel", a separate
# resolution namespace from "virtual/kernel": PREFERRED_PROVIDER_virtual/kernel
# above steers the latter to linux-dummy, but linux-dummy never claims the
# "kernel" RPROVIDES alias itself (only real kernel recipes' kernel-image
# packages do, via kernel.bbclass, which linux-dummy doesn't inherit), so
# it's invisible to this lookup and nothing satisfies it. Same fix shape as
# virtual/kernel: make linux-dummy claim this alias too, rather than pull in
# a real kernel for an SDK/toolchain container that will never boot one.
#
# The alias has to land on a package that actually gets an ipk built, not
# linux-dummy's own default ${PN} package: its do_install is a no-op, and
# unlike kernel-modules/kernel-vmlinux (linux-dummy.bb sets ALLOW_EMPTY = "1"
# on both of those, not on ${PN} itself), an empty default package produces
# no ipk at all -- confirmed via a real do_populate_sdk run, where only
# linux-dummy-dbg/-dev existed in the deploy feed and this RPROVIDES,
# originally set on linux-dummy itself, was never actually published
# anywhere for opkg to see. kernel-vmlinux is both real (ALLOW_EMPTY) and
# the semantically apt target: the package standing in for "the kernel
# image exists."
RPROVIDES:kernel-vmlinux += "kernel"
