SUMMARY = "Vendor-neutral inference runtimes and pipeline plumbing"
LICENSE = "MIT"
inherit packagegroup

PACKAGES = "${PN} ${PN}-runtimes ${PN}-vision ${PN}-pipeline ${PN}-python ${PN}-tools"

RDEPENDS:${PN} = "${PN}-runtimes ${PN}-vision ${PN}-pipeline ${PN}-python"

# meta-onnxruntime / meta-tensorflow-lite; vendor EPs are added in the vendor groups
RDEPENDS:${PN}-runtimes = " \
    onnxruntime \
    tensorflow-lite \
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

RDEPENDS:${PN}-python = " \
    python3-numpy \
    python3-onnxruntime \
    python3-tensorflow-lite \
    python3-opencv \
    python3-pillow \
"

# Optional: on-device profiling/debug
RDEPENDS:${PN}-tools = " \
    onnxruntime-tools \
    perf \
    htop \
"
