SUMMARY = "Edge AI for ROS 2: vendor-neutral core + per-MACHINE accelerator stack"
LICENSE = "MIT"
inherit packagegroup

PACKAGE_ARCH = "${MACHINE_ARCH}"

RDEPENDS:${PN} = " \
    packagegroup-oeros-edgeai-core \
    packagegroup-oeros-edgeai-ros \
    ${@'packagegroup-oeros-edgeai-' + d.getVar('OEROS_EDGEAI_VENDOR') if d.getVar('OEROS_EDGEAI_VENDOR') else ''} \
"
