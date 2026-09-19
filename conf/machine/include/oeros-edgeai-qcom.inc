# Qualcomm Dragonwing (meta-qcom + meta-qcom-distro; QIM product SDK pipeline/ROS
# nodes deferred to feature/edgeai-amd-xilinx-lag, see packagegroup-oeros-edgeai-qcom.bb).
# MACHINE: qcs6490-rb3gen2-core-kit, qcs9100-ride-sx
OEROS_EDGEAI_VENDOR = "qcom"
MACHINE_FEATURES:append = " edgeai edgeai-npu edgeai-hexagon"
LICENSE_FLAGS_ACCEPTED:append = " commercial"
