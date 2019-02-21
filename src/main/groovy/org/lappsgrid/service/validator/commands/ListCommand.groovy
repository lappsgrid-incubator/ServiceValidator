/*
 * Copyright (c) 2019 The Language Applications Grid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lappsgrid.service.validator.commands


import org.lappsgrid.metadata.ServiceMetadata
import org.lappsgrid.service.validator.ID
import org.lappsgrid.service.validator.ServiceIndex
import org.lappsgrid.service.validator.ServicesValidator
import picocli.CommandLine.Command
import picocli.CommandLine.Option

/**
 *
 */
@Command(name="list", aliases = ["ls"],
        description = "List services.",
        sortOptions = false,
        headerHeading = "%n@|bold Synopsis |@%n"
)
class ListCommand extends FilteredCommand implements Runnable {
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

    void printType(String type) {
        List<String> services = index[type]
        if (services == null || services.size() == 0) {
            println "No services produce $type"
            return
        }
        List<String> accepted = []
        services.each {
            if (accept(it)) {
                accepted.add(it)
            }
        }
        if (latest) {
            List<ID> ids = latest(accepted)
            if (ids.size() > 0) {
                println type
                ids.each { ID id ->
                    println "\t${id.id}"
                    if (requires) {
                        printRequires(id.id)
                    }
                }
            }
        }
        else {
            println type
            accepted.each {
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
        metadata.requires.annotations.each {
            println "\t\tRequires $it"
        }
    }
}
