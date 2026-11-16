# Ported from Xilinx/meta-kria-apps (main, LAYERSERIES_COMPAT scarthgap; MIT,
# Copyright (C) 2026 Advanced Micro Devices, Inc.) to wrynose / Oeros:
#   - S moved from ${WORKDIR}/git to ${UNPACKDIR}/${BP} (wrynose unpack layout)
#   - proprietary (Firmware-AP1302, onsemi): only installed via the oeros/amd-proprietary opt-in

SUMMARY = "ON Semiconductor AP1302 ISP firmware blobs"
DESCRIPTION = "Pre-built AP1302 ISP firmware binaries (ar0144 and ar1335 \
single-sensor variants) loaded by the in-tree ap1302 V4L2 subdev driver \
via request_firmware() at probe time. Required for the Kria KV260 BIST \
camera pipeline."
HOMEPAGE = "https://github.com/Xilinx/ap1302-firmware"
SECTION = "firmware"

# Proprietary onsemi/AP1302 firmware license. The text shipped in the
# upstream repo (https://github.com/Xilinx/ap1302-firmware) is the
# binding agreement, not a recognised OSI license. Declare it as a
# custom firmware license so do_populate_lic stages the agreement text
# alongside the binary; no LICENSE_FLAGS gating is added because the
# upstream Xilinx repo already redistributes these blobs publicly,
# matching how meta-* layers ship comparable linux-firmware blobs.
LICENSE = "Firmware-AP1302"
NO_GENERIC_LICENSE[Firmware-AP1302] = "LICENSE.txt"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=9c13aad1aab42f76326f1beceafc40c4"

SRC_BRANCH = "main"
SRC_URI = "git://github.com/Xilinx/ap1302-firmware.git;protocol=https;branch=${SRC_BRANCH}"
SRCREV = "eafedf17d971b1bddb37e49577c7a4ba0ec6f49e"
PV = "1.0+git"

S = "${UNPACKDIR}/${BP}"

inherit allarch

# The in-tree ap1302 driver calls request_firmware() with names of the
# form "ap1302_<sensor>_<count>_fw.bin", which the kernel firmware
# loader resolves at the root of ${nonarch_base_libdir}/firmware (i.e.
# /lib/firmware) on the host. We cannot change the host layout without
# a coordinated kernel patch that moves the request_firmware() lookup to
# an ap1302/<...> subdirectory.
#
# Inside the container, however, the blobs only need to live somewhere
# stable for dfx-fw-install to rsync them onto the host. Stage them
# under ${nonarch_base_libdir}/firmware/ap1302/ so they sit next to
# (rather than colliding with) the other /lib/firmware contents of the
# image. The container-side helper is invoked as
#
#   docker run -v /lib/firmware:/opt/firmware/xilinx/ap1302 <image> \
#       dfx-fw-install --fw-name ap1302 \
#                      --src /lib/firmware/ap1302
#
# which copies the blobs flat onto the host's /lib/firmware/ as the
# ap1302 driver expects.
#
# Note: In future the driver will also look for the firmware inside
# ap1302 directory

do_install() {
    install -d ${D}${nonarch_base_libdir}/firmware/ap1302
    install -m 0644 ${S}/ap1302_ar0144_single_fw.bin \
        ${D}${nonarch_base_libdir}/firmware/ap1302/ap1302_ar0144_single_fw.bin
    install -m 0644 ${S}/ap1302_ar1335_single_fw.bin \
        ${D}${nonarch_base_libdir}/firmware/ap1302/ap1302_ar1335_single_fw.bin
}

FILES:${PN} = "${nonarch_base_libdir}/firmware/ap1302"
