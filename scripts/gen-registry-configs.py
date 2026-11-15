#!/usr/bin/env python3
# Copyright (c) 2026 Wind River Systems, Inc.
#
# Generate the per-vendor Oeros bitbake-setup configurations under
# conf/registry/configurations/ from one shared description, so the common
# layer set cannot drift between vendors.
#
# bitbake-setup has no "extends": a config file's sources are all fetched,
# and its bb-layers are shared by every machine option in it. One file per
# vendor therefore keeps each build's BBLAYERS (and parse time) down to one
# vendor's layers and stops vendor-global overrides from leaking across
# vendors (e.g. meta-xilinx-multimedia's PREFERRED_VERSION_gstreamer1.0*).
#
#   scripts/gen-registry-configs.py            # rewrite the generated files
#   scripts/gen-registry-configs.py --check    # exit 1 if any is out of date
#
# oeros-wrynose-jazzy.conf.json (Intel Robotics AI Suite) and
# oeros-master-rolling.conf.json are maintained by hand.

import json
import os
import sys

TOP = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(TOP, "conf", "registry", "configurations")


def git(uri, branch):
    return {"git-remote": {"uri": uri, "branch": branch, "rev": branch}}


W = "wrynose"
AMD = "rel-v2026.2"

# Sources and layers every Oeros lyrical config shares.
COMMON_SOURCES = {
    "bitbake": git("https://git.openembedded.org/bitbake", "2.18"),
    "openembedded-core": git("https://git.openembedded.org/openembedded-core", W),
    "meta-clang": git("https://github.com/kraj/meta-clang.git", W),
    "meta-intel-realsense": git("https://github.com/realsenseai/meta-intel-realsense.git", W),
    "meta-oeros": git("https://github.com/robwoolley/meta-oeros", W),
    "meta-tensorflow-lite": git("https://github.com/robwoolley/meta-tensorflow-lite", W),
    "meta-onnxruntime": git("https://github.com/robwoolley/meta-onnxruntime", W),
    "meta-openembedded": git("https://github.com/openembedded/meta-openembedded", W),
    "meta-python-ai": git("https://github.com/zboszor/meta-python-ai", W),
    "meta-qt6": git("https://code.qt.io/yocto/meta-qt6.git", "6.11"),
    "meta-ros": git("https://github.com/ros/meta-ros.git", W),
    "meta-virtualization": git("https://git.yoctoproject.org/meta-virtualization", W),
    "meta-security": git("https://git.yoctoproject.org/meta-security", W),
    "meta-zenoh": git("https://github.com/Jarsop/meta-zenoh.git", W),
}

COMMON_LAYERS_HEAD = [
    "openembedded-core/meta",
    "meta-openembedded/meta-filesystems",
    "meta-openembedded/meta-gnome",
    "meta-openembedded/meta-initramfs",
    "meta-openembedded/meta-multimedia",
    "meta-openembedded/meta-networking",
    "meta-openembedded/meta-oe",
    "meta-openembedded/meta-perl",
    "meta-openembedded/meta-python",
    "meta-openembedded/meta-webserver",
    "meta-openembedded/meta-xfce",
    "meta-clang",
    "meta-intel-realsense",
    "meta-oeros",
    "meta-tensorflow-lite",
    "meta-onnxruntime",
]
# vendor layers go between HEAD and TAIL
COMMON_LAYERS_TAIL = [
    "meta-python-ai",
    "meta-qt6",
    "meta-ros/meta-ros-common",
    "meta-ros/meta-ros2",
    "meta-ros/meta-ros2-lyrical",
    "meta-virtualization",
    "meta-security",
    "meta-security/meta-tpm",
    "meta-zenoh/meta-zenoh",
]

DISTROS = [("distro/oeros", "Oeros Reference Distro")]
PASSTHROUGH = ["DL_DIR", "SSTATE_DIR"]

QEMU = [
    ("machine/qemux86-64", "Machine configuration for running an x86-64 system on QEMU"),
    ("machine/qemuarm64", "Machine configuration for running an ARMv8 system on QEMU"),
]

