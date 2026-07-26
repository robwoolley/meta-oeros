# Copyright (c) 2026 Wind River Systems, Inc.

# Set distro-level settings for meta-zenoh
INSANE_SKIP:pn-zenoh-c += "buildpaths"
# Needed for scarthgap
DEBUG_PREFIX_MAP:remove:pn-zenoh-c = "-fcanon-prefix-map"
DEBUG_PREFIX_MAP:remove:pn-zenoh-c-native = "-fcanon-prefix-map"
