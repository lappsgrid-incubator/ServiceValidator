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

package org.lappsgrid.service.validator

import org.lappsgrid.discriminator.Discriminators
import org.lappsgrid.metadata.ServiceMetadata
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer

/**
 *
 */
class ServiceIndex implements Iterable<String> {

    File root

    Map<String, List<String>> index = [:]
    Map<String, ServiceMetadata> metadata = [:]
    Map<String, String> urls = [:]
    List<String> all = []

    ServiceIndex() { }
    ServiceIndex(String path) { this(new File(path)) }
    ServiceIndex(File directory) {
        this.root = directory
    }

    Iterator<String> iterator() {
        return all.iterator()
    }

    void load(String organization) {
        File directory = new File(root, organization)
        if (!directory.exists()) {
            println "ERROR: No such organization ${organization} found in ${root.path}"
            return
        }
        FileFilter filter = { File f -> return (f.name.endsWith('.json')) && (f.name != "services.json") }
        directory.listFiles(filter).each { File file ->
            String id = file.name.replaceAll("\\.json", "")
            Data data = Serializer.parse(file.text)
            if (data.discriminator == Discriminators.Uri.META) {
                String schema = data.payload['$schema']
                String url = data.parameters['url']
                if (url) {
                    urls[id] = url
                }
                else {
                    println "WARNING: No url for service $id"
                }
                if (schema.contains('service-schema')) {
                    ServiceMetadata md = new ServiceMetadata((Map) data.payload)
                    metadata.put(id, md)
                    md.produces.annotations.each { String type ->
                        register(type, id)
                    }
                }
            }
            else {
                println "${file.path} : Unexpected discriminator: ${data.discriminator}"
            }
        }
    }

    void register(String type, String service) {
        List<String> services = index[type]
        if (services == null) {
            services = []
            index[type] = services
        }
        services.add(service)
        all.add(service)
    }

    List<String> getAt(String type) {
        return get(type)
    }

    List<String> get(String type) {
        return index[type]
    }

    ServiceMetadata getMetadata(String id) {
        return metadata[id]
    }

    String getUrl(String id) {
        return urls[id]
    }
}
