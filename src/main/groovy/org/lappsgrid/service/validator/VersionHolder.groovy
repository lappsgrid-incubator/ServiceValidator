package org.lappsgrid.service.validator

/**
 *
 */
class VersionHolder implements Comparable<VersionHolder> {
    int major
    int minor
    int revision
    boolean snapshot

    VersionHolder(final String version) {
        snapshot = version.endsWith("-SNAPSHOT")
        if (snapshot) {
            initialize(version[0..-10])
        }
        else {
            initialize(version)
        }
    }

    String toString() {
        String version = "${major}.${minor}.${revision}"
        if (snapshot) {
            return "${version}-SNAPSHOT"
        }
        return version
    }

    private initialize(final String version) {
        String[] parts = version.split("\\.")
        try {
            major = parts[0] as int
            minor = parts[1] as int
            revision = parts[2] as int
        }
        catch (NumberFormatException e) {
            println "Invalid version number: $version"
            major = minor = revision = 0
        }
    }

    @Override
    int compareTo(VersionHolder other) {
        if (this.major < other.major) {
            return -1
        }
        else if (this.major > other.major) {
            return 1
        }
        else if (this.minor < other.minor) {
            return -1
        }
        else if (this.minor > other.minor) {
            return 1
        }
        else if (this.revision < other.revision) {
            return -1
        }
        else if (this.revision > other.revision) {
            return 1
        }
        else if (this.snapshot && other.snapshot) {
            return 0
        }
        else if (this.snapshot) {
            return -1
        }
        else if (other.snapshot) {
            return 1
        }
        else {
            return 0
        }
    }
}
