SUMMARY = "NXP i.MX: eIQ runtimes with NPU delegates"
LICENSE = "MIT"
inherit packagegroup
PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "(mx8mp-nxp-bsp|mx93-nxp-bsp|mx95-nxp-bsp)"

PACKAGES = "${PN} ${PN}-runtime ${PN}-pipeline ${PN}-dev"
RDEPENDS:${PN} = "${PN}-runtime ${PN}-pipeline"

# meta-imx-ml package names; delegates vary per SoC (VX on 8MP, Ethos-U on 93, Neutron on 95)
RDEPENDS:${PN}-runtime = " \
    tensorflow-lite tensorflow-lite-vx-delegate \
    onnxruntime onnxruntime-vsinpu-ep \
    tim-vx nn-imx \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ethosu', 'ethos-u-driver-stack ethos-u-firmware', '', d)} \
    eiq-toolkit-inference \
"
RDEPENDS:${PN}-pipeline = "nnstreamer nnstreamer-tensorflow-lite gstreamer1.0-plugins-imx"
RDEPENDS:${PN}-dev = "tensorflow-lite-dev onnxruntime-dev opencl-headers imx-gpu-sdk"
