SUMMARY = "Nvidia Jetson: CUDA/TensorRT/VPI/DeepStream + Isaac ROS"
LICENSE = "MIT"
inherit packagegroup
PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "(tegra)"

PACKAGES = "${PN} ${PN}-runtime ${PN}-multimedia ${PN}-ros ${PN}-dev"
# Everything below is NVIDIA-licensed (cuDNN, TensorRT, libnv*, DeepStream)
# or built against it, so the whole vendor stack is opt-in: it is only
# pulled in with the edgeai-nvidia-proprietary MACHINE_FEATURE that the
# oeros/nvidia-proprietary fragment sets. Each sub-package's own RDEPENDS
# is gated too, since bitbake resolves every sub-package of a packagegroup.
OEROS_NV = "${@bb.utils.contains('MACHINE_FEATURES', 'edgeai-nvidia-proprietary', '1', '', d)}"
RDEPENDS:${PN} = "${@'${PN}-runtime ${PN}-multimedia ${PN}-ros' if d.getVar('OEROS_NV') else ''}"

OEROS_NV_RUNTIME = " \
    cuda-cudart cuda-libraries cudnn tensorrt-core tensorrt-plugins \
    onnxruntime-tensorrt-ep \
    tegra-libraries-multimedia vpi \
"
# DeepStream comes from meta-tegra-community, whose recipe name carries
# the major version (deepstream-9.1); meta-tegra itself has no deepstream.
OEROS_NV_MULTIMEDIA = " \
    gstreamer1.0-plugins-nvvideo4linux2 gstreamer1.0-plugins-nvarguscamerasrc \
    deepstream-9.1 nvidia-container-runtime \
"
# Isaac ROS built from source against meta-tegra TensorRT (recipes in this layer)
OEROS_NV_ROS = " \
    isaac-ros-nitros isaac-ros-dnn-inference isaac-ros-tensor-rt \
    isaac-ros-image-proc isaac-ros-visual-slam \
"
OEROS_NV_DEV = "cuda-nvcc cuda-toolkit tensorrt-dev cudnn-dev"
RDEPENDS:${PN}-runtime = "${@d.getVar('OEROS_NV_RUNTIME') if d.getVar('OEROS_NV') else ''}"
RDEPENDS:${PN}-multimedia = "${@d.getVar('OEROS_NV_MULTIMEDIA') if d.getVar('OEROS_NV') else ''}"
RDEPENDS:${PN}-ros = "${@d.getVar('OEROS_NV_ROS') if d.getVar('OEROS_NV') else ''}"
RDEPENDS:${PN}-dev = "${@d.getVar('OEROS_NV_DEV') if d.getVar('OEROS_NV') else ''}"
