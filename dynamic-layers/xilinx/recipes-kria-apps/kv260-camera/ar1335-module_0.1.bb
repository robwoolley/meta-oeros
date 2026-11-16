# Ported from Xilinx/meta-kria-apps (main, LAYERSERIES_COMPAT scarthgap; MIT,
# Copyright (C) 2026 Advanced Micro Devices, Inc.) to wrynose / Oeros:
#   - S moved from ${WORKDIR}/git to ${UNPACKDIR}/${BP} (wrynose unpack layout)
#   - COMPATIBLE_MACHINE limited to the Kria SOMs

SUMMARY = "Out-of-tree AR1335 image sensor V4L2 subdev driver"
DESCRIPTION = "Kernel module providing a V4L2 subdevice driver for the \
onsemi AR1335 13MP image sensor used on Kria carrier cameras."
HOMEPAGE = "https://github.com/Xilinx/ar1335-module"
SECTION = "kernel/modules"
LICENSE = "GPL-2.0-only"
# LICENSE lives at the repo root, while sources are under src/. Reference it
# relative to the unpacked tree so do_package can stage it without warnings.
LIC_FILES_CHKSUM = "file://../LICENSE;md5=7aaf58a37e0aa851bb718d75c1734884"

# Upstream has no tags; pin to a known-good main commit.
SRC_BRANCH = "main"
SRC_URI = "git://github.com/Xilinx/ar1335-module.git;protocol=https;branch=${SRC_BRANCH}"
SRCREV = "a4f6b8c4b960a62f0f179a741b1e4d9642a1902d"
PV = "0.1+git"
COMPATIBLE_MACHINE = "^$"
COMPATIBLE_MACHINE:kria = "${MACHINE}"

S = "${UNPACKDIR}/${BP}/src"

inherit module

INHIBIT_PACKAGE_STRIP = "1"

RDEPENDS:${PN} += "kernel-module-ar1335"