CONFIGS = {
    "oeros-wrynose-lyrical": {
        "description": "OpenEmbedded ROS - Wrynose Lyrical (generic and Raspberry Pi machines; vendor Edge AI boards are in the per-vendor oeros-wrynose-lyrical-<vendor> configs)",
        "short": "OpenEmbedded ROS Wrynose Lyrical",
        "sources": {
            "meta-raspberrypi": git("https://github.com/agherzan/meta-raspberrypi.git", W),
        },
        "layers": ["meta-raspberrypi"],
        "machines": QEMU + [
            ("machine/qemuriscv64", "Machine configuration for running an RISC-V system on QEMU"),
            ("machine/genericarm64", "Machine configuration for Arm64 SystemReady IR/ES platforms"),
            ("machine/genericx86-64", "Machine configuration for generic x86_64 (64-bit) PCs and servers"),
            ("machine/raspberrypi0-2w-64", "Machine configuration for Raspberry Pi Zero 2W (64-bit)"),
            ("machine/raspberrypi4-64", "Machine configuration for Raspberry Pi 4 (64-bit)"),
            ("machine/raspberrypi5", "Machine configuration for Raspberry Pi 5"),
        ],
    },
    "oeros-wrynose-lyrical-nvidia": {
        "description": "OpenEmbedded ROS - Wrynose Lyrical (Nvidia Jetson Edge AI). The CUDA/TensorRT/DeepStream stack is opt-in: enable oeros/nvidia-proprietary. See docs/edgeai.md.",
        "short": "OpenEmbedded ROS Wrynose Lyrical -- Nvidia Jetson Edge AI",
        "sources": {
            "meta-tegra": git("https://github.com/OE4T/meta-tegra", W),
            "meta-tegra-community": git("https://github.com/OE4T/meta-tegra-community", W),
        },
        "layers": ["meta-tegra", "meta-tegra-community"],
        "passthrough": ["NVIDIA_DEVNET_MIRROR"],
        "machines": [
            ("oeros/machine-jetson-orin-nano-devkit", "Edge AI: Nvidia Jetson Orin Nano (Super nvpmodel by default) -- CUDA/TensorRT/VPI, Isaac ROS (with oeros/nvidia-proprietary)"),
            ("oeros/machine-jetson-agx-orin-devkit", "Edge AI: Nvidia Jetson AGX Orin -- CUDA/TensorRT/VPI, Isaac ROS (with oeros/nvidia-proprietary)"),
        ],
    },
    "oeros-wrynose-lyrical-nxp": {
        "description": "OpenEmbedded ROS - Wrynose Lyrical (NXP i.MX Edge AI). i.MX builds need the NXP EULA: enable oeros/nxp-proprietary. See docs/edgeai.md.",
        "short": "OpenEmbedded ROS Wrynose Lyrical -- NXP i.MX Edge AI",
        "sources": {
            "meta-freescale": git("https://github.com/Freescale/meta-freescale", W),
            "meta-freescale-distro": git("https://github.com/Freescale/meta-freescale-distro", W),
            # base tensorflow-lite, delegate, tim-vx and nnstreamer recipes;
            # meta-imx-ml only bbappends them (imx-setup-release.sh adds it too)
            "meta-freescale-ml": git("https://github.com/Freescale/meta-freescale-ml", W),
            "meta-yocto": git("https://git.yoctoproject.org/meta-yocto", W),
            "meta-imx": git("https://github.com/nxp-imx/meta-imx", "wrynose-6.18.37-2.1.0"),
        },
        # meta-imx-sdk LAYERDEPENDS on freescale-distro, which LAYERDEPENDS on
        # yocto (meta-yocto/meta-poky)
        "layers": ["meta-freescale", "meta-freescale-distro", "meta-freescale-ml", "meta-yocto/meta-poky",
                   "meta-imx/meta-imx-bsp", "meta-imx/meta-imx-sdk", "meta-imx/meta-imx-ml"],
        "machines": [
            ("oeros/machine-imx8mp-lpddr4-evk", "Edge AI: NXP i.MX 8M Plus -- eIQ, VX delegate on the Vivante NPU (needs oeros/nxp-proprietary)"),
            ("oeros/machine-imx93-11x11-lpddr4x-evk", "Edge AI: NXP i.MX 93 -- eIQ, Ethos-U NPU delegate (needs oeros/nxp-proprietary)"),
            ("oeros/machine-imx95-19x19-lpddr5-evk", "Edge AI: NXP i.MX 95 -- eIQ, Neutron NPU delegate (needs oeros/nxp-proprietary)"),
        ],
    },
    "oeros-wrynose-lyrical-qualcomm": {
        "description": "OpenEmbedded ROS - Wrynose Lyrical (Qualcomm Dragonwing Edge AI + QIR SDK). QAIRT/FastCV/Hexagon binaries are opt-in: enable oeros/qcom-proprietary. See docs/edgeai.md.",
        "short": "OpenEmbedded ROS Wrynose Lyrical -- Qualcomm Dragonwing Edge AI",
        "sources": {
            "meta-qcom": git("https://github.com/qualcomm-linux/meta-qcom", W),
            "meta-qcom-distro": git("https://github.com/qualcomm-linux/meta-qcom-distro", W),
            "meta-qcom-robotics-sdk": git("https://github.com/qualcomm-linux/meta-qcom-robotics-sdk", W),
            # collection "ai": enables meta-qcom's dynamic-layers/ai
            # (onnxruntime-qnn plugin EP), as in meta-qcom's ci/qcom-distro.yml
            "meta-ai": git("https://github.com/qualcomm-linux/meta-ai", W),
            # LAYERDEPENDS of meta-qcom (lts-linux-firmware-mixin) and of
            # meta-qcom-distro (selinux, sota); branches as in their ci/*.yml
            "meta-lts-mixins": git("https://git.yoctoproject.org/meta-lts-mixins", "wrynose/linux-firmware"),
            "meta-selinux": git("https://git.yoctoproject.org/meta-selinux", W),
            "meta-updater": git("https://github.com/uptane/meta-updater", W),
        },
        "layers": ["meta-lts-mixins", "meta-ai", "meta-qcom", "meta-qcom-distro", "meta-qcom-robotics-sdk",
                   "meta-selinux", "meta-updater"],
        "machines": [
            ("oeros/machine-qcs6490-rb3gen2-core-kit", "Edge AI: Qualcomm RB3 Gen 2 -- QAIRT/QNN on Hexagon (with oeros/qcom-proprietary)"),
            ("oeros/machine-qcs9100-ride-sx", "Edge AI: Qualcomm QCS9100 Ride SX -- QAIRT/QNN on Hexagon (with oeros/qcom-proprietary)"),
            ("oeros/machine-iq-9075-evk", "Edge AI + robotics: Qualcomm Dragonwing IQ-9075 EVK (QCS9100 family) -- Qualcomm Intelligent Robotics SDK (Nav2/MoveIt2, QRB-ROS nodes); QAIRT/QNN with oeros/qcom-proprietary"),
            ("oeros/machine-iq-8275-evk", "Edge AI + robotics: Qualcomm Dragonwing IQ-8275 EVK (QCS8300 family) -- Qualcomm Intelligent Robotics SDK (Nav2/MoveIt2, QRB-ROS nodes); QAIRT/QNN with oeros/qcom-proprietary"),
        ],
    },
    "oeros-wrynose-lyrical-ti": {
        "description": "OpenEmbedded ROS - Wrynose Lyrical (TI Sitara/Jacinto Edge AI). See docs/edgeai.md.",
        "short": "OpenEmbedded ROS Wrynose Lyrical -- TI Edge AI",
        "sources": {
            "meta-arm": git("https://git.yoctoproject.org/meta-arm", W),
            "meta-ti": git("https://git.yoctoproject.org/meta-ti", W),
            # meta-edgeai LAYERDEPENDS on meta-ti-test, meta-arago-distro,
            # meta-arago-extras and meta-ti-foundational (meta-tisdk), per
            # meta-edgeai's own README
            "meta-arago": git("https://git.yoctoproject.org/meta-arago", W),
            "meta-tisdk": git("https://github.com/TexasInstruments/meta-tisdk", W),
            "meta-edgeai": git("https://git.ti.com/git/edgeai/meta-edgeai", W),
        },
        "layers": ["meta-arm/meta-arm", "meta-arm/meta-arm-toolchain",
                   "meta-ti/meta-ti-bsp", "meta-ti/meta-ti-extras", "meta-ti/meta-ti-test",
                   "meta-arago/meta-arago-distro", "meta-arago/meta-arago-extras",
                   "meta-tisdk/meta-ti-foundational", "meta-edgeai"],
        "machines": [
            ("oeros/machine-am62axx-evk", "Edge AI: TI AM62A -- TIDL on C7x/MMA, Edge AI GStreamer, robotics kit"),
            ("oeros/machine-am68-sk", "Edge AI: TI AM68 -- TIDL on C7x/MMA, Edge AI GStreamer, robotics kit"),
            ("oeros/machine-am69-sk", "Edge AI: TI AM69 -- TIDL on C7x/MMA, Edge AI GStreamer, robotics kit"),
        ],
    },
    "oeros-wrynose-lyrical-intel": {
        "description": "OpenEmbedded ROS - Wrynose Lyrical (Intel Core Ultra/Atom Edge AI). The Robotics AI Suite variant is the separate oeros-wrynose-jazzy config. See docs/edgeai.md.",
        "short": "OpenEmbedded ROS Wrynose Lyrical -- Intel Edge AI",
        "sources": {
            "meta-intel": git("https://git.yoctoproject.org/meta-intel", W),
            "meta-openvino": git("https://github.com/intel/meta-openvino", W),
        },
        "layers": ["meta-intel", "meta-openvino"],
        "machines": [
            ("machine/genericx86-64", "Machine configuration for generic x86_64 (64-bit) PCs and servers"),
            ("oeros/machine-intel-corei7-64", "Edge AI: Intel Core Ultra/Atom -- OpenVINO on CPU/iGPU/NPU"),
        ],
    },
    "oeros-wrynose-lyrical-amd": {
        "description": "OpenEmbedded ROS - Wrynose Lyrical (AMD/Xilinx Kria + Versal). meta-xilinx-multimedia sets PREFERRED_VERSION_gstreamer1.0* to AMD's 1.28.2+amd fork from its layer.conf, so these layers must not share a config with other vendors. AMD layers are pinned to the rel-v2026.2 release (LAYERSERIES_COMPAT wrynose). Proprietary firmware/drivers are opt-in: enable oeros/amd-proprietary. See docs/amd-xilinx.md.",
        "short": "OpenEmbedded ROS Wrynose Lyrical -- AMD/Xilinx Kria + Versal Edge AI",
        "sources": {
            "meta-arm": git("https://git.yoctoproject.org/meta-arm", W),
            "meta-xilinx": git("https://github.com/Xilinx/meta-xilinx.git", AMD),
            "meta-kria": git("https://github.com/Xilinx/meta-kria.git", AMD),
            "meta-amd-adaptive-socs": git("https://github.com/Xilinx/meta-amd-adaptive-socs.git", AMD),
            "meta-vitis": git("https://github.com/Xilinx/meta-vitis.git", AMD),
            "meta-openamp": git("https://github.com/Xilinx/meta-openamp.git", AMD),
            "meta-xilinx-tsn": git("https://github.com/Xilinx/meta-xilinx-tsn.git", AMD),
            # meta-xilinx-mali400 carries python3-qtconsole/qtpy bbappends
            # (its meta-qt6 dynamic layer) whose recipes live here
            "meta-jupyter": git("https://github.com/Xilinx/meta-jupyter.git", AMD),
        },
        "layers": ["meta-arm/meta-arm", "meta-arm/meta-arm-toolchain",
                   "meta-xilinx/meta-microblaze", "meta-xilinx/meta-xilinx-core",
                   "meta-xilinx/meta-xilinx-standalone", "meta-xilinx/meta-xilinx-standalone-sdt",
                   "meta-xilinx/meta-xilinx-multimedia", "meta-xilinx/meta-xilinx-mali400",
                   "meta-xilinx/meta-xilinx-gpu-malig78ae",
                   "meta-kria/meta-kria-core", "meta-kria/meta-kria-bsp",
                   "meta-amd-adaptive-socs/meta-amd-adaptive-socs-core",
                   "meta-amd-adaptive-socs/meta-amd-adaptive-socs-bsp",
                   "meta-vitis", "meta-openamp", "meta-xilinx-tsn", "meta-jupyter"],
        "machines": [
            ("machine/qemuarm64", "Machine configuration for running an ARMv8 system on QEMU"),
            ("oeros/machine-k26-smk-kr-sdt", "Edge AI + robotics: AMD Kria KR260 Robotics Starter Kit (K26 SOM, Zynq UltraScale+ EV) -- XRT/DFX accelerators, ros2_control, TSN"),
            ("oeros/machine-k26-smk-kv-sdt", "Edge AI: AMD Kria KV260 Vision AI Starter Kit (K26 SOM) -- XRT/DFX accelerators, camera pipelines (VCU with oeros/amd-proprietary)"),
            ("oeros/machine-k24-smk-kd-sdt", "Robotics/industrial: AMD Kria KD240 Drives Starter Kit (K24 SOM) -- motor control, ros2_control, CAN/TSN"),
            ("oeros/machine-versal-vek280-sdt-seg", "Edge AI: AMD Versal AI Edge VEK280 -- AIE-ML array, XRT (Vitis AI runtime not yet on wrynose)"),
            ("oeros/machine-versal-2ve-2vm-vek385-revb-sdt-seg", "Edge AI: AMD Versal AI Edge Gen 2 VEK385 Rev B -- AIE-MLv2 array, XRT (Vitis AI runtime not yet on wrynose)"),
            ("oeros/machine-k26-smk-kr-sdt-multidomain", "Edge AI + robotics, AMP: KR260 -- Linux on the A53s + FreeRTOS/Zephyr on the Cortex-R5F over OpenAMP RPMsg"),
            ("oeros/machine-k26-smk-kv-sdt-multidomain", "Edge AI, AMP: KV260 -- Linux on the A53s + FreeRTOS/Zephyr on the Cortex-R5F over OpenAMP RPMsg"),
            ("oeros/machine-k24-smk-kd-sdt-multidomain", "Robotics/industrial, AMP: KD240 -- Linux + real-time motor-control loops on the Cortex-R5F over OpenAMP RPMsg"),
            ("oeros/machine-versal-vek280-multidomain", "Edge AI, AMP: VEK280 -- Linux on the A72s + FreeRTOS/bare metal on the Cortex-R5F over OpenAMP RPMsg"),
            ("oeros/machine-versal-2ve-2vm-vek385-revb-multidomain", "Edge AI, AMP: VEK385 Rev B -- Linux on the A78s + Zephyr/bare metal on the Cortex-R52 over OpenAMP RPMsg"),
        ],
    },
}


