package org.lappsgrid.service.validator.commands

import org.lappsgrid.service.validator.ID
import picocli.CommandLine.Command
import picocli.CommandLine.Option

/**
 *
 */
@Command
class FilteredCommand extends CommonOptions {
    @Option(names=["-f", "--filter"], description = "Strings to match in the service ID.")
    protected String[] filters
    @Option(names=["-l","--latest"], description = "Only test the latest version of each service.")
    protected Boolean latest

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

//    List<ID> latest() {
//        return latest(index.all)
//    }

    List<ID> latest(List<String> idList) {
        List<ID> newest = []
        Map<String,List<ID>> serviceIndex = [:]
        idList.each { String s ->
            ID id = new ID(s)
            List<ID> list = serviceIndex[id.key()]
            if (list == null) {
                list = []
                serviceIndex.put(id.key(), list)
            }
            list.add(id)
        }
        serviceIndex.each { String id, List<ID> ids ->
            if (ids.size() > 0) {
//                ID last
//                ids.sort().each { ID it ->
//                    println it
//                    last = it
//                }
//                newest.add(last)
                newest.add(ids.sort()[-1])
            }
        }
        return newest
    }


}
