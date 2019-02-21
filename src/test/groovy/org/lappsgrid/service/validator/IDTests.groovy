package org.lappsgrid.service.validator

import org.junit.Test

/**
 *
 */
class IDTests {

    @Test
    void constructor() {
        ID id = new ID("test.tool_1.0.0")
        assert "test" == id.provider
        assert "tool" == id.tool
        assert "1.0.0" == id.version.toString()
        assert new VersionHolder("1.0.0") == id.version
    }

}
