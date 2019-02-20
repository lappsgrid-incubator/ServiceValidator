package org.lappsgrid.service.validator.commands

import org.lappsgrid.client.ServiceClient
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.DataContainer
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Annotation
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

import static org.lappsgrid.discriminator.Discriminators.Uri

import org.lappsgrid.metadata.ServiceMetadata
import org.lappsgrid.service.validator.ServiceIndex
import org.lappsgrid.service.validator.ServicesValidator
import picocli.CommandLine
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

    void run() {
        ServicesValidator app = ServicesValidator.INSTANCE

        index = new ServiceIndex(app.destination)
        if (vassar) {
            index.load("vassar")
        }
        if (brandeis) {
            index.load("brandeis")
        }

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
        else {
            println "ERROR: One of --type or --service must be specified."
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
            if (metadata.requires.format.contains(Uri.LIF)) {
                if (testService(url, metadata)) {
                    println "PASSED: $id"
                }
                else {
                    println "FAILED: $id"
                }
            }
            else {
                println "It is only possible to validate services that accept LIF input."
                println "Formats accepted by the $id service:"
                metadata.requires.format.each { String format ->
                    println "\t$format"
                }
            }

        }
    }

    boolean testService(String url, ServiceMetadata metdata) {
        boolean failed = false
        try {
            InputStream stream = this.class.getResourceAsStream("/inception-data.lif")
            if (stream == null) {
                println "ERROR: Unable to load test data."
                return false
            }
            String json = stream.text
            DataContainer data = Serializer.parse(json, DataContainer)
            Container original = data.payload

            ServiceClient client = new ServiceClient(url, "tester", "tester")
            String result = client.execute(json)
            Data responseData = Serializer.parse(result)
            if (Uri.ERROR == responseData.discriminator) {
                println "ERROR: ${responseData.payload}"
                return false
            }

            if (verbose) {
                println responseData.asPrettyJson()
            }

            Container response = new Container((Map) responseData.payload)

            // One or more views were added by the service
            if (response.views.size() == original.views.size()) {
                println "ERROR: No view was created"
                failed = true
            }

            Set<String> invalidTypes = new HashSet<>()
            metdata.produces.annotations.each { String type ->
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
            return false
        }
        return !failed
    }
}
