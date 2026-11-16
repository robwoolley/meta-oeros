SUMMARY = "NXP i.MX: eIQ runtimes with NPU delegates"
LICENSE = "MIT"
inherit packagegroup
PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "(mx8mp-nxp-bsp|mx93-nxp-bsp|mx95-nxp-bsp)"

PACKAGES = "${PN} ${PN}-runtime ${PN}-pipeline ${PN}-dev"
RDEPENDS:${PN} = "${PN}-runtime ${PN}-pipeline"

# Package names from meta-freescale-ml / meta-imx-ml (wrynose-6.18.37-2.1.0),
# following meta-imx-ml's own packagegroup-imx-ml. The ONNX Runtime EPs
# (VSINPU on i.MX 8, Neutron on i.MX 95) are compiled into meta-imx-ml's
# onnxruntime itself via PACKAGECONFIG, so there is no separate EP package.
# Delegates vary per SoC: VX on 8M Plus, Ethos-U on 93, Neutron on 95.
OEROS_IMX_NPU = ""
OEROS_IMX_NPU:mx8mp-nxp-bsp = "tensorflow-lite-vx-delegate tim-vx"
OEROS_IMX_NPU:mx93-nxp-bsp = "tensorflow-lite-ethosu-delegate ethos-u-driver-stack ethos-u-firmware ethos-u-vela"
OEROS_IMX_NPU:mx95-nxp-bsp = "tensorflow-lite-neutron-delegate neutron"

RDEPENDS:${PN}-runtime = " \
    tensorflow-lite \
    onnxruntime \
    ${OEROS_IMX_NPU} \
"
RDEPENDS:${PN}-pipeline = "nnstreamer nnstreamer-tensorflow-lite imx-gst1.0-plugin"
# opencl-headers is left out: meta-imx-sdk's recipe and imx-gpu-viv both provide
# it on the GPU SoCs, so naming it here makes bitbake stop on the ambiguity.
# imx-gpu-sdk is COMPATIBLE_MACHINE "imxgpu" (not i.MX 93/95).
OEROS_IMX_GPUSDK = ""
OEROS_IMX_GPUSDK:imxgpu = "imx-gpu-sdk"
RDEPENDS:${PN}-dev = "tensorflow-lite-dev onnxruntime-dev ${OEROS_IMX_GPUSDK}"
