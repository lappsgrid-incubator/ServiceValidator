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

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.lappsgrid.client.ServiceClient
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.service.validator.ServicesValidator
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option

import static org.lappsgrid.discriminator.Discriminators.Uri

/**
 *
 */
@Command(name="update", aliases = ["up"],
        description = "Updates metadata from LAPPS Grid services.",
        sortOptions = false,
        headerHeading = "%n@|bold Synopsis |@%n"
)
class UpdateCommand extends CommonOptions implements Runnable {

    @Option(names = ["-h", "--help"], description = "Prints this help screens and exits.", help = true, usageHelp = true)
    Boolean usageHelp

    void run() {
        println "Update"
        if (brandeis) {
            update('brandeis')
        }
        if (vassar) {
            update('vassar')
        }
    }

    void update(String organization) {
        JsonSlurper parser = new JsonSlurper()
        File destination = ServicesValidator.INSTANCE.destination

        println "Getting services for $organization"
        String json = get("https://api.lappsgrid.org/services/$organization")
        if (!json) {
            return
        }

        File orgDir = new File(destination, organization)
        if (!orgDir.exists()) {
            if (!orgDir.mkdirs()) {
                println "Unable to create ${orgDir.path}"
                return
            }
        }
        File servicesFile = new File(orgDir, "services.json")
        servicesFile.text = JsonOutput.prettyPrint(json)
        println "Wrote ${servicesFile.path}"

        int count = 0
        Map allServices = (Map) parser.parseText(json)
        allServices.elements.each { e ->
            if (e.active) {
                String url = e.endpointUrl
                int index = url.lastIndexOf(":")
                String name = url.substring(index + 1)
                String metadata = getMetadata(e.endpointUrl)
                File file = new File(orgDir, name + ".json")
                file.text = metadata
                println "Wrote ${file.path}"
                ++count
            }

        }
        println "Downloaded metadata for ${count} services."
    }

    String getMetadata(String url) {
        ServiceClient client = new ServiceClient(url, "tester", "tester")
        String json = client.getMetadata()
        Data data = Serializer.parse(json)
        if (data.discriminator == Uri.META) {
//            data.parameters['url'] = url
            data.setParameter("url", url)
        }
        return data.asPrettyJson()
    }

    String get(String url) {
        println "GET $url"
        OkHttpClient http = new OkHttpClient()
        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .url(url)
                .build()
        Response response = http.newCall(request).execute()
        if (response.code() != 200) {
            println "ERROR: ${response.code()}"
            println response.message()
            return null
        }
        return response.body().string()
    }

}
