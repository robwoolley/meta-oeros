# Nvidia Jetson (meta-tegra). MACHINE: jetson-orin-nano-devkit-super, jetson-agx-orin-devkit
OEROS_EDGEAI_VENDOR = "tegra"
MACHINE_FEATURES:append = " edgeai edgeai-gpu edgeai-cuda"
LICENSE_FLAGS_ACCEPTED:append = " commercial_tegra"
