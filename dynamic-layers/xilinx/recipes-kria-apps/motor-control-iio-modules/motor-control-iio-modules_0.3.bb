# Ported from Xilinx/meta-kria-apps (main, LAYERSERIES_COMPAT scarthgap; MIT,
# Copyright (C) 2026 Advanced Micro Devices, Inc.) to wrynose / Oeros:
#   - S moved from ${WORKDIR}/git to ${UNPACKDIR}/${BP} (wrynose unpack layout)
#   - COMPATIBLE_MACHINE limited to the Kria SOMs (out-of-tree modules for linux-xlnx)

SUMMARY = "Out-of-tree IIO kernel modules for FOC motor control"
DESCRIPTION = "IIO framework drivers for the Kria KD240 motor control \
subsystem: ADC hub, FOC periodic, QEI, PWM generator, and SVPWM duty."
HOMEPAGE = "https://github.com/Xilinx/motor-control-iio-modules"
SECTION = "kernel/modules"
LICENSE = "GPL-2.0-only"
# LICENSE lives at the repo root, while sources are under src/. Reference it
# relative to the unpacked tree so do_package can stage it without warnings.
LIC_FILES_CHKSUM = "file://../LICENSE;md5=7d3d4519e1b227873d840232a002e048"

SRC_BRANCH = "main"
SRC_URI = "git://github.com/Xilinx/motor-control-iio-modules.git;protocol=https;branch=${SRC_BRANCH}"
# v0.3 release (commit 3900e31). SRCREV is pinned to the immutable commit
# for reproducibility; the tag is noted here for readability.
SRCREV = "3900e31dce144199c42a528bbce93ede2946a690"
PV = "0.3+git"
COMPATIBLE_MACHINE = "^$"
COMPATIBLE_MACHINE:kria = "${MACHINE}"

S = "${UNPACKDIR}/${BP}/src"

inherit module

INHIBIT_PACKAGE_STRIP = "1"

# The kernel-module-split bbclass produces one package per .ko under names
# like kernel-module-<basename>. Pull all of them in via the recipe wrapper
# so listing 'motor-control-iio-modules' in IMAGE_INSTALL / CONTAINER_APP
# is enough to install the full driver set.
RDEPENDS:${PN} += " \
    kernel-module-xilinx-adc-hub \
    kernel-module-hls-foc-periodic \
    kernel-module-hls-pwm-gen \
    kernel-module-hls-qei-axi \
    kernel-module-hls-svpwm-duty \
"
