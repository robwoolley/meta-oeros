SUMMARY = "TI Sitara/Jacinto: TIDL on C7x/MMA + Edge AI GStreamer + robotics kit"
LICENSE = "MIT"
inherit packagegroup
PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "(am62axx|am68|am69|j7)"

PACKAGES = "${PN} ${PN}-runtime ${PN}-pipeline ${PN}-ros ${PN}-dev"
RDEPENDS:${PN} = "${PN}-runtime ${PN}-pipeline ${PN}-ros"

# meta-edgeai: ti-tidl-osrt = TFLite/ONNX Runtime/TVM runtimes with TIDL
# (COMPATIBLE_MACHINE j721e|j721s2|j784s4|j722s|j742s2|am62axx|am62dxx).
# ti-tidl = TIDL-RT, the TFLite delegate and the ONNX Runtime EP built from
# arm-tidl; it is COMPATIBLE_MACHINE am62axx only. The names tidl-rt,
# tensorflow-lite-tidl and onnxruntime-tidl do not exist in any layer.
OEROS_TI_TIDL = ""
OEROS_TI_TIDL:am62axx = "ti-tidl"
RDEPENDS:${PN}-runtime = " \
    ti-vision-apps ti-tidl-osrt ${OEROS_TI_TIDL} \
    ti-rpmsg-char \
"
RDEPENDS:${PN}-pipeline = "edgeai-gst-plugins edgeai-gst-apps edgeai-tiovx-modules edgeai-tiovx-kernels"
# edgeai-robotics-kit, ti-vision-cnn and ti-estop (TI's Robotics SDK ROS 2
# nodes) have no recipe in meta-edgeai, meta-ti, meta-tisdk or meta-arago, so
# this group is empty until they are packaged.
RDEPENDS:${PN}-ros = ""
RDEPENDS:${PN}-dev = "ti-vision-apps-dev"
