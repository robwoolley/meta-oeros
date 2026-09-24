# Copyright (c) 2026 Wind River Systems, Inc.
#
# meta-ros's generated rosx-introspection recipe vendors upstream's
# cmake/CPM.cmake, whose bootstrap boilerplate unconditionally calls
# file(DOWNLOAD https://github.com/cpm-cmake/CPM.cmake/...) to fetch itself
# -- bypassing bitbake's own SRC_URI fetcher entirely. do_configure
# correctly runs network-isolated (only do_fetch gets network access), so
# this fails every time with "Could not resolve hostname", regardless of
# whether the host actually has internet access.
#
# Upstream (https://github.com/ros/meta-ros/pull/1805, currently scoped to
# meta-ros2-rolling's rosx-introspection_2.3.0-1, "Pending" as of this
# writing) has the better fix: CMakeLists.txt's top-level
# "include(cmake/CPM.cmake)" runs unconditionally on every configure, but
# the two things that actually need CPM -- RapidJSON and the (default-off)
# benchmarks -- are each already gated behind their own if/endif. In a
# normal OE/Yocto build, RapidJSON is staged via this recipe's own DEPENDS,
# so find_package(RapidJSON) succeeds and that branch, CPM include and all,
# is simply never entered; CPM's self-download never fires because the code
# path that would trigger it never runs. That is a strictly better fix than
# working around CPM's own bootstrap network call (which was this file's
# first attempt): it addresses why CPM gets invoked at all, rather than
# satisfying its self-check locally, and needs no maintenance as CPM's own
# pinned version changes upstream.
#
# The patch below is copied verbatim from that PR. Its recipe version
# (2.3.0-1) doesn't match meta-ros2-lyrical's (2.3.0-3), but the CMakeLists.txt
# content and line numbers here are effectively identical -- confirmed by
# diffing the two -- so the same patch applies cleanly; this bbappend is
# wildcarded to any version so it keeps applying if that version changes.
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://0001-Include-the-CPM.cmake-only-when-needed.patch"
