# yolo (recipes-robotics-ai-suite/yolo) unconditionally calls
# cv::dnn::blobFromImage(), but opencv's "dnn" PACKAGECONFIG option is not
# in the default PACKAGECONFIG set -- do_compile fails with "'cv::dnn' has
# not been declared". Only enable it for machines that actually need the
# Robotics AI Suite, rather than growing every OpenCV build's dependency
# closet (protobuf/protobuf-native) and build time distro-wide.
PACKAGECONFIG:append = "${@bb.utils.contains('MACHINE_FEATURES', 'edgeai-robotics-ai-suite', ' dnn', '', d)}"
