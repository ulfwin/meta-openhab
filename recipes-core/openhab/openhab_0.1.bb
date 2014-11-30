ESCRIPTION = "openHAB home automation recipe including addons. Note that wanted addons \
              has to be manually copied over to the openhab/addons folder"
HOMEPAGE = "http://www.openhab.org"
LICENSE = "EPL-1.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/EPL-1.0;md5=57f8d5e2b3e98ac6e088986c12bf94e6"
PR = "r0"

# The below is supposed to add java as a dependency, and chose provider (the meta-oracle-java
# layer provides one recipe per architecture). However, for some reason the the PREFERRED_PROVIDER
# is not noticed, even if it's manually chosen as the line below. Therefore it's commented out.
#PREFERRED_PROVIDER_java2-runtime = "oracle-jse-ejre-arm-vfp-hflt-client-headless"
#JAVA = "java2-runtime"
#RDEPENDS_${PN} += "${JAVA}"
#
#python () {
#	# Choose preferred provider based on target architecture
#	TA = d.getVar('TARGET_ARCH', True)
#	if TA == "arm":
#		javaPkg = "oracle-jse-ejre-arm-vfp-hflt-client-headless"
#	elif TA == "i586":
#		javaPkg = "oracle-jse-jre-i586"
#	elif TA == "x86_64":
#		javaPkg = "oracle-jse-jre-x86-64"
#	else:
#		raise error("The target architecture '%s' is not supported by the meta-oracle-java layer" %TA)
#
#	# Set preferred provider
#	JV = d.getVar('JAVA', True)
#	d.setVar('PREFERRED_PROVIDER_' + JV, javaPkg)
#}


SRC_URI = "https://github.com/openhab/openhab/releases/download/v1.5.1/distribution-1.5.1-runtime.zip;name=java-runtime \
           https://github.com/openhab/openhab/releases/download/v1.5.1/distribution-1.5.1-addons.zip;name=java-addons"

# runtime package
SRC_URI[java-runtime.md5sum] = "761af37608deba46c3dade42936238a1"
SRC_URI[java-runtime.sha256sum] = "1dad0a5e1b101db07560220beffe24d1ce418498368958b0d007e145f9adf632"

# Addon package
SRC_URI[java-addons.md5sum] = "3cf016595a92ba302a46fc347069f6d9"
SRC_URI[java-addons.sha256sum] = "4a3402f79a943ef25ede865cd40095abbad33626bdd91d32d13e32ebaec8b77b"

S = "${WORKDIR}"
OH = "${S}/openhab-runtime"
OHaddons = "${S}/openhab-addons"

# Need to rewrite unpack task, since we need the extracted files
# in separate folders under WORKDIR.
do_unpack[dirs] += "${OH} ${OHaddons}"
python do_unpack() {
    src_uri = (d.getVar('SRC_URI', True) or "").split()
    if len(src_uri) == 0:
        return

    OHdir = d.getVar('OH', True)
    OHaddonsdir = d.getVar('OHaddons', True)

    try:
        fetcher = bb.fetch2.Fetch([src_uri[0]], d)
        fetcher.unpack(OHdir)
    except bb.fetch2.BBFetchException as e:
        raise bb.build.FuncFailed(e)

    try:
        fetcher = bb.fetch2.Fetch([src_uri[1]], d)
        fetcher.unpack(OHaddonsdir)
    except bb.fetch2.BBFetchException as e:
        raise bb.build.FuncFailed(e)
}

# Simply copy folders to /usr/
do_install() {
	install -d ${D}/${prefix}
        cp -a ${OH} ${D}/${prefix}
        cp -a ${OHaddons} ${D}/${prefix}
}

# All the files are provided in a binaray package, and keeping all the
# files in a single package causes packaging QA errors and warnings.
# Avoid these packaging failure by skiping all the QA checks
INSANE_SKIP_${PN} = "${ERROR_QA} ${WARN_QA}"

# Inhibit warnings about files being stripped, we can't do anything about it.
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

FILES_${PN} = "/usr/openhab-addons /usr/openhab-runtime"