def render(name, spec):
    sources = dict(COMMON_SOURCES)
    sources.update(spec["sources"])
    layers = COMMON_LAYERS_HEAD + spec["layers"] + COMMON_LAYERS_TAIL
    q = json.dumps

    out = ["{", f'    "description": {q(spec["description"])},', '    "sources": {']
    src_lines = []
    for k, v in sources.items():
        g = v["git-remote"]
        src_lines.append(
            f'        {q(k)}: {{\n'
            f'            "git-remote": {{\n'
            f'                "uri": {q(g["uri"])},\n'
            f'                "branch": {q(g["branch"])},\n'
            f'                "rev": {q(g["rev"])}\n'
            f'            }}\n'
            f'        }}')
    out.append(",\n".join(src_lines))
    out.append("    },")
    out.append('    "bitbake-setup": {')
    out.append('        "configurations": [')
    out.append("            {")
    out.append(f'                "name": {q(name)},')
    out.append(f'                "description": {q(spec["short"])},')
    out.append(f'                "setup-dir-name": {q(name)},')
    out.append('                "bb-layers": [')
    out.append(",\n".join(f"                    {q(l)}" for l in layers))
    out.append("                ],")
    out.append(f'                "bb-env-passthrough-additions": {q(PASSTHROUGH + spec.get("passthrough", []))},')
    out.append('                "oe-fragments-one-of": {')
    out.append('                    "distro": {')
    out.append('                        "description": "Target distributions",')
    out.append('                        "options" : [')
    out.append(",\n".join(f'                            {{ "name": {q(n)}, "description": {q(d)} }}' for n, d in DISTROS))
    out.append("                        ]")
    out.append("                    },")
    out.append('                    "machine": {')
    out.append('                        "description": "Target machines",')
    out.append('                        "options" : [')
    out.append(",\n".join(f'                            {{ "name": {q(n)}, "description": {q(d)} }}' for n, d in spec["machines"]))
    out.append("                        ]")
    out.append("                    }")
    out.append("                }")
    out.append("            }")
    out.append("        ]")
    out.append("    },")
    out.append('    "version": "1.0"')
    out.append("}")
    text = "\n".join(out) + "\n"
    json.loads(text)  # must be valid JSON
    return text


def main():
    check = "--check" in sys.argv[1:]
    stale = []
    for name, spec in CONFIGS.items():
        path = os.path.join(OUT, name + ".conf.json")
        text = render(name, spec)
        old = open(path).read() if os.path.exists(path) else None
        if old != text:
            stale.append(path)
            if not check:
                with open(path, "w") as f:
                    f.write(text)
    for p in stale:
        print(("out of date: " if check else "wrote: ") + os.path.relpath(p, TOP))
    return 1 if (check and stale) else 0


if __name__ == "__main__":
    sys.exit(main())
