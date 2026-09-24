# Copyright (c) 2026 Wind River Systems, Inc.
#
# tools/realsense-viewer/CMakeLists.txt has a genuine upstream bug:
#     install(DIRECTORY presets/
#         DESTINATION $ENV{HOME}/Documents/librealsense2/presets
#         FILES_MATCHING PATTERN "*.preset")
# -- installing sample presets to the *build machine's* home directory
# rather than any proper package path, regardless of platform.
#
# meta-intel-realsense's own librealsense2_2.57.7.bb already carries a
# do_install:append with a comment ("Remove preset file") that tries to
# clean this up via "rm -rf ${D}/tmp", which only works when $HOME happens
# to be /tmp -- true on many CI/container setups, but not guaranteed
# anywhere, and not true on this host, where $HOME is a real interactive
# user's home directory. That mismatch is exactly what surfaced here as
# do_package's QA correctly flagging six installed-but-not-shipped files
# under /home/<user>/Documents/librealsense2/presets.
#
# Stack an additional do_install:append (bitbake composes multiple :append
# definitions across bbappends, it does not replace the recipe's own) that
# cleans up whatever $HOME actually resolves to on whichever machine builds
# this, on top of the existing /tmp cleanup, so this holds everywhere.
do_install:append() {
    if [ -n "$HOME" ] && [ "$HOME" != "/" ]; then
        rm -rf "${D}$HOME"
        # Leaves any now-empty parent (e.g. ${D}/home) behind as its own
        # unshipped, QA-flagged directory; walk back up removing empty ones.
        rmdir -p --ignore-fail-on-non-empty "${D}$(dirname "$HOME")" 2>/dev/null || true
    fi
}
