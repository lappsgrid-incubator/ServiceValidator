package org.lappsgrid.service.validator

import org.lappsgrid.discriminator.Discriminators
import org.lappsgrid.metadata.ServiceMetadata
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer

/**
 *
 */
class ServiceIndex {

    File root

    Map<String, List<String>> index = [:]
    Map<String, ServiceMetadata> metadata = [:]
    Map<String, String> urls = [:]

    ServiceIndex() { }
    ServiceIndex(String path) { this(new File(path)) }
    ServiceIndex(File directory) {
        this.root = directory
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
                    println "WARING: No url for service $id"
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
