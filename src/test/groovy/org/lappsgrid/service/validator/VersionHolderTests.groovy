package org.lappsgrid.service.validator

import org.junit.Test

/**
 *
 */
class VersionHolderTests {

    @Test
    void toStringTest() {
        final String expected = "1.2.3-SNAPSHOT"
        VersionHolder v = new VersionHolder(expected)
        assert expected == v.toString()
    }

    @Test
    void testParts() {
        final String expected = "1.2.3-SNAPSHOT"
        VersionHolder v = new VersionHolder(expected)
        assert 1 == v.major
        assert 2 == v.minor
        assert 3 == v.revision
        assert v.snapshot
    }

    @Test
    void testCompare() {
        assert new VersionHolder("1.0.0") < new VersionHolder("2.0.0")
        assert new VersionHolder("1.0.0-SNAPSHOT") < new VersionHolder("1.0.0")
        assert new VersionHolder("1.0.0") < new VersionHolder("1.1.0")
        assert new VersionHolder("1.1.0") < new VersionHolder("1.1.1")
        assert new VersionHolder("1.1.1") > new VersionHolder("1.1.0")
    }

    @Test
    void sort() {
        VersionHolder v1 = new VersionHolder("1.0.0")
        VersionHolder v2 = new VersionHolder("1.1.0")
        VersionHolder v3 = new VersionHolder("2.0.0-SNAPSHOT")
        VersionHolder v4 = new VersionHolder("2.0.0")

        [ v4, v2, v3, v1 ].sort().each { println it }

    }
}
