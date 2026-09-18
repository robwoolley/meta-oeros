# OEROS container images

Container images for ROS 2 Lyrical on Yocto Wrynose, built with BitBake and
produced as OCI layouts by `image-oci.bbclass` from meta-virtualization.

## What gets built

Every image is a normal OpenEmbedded image recipe that installs its *complete*
contents. The layering happens at pack time: `image-oci` unpacks the parent
image's OCI layout, rsyncs this image's `IMAGE_ROOTFS` over it, and runs
`umoci repack`, which diffs against the unpacked parent. The layer that ends up
in the image therefore contains only what this image adds.

```
oeros-container-base                    minimal OS, no init, no kernel
└── oeros-container-ros-core            REP-2001 ros_core
    ├── oeros-container-tools           ros2 CLI + rosbag2
    └── oeros-container-ros-base        REP-2001 ros_base
        ├── oeros-container-ros-dev     gdb/strace/valgrind, dbg-pkgs
        │   └── oeros-container-devcontainer
        ├── oeros-container-ci          colcon + ament linters + lcov
        ├── oeros-container-sdk-target  on-target toolchain + dev headers
        ├── oeros-container-rviz        rviz2 only, rocker-ready
        ├── oeros-container-foxglove-bridge
        ├── oeros-container-rosbridge
        ├── oeros-container-realsense
        ├── oeros-container-simulation-headless
        ├── oeros-container-turtlebot3-core
        └── oeros-container-perception  REP-2001 perception
            └── oeros-container-simulation
                └── oeros-container-desktop
                    ├── oeros-container-turtlebot3-all
                    └── oeros-container-desktop-full   rocker-ready
oeros-container-base
└── oeros-container-zenoh-router        standalone zenohd, no ROS
(from scratch)
    oeros-container-sdk-cross           relocatable cross SDK, host arch
```

## Multiconfigs

`conf/multiconfig/` defines one configuration per target and purpose. Each has
its own `TMPDIR`; they share `DL_DIR` and `SSTATE_DIR`.

| Multiconfig | MACHINE | SDKMACHINE | For |
| --- | --- | --- | --- |
| `oeros-x86-64` | genericx86-64 | – | base, ros-core, ros-base and the lean images |
| `oeros-arm64` | genericarm64 | – | same, for arm64 |
| `oeros-x86-64-desktop` | genericx86-64 | – | perception → desktop-full, rviz, turtlebot3-all |
| `oeros-arm64-desktop` | genericarm64 | – | same, for arm64 |
| `oeros-sdk-x86-64` | genericx86-64 | x86_64 | both SDK images for the x86-64 target |
| `oeros-sdk-arm64-cross` | genericarm64 | x86_64 | cross SDK targeting arm64, hosted on x86-64 |
| `oeros-sdk-arm64-target` | genericarm64 | aarch64 | on-target arm64 SDK |

The desktop multiconfigs are separate from the runtime ones because they need
two things that should not leak into the small images:

* `LICENSE_FLAGS_ACCEPTED` — the graphical and simulation variants pull in
  components with commercial license flags. Accepting those globally would let
  one slip into `ros-core` unnoticed.
* `x11` in `DISTRO_FEATURES` — `oeros.conf` ships a wayland/opengl/vulkan
  desktop, but rocker's `--x11` extension is X11-specific: it runs `xauth`
  inside the container and bind-mounts `/tmp/.X11-unix`. Adding `x11` changes
  the signature of a large number of recipes, so it is confined to where it is
  actually needed.

Chaining only works *within* a multiconfig: `OCI_BASE_IMAGE` resolves through
`DEPLOY_DIR_IMAGE`, and each multiconfig has its own. That is why `ros-core`
and `ros-base` are built in both the runtime and the desktop multiconfigs.
sstate is shared, so the second build of each is cheap.

## Building

