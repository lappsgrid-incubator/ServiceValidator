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
