# meta-ros2-jazzy's generated recipe sets LICENSE = "BSD" verbatim from
# package.xml's own <license>BSD</license> tag -- upstream never specifies
# a variant (2-Clause vs 3-Clause) and ships no LICENSE file anywhere in
# the repo; upstream's own package.xml comments acknowledge this ambiguity
# (ament_lint_copyright doesn't support a bare "BSD" license either). A
# plain "BSD" has no match in OE's common-licenses/, so do_create_spdx
# fails with "Cannot find any text for license BSD". Rather than guess a
# specific variant we can't confirm, point SPDX at the same package.xml
# text LIC_FILES_CHKSUM already trusts.
NO_GENERIC_LICENSE[BSD] = "package.xml"
