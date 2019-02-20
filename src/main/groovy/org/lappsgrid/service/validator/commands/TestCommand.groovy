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

import org.lappsgrid.client.ServiceClient
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Annotation
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

import static org.lappsgrid.discriminator.Discriminators.Uri

import org.lappsgrid.metadata.ServiceMetadata
import org.lappsgrid.service.validator.ServiceIndex
import org.lappsgrid.service.validator.ServicesValidator
import picocli.CommandLine.Command
import picocli.CommandLine.Option

/**
 *
 */
@Command(name="test",
        description = "Send document to services for testing and evaluation.",
        sortOptions = false,
        headerHeading = "%n@|bold Synopsis |@%n",
        footer = [
                "",
                "If @|bold --service|@ is specified then @|bold --type|@ is ignored.",
                "",
                "Copyright(c) 2019 The Lanuage Applications Grid.",
                 ""
        ]
)
class TestCommand extends CommonOptions implements Runnable {
    @Option(names=["-a", "--validate"], description = "Check the annotation types produced and reject any with # in the URI.")
    Boolean validate
    @Option(names=["-n", "--no-view"], description = "Do not expect a new view in the output.")
    Boolean noNewView
    @Option(names=["-s", "--service"], description = "Service ID of a single service to be tested.")
    String[] services
    @Option(names=["-t", "--type"], description = "Sevices that produces this annotation type will be tested.")
    String type
    @Option(names=["-f", "--filter"], description="Only test services that match the filter.")
    String[] filters
    @Option(names=["--verbose"], description = "Prints the JSON returned by the LAPPS Grid service.")
    Boolean verbose
    @Option(names=["-h", "--help"], description = "Print this help message and exit.", help=true, usageHelp=true)
    Boolean help

    ServiceIndex index
    int passed
    int failed

    void run() {
        ServicesValidator app = ServicesValidator.INSTANCE

        index = new ServiceIndex(app.destination)
        if (vassar) {
            index.load("vassar")
        }
        if (brandeis) {
            index.load("brandeis")
        }

        passed = 0
        failed = 0
        if (services && services.size() > 0) {
            services.each { validateService(it) }
        }
        else if (type) {
            String uri = app.uri(type)
            List<String> ids = index[uri]
            if (ids == null || ids.size() == 0) {
                println "No services produce $type"
                return
            }
            ids.each { String id ->
                validateService(id)
            }
        }
        else if (filters && filters.size() > 0) {
            index.each { String id ->
                validateService(id)
            }
        }
        else {
            println "ERROR: One of --type, --filter, or --service must be specified."
        }
        int total = passed + failed
        if (total > 0) {
            println "Services tested: $total"
            println "Passed: $passed"
            println "Failed: $failed"
        }
    }

    boolean accept(String filter, String id) {
        if (filter.startsWith("~")) {
            return ! accept(filter.substring(1), id)
        }
        return id.contains(filter)
    }

    boolean accept(String id) {
        if (filters == null || filters.size() == 0) {
            return true
        }
        for (String filter : filters) {
            if (!accept(filter, id)) {
                return false
            }
        }
        return true
    }

    void validateService(String id) {
        if (!accept(id)) {
            return
        }
        ServiceMetadata metadata = index.getMetadata(id)
        if (metadata == null) {
            println "WARNING: No metadata for service $id"
        }
        else {
            String url = index.getUrl(id)
            println "Validating service $id at $url"
            if (testService(url, metadata)) {
                println "PASSED: $id"
                ++passed
            }
            else {
                println "FAILED: $id"
                ++failed
            }
        }
    }

