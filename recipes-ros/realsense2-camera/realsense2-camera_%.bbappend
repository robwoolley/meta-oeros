# Copyright (c) 2026 Wind River Systems, Inc.
#
# realsense2-camera's CMakeLists.txt hard-codes an allow-list of ROS_DISTRO
# names and FATAL_ERRORs on anything not in it. "lyrical" postdates this
# release, so it isn't there yet -- see the patch for details.
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://0001-Recognize-the-lyrical-ROS-2-distro.patch"

# src/actions.cpp calls ROS_ERROR(error_msg.c_str()) -- passing a runtime
# string as the format argument itself, rather than "%s", error_msg.c_str().
# Harmless here (error_msg is never attacker-controlled), but OE's default
# hardening flags promote -Wformat-security to a hard error. Downgrade just
# that one check back to a warning; leave -Werror active for everything else.
CXXFLAGS:append = " -Wno-error=format-security"
