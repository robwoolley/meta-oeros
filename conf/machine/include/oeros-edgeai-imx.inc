# NXP i.MX (meta-imx-ml). MACHINE: imx8mp-lpddr4-evk, imx93-11x11-lpddr4x-evk, imx95-19x19-lpddr5-evk
OEROS_EDGEAI_VENDOR = "imx"
MACHINE_FEATURES:append = " edgeai edgeai-npu"
# The NXP EULA is no longer accepted here: enable the oeros/nxp-proprietary
# fragment (required -- i.MX boot firmware is EULA-covered; docs/edgeai.md).
