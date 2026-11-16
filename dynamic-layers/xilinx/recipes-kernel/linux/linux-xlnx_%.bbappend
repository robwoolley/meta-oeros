# Kernel options the Kria apps in dynamic-layers/xilinx/recipes-kria-apps/ need (from
# Xilinx/meta-kria-apps): MCP251x SPI CAN (KR260 TSN/RS485 Pmod firmware),
# virtual CAN, GPIO PPS client (TSN time sync), DS18B20 1-Wire thermometer
# (KD240 motor-control firmware). Kria machines only.
FILESEXTRAPATHS:prepend:kria := "${THISDIR}/${PN}:"
SRC_URI:append:kria = " file://kria-apps-extra.cfg"
