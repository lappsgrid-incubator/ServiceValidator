package org.lappsgrid.service.validator

import org.junit.Ignore
import org.junit.Test

/**
 *
 */
@Ignore
class ListRunner {

    @Test
    void latest() {
        ServicesValidator.main("list -vb --latest".split())
    }

    @Test
    void listNER() {
        ServicesValidator.main("list -vbt NamedEntity".split())
    }

    @Test
    void latestNER() {
        ServicesValidator.main("list -vblt NamedEntity".split())
    }

    @Test
    void latestStanfordNERAtBrandeis() {
        ServicesValidator.main("list -blt NamedEntity -f stanford".split())
    }
}
