package org.lappsgrid.service.validator

import org.junit.Ignore
import org.junit.Test

/**
 *
 */
@Ignore
class StanfordVassarRunner {

    @Test
    void vassarStanford() {
        ServicesValidator.main("test -vs stanford.tagger_2.1.0-SNAPSHOT --verbose".split())
    }
}
