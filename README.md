# Service Validator

The `service-validator` is a command line Groovy program that can be used to query and test LAPPS Grid services to ensure that the service produces the annotations that are listed in the service's metadata (as returned by calling `getMetadata()` on the service.)

## Usage

The `service-validator` is packaged as an executable JAR file.

```bash
java -jar service-validator.jar [COMMAND] [OPTIONS...]
```

A simple Bash shell script is provided to invoke the Java JAR file.

```bash
./service-validator [COMMAND] [OPTIONS...]
```

## First Use

The `service-validator` caches local copies of the service metadata for each service registerd to the LAPPS Grid. Before testing any services this metadata must be downloaded from the services.

```bash
./service-validator update --brandeis --vassar
```

By default the metadata will be cached in the user's $HOME directory ($HOME/.lappsgrid).  The cache directory location can also be changed with the `-d`, `--cache-dir` option:

```bash
./service-validator -d /var/lib/lappsgrid update -bv
```

**NOTE** The `--cache-dir` must be specified before the *command* (*update* in this example.)

## Commands

All sub-commands include their own help screens:

```bash
./service-validator [list|test|update] --help

```

### Update

Download and cache metadata from LAPPS Grid services.  This command should be run whenever new services are dedeployed to the grid.

``` 
  -v, --vassar     Call Vassar services.
  -b, --brandeis   Call Brandeis services.
  -h, --help       Prints this help screens and exits.
```

**Examples**

Update metadata for all services:
```bash
./service-validator update --vassar --bradeis
./service-validator update -v -b
./service-validator update -vb
```

### List

Prints a list of service that produce a give annotation type (`-t`, `--type`).  If the `--type` is not specified then all services are listed. Use the `--filter` option to restrict the output to only the services that contain the filter string in their service ID.  At lease one of `--vassar` or `--brandeis` must be specified.

```
  -v, --vassar             Call Vassar services.
  -b, --brandeis           Call Brandeis services.
  -f, --filter=<filters>   Strings to match in the service ID.
  -t, --type=<type>        Annotation type produced by the service
  -r, --requires           Print the annotation types required by the service.
  -h, --help               Disply this help message and exit.
```

**Examples**

List all services hosted at Brandeis:
``` 
./service-validator list --brandeis
```
List all part-of-speech taggers at Vassar:
``` 
./service-validator list --vassar --type Token#pos
```
List all services that have the string `dkpro` in their service ID:
``` 
./service-validator list -vbf dkpro
```

### Test
Sends a LIF document to one or more services and verify that they produce the correct annotation types.
``` 
  -v, --vassar               Call Vassar services.
  -b, --brandeis             Call Brandeis services.
  -a, --validate             Check the annotation types produced and reject any with
                               # in the URI.
  -s, --service=<services>   Service ID of a single service to be tested.
  -t, --type=<type>          Sevices that produces this annotation type will be
                               tested.
  -f, --filter=<filters>     Only test services that match the filter.
      --verbose              Prints the JSON returned by the LAPPS Grid service.
  -h, --help                 Print this help message and exit.
```

**Examples**

Test all part-of-speech taggers.

```bash
./service-validator test -abvt Token#pos
```

Test all DKPro named entity recognizers:

```bash
./service-validator test -abvt NamedEntity -f dkpro
```

Test the Stanford Tagger v2.0.0 at Vassar and display the generated annotations:

```bash
./service-validator -vs stanford.tagger_2.0.0 --verbose
```

## Notes

A `--filter` can be negated by prepending a tilde (~) to the filter term.  For example, to list all services that do not contain the string *stanford* in their ID:

**Example**
```bash
./service-validator list -vb -f ~stanford
```

Both the `--filter` and `--service` options can be specified multiple times. 

**Examples**
Test the Vassar Stanford Taggers v2.0.0 and v2.1.0-SNAPSHOT:
```bash
./service-validator test -v -s stanford.tagger_2.0.0 -s stanford.tagger_2.1.0-SNAPSHOT
```
List the Stanford POS taggers at Brandeis except for the DKPro taggers
```bash
./service-validator ls -b --type Token#pos --filter stanford --filter ~dkpro
```
