DESCRIPTION = "openHAB home automation recipe including addons. Note that wanted addons \
              has to be manually copied over to the openhab/addons folder"
HOMEPAGE = "http://www.openhab.org"
LICENSE = "EPL-1.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/EPL-1.0;md5=57f8d5e2b3e98ac6e088986c12bf94e6"
PR = "r0"

RDEPENDS_${PN} += "java2-runtime"

OH_VERSION = "1.6.2"

SRC_URI = "https://github.com/openhab/openhab/releases/download/v${OH_VERSION}/distribution-${OH_VERSION}-runtime.zip;name=runtime \
           https://github.com/openhab/openhab/releases/download/v${OH_VERSION}/distribution-${OH_VERSION}-addons.zip;name=addons \
           file://init"

# runtime package
SRC_URI[runtime.md5sum] = "99001ed7d1fec56ae5ad9834b14a0b93"
SRC_URI[runtime.sha256sum] = "000a391e346015c0b1fa7fc129763593d495f6d9f77fb94723f196b075db016e"

# Addon package
SRC_URI[addons.md5sum] = "d401f705a7721324c9511b7ae641b17b"
SRC_URI[addons.sha256sum] = "688b02e6ae937f377f37996e90b5b3a827b15c7f3c775b48756a5fcc56cdde41"

S = "${WORKDIR}"
OH = "${S}/openhab-runtime"
OHaddons = "${S}/openhab-addons"

# Add autostart ability
inherit autotools update-rc.d
INITSCRIPT_NAME = "openhab"
INITSCRIPT_PARAMS = "defaults"

# Need to rewrite unpack task, since we need the extracted files
# in separate folders under WORKDIR.
do_unpack[dirs] += "${OH} ${OHaddons}"
python do_unpack() {
    src_uri = (d.getVar('SRC_URI', True) or "").split()

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

    # Handle any remaining files
    src_uri = src_uri[2:]
    if len(src_uri) == 0:
        return

    rootdir = d.getVar('WORKDIR', True)

    try:
        fetcher = bb.fetch2.Fetch(src_uri, d)
        fetcher.unpack(rootdir)
    except bb.fetch2.BBFetchException as e:
        raise bb.build.FuncFailed(e)
}

do_install() {
	# Simply copy folders to datadir
	install -d ${D}/${datadir}
        cp -a ${OH} ${D}/${datadir}
        cp -a ${OHaddons} ${D}/${datadir}

	# Move desired addons
	for addon in ${OPENHAB_ADDONS}
	do
		if [ -e "$(find ${OHaddons} -name "*${addon}*")" ]
		then
			cp $(find ${OHaddons} -name "*${addon}*") ${D}/${datadir}/openhab-runtime/addons
		else
			bbwarn "openHAB addon cannot be found: ${addon}"
		fi
	done

	# Add init script to allow autostart
	install -d ${D}/${sysconfdir}/init.d
	install -m 0755 ${S}/init ${D}/${sysconfdir}/init.d/openhab
}

FILES_${PN} = "${datadir}/openhab-addons ${datadir}/openhab-runtime ${sysconfdir}/init.d/openhab"
