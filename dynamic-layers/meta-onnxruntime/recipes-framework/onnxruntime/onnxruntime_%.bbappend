# Split the model-optimization tooling out of the onnxruntime wheel into its
# own onnxruntime-tools package, matching the PyPI "onnxruntime-tools"
# module (1.7.0: onnxruntime_tools/{quantization,transformers}). That PyPI
# project is frozen since 2021; its code now ships inside the onnxruntime
# wheel itself as onnxruntime/{quantization,transformers,tools}, which this
# layer's recipe installs into ${PN}. The ONNX Runtime Python bindings
# (import onnxruntime) stay in ${PN}: there is no separate
# python3-onnxruntime package.
PACKAGES =+ "${PN}-tools"
SUMMARY:${PN}-tools = "ONNX Runtime model tools: quantization, transformer optimizer, conversion helpers"
FILES:${PN}-tools = " \
    ${PYTHON_SITEPACKAGES_DIR}/onnxruntime/quantization \
    ${PYTHON_SITEPACKAGES_DIR}/onnxruntime/transformers \
    ${PYTHON_SITEPACKAGES_DIR}/onnxruntime/tools \
"
RDEPENDS:${PN}-tools = "${PN} python3-onnx python3-numpy python3-packaging"
RRECOMMENDS:${PN}-tools = "python3-sympy python3-coloredlogs python3-protobuf"
