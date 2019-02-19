package org.lappsgrid.service.validator

import org.junit.Ignore
import org.junit.Test

/**
 *
 */
class ServicesValidatorTest {

    PrintStream cached
    ByteArrayOutputStream stream

    void repaceSystemOut() {
        cached = System.out
        stream = new ByteArrayOutputStream()
        System.setOut(new PrintStream(stream))
    }

    String restoreSystemOut() {
        PrintStream current = System.out
        System.setOut(cached)
        String output = stream.toString()
        current.close()
        stream = null
        return output
    }

    void replaceSystemErr() {
        cached = System.err
        stream = new ByteArrayOutputStream()
        System.setErr(new PrintStream(stream))
    }

    String restoreSystemErr() {
        PrintStream current = System.err
        System.setErr(cached)
        String output = stream.toString()
        current.close()
        stream = null
        return output
    }

    @Test
    void mainVersion() {
        repaceSystemOut()
        ServicesValidator.main(['--version'] as String[])
        String actual = restoreSystemOut().trim()
        String expected = "service-validator v${Version.version}"
        assert actual == expected
    }

    @Test
    void mainHelp1() {
        repaceSystemOut()
        ServicesValidator.main(['-h'] as String[])
        String output = restoreSystemOut()
        // Assert some output
        assert output.length() > 100
    }
    @Test
    void mainHelp2() {
        repaceSystemOut()
        ServicesValidator.main(['--help'] as String[])
        String output = restoreSystemOut()
        // Assert there was some output
        assert output.length() > 100
    }

    @Test
    void unknownArg() {
        replaceSystemErr()
        ServicesValidator.main('--foo -d /tmp --bar'.split())
        String output = restoreSystemErr()
        assert output.startsWith("Unknown options:")
    }

    @Test
    void testHelp() {
        repaceSystemOut()
        ServicesValidator.main("test -h".split())
        String output = restoreSystemOut()
        assert output.trim().startsWith('Synopsis')
    }

    @Test
    void updateHelp() {
        repaceSystemOut()
        ServicesValidator.main("up -h".split())
        String output = restoreSystemOut()
        assert output.trim().startsWith('Synopsis')
    }

    @Test
    void listHelp() {
        repaceSystemOut()
        ServicesValidator.main("ls -h".split())
        String output = restoreSystemOut()
        assert output.trim().startsWith('Synopsis')
    }

    @Ignore
    void testVassarCached() {
        ServicesValidator.main("-vcd /tmp".split())
    }
}
