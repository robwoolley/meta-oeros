# AMD's GStreamer fork (meta-xilinx-multimedia, selected for every MACHINE in
# the build by PREFERRED_VERSION_gstreamer1.0-plugins-bad = "1.28.2+amd+git"
# in that layer's own layer.conf) unconditionally does
#     PACKAGECONFIG:append = " faac kms faad opusparse"
# faac and faad2 carry LICENSE_FLAGS = "commercial", so without this the
# default (open-source-only) Oeros AMD build fails with
# "Nothing PROVIDES 'faac'" for anything pulling in plugins-bad.
# Drop the two AAC codecs unless the user opted in to 'commercial' (the
# oeros/amd-proprietary fragment does that).
OEROS_AAC_COMMERCIAL = "${@bb.utils.contains_any('LICENSE_FLAGS_ACCEPTED', 'commercial commercial_faac commercial_faad2', '1', '', d)}"
PACKAGECONFIG:remove = "${@'' if d.getVar('OEROS_AAC_COMMERCIAL') else 'faac faad'}"
