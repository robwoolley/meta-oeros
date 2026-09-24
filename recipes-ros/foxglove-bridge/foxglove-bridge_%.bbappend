# Copyright (c) 2026 Wind River Systems, Inc.
#
# CMakeLists.txt's FetchContent_Declare(foxglove_sdk URL ... URL_HASH ...)
# / FetchContent_MakeAvailable(foxglove_sdk) downloads a prebuilt,
# platform-specific foxglove-sdk release archive directly at configure
# time -- bypassing bitbake's own SRC_URI fetcher entirely, same class of
# problem as rosx-introspection's CPM.cmake, just via CMake's native
# FetchContent instead of a third-party bootstrap script. do_configure
# correctly runs network-isolated, so this always fails with
# FETCHCONTENT_FULLY_DISCONNECTED requiring a directory that was never
# populated.
#
# CMake's FetchContent has an official mechanism for exactly this case:
# setting FETCHCONTENT_SOURCE_DIR_<uppercased name> to a directory that
# already holds the dependency's contents skips the download step
# entirely and uses that directory as-is -- no URL, no hash check needed
# at configure time. Fetch the identical archive (same URL, same SHA256,
# read straight out of CMakeLists.txt) through bitbake's own fetcher,
# where network access is available, let bitbake unpack the zip (it's
# nothing more than what FetchContent would have unpacked itself), and
# point CMake at it.
#
# The archive is platform-specific (two different downloads, two
# different hashes, one per target architecture), hence the
# machine-arch-scoped SRC_URI:append overrides below.
FOXGLOVE_SDK_VERSION = "0.25.1"

SRC_URI:append:x86-64 = " https://github.com/foxglove/foxglove-sdk/releases/download/sdk%2Fv${FOXGLOVE_SDK_VERSION}/foxglove-v${FOXGLOVE_SDK_VERSION}-cpp-x86_64-unknown-linux-gnu.zip;name=foxglovesdkx8664;subdir=foxglove-sdk-prefetched;unpack=1"
SRC_URI[foxglovesdkx8664.sha256sum] = "0b9d348df3a8e98d2c2cee2360cc9f52e83bb3445044debd771d1b46dc358be4"

SRC_URI:append:aarch64 = " https://github.com/foxglove/foxglove-sdk/releases/download/sdk%2Fv${FOXGLOVE_SDK_VERSION}/foxglove-v${FOXGLOVE_SDK_VERSION}-cpp-aarch64-unknown-linux-gnu.zip;name=foxglovesdkaarch64;subdir=foxglove-sdk-prefetched;unpack=1"
SRC_URI[foxglovesdkaarch64.sha256sum] = "c70e35c78f0e37f3ba8d0251cd31f353bb10b1fd106200dc6c5f409ed4df1918"

# The release zip wraps its contents in a single top-level "foxglove/"
# directory (confirmed by inspecting the unpacked archive), so the actual
# SDK root -- containing lib/cmake/foxglove-sdk/foxglove-sdkConfig.cmake --
# is one level deeper than where it gets unpacked.
EXTRA_OECMAKE += "-DFETCHCONTENT_SOURCE_DIR_FOXGLOVE_SDK=${UNPACKDIR}/foxglove-sdk-prefetched/foxglove"

# FETCHCONTENT_SOURCE_DIR_FOXGLOVE_SDK alone stops the download and sets
# foxglove_sdk_SOURCE_DIR correctly (confirmed: both the flag and the
# actual foxglove-sdkConfig.cmake file are exactly where expected), but
# CMakeLists.txt's find_package(foxglove-sdk CONFIG REQUIRED HINTS
# "${foxglove_sdk_SOURCE_DIR}") still fails to locate it -- HINTS-based
# CONFIG-mode search applies its own subdirectory-pattern heuristics that
# apparently don't match here for reasons not worth chasing further.
# foxglove-sdk_DIR is CMake's direct, unambiguous override for CONFIG-mode
# find_package: point it exactly at the directory containing
# foxglove-sdkConfig.cmake, bypassing HINTS search entirely. This is
# literally what the original CMake error message itself suggests.
EXTRA_OECMAKE += "-Dfoxglove-sdk_DIR=${UNPACKDIR}/foxglove-sdk-prefetched/foxglove/lib/cmake/foxglove-sdk"

# rosx_introspection's vendored contrib/SmallVector.h uses old-style C casts
# throughout (e.g. "(char*)this->BeginX"), which foxglove-bridge's own
# strict warning flags promote to a hard error via -Werror=old-style-cast
# the moment that header gets included -- 18 instances, all the same check,
# all in code we don't own. Downgrade just that one check back to a
# warning; leave -Werror active for everything else.
CXXFLAGS:append = " -Wno-error=old-style-cast"

# do_package_qa's buildpaths check correctly flags our own compiled
# libfoxglove_cpp_shared.so / foxglove_bridge / their .debug symbols:
# their DWARF debug info embeds the literal TMPDIR-rooted path to the
# pre-staged foxglove-sdk headers, because oe-core's automatic debug-path
# remapping only covers the standard ${S}/${B}/recipe-sysroot locations,
# not this recipe-specific custom staging directory. Map it the same way,
# to the same kind of neutral path oe-core uses for everything else.
CXXFLAGS:append = " -ffile-prefix-map=${UNPACKDIR}/foxglove-sdk-prefetched=/usr/src/debug/${PN}/foxglove-sdk"
