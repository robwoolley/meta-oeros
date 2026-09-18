# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "ROS environment entrypoint script for OEROS container images"
DESCRIPTION = "Installs /ros_entrypoint.sh, the ENTRYPOINT used by every OEROS \
ROS 2 container image.  It sources the ROS 2 setup script and then execs the \
container's command, matching the convention used by the osrf/ros images so \
that upstream ROS documentation and rocker invocations apply unchanged."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "file://ros_entrypoint.sh"

S = "${UNPACKDIR}"

# A shell script with no compiled content.  The ROS prefix is spelled out here
# rather than taken from ros_opt_prefix.bbclass so that this recipe does not
# have to inherit ros_distro_${ROS_DISTRO}, which is not allarch-safe.
# ROS_DISTRO itself is set for the whole build by meta-ros2-lyrical.
inherit allarch

ROS_SETUP_SH = "/opt/ros/${ROS_DISTRO}/setup.sh"

do_install() {
    install -d ${D}
    install -m 0755 ${S}/ros_entrypoint.sh ${D}/ros_entrypoint.sh
    sed -i -e 's|@ROS_SETUP@|${ROS_SETUP_SH}|g' ${D}/ros_entrypoint.sh
}

FILES:${PN} = "/ros_entrypoint.sh"
