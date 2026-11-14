# Ported from Xilinx/meta-kria-apps (main, LAYERSERIES_COMPAT scarthgap; MIT,
# Copyright (C) 2026 Advanced Micro Devices, Inc.) to wrynose / Oeros:

#
# Shared SRC_URI / SRCREV for every recipe that builds out of the
# foc-motor-ctrl upstream tree (foc-motor-ctrl and kria-motor-control).
# Pinning both recipes through this single file keeps them in lockstep
# so the ROS 2 client (kria_motor_control package.xml + launch files)
# can never drift from the native lib / server / dashboard build.
#
# LIC_FILES_CHKSUM stays in the per-recipe .bb because S differs
# between consumers and the relative path to LICENSE changes accordingly.

HOMEPAGE = "https://github.com/Xilinx/foc-motor-ctrl"
LICENSE  = "MIT"

SRC_BRANCH = "main"
SRC_URI = "git://github.com/Xilinx/foc-motor-ctrl.git;branch=${SRC_BRANCH};protocol=https"
# v0.6 release (commit 601fc08).
SRCREV  = "601fc08165438eae8c8853bd6c9efc39020a34ef"
