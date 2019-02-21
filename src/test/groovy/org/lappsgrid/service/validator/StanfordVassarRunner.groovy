package org.lappsgrid.service.validator

import org.junit.Ignore
import org.junit.Test

/**
 *
 */
@Ignore
class StanfordVassarRunner {

    final String OPTIONS = "--vassar --latest --validate --summarize"
    @Test
    void vassarStanford() {
        ServicesValidator.main("test -vs stanford.tagger_2.1.0-SNAPSHOT --verbose".split())
    }

    @Test
    void summarizeVassarGateTaggers() {
        ServicesValidator.main("test $OPTIONS -t Token#pos --no-view --filter gate".split())
    }

    @Test
    void summarizeVassarWeblichtTaggers() {
        ServicesValidator.main("test $OPTIONS -t Token#pos --no-view --filter weblicht".split())
    }


    @Test
    void summarizeVassarNonGateTaggers() {
        ServicesValidator.main("test $OPTIONS -t Token#pos --filter ~gate --filter ~weblicht".split())
    }
}
