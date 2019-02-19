package org.lappsgrid.service.validator.commands

import picocli.CommandLine.Option
import picocli.CommandLine.Command

/**
 *
 */
@Command(descriptionHeading = '%n@|bold Description|@%n',
        optionListHeading = '%n@|bold Options|@%n',
        footer = "%nCopyright(c) 2019 The Lanuage Applications Grid.%n")
class CommonOptions {
    @Option(names=['-v', '--vassar'], description = "Call Vassar services." )
    Boolean vassar
    @Option(names = ['-b', '--brandeis'], description = "Call Brandeis services.")
    Boolean brandeis
}
