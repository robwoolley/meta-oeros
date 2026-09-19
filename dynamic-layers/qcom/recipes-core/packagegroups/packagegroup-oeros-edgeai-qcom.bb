SUMMARY = "Qualcomm Dragonwing: QAIRT/QNN on Hexagon"
LICENSE = "MIT"
inherit packagegroup
PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "(qcs6490|qcm6490|qcs8300|qcs9100)"

# meta-qcom-hwe and meta-qcom-qim-product-sdk have no branch past scarthgap
# (4 releases behind wrynose), so the QIM GStreamer pipeline (-pipeline) and
# QRB ROS 2 offload nodes (-ros) they provide are deferred to
# feature/edgeai-amd-xilinx-lag alongside the AMD/Xilinx Vitis AI stack.
# Only -runtime ships here: it comes from meta-qcom + meta-qcom-distro,
# which both have a wrynose branch.
PACKAGES = "${PN} ${PN}-runtime ${PN}-dev"
RDEPENDS:${PN} = "${PN}-runtime"

# meta-qcom (prebuilt Hexagon/FastCV) + meta-qcom-distro
RDEPENDS:${PN}-runtime = " \
    qnn-sdk qairt-runtime snpe \
    tensorflow-lite-qnn-delegate onnxruntime-qnn-ep \
    hexagon-dsp-binaries qcom-fastcv-binaries \
"
RDEPENDS:${PN}-dev = "qnn-sdk-dev qairt-tools"
