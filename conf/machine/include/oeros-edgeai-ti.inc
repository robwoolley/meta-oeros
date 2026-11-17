# TI Sitara/Jacinto Edge AI (meta-ti + meta-edgeai). MACHINE: am62axx-evm, am68-sk, am69-sk
OEROS_EDGEAI_VENDOR = "ti"
MACHINE_FEATURES:append = " edgeai edgeai-npu edgeai-tidl"

# The J721S2 (AM68) and J784S4 (AM69) GPU stacks replace mesa, which
# rviz-ogre-vendor (behind vision-msgs-rviz-plugins in
# packagegroup-oeros-edgeai-ros) DEPENDS on
OEROS_RVIZ_PLUGINS:j721s2 = ""
OEROS_RVIZ_PLUGINS:j784s4 = ""
