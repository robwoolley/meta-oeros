# Copyright (c) 2026 Wind River Systems, Inc.

# Adjust PACKAGECONFIG settings
# python3's tk support lives in recipes-devtools/python/python3_%.bbappend instead: python3 is
# always present (oe-core), so a bbappend needs no dynamic-layers gating and is the more
# idiomatic home for a recipe-specific PACKAGECONFIG. clang/qtbase-native stay here for now:
# moving them the same way needs dynamic-layers/BBFILES_DYNAMIC plumbing for meta-clang/meta-qt5
# that doesn't exist yet in this layer.
PACKAGECONFIG:append:pn-clang = " libomp"
PACKAGECONFIG:append:pn-qtbase-native = " gui"
