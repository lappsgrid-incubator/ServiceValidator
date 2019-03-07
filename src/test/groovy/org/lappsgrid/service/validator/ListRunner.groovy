package org.lappsgrid.service.validator

import org.junit.Ignore
import org.junit.Test

/**
 *
 */
@Ignore
class ListRunner {

    @Test
    void listVassar() {
        ServicesValidator.main("ls --vassar".split())
    }

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
