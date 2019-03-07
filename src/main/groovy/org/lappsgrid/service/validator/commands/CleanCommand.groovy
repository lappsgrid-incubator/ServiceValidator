/*
 * Copyright (c) 2019 The American National Corpus
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

import org.lappsgrid.service.validator.ServicesValidator
import picocli.CommandLine

/**
 *
 */
@CommandLine.Command(name="clean", aliases = ["cl"],
        description = "Deletes downloaded service metadata.",
        sortOptions = false,
        headerHeading = "%n@|bold Synopsis |@%n"
)
class CleanCommand extends CommonOptions implements Runnable {
    @CommandLine.Option(names = ["-h", "--help"], description = "Prints this help screens and exits.", help = true, usageHelp = true)
    Boolean usageHelp
    
    void run() {
        if (vassar) {
            delete("vassar")
        }    
        if (brandeis) {
            delete("brandeis")
        }
    }
    
    private void delete(String org) {
        File destination = ServicesValidator.INSTANCE.destination
        if (!destination.exists()) {
            return
        }
        
        File directory = new File(destination, org)
        if (!directory.exists()) {
            return
        }
        println "Removing ${directory.path}"
        directory.listFiles().each { it.delete() }
        directory.delete()
    }
}
