# Intel Core Ultra / Atom (meta-intel + OpenVINO). MACHINE: intel-corei7-64
OEROS_EDGEAI_VENDOR = "intel"
MACHINE_FEATURES:append = " edgeai edgeai-gpu edgeai-npu edgeai-openvino"

# openvino-inference-engine builds its Python API (-python3 sub-package, with
# ovc) and the iGPU plugin only with these PACKAGECONFIGs; the recipe default is
# "tbb tf tflite paddle pytorch samples".
PACKAGECONFIG:append:pn-openvino-inference-engine = " python3 opencl"
