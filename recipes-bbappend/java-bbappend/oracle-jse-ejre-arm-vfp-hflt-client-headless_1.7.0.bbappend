# By the default, java does not place a link to the binary
# in /usr/bin, which is required by openHAB

do_install_append() {
    install -d ${D}${bindir}
    ln -sf ${JDK_JRE}${PV}_${PV_UPDATE}/bin/java ${D}/usr/bin/java
}

