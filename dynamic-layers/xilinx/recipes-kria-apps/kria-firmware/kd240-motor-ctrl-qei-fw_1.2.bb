# Ported from Xilinx/meta-kria-apps (main, LAYERSERIES_COMPAT scarthgap; MIT,
# Copyright (C) 2026 Advanced Micro Devices, Inc.) to wrynose / Oeros:
#   - S moved from ${WORKDIR}/git to ${UNPACKDIR}/${BP} (wrynose unpack layout)
#   - proprietary (Advanced-Micro-Devices-Xilinx-binaries): only installed via the
#   -   oeros/amd-proprietary opt-in (packagegroup-oeros-kria-apps-proprietary)

SUMMARY = "Kria KD240 FOC motor-control (QEI) firmware (DT overlay + bitstream)"
HOMEPAGE = "https://github.com/Xilinx/kria-vitis-platforms"
SECTION = "firmware"
# Released tarball bundles AMD/Xilinx binary-only license at
# <fw-name>/LICENSE; declare it via NO_GENERIC_LICENSE since the license
# text is not part of poky's COMMON_LICENSE_DIR.
LICENSE = "Advanced-Micro-Devices-Xilinx-binaries"
LIC_FILES_CHKSUM = "file://LICENSE;md5=17ed81f78abe63ee929646937685e8d1"
NO_GENERIC_LICENSE[Advanced-Micro-Devices-Xilinx-binaries] = "LICENSE"

FW_NAME = "kd240-motor-ctrl-qei"
FW_VERSION = "v1.2"
FW_BASE_URL = "https://github.com/Xilinx/kria-vitis-platforms/releases/download/${FW_VERSION}"

SRC_URI = "${FW_BASE_URL}/${FW_NAME}.tar.gz"
SRC_URI[sha256sum] = "b770918838e7b53ee618d0c92f9fd133a0be8d022fe737277c50fa90691d298e"

S = "${UNPACKDIR}/${FW_NAME}"

inherit allarch

do_install() {
    install -d ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_NAME}
    cp -r ${S}/lib/firmware/xilinx/${FW_NAME}/. \
        ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_NAME}/
}

FILES:${PN} = "${nonarch_base_libdir}/firmware/xilinx/${FW_NAME}"

# Out-of-tree IIO drivers and in-tree =m modules referenced by the
# kd240-motor-ctrl-qei DT overlay (see recipes-firmware/module-dependencies.md).
RDEPENDS:${PN} += " \
    motor-control-iio-modules \
    kernel-module-uio-pdrv-genirq \
    kernel-module-amd-axi-w1 \
    kernel-module-w1-therm \
"

# Modules to load for the kd240-motor-ctrl-qei overlay. uio_pdrv_genirq
# must come first so generic-uio nodes can bind. amd_axi_w1 drives the
# AXI 1-Wire master IP; w1_therm must follow it so the 1-Wire thermal
# slaves bind and expose their hwmon temp inputs. The TSN driver set is
# built into the kernel (CONFIG_XILINX_TSN=y) and does not need loading.
KRIA_FW_MODULES = "uio_pdrv_genirq amd_axi_w1 w1_therm xilinx-adc-hub hls-foc-periodic hls-pwm-gen hls-qei-axi hls-svpwm-duty"

require kria-fw-modules-load.inc
