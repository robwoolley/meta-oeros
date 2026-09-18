#!/bin/sh
#
# Container entrypoint for OEROS ROS 2 images.
#
# Sources the ROS environment and then execs whatever the container was asked
# to run, so that "podman run <image> ros2 topic list" behaves the same way it
# does against the osrf/ros images.
#
# @ROS_SETUP@ is substituted at build time with ${ros_prefix}/setup.sh.

set -e

if [ -f "@ROS_SETUP@" ]; then
    # setup.sh is not written to survive "set -u" or a POSIX-strict shell's
    # treatment of unset colon-separated path variables.
    set +e
    # shellcheck disable=SC1090
    . "@ROS_SETUP@"
    set -e
fi

# Sourced by a caller rather than run as an entrypoint: leave the shell alone.
if [ "$#" -eq 0 ]; then
    exec /bin/sh
fi

exec "$@"
