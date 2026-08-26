Oeros Distro
============

This is a reference distribution for OpenEmbedded (OE) with the Robot Operating
System (ROS).

bitbake-setup
-------------

```
git clone https://git.openembedded.org/bitbake

curl -O https://raw.githubusercontent.com/robwoolley/meta-oeros/refs/heads/master/conf/registry/configurations/oeros-master-rolling.conf.json

./bitbake/bin/bitbake-setup init --non-interactive \
     oeros-master-rolling.conf.json \
     oeros-master-rolling machine/qemux86-64

. ./bitbake-builds/oeros-master-rolling/build/init-build-env

bitbake ros-image-core
```

Contribution Guidelines
-----------------------

Please refer to our contributor guide here: https://docs.yoctoproject.org/dev/contributor-guide/
for full details on how to submit changes.

Where to Send Patches
---------------------

meta-ros
- Git repository: <https://github.com/ros/meta-ros/>

