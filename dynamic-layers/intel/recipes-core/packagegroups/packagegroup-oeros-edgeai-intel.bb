SUMMARY = "Intel Core Ultra/Atom: OpenVINO on CPU/iGPU/NPU + oneAPI"
LICENSE = "MIT"
inherit packagegroup
PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "(intel-corei7-64|intel-skylake-64)"

# -robotics-ai-suite is gated on the edgeai-robotics-ai-suite MACHINE_FEATURE,
# set only by oeros/machine-intel-corei7-64-robotics-ai-suite (the ROS 2
# Jazzy variant, on the separate oeros-wrynose-jazzy config) -- not every
# Intel machine using this vendor packagegroup should pull in the suite.
# See docs/edgeai.md for what's in it and what's deferred.
PACKAGES = "${PN} ${PN}-runtime ${PN}-gpu ${PN}-npu ${PN}-ros ${PN}-robotics-ai-suite ${PN}-dev"
RDEPENDS:${PN} = " \
    ${PN}-runtime \
    ${PN}-gpu \
    ${PN}-npu \
    ${PN}-ros \
    ${@bb.utils.contains('MACHINE_FEATURES', 'edgeai-robotics-ai-suite', '${PN}-robotics-ai-suite', '', d)} \
"

# No layer on wrynose builds the ONNX Runtime OpenVINO EP (meta-openvino has
# only openvino-inference-engine), so it is not listed; ONNX models run on
# OpenVINO directly or on the ORT CPU EP. openvino-model-optimizer is a
# known pre-existing break (docs/edgeai.md) left for a separate fix.
RDEPENDS:${PN}-runtime = "openvino-inference-engine openvino-model-optimizer python3-openvino"
RDEPENDS:${PN}-gpu = "intel-compute-runtime intel-graphics-compiler level-zero onednn intel-media-driver"
RDEPENDS:${PN}-npu = "intel-npu-driver linux-firmware-intel-vpu"
RDEPENDS:${PN}-ros = "ros2-openvino-toolkit openvino-model-server"
RDEPENDS:${PN}-robotics-ai-suite = "packagegroup-oeros-robotics-ai-suite"
RDEPENDS:${PN}-dev = "openvino-inference-engine-dev opencl-headers"
