SUMMARY = "Intel Robotics AI Suite: ROS 2 perception/navigation components (OpenVINO-based)"
LICENSE = "MIT"
inherit packagegroup
PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "(intel-corei7-64|intel-skylake-64)"

PACKAGES = "${PN} ${PN}-perception ${PN}-navigation ${PN}-tutorials"

RDEPENDS:${PN} = "${PN}-perception ${PN}-navigation"

# YOLOv8 + OpenVINO object detection/segmentation node, adaptive DBSCAN clustering
RDEPENDS:${PN}-perception = "yolo yolo-msgs adbscan-ros2"

# Nav2-based wandering demo (validates the Nav2 dependency chain end to end)
RDEPENDS:${PN}-navigation = "wandering-app"

# Launch-bundle demos. object-detection-tutorial is currently unbuildable:
# it RDEPENDS on "openvino-node", which does not exist anywhere in this
# distro's layers, nor as any package name in the real upstream
# intel/ros2_openvino_toolkit repo (its actual packages are
# openvino_wrapper_lib/openvino_msgs/openvino_param_lib) -- see
# docs/edgeai.md. segmentation-realsense-tutorial has no such gap and
# builds cleanly.
RDEPENDS:${PN}-tutorials = "object-detection-tutorial segmentation-realsense-tutorial"
