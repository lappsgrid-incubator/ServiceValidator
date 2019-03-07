package org.lappsgrid.service.validator

import org.junit.*
import static org.junit.Assert.*

/**
 *
 */
class LingpipeTests {

    @Test
    void tokens() {
        ServicesValidator.main("test -v -l -f lingpipe -t Token".split())
    }
}
