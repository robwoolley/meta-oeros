# AMD/Xilinx adaptive SoCs (meta-xilinx + meta-kria + meta-amd-adaptive-socs,
# rel-v2026.2, wrynose). MACHINE: k26-smk-kr-sdt (KR260), k26-smk-kv-sdt
# (KV260), k24-smk-kd-sdt (KD240), versal-vek280-sdt-seg (VEK280),
# versal-2ve-2vm-vek385-revb-sdt-seg (VEK385 Rev B).
#
# Unlike the Qualcomm/Nvidia .inc files, this one deliberately does NOT add
# anything to LICENSE_FLAGS_ACCEPTED or pull in proprietary blobs. The
# default AMD build is open source only: XRT/zocl, dfx-mgr, the AI Engine
# driver, Lima (Mesa) on the Kria Mali-400. Proprietary firmware and
# drivers (VCU/VCU2 firmware, libmali, Kria starter-kit PL firmware,
# Vitis AI) are an explicit opt-in via the oeros/amd-proprietary fragment;
# see docs/amd-xilinx.md.
OEROS_EDGEAI_VENDOR = "xilinx"
MACHINE_FEATURES:append = " edgeai edgeai-fpga edgeai-xrt"
# The Versal AI Edge parts carry AI Engine (AIE-ML / AIE-MLv2) arrays
MACHINE_FEATURES:append:versal = " edgeai-aie"
MACHINE_FEATURES:append:versal-2ve-2vm = " edgeai-aie"
