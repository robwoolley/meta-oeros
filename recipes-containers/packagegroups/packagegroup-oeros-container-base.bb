# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "Minimal userland shared by every OEROS container image"
DESCRIPTION = "The smallest set of packages that makes a container rootfs \
usable: an account database, name resolution, a shell, a CA bundle and time \
zone data.  Deliberately excludes any init system -- nothing in a container \
runs systemd or sysvinit, and packagegroup-core-boot would drag in a kernel, \
udev and the boot scripts along with it."

inherit packagegroup

RDEPENDS:${PN} = " \
    base-files \
    base-passwd \
    netbase \
    busybox \
    bash \
    ca-certificates \
    tzdata \
    ros-entrypoint \
"
