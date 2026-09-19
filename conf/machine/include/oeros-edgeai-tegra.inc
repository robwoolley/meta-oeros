# Nvidia Jetson (meta-tegra). MACHINE: jetson-orin-nano-devkit (defaults to the
# Orin Nano Super nvpmodel/devicetree on this meta-tegra release -- there is
# no separate "-super" MACHINE), jetson-agx-orin-devkit
OEROS_EDGEAI_VENDOR = "tegra"
MACHINE_FEATURES:append = " edgeai edgeai-gpu edgeai-cuda"
LICENSE_FLAGS_ACCEPTED:append = " commercial_tegra"
