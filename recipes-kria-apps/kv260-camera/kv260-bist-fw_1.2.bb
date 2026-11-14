# Ported from Xilinx/meta-kria-apps (main, LAYERSERIES_COMPAT scarthgap; MIT,
# Copyright (C) 2026 Advanced Micro Devices, Inc.) to wrynose / Oeros:
#   - S moved from ${WORKDIR}/git to ${UNPACKDIR}/${BP} (wrynose unpack layout)
#   - kria-bist-fw.inc inlined; used here because its PL design is the
#   -   KV260 AR1335 + AP1302 MIPI capture pipeline (plus VCU); not for BIST itself
#   - proprietary (Advanced-Micro-Devices-Xilinx-binaries): only installed via the oeros/amd-proprietary opt-in

HOMEPAGE = "https://github.com/Xilinx/kria-vitis-platforms"
SECTION = "firmware"
LICENSE = "Advanced-Micro-Devices-Xilinx-binaries"
LIC_FILES_CHKSUM = "file://LICENSE;md5=17ed81f78abe63ee929646937685e8d1"
NO_GENERIC_LICENSE[Advanced-Micro-Devices-Xilinx-binaries] = "LICENSE"

FW_VERSION ?= "v1.2"
FW_BASE_URL ?= "https://github.com/Xilinx/kria-vitis-platforms/releases/download/${FW_VERSION}"

SRC_URI = "${FW_BASE_URL}/${FW_NAME}.tar.gz"

S = "${UNPACKDIR}/${FW_NAME}"

inherit allarch

do_install() {
    install -d ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_NAME}
    cp -r ${S}/lib/firmware/xilinx/${FW_NAME}/. \
        ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_NAME}/
}

FILES:${PN} = "${nonarch_base_libdir}/firmware/xilinx/${FW_NAME}"

require ../kria-firmware/kria-fw-modules-load.inc

SUMMARY = "Kria KV260 Built-In Self-Test (BIST) firmware (DT overlay + bitstream)"



FW_NAME = "kv260-bist"

SRC_URI[sha256sum] = "4282f3dc27611f74672526f95f0dd7c57328697a8feebfccbfb4e9cadff41d8a"

# Out-of-tree AR1335 sensor driver and in-tree =m modules referenced by
# the kv260-bist DT overlay (see recipes-firmware/module-dependencies.md).
RDEPENDS:${PN} += " \
    ar1335-module \
    kernel-module-ap1302 \
    kernel-module-xlnx-vcu \
    ap1302-firmware \
"

# Modules to load for the kv260-bist overlay. ap1302 (the on-board ISP)
# must be loaded before ar1335 so the ar1335 sensor binding finds the
# upstream ISP. xlnx_vcu drives the VCU encode/decode IP. Other video
# drivers (csi2rxss, demosaic, vpss-scaler, frmbuf, i2c-mux-pca954x,
# i2c-xiic, gpio-xilinx, imx219) are =y in the kernel.
KRIA_FW_MODULES = "ap1302 ar1335 xlnx_vcu"
