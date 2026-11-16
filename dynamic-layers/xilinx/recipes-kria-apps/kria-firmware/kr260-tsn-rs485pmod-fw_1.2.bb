# Ported from Xilinx/meta-kria-apps (main, LAYERSERIES_COMPAT scarthgap; MIT,
# Copyright (C) 2026 Advanced Micro Devices, Inc.) to wrynose / Oeros:
#   - S moved from ${WORKDIR}/git to ${UNPACKDIR}/${BP} (wrynose unpack layout)
#   - proprietary (Advanced-Micro-Devices-Xilinx-binaries): only installed via the
#   -   oeros/amd-proprietary opt-in (packagegroup-oeros-kria-apps-proprietary)

SUMMARY = "Kria KR260 TSN + RS485 PMOD firmware (DT overlay + bitstream)"
HOMEPAGE = "https://github.com/Xilinx/kria-vitis-platforms"
SECTION = "firmware"
# Released tarball bundles AMD/Xilinx binary-only license at
# <fw-name>/LICENSE; declare it via NO_GENERIC_LICENSE since the license
# text is not part of poky's COMMON_LICENSE_DIR.
LICENSE = "Advanced-Micro-Devices-Xilinx-binaries"
LIC_FILES_CHKSUM = "file://LICENSE;md5=17ed81f78abe63ee929646937685e8d1"
NO_GENERIC_LICENSE[Advanced-Micro-Devices-Xilinx-binaries] = "LICENSE"

FW_NAME = "kr260-tsn-rs485pmod"
FW_VERSION = "v1.2"
FW_BASE_URL = "https://github.com/Xilinx/kria-vitis-platforms/releases/download/${FW_VERSION}"

SRC_URI = "${FW_BASE_URL}/${FW_NAME}.tar.gz"
SRC_URI[sha256sum] = "16d41e613ee71e748acb774a6eef9c8147fba997a4f8d6f5b06058649f9b3856"

S = "${UNPACKDIR}/${FW_NAME}"

inherit allarch

do_install() {
    install -d ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_NAME}
    cp -r ${S}/lib/firmware/xilinx/${FW_NAME}/. \
        ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_NAME}/
}

FILES:${PN} = "${nonarch_base_libdir}/firmware/xilinx/${FW_NAME}"

# In-tree =m modules referenced by the kr260-tsn-rs485pmod DT overlay
# (see recipes-firmware/module-dependencies.md). The TSN driver itself is
# built into the kernel (CONFIG_XILINX_TSN=y), so no TSN module is listed.
RDEPENDS:${PN} += " \
    kernel-module-uio-pdrv-genirq \
    kernel-module-mcp251x \
"

# Modules to load for the kr260-tsn-rs485pmod overlay. uio_pdrv_genirq
# must come first so generic-uio nodes can bind, then mcp251x drives
# the SPI-attached MCP25625 CAN transceiver on the PMOD. TSN, axi-mcdma,
# uartlite, spi-xilinx and xps-intc are =y in the kernel.
KRIA_FW_MODULES = "uio_pdrv_genirq mcp251x"

require kria-fw-modules-load.inc
