# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "Test harness for running colcon test inside an OEROS container"
DESCRIPTION = "colcon plus the ament test tooling and coverage support, so \
that CI runs its tests against the same binaries that ship in the runtime \
images rather than against a separately built copy."

inherit packagegroup
inherit ros_distro_${ROS_DISTRO}

RDEPENDS:${PN} = " \
    python3-colcon-common-extensions \
    ament-lint \
    ament-lint-auto \
    ament-lint-common \
    ament-cmake-test \
    python3-pytest \
    python3-pytest-cov \
    lcov \
    gcov-symlinks \
"
