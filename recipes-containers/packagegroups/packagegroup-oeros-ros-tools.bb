# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "ROS 2 command line tooling"
DESCRIPTION = "The ros2 CLI and bag recording/playback, without the rest of a \
ros-base install.  Enough to introspect or record from a running graph."

inherit packagegroup
inherit ros_distro_${ROS_DISTRO}

RDEPENDS:${PN} = " \
    ros2cli \
    ros2cli-common-extensions \
    ros2action \
    ros2bag \
    ros2component \
    ros2doctor \
    ros2interface \
    ros2lifecycle \
    ros2multicast \
    ros2node \
    ros2param \
    ros2pkg \
    ros2run \
    ros2service \
    ros2topic \
    rosbag2 \
    rosbag2-storage-mcap \
"
