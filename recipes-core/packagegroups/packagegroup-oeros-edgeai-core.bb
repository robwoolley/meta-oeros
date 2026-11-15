SUMMARY = "Vendor-neutral inference runtimes and pipeline plumbing"
LICENSE = "MIT"
inherit packagegroup

# OEROS_EDGEAI_TFLITE* below depend on MACHINE
PACKAGE_ARCH = "${MACHINE_ARCH}"

# TensorFlow Lite package names. The default is meta-tensorflow-lite
# (C++ library and Python bindings in separate packages). A vendor whose
# BSP ships its own TFLite build that its NPU delegates link against
# overrides these in conf/machine/include/oeros-edgeai-<vendor>.inc, so an
# image doesn't carry two TFLite builds (see docs/edgeai.md).
OEROS_EDGEAI_TFLITE ?= "libtensorflow-lite"
OEROS_EDGEAI_TFLITE_PYTHON ?= "python3-tensorflow-lite"

PACKAGES = "${PN} ${PN}-runtimes ${PN}-vision ${PN}-pipeline ${PN}-python ${PN}-tools"

RDEPENDS:${PN} = "${PN}-runtimes ${PN}-vision ${PN}-pipeline ${PN}-python"

# meta-onnxruntime / meta-tensorflow-lite; vendor EPs are added in the vendor groups
RDEPENDS:${PN}-runtimes = " \
    onnxruntime \
    ${OEROS_EDGEAI_TFLITE} \
    flatbuffers \
    protobuf \
"

RDEPENDS:${PN}-vision = " \
    opencv \
    opencv-apps \
    libjpeg-turbo \
    libpng \
"

# GStreamer is the common AI pipeline substrate on NXP/TI/Qualcomm
RDEPENDS:${PN}-pipeline = " \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-good \
    gstreamer1.0-plugins-bad \
    v4l-utils \
"

# The ONNX Runtime Python bindings ship inside the onnxruntime package
# (meta-onnxruntime installs the wheel into ${PN}); there is no separate
# python3-onnxruntime.
RDEPENDS:${PN}-python = " \
    python3-numpy \
    onnxruntime \
    ${OEROS_EDGEAI_TFLITE_PYTHON} \
    python3-opencv \
    python3-pillow \
"

# Optional: on-device profiling/debug. onnxruntime-tools is split out of
# the onnxruntime recipe by dynamic-layers/meta-onnxruntime/.../onnxruntime_%.bbappend
RDEPENDS:${PN}-tools = " \
    onnxruntime-tools \
    perf \
    htop \
"