    boolean testService(String url, ServiceMetadata metadata) {
        boolean failed = false
        try {
            String json = getTestData(metadata)
            if (json == null) {
                println "ERROR: Unable to get test data for $url"
                return false
            }

            Data data = Serializer.parse(json)
            data = convert(data)
            Container original = new Container((Map) data.payload)

            ServiceClient client = new ServiceClient(url, "tester", "tester")
            String result = client.execute(json)
            Data responseData = Serializer.parse(result)
            if (Uri.ERROR == responseData.discriminator) {
                println "ERROR: ${responseData.payload}"
                return false
            }

            responseData = convert(responseData)
            Container response = new Container((Map) responseData.payload)

            if (verbose) {
                println responseData.asPrettyJson()
            }

            // One or more views were added by the service
            if (!noNewView && response.views.size() == original.views.size()) {
                println "ERROR: No view was created"
                failed = true
            }

            Set<String> invalidTypes = new HashSet<>()
            metadata.produces.annotations.each { String type ->
                // See if the original document contained the produced type.
                List<View> originalViews = original.findViewsThatContain(type)
                List<View> generatedViews = response.findViewsThatContain(type)
                // There should be exactly one more view
                if (generatedViews.size() != originalViews.size() + 1) {
                    println "ERROR: No view with $type was created"
                    failed = true
                }
                int annotationsFound = 0
                String expectedType = type
                int hash = expectedType.indexOf("#")
                if (hash > 0) {
                    expectedType = expectedType.substring(0, hash - 1)
                }
                if (validate && generatedViews.size() > 0) {
                    // Check annotations produced in the last view.
                    View view = generatedViews[-1]
                    view.annotations.each { Annotation a ->
                        if (a.atType.contains("#")) {
                            failed = true
                            invalidTypes.add(a.atType)
                        }
                        if (expectedType == a.atType) {
                            ++annotationsFound
                        }
                    }
                    if (annotationsFound == 0) {
                        println "WARNING: No $expectedType annotations found."
                        failed = true
                    }
                }

            }
            if (invalidTypes.size() > 0) {
                failed = true
                println "ERROR: View contains invalid types. "
                invalidTypes.sort().each { println "\t$it"}
            }
        }
        catch (Exception e) {
            if (verbose) {
                e.printStackTrace()
            }
            return false
        }
        return !failed
    }

    String getTestData(ServiceMetadata metadata) {
        String filename
        List<String> requires = metadata.requires.annotations
        if (requires.size() == 0) {
            filename = "text.json"
        }
        else if (requires.contains(Uri.POS) && requires.contains(Uri.SENTENCE)) {
            filename = "tokens-pos-sentences.json"
        }
        else if (requires.contains(Uri.POS)) {
            filename = "tokens-pos.json"
        }
        else if (requires.contains(Uri.SENTENCE)) {
            filename = "tokens-sentences.json"
        }
        else if (requires.contains(Uri.TOKEN)) {
            filename = "tokens.json"
        }
        else {
            // We do not have any test data that meets the requirements!
            println "WARNING: No test data meets the requirements: " + requires.join(", ")
            return null
        }
        List<String> formats = metadata.requires.format
        String dir
        if (formats.contains(Uri.LIF)) {
            dir = "lif"
        }
        else if (formats.contains(Uri.GATE)) {
            dir = "gate"
        }
        else if (formats.contains(Uri.TCF)) {
            dir = "tcf"
        }
        else {
            println "WARNING: No test data available in format: " + formats.join(", ")
            return null
        }

        String path = "/$dir/$filename"
        InputStream stream = this.class.getResourceAsStream(path)
        if (stream == null) {
            println "WARNING: Unable to load test data from $path"
            return null
        }
        return stream.text
    }

    Data convert(Data input) {
        if (Uri.LIF == input.discriminator) {
            return input
        }
        String url
        if (Uri.GATE == input.discriminator) {
            url = "http://vassar.lappsgrid.org/invoker/anc:convert.gate2json_2.1.0"
        }
        else if (Uri.TCF == input.discriminator) {
            url = "http://vassar.lappsgrid.org/invoker/anc:tcf-converter_1.0.1"
        }
        else {
            println "WARNING: No coverter for type ${input.discriminator}"
            return null
        }
        ServiceClient converter = new ServiceClient(url, "tester", "tester")
        String json = converter.execute(input.asJson())
        return Serializer.parse(json)
    }

}
