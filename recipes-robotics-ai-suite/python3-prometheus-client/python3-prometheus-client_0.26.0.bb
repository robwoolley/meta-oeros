SUMMARY = "Python client for the Prometheus monitoring system"
HOMEPAGE = "https://github.com/prometheus/client_python"
LICENSE = "Apache-2.0 & BSD-2-Clause"
LIC_FILES_CHKSUM = " \
    file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327 \
    file://prometheus_client/decorator.py;md5=ed220e8a3f18391539a75b6311f3d16f \
"
# BSD-2-Clause: prometheus_client/decorator.py bundles decorator 4.0.10
# (Michele Simionato) -- see NOTICE.

SRC_URI[sha256sum] = "04a91bcf94e2cf74a44a1a874d651a2e853ed354b6e822f3b7487751465d5c2b"

inherit pypi python_setuptools_build_meta

PYPI_PACKAGE = "prometheus_client"

BBCLASSEXTEND = "native nativesdk"
