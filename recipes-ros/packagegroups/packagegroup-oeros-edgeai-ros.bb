SUMMARY = "ROS 2 perception/inference interfaces (meta-ros, ROS_DISTRO=${ROS_DISTRO})"
LICENSE = "MIT"
inherit packagegroup ros_distro_${ROS_DISTRO}

PACKAGES = "${PN} ${PN}-msgs ${PN}-image ${PN}-inference"
RDEPENDS:${PN} = "${PN}-msgs ${PN}-image ${PN}-inference"

RDEPENDS:${PN}-msgs = " \
    vision-msgs \
    sensor-msgs \
    geometry-msgs \
    stereo-msgs \
"

RDEPENDS:${PN}-image = " \
    cv-bridge \
    image-transport \
    image-transport-plugins \
    image-pipeline \
    camera-info-manager \
    v4l2-camera \
    gscam \
"

# oeros-inference-node: an ONNX Runtime node in this layer whose execution
# provider is selected at runtime by OEROS_EDGEAI_VENDOR
RDEPENDS:${PN}-inference = " \
    oeros-inference-node \
    vision-msgs-rviz-plugins \
"
