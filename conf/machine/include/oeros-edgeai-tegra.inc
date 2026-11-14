# Nvidia Jetson (meta-tegra). MACHINE: jetson-orin-nano-devkit (defaults to the
# Orin Nano Super nvpmodel/devicetree on this meta-tegra release -- there is
# no separate "-super" MACHINE), jetson-agx-orin-devkit
OEROS_EDGEAI_VENDOR = "tegra"
MACHINE_FEATURES:append = " edgeai edgeai-gpu edgeai-cuda"
# No license flags here: the proprietary CUDA/TensorRT/DeepStream stack is
# opt-in via the oeros/nvidia-proprietary fragment (docs/edgeai.md).
