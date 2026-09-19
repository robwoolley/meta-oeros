SUMMARY = "TI Sitara/Jacinto: TIDL on C7x/MMA + Edge AI GStreamer + robotics kit"
LICENSE = "MIT"
inherit packagegroup
PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "(am62axx|am68|am69|j7)"

PACKAGES = "${PN} ${PN}-runtime ${PN}-pipeline ${PN}-ros ${PN}-dev"
RDEPENDS:${PN} = "${PN}-runtime ${PN}-pipeline ${PN}-ros"

RDEPENDS:${PN}-runtime = " \
    tidl-rt ti-vision-apps tiovx \
    tensorflow-lite-tidl onnxruntime-tidl \
    edgeai-tidl-tools ti-rpmsg-char \
"
RDEPENDS:${PN}-pipeline = "edgeai-gst-plugins edgeai-gst-apps edgeai-tiovx-modules edgeai-tiovx-kernels"
# TI ships ROS 2 nodes in meta-edgeai's own ros layer; these map onto meta-ros names
RDEPENDS:${PN}-ros = "edgeai-robotics-kit ti-vision-cnn ti-estop"
RDEPENDS:${PN}-dev = "tiovx-dev ti-vision-apps-dev"
