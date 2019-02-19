package org.lappsgrid.service.validator.commands

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer
import org.lappsgrid.metadata.ServiceMetadata
import org.lappsgrid.service.validator.ServiceIndex
import org.lappsgrid.service.validator.ServicesValidator
import org.lappsgrid.service.validator.commands.CommonOptions
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Mixin


/**
 *
 */
@Command(name="list", aliases = ["ls"],
        description = "List services.",
        sortOptions = false,
        headerHeading = "%n@|bold Synopsis |@%n"
)
class ListCommand extends CommonOptions implements Runnable {
    @Option(names=["-f", "--filter"], description = "Strings to match in the service ID.")
    String[] filters
    @Option(names = ["-t", "--type"], description = "Annotation type produced by the service")
    String type
    @Option(names=["-r", "--requires"], description = "Print the annotation types required by the service.")
    Boolean requires
    @Option(names=["-h", "--help"], description = "Disply this help message and exit.", help = true, usageHelp = true)
    Boolean help

    ServiceIndex index

    void run() {
        ServicesValidator app = ServicesValidator.INSTANCE

        index = new ServiceIndex(app.destination)
        if (vassar) {
            index.load("vassar")
        }
        if (brandeis) {
            index.load("brandeis")
        }

        if (type) {
            printType(app.uri(type))
        }
        else {
            index.index.keySet().sort().each { printType(it) }
        }
    }

    boolean accept(String filter, String type) {
        if (filter.startsWith("~")) {
            return ! accept(filter.substring(1), type)
        }
        return type.contains(filter)
    }

    boolean accept(String type) {
        if (filters && filters.size() > 0) {
            for (String filter : filters) {
                if (!accept(filter, type)) {
                    return false
                }
            }
        }
        return true
    }

    void printType(String type) {
        List<String> services = index[type]
        if (services == null || services.size() == 0) {
            println "No services produce $type"
            return
        }
        println type
        services.each {
            if (accept(it)) {
                println "\t$it"
                if (requires) {
                    printRequires(it)
                }
            }
        }
        println()
    }

    void printRequires(String id) {
        ServiceMetadata metadata = index.getMetadata(id)
        if (!metadata.requires.annotations || metadata.requires.annotations.size() == 0) {
            return
        }
        println "\tRequires:"
        metadata.requires.annotations.each {
            println "\t\t$it"
        }
    }
}
