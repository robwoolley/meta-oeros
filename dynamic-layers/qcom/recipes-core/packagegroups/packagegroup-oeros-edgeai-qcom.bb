SUMMARY = "Qualcomm Dragonwing: QAIRT/QNN on Hexagon + QIR robotics SDK"
LICENSE = "MIT"
inherit packagegroup
PACKAGE_ARCH = "${MACHINE_ARCH}"
# Substring match against MACHINE: qcs6490-rb3gen2-core-kit and
# qcs9100-ride-sx embed their SOC family in the MACHINE name, but the
# QIR SDK boards (iq-9075-evk = qcs9100 family, iq-8275-evk = qcs8300
# family) don't, so they're listed explicitly.
COMPATIBLE_MACHINE = "(qcs6490|qcm6490|qcs8300|qcs9100|iq-9075-evk|iq-8275-evk)"

# meta-qcom-hwe and meta-qcom-qim-product-sdk have no branch past scarthgap
# (4 releases behind wrynose), so the QIM GStreamer pipeline (-pipeline) and
# QRB ROS 2 offload nodes (-ros) they provide are deferred to
# feature/edgeai-amd-xilinx-lag alongside the AMD/Xilinx Vitis AI stack.
# Only -runtime ships here: it comes from meta-qcom + meta-qcom-distro,
# which both have a wrynose branch.
#
# -robotics is a SEPARATE, wrynose-ready addition (not part of the deferred
# QIM SDK): meta-qcom-robotics-sdk (the Qualcomm Intelligent Robotics SDK,
# "QIR SDK") has its own wrynose branch and ships real qrb-ros-* nodes,
# Nav2/MoveIt2 integration, and community robotics packages. It only
# officially supports iq-9075-evk/iq-8275-evk (its own CI), so it's gated
# on the edgeai-robotics-sdk MACHINE_FEATURE those two machines' fragments
# set, not on the whole qcom vendor family.
PACKAGES = "${PN} ${PN}-runtime ${PN}-robotics ${PN}-dev"
RDEPENDS:${PN} = " \
    ${PN}-runtime \
    ${@bb.utils.contains('MACHINE_FEATURES', 'edgeai-robotics-sdk', '${PN}-robotics', '', d)} \
"

# meta-qcom (prebuilt Hexagon/FastCV) + meta-qcom-distro
RDEPENDS:${PN}-runtime = " \
    qnn-sdk qairt-runtime snpe \
    tensorflow-lite-qnn-delegate onnxruntime-qnn-ep \
    hexagon-dsp-binaries qcom-fastcv-binaries \
"

# meta-qcom-robotics-sdk packagegroups: community ROS 2 + Nav2/MoveIt2
# (packagegroup-qcom-ros2), QRB-ROS open-source nodes + community robotics
# packages (packagegroup-robotics-opensource), and QRB-ROS nodes/samples
# that are open-source but need the proprietary QNN/camera stack already
# pulled in via -runtime (packagegroup-oss-with-prop-deps).
RDEPENDS:${PN}-robotics = " \
    packagegroup-qcom-ros2 \
    packagegroup-robotics-opensource \
    packagegroup-oss-with-prop-deps \
"

RDEPENDS:${PN}-dev = "qnn-sdk-dev qairt-tools"
