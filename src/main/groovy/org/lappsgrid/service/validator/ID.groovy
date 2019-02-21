package org.lappsgrid.service.validator

/**
 *
 */
class ID implements Comparable<ID> {
    String id
    String provider
    String tool
    String key
    VersionHolder version

    ID(String id) {
        this.id = id
        int underscore = id.lastIndexOf("_")
        String prefix = id.substring(0, underscore)
        String suffix = id.substring(underscore + 1)
        int dot = prefix.lastIndexOf(".")
        if (dot > 0) {
            provider = prefix.substring(0, dot)
            tool = prefix.substring(dot + 1)
            key = provider + "." + tool
        }
        else {
            provider = tool = key = prefix
        }
        version = new VersionHolder(suffix)
    }

    String key() {
        return key
    }

    @Override
    String toString() {
        return id
    }

    @Override
    int compareTo(ID other) {
        return this.version <=> other.version
    }
}
