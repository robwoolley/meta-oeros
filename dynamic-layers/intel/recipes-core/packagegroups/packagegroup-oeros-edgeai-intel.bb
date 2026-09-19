SUMMARY = "Intel Core Ultra/Atom: OpenVINO on CPU/iGPU/NPU + oneAPI"
LICENSE = "MIT"
inherit packagegroup
PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "(intel-corei7-64|intel-skylake-64)"

PACKAGES = "${PN} ${PN}-runtime ${PN}-gpu ${PN}-npu ${PN}-ros ${PN}-dev"
RDEPENDS:${PN} = "${PN}-runtime ${PN}-gpu ${PN}-npu ${PN}-ros"

RDEPENDS:${PN}-runtime = "openvino-inference-engine openvino-model-optimizer onnxruntime-openvino-ep python3-openvino"
RDEPENDS:${PN}-gpu = "intel-compute-runtime intel-graphics-compiler level-zero onednn intel-media-driver"
RDEPENDS:${PN}-npu = "intel-npu-driver linux-firmware-intel-vpu"
RDEPENDS:${PN}-ros = "ros2-openvino-toolkit openvino-model-server"
RDEPENDS:${PN}-dev = "openvino-inference-engine-dev opencl-headers"