```sh
# Enable the multiconfigs (already in local.conf if bitbake-setup wrote it)
BBMULTICONFIG = "oeros-x86-64 oeros-arm64 oeros-x86-64-desktop oeros-arm64-desktop \
                 oeros-sdk-x86-64 oeros-sdk-arm64-cross oeros-sdk-arm64-target"
```

```sh
# Smallest useful image
bitbake mc:oeros-x86-64:oeros-container-ros-core

# The six-layer desktop-full stack (this is the expensive one)
bitbake mc:oeros-x86-64-desktop:oeros-container-desktop-full

# Several at once
bitbake mc:oeros-x86-64:oeros-container-ros-base \
        mc:oeros-arm64:oeros-container-ros-base
```

The SDK images need the SDK built first, in the same multiconfig:

```sh
bitbake mc:oeros-sdk-arm64-cross:ros2-image-sdktest -c populate_sdk
bitbake mc:oeros-sdk-arm64-cross:oeros-container-sdk-cross
```

## Loading and pushing

`image-oci` deploys an OCI archive plus a directory symlink into
`DEPLOY_DIR_IMAGE`. `scripts/oeros-container-load` wraps skopeo:

```sh
scripts/oeros-container-load \
    -d build/tmp-oeros-x86-64/deploy/images/genericx86-64 \
    oeros-container-ros-core
# -> oeros/ros-core:lyrical-genericx86-64 in podman

scripts/oeros-container-load \
    -d build/tmp-oeros-x86-64-desktop/deploy/images/genericx86-64 \
    -p registry.example.com/oeros/desktop-full:lyrical-genericx86-64 \
    oeros-container-desktop-full
```

## Using with rocker

`oeros-container-desktop-full`, `oeros-container-rviz`,
`oeros-container-turtlebot3-all` and `oeros-container-devcontainer` install
`packagegroup-oeros-rocker` and are labelled `io.oeros.rocker-ready=true`.

```sh
rocker --x11 --user --home oeros/desktop-full:lyrical-genericx86-64 rviz2
```

Supported extensions: `--user`, `--user-preserve-home`, `--home`, `--x11`,
`--git`, `--ssh`, `--network`, `--privileged`. `--nvidia` is **not** covered:
it needs the proprietary driver's userspace inside the container, matched to
the host driver version, which is outside what this layer builds.

## Entrypoint

Every ROS image has `ENTRYPOINT ["/ros_entrypoint.sh"]` and `CMD ["/bin/bash"]`,
matching the osrf/ros convention, so upstream ROS documentation applies:

```sh
podman run --rm oeros/ros-base:lyrical-genericx86-64 ros2 topic list
```

The entrypoint sources `/opt/ros/lyrical/setup.sh` when it exists and then
execs its arguments. `oeros-container-base` and `oeros-container-zenoh-router`
carry no ROS, so the entrypoint degrades to a plain exec (the router overrides
the entrypoint entirely).

## Notes and limitations

* **No init system.** Nothing here runs systemd or sysvinit.
  `packagegroup-core-boot` is deliberately not installed, which is also what
  keeps a kernel out of the dependency graph. `IMAGE_CLASSES:remove =
  "qemuboot"` in the multiconfig include is the other half of that: both
  generic machines add `qemuboot`, whose `do_write_qemuboot_conf` depends on
  `virtual/kernel:do_deploy`.
* **`ipk`, not `rpm`.** The OCI multi-layer path drives the package manager
  once per layer and opkg is much cheaper to stand up repeatedly.
* **No multi-arch manifests yet.** `oci-multiarch.bbclass` could fuse the
  x86-64 and arm64 builds of an image into a single tag; that needs
  cross-multiconfig dependencies and is not wired up.
* **micro-ROS agent is not included.** meta-ros/meta-ros2-lyrical packages
  `micro-ros-msgs` and `micro-ros-diagnostics` but not `micro_ros_agent`, so
  there is nothing to build a container around yet.
