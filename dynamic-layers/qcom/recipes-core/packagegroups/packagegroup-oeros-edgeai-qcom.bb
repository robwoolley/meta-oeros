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
# The Qualcomm-licensed binaries (QAIRT, FastCV, Hexagon DSP) are opt-in via
# the oeros/qcom-proprietary fragment (MACHINE_FEATURE
# edgeai-qcom-proprietary). Sub-package RDEPENDS are gated too, since
# bitbake resolves every sub-package of a packagegroup.
OEROS_QC = "${@bb.utils.contains('MACHINE_FEATURES', 'edgeai-qcom-proprietary', '1', '', d)}"
RDEPENDS:${PN} = " \
    ${@'${PN}-runtime' if d.getVar('OEROS_QC') else ''} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'edgeai-robotics-sdk', '${PN}-robotics', '', d)} \
"

# meta-qcom (prebuilt Hexagon/FastCV) + meta-qcom-distro
# qairt-sdk (meta-qcom recipes-ml) carries QNN, SNPE and the HTP backend
# libraries; onnxruntime-qnn is the ONNX Runtime QNN plugin EP from
# meta-qcom's dynamic-layers/ai, parsed only when qualcomm-linux/meta-ai
# (collection "ai") is in BBLAYERS. The per-SoC qairt-sdk-hexagon-vNN skel
# libraries, hexagon-dsp-binaries-<board>-* and the FastCV binaries are
# already pulled in by each meta-qcom MACHINE's
# MACHINE_ESSENTIAL_EXTRA_RRECOMMENDS, so they are not repeated here.
# qairt-sdk ships no TFLite QNN delegate on wrynose.
OEROS_QC_RUNTIME = " \
    qairt-sdk \
    onnxruntime-qnn \
"

# meta-qcom-robotics-sdk packagegroups: community ROS 2 + Nav2/MoveIt2
# (packagegroup-qcom-ros2), QRB-ROS open-source nodes + community robotics
# packages (packagegroup-robotics-opensource), and QRB-ROS nodes/samples
# that are open-source but need the proprietary QNN/camera stack already
# pulled in via -runtime (packagegroup-oss-with-prop-deps).
RDEPENDS:${PN}-robotics = " \
    packagegroup-qcom-ros2 \
    packagegroup-robotics-opensource \
    ${@'packagegroup-oss-with-prop-deps' if d.getVar('OEROS_QC') else ''} \
"

OEROS_QC_DEV = "qairt-sdk-dev"
RDEPENDS:${PN}-runtime = "${@d.getVar('OEROS_QC_RUNTIME') if d.getVar('OEROS_QC') else ''}"
RDEPENDS:${PN}-dev = "${@d.getVar('OEROS_QC_DEV') if d.getVar('OEROS_QC') else ''}"
