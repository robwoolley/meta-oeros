# NXP i.MX (meta-imx-ml). MACHINE: imx8mp-lpddr4-evk, imx93-11x11-lpddr4x-evk, imx95-19x19-lpddr5-evk
OEROS_EDGEAI_VENDOR = "imx"
MACHINE_FEATURES:append = " edgeai edgeai-npu"
# NXP's delegates (meta-freescale-ml) link against meta-freescale-ml's own
# tensorflow-lite, which also carries the Python tflite_runtime module, so
# use it instead of meta-tensorflow-lite's libtensorflow-lite.
OEROS_EDGEAI_TFLITE = "tensorflow-lite"
OEROS_EDGEAI_TFLITE_PYTHON = ""
# The NXP EULA is no longer accepted here: enable the oeros/nxp-proprietary
# fragment (required -- i.MX boot firmware is EULA-covered; docs/edgeai.md).
