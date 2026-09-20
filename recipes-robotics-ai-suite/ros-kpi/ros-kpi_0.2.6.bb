SUMMARY = "ROS2 Monitoring Stack for KPI Analysis"
DESCRIPTION = "Prometheus/Grafana-based ROS 2 monitoring, benchmarking, and \
KPI analysis tooling from the Intel Robotics AI Suite. NOTE: this package \
mixes on-target ROS 2 topic monitors/exporters (rclpy-based, plausibly run \
on the robot itself) with clearly desktop-oriented analysis/visualization \
tools (matplotlib GUI popups, Tkinter, openpyxl Excel report generation) \
and a docker-compose-based Grafana/Prometheus deployment bundle (bundled \
as wheel data, not something meant to run inside this image). Packaged \
here as upstream's own pyproject.toml defines it, matching every other \
recipe in this integration -- whether the desktop-analysis pieces belong \
on an actual embedded robot image, versus being a companion tool for a \
developer's workstation, is a real scoping question not resolved here."
HOMEPAGE = "https://github.com/open-edge-platform/edge-ai-suites/tree/main/robotics-ai-suite/components/ros-kpi"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSES/Apache-2.0.txt;md5=c846ebb396f8b174b10ded4771514fcc"

SRC_URI = "git://github.com/open-edge-platform/edge-ai-suites.git;protocol=https;branch=main"
SRCREV = "e67ecbab6ccce6ca89675b22d784bdb8b430ae3e"
PV = "0.2.6+git"

S = "${UNPACKDIR}/${BP}/robotics-ai-suite/components/ros-kpi"

inherit python_hatchling

# rclpy is required (per upstream's own pyproject.toml comment) but must
# come from ROS 2 system packages, not pip -- meta-ros, not PyPI.
RDEPENDS:${PN} += " \
    rclpy \
    python3-numpy \
    python3-psutil \
    python3-prometheus-client \
    python3-pyyaml \
    python3-jsonschema \
    python3-openpyxl \
    python3-matplotlib \
"

# Only needed for interactive graph click popups (upstream's own
# pyproject.toml comment) -- not required for headless/on-target use.
# Real OE package name is python3-tkinter (a python3 sub-package gated on
# PACKAGECONFIG[tk]), not "python3-tk" as upstream's comment assumes.
RRECOMMENDS:${PN} += "python3-tkinter"
