package org.lappsgrid.service.validator.commands

/**
 *
 */
class TestResult {
    String url
    boolean ok
    List<String> messages

    TestResult(String url) {
        this.url = url
        this.ok = true
        this.messages = []
    }

    void error(String message) {
        ok = false
        messages << "ERROR: " + message
    }

    void warning(String message) {
        ok = false
        messages << "WARNING: " + message
    }

    void message(String message) {
        messages << "INFO: " + message
    }
}
