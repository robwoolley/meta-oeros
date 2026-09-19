SUMMARY = "Nvidia Jetson: CUDA/TensorRT/VPI/DeepStream + Isaac ROS"
LICENSE = "MIT"
inherit packagegroup
PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "(tegra)"

PACKAGES = "${PN} ${PN}-runtime ${PN}-multimedia ${PN}-ros ${PN}-dev"
RDEPENDS:${PN} = "${PN}-runtime ${PN}-multimedia ${PN}-ros"

RDEPENDS:${PN}-runtime = " \
    cuda-cudart cuda-libraries cudnn tensorrt-core tensorrt-plugins \
    onnxruntime-tensorrt-ep \
    tegra-libraries-multimedia vpi \
"
RDEPENDS:${PN}-multimedia = " \
    gstreamer1.0-plugins-nvvideo4linux2 gstreamer1.0-plugins-nvarguscamerasrc \
    deepstream nvidia-container-runtime \
"
# Isaac ROS built from source against meta-tegra TensorRT (recipes in this layer)
RDEPENDS:${PN}-ros = " \
    isaac-ros-nitros isaac-ros-dnn-inference isaac-ros-tensor-rt \
    isaac-ros-image-proc isaac-ros-visual-slam \
"
RDEPENDS:${PN}-dev = "cuda-nvcc cuda-toolkit tensorrt-dev cudnn-dev"
