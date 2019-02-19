package org.lappsgrid.service.validator

import picocli.CommandLine

/**
 *
 */
class VersionProvider implements CommandLine.IVersionProvider {
    @Override
    String[] getVersion() throws Exception {
        return [ "service-validator v${Version.version}" ] as String[]
    }
}
