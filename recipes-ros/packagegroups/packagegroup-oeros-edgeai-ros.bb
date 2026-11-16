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
# provider is selected at runtime by OEROS_EDGEAI_VENDOR. There is no recipe
# for it yet (docs/edgeai.md, "oeros-inference-node: options"), and an
# RDEPENDS on a name nothing provides fails the whole world build, including
# yocto-check-layer. Set OEROS_INFERENCE_NODE = "oeros-inference-node" (or the
# chosen provider) once the recipe exists; it is empty until then.
OEROS_INFERENCE_NODE ??= ""
RDEPENDS:${PN}-inference = " \
    ${OEROS_INFERENCE_NODE} \
    vision-msgs-rviz-plugins \
"
