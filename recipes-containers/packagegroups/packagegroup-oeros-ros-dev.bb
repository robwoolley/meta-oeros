# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "Debugging and introspection tools for OEROS development containers"
DESCRIPTION = "What you want present when you exec into a container to find \
out why a node is misbehaving, rather than what you want shipped to a fleet."

inherit packagegroup
inherit ros_distro_${ROS_DISTRO}

RDEPENDS:${PN} = " \
    gdb \
    gdbserver \
    strace \
    ltrace \
    valgrind \
    tcpdump \
    iproute2 \
    iputils \
    net-tools \
    lsof \
    htop \
    procps \
    psmisc \
    vim \
    less \
    tmux \
    python3-pip \
"
