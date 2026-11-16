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
# openvino-inference-engine is the only OpenVINO recipe in meta-openvino; its
# python3 sub-package (needs PACKAGECONFIG python3, set in oeros-edgeai-intel.inc)
# carries the Python API and the ovc model converter, so there is no separate
# openvino-model-optimizer or python3-openvino.
RDEPENDS:${PN}-runtime = "openvino-inference-engine openvino-inference-engine-python3"
RDEPENDS:${PN}-gpu = "intel-compute-runtime intel-graphics-compiler level-zero onednn intel-media-driver"
# linux-npu-driver (meta-intel) packages the NPU firmware as its -firmware sub-package
RDEPENDS:${PN}-npu = "linux-npu-driver linux-npu-driver-firmware"
# No wrynose recipe exists for ros2-openvino-toolkit or openvino-model-server
# (not in meta-openvino, meta-intel or meta-ros lyrical), so this group is empty.
RDEPENDS:${PN}-ros = ""
# Gated like the qcom -robotics group: bitbake resolves the RDEPENDS of every
# sub-package, so an ungated line made the lyrical Intel config try to resolve
# the Jazzy-only Robotics AI Suite recipes (and their known openvino-node gap).
RDEPENDS:${PN}-robotics-ai-suite = "${@bb.utils.contains('MACHINE_FEATURES', 'edgeai-robotics-ai-suite', 'packagegroup-oeros-robotics-ai-suite', '', d)}"
RDEPENDS:${PN}-dev = "openvino-inference-engine-dev opencl-headers"
