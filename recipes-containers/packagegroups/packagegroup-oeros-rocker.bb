# Copyright (c) 2026 Wind River Systems, Inc.

SUMMARY = "Packages required to run an OEROS container under osrf/rocker"
DESCRIPTION = "rocker (https://github.com/osrf/rocker) does not just run a \
container -- it generates a Dockerfile that extends the image and runs \
commands inside it.  Each extension has its own requirements on the base \
image, and this packagegroup covers the ones OEROS supports."

inherit packagegroup

# --user / --user-preserve-home: rocker's generated snippet calls groupadd,
#   useradd and usermod, then adds the new account to sudoers so that the
#   in-container user can install things.  All of that lives in shadow.
ROCKER_USER = " \
    shadow \
    sudo \
    shadow-securetty \
"

# --x11: the nvidia/x11 extensions run xauth inside the container to merge the
#   host's cookie, and bind-mount /tmp/.X11-unix.  That needs a working X11
#   client stack, which is also what rviz2 and the Qt tools use when they are
#   not on Wayland.  x11 must be in DISTRO_FEATURES for these to exist, which
#   is why the desktop multiconfigs add it.
ROCKER_X11 = " \
    xauth \
    libx11 \
    libxext \
    libxrender \
    libxtst \
    libxi \
    libxrandr \
    libxcursor \
    libxinerama \
    libxkbcommon \
    libglu \
    mesa \
    mesa-megadriver \
"

# --git and --ssh forward the host's configuration and agent socket; they are
# only useful if the tools that read them are present.
ROCKER_DEV = " \
    git \
    openssh-ssh \
    openssh-sftp-server \
    less \
    vim \
    procps \
    findutils \
    coreutils \
    which \
    file \
"

RDEPENDS:${PN} = " \
    ${ROCKER_X11} \
    ${ROCKER_USER} \
    ${ROCKER_DEV} \
"
