# Ported from Xilinx/meta-kria-apps (main, LAYERSERIES_COMPAT scarthgap; MIT,
# Copyright (C) 2026 Advanced Micro Devices, Inc.) to wrynose / Oeros:
#   - S moved from ${WORKDIR}/git to ${UNPACKDIR}/${BP} (wrynose unpack layout)
#   - COMPATIBLE_MACHINE limited to the Kria SOMs (needs dfx-mgr-client from meta-xilinx-core)

SUMMARY = "Field Oriented Control (FOC) motor-control library, Python binding, CANopen server and Bokeh dashboard"
DESCRIPTION = "Builds the foc-motor-ctrl library (libfoc_motor_ctrl) and \
its pybind11 Python binding (py_foc_motor_ctrl), the CANopen-driven motor \
server (fmc_canopen) and the Bokeh-based dashboard, both started through \
the start_motor_control launcher, used on Kria KD240. Each component is split into its own package \
so a downstream image only carries what it actually needs - the BIST \
container only pulls the lib + python binding, while the full motor \
server container pulls the lib + server + dashboard."
SECTION = "apps"

# Pulls in HOMEPAGE, LICENSE, SRC_URI, SRCREV so this recipe and
# kria-motor-control.bb stay in lockstep against the same SRCREV.
require foc-motor-ctrl-src.inc

PV = "0.6+git"
COMPATIBLE_MACHINE = "^$"
COMPATIBLE_MACHINE:kria = "${MACHINE}"

LIC_FILES_CHKSUM = "file://LICENSE;md5=370aacba9d9e1a362c89a2b36dc0608d"

S = "${UNPACKDIR}/${BP}"

inherit cmake python3targetconfig pkgconfig

# - libiio: target lib for the IIO motor-control sysfs interfaces
# - python3-pybind11: headers/cmake config for the python binding
# - lely-core: provides liblely-coapp + headers for the CANopen server
DEPENDS = " \
    lely-core \
    libiio \
    pkgconfig-native \
    python3 \
    python3-pybind11 \
"

# Build the standalone CANopen motor-control server and the Bokeh dashboard
# launcher; the cmdline app and tests are not shipped. Let Yocto handle
# post-install symlink creation rather than the upstream CMake snippet.
# Pin the python binding install dir to PYTHON_SITEPACKAGES_DIR so
# `import py_foc_motor_ctrl` resolves without any PYTHONPATH workaround.
EXTRA_OECMAKE = " \
    -DBUILD_CANOPEN_APP=ON \
    -DBUILD_DASHBOARD_APP=ON \
    -DBUILD_CMDLINE_APP=OFF \
    -DBUILD_TEST=OFF \
    -DSKIP_POST_INSTALL=ON \
    -DPYBIND11_FINDPYTHON=ON \
    -DPython3_INCLUDE_DIR=${STAGING_INCDIR}/python${PYTHON_BASEVERSION} \
    -DPython3_LIBRARY=${STAGING_LIBDIR}/libpython${PYTHON_BASEVERSION}${PYTHON_ABI}.so \
    -DPython_INCLUDE_DIR=${STAGING_INCDIR}/python${PYTHON_BASEVERSION} \
    -DPython_LIBRARY=${STAGING_LIBDIR}/libpython${PYTHON_BASEVERSION}${PYTHON_ABI}.so \
"

do_install:append() {
    # Recreate the /etc/motor-control/config -> ${prefix}/etc/motor-control/anaheim.config
    # symlink that upstream's post-install snippet would have made.
    install -d ${D}${sysconfdir}/motor-control
    ln -sf ${prefix}/etc/motor-control/anaheim.config \
        ${D}${sysconfdir}/motor-control/config

    # The upstream CMake installs the pybind11 module flat under
    # ${libdir} (i.e. /usr/lib/py_foc_motor_ctrl.cpython-*.so), which
    # is not on python's sys.path, so `import py_foc_motor_ctrl`
    # would fail at runtime. Move it into PYTHON_SITEPACKAGES_DIR
    # (/usr/lib/python3.12/site-packages/) where the import machinery
    # actually looks.
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}
    mv ${D}${libdir}/py_foc_motor_ctrl*.so ${D}${PYTHON_SITEPACKAGES_DIR}/
}

# -----------------------------------------------------------------------
# Package layout
# -----------------------------------------------------------------------
# The upstream tree ships four runnable artefacts off the same build:
#   1. libfoc_motor_ctrl.so  + py_foc_motor_ctrl.cpython-*.so   (lib)
#   2. fmc_canopen / foc-mc.eds                                  (CANopen server)
#   3. dashboard/ (Bokeh app)                                    (Bokeh UI)
#   4. start_motor_control [--dashboard|--canopen-server]       (launcher for 2 and 3)
#   5. (kria-motor-control ROS2 package - shipped separately)
# Split (1) from the rest so consumers (e.g. the BIST container, which
# only needs the python binding) don't drag in lely-coapp / bokeh /
# numpy. Since 0.6 one launcher script starts either (2) or (3), so it
# gets its own package that both of them pull in.
PACKAGES =+ "${PN}-launcher ${PN}-server ${PN}-dashboard"

FILES:${PN} = " \
    ${libdir}/libfoc_motor_ctrl.so.* \
    ${PYTHON_SITEPACKAGES_DIR}/py_foc_motor_ctrl*.so \
    ${prefix}/etc/motor-control/anaheim.config \
    ${sysconfdir}/motor-control \
"
RDEPENDS:${PN} = " \
    bash \
    libiio \
    python3-core \
"

# start_motor_control checks the firmware with dfx-mgr-client, finds the
# board IP with `ip -o addr` and clears stale Bokeh servers with
# killall / fuser.
FILES:${PN}-launcher = " \
    ${bindir}/start_motor_control \
"
RDEPENDS:${PN}-launcher = " \
    bash \
    dfx-mgr-client \
    iproute2 \
    psmisc \
"
SUMMARY:${PN}-launcher = "start_motor_control launcher for the FOC motor-control server and dashboard"

FILES:${PN}-server = " \
    ${bindir}/fmc_canopen \
    ${datadir}/foc-motor-ctrl/foc-mc.eds \
"
RDEPENDS:${PN}-server = " \
    ${PN} \
    ${PN}-launcher \
    liblely-coapp2 \
"
SUMMARY:${PN}-server = "FOC motor-control CANopen server (fmc_canopen, run via start_motor_control --canopen-server)"

FILES:${PN}-dashboard = " \
    ${datadir}/foc-motor-ctrl/dashboard \
"
RDEPENDS:${PN}-dashboard = " \
    ${PN} \
    ${PN}-launcher \
    python3-bokeh \
    python3-image \
    python3-numpy \
"
SUMMARY:${PN}-dashboard = "Bokeh-based dashboard for the FOC motor-control server (run via start_motor_control --dashboard)"

# pybind11 strips the python module .so during build, which trips Yocto's
# already-stripped QA check; suppress it for this single package.
INSANE_SKIP:${PN} += "already-stripped"

# CMake exported targets file contains absolute paths from the build dir
# under TMPDIR. They are harmless on the rootfs (only consumed by other
# CMake projects targeting the SDK) but trip the buildpaths QA gate.
INSANE_SKIP:${PN}-dev += "buildpaths"
