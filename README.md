caDSR to CEDAR Convertor
========================

This project converts XML-encoded [caDSR](https://wiki.nci.nih.gov/display/caDSR/caDSR+Wiki) [11179-based](http://metadata-standards.org/11179/)
common data elements (CDEs) to instances of a JSON-encoded CEDAR template that represents those elements.

The format of the caDSR CDEs is described by an [XML Schema document]
(https://github.com/metadatacenter/cadsr2cedar/blob/master/src/main/resources/xsd/DataElement_V4.0.xsd).
The [JAXB](http://www.oracle.com/technetwork/articles/javase/index-140168.html) library uses this document to generate Java classes to read the XML-encoded instances of caDSR CDEs.

Similarly, the [jsonchema2pojo](http://www.jsonschema2pojo.org/) library uses a
[JSON Schema-encoded CEDAR CDE](https://github.com/metadatacenter/cadsr2cedar/blob/master/src/main/resources/json-schema/CDE.json)
template to automatically generate Java classes that can serialize JSON instances conforming to this template.

The [core translation routines](https://github.com/metadatacenter/cadsr2cedar/blob/master/src/main/java/org/metadatacenter/ingestor/cadsr/CDEXMLInstances2CEDARCDEInstances.java)
extract information from the JAXB-generated Java objects and insert the information into the jsonschema2pojo-generated Java objects.
These latter objects are then serialized into CEDAR-conformant JSON instances.

Note that the [caDSR CDE XML Schema](https://github.com/metadatacenter/cadsr2cedar/blob/master/src/main/resources/xsd/DataElement_V4.0.xsd)
was produced semi-automatically from the caDSR-supplied [DTD-encoded schema](https://github.com/metadatacenter/cadsr2cedar/blob/master/src/main/resources/dtd/DataElement_V4.0.dtd).
[JAXB bindings](https://github.com/metadatacenter/cadsr2cedar/blob/master/src/main/resources/xjb/bindings.xjb)
were required to rename some generated classes and these bindings do not seem to work with DTD-based documents.

#### Building and Running

To build this library you must have the following items installed:

+ [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
+ A tool for checking out a [Git](http://git-scm.com/) repository.
+ Apache's [Maven](http://maven.apache.org/index.html).

Get a copy of the latest code:

    git clone https://github.com/metadatacenter/cadsr2cedar.git

Change into the biosample-exporter directory:

    cd cadsr2cedar 

Then build it with Maven:

    mvn clean install

To run the transformer tool:

    mvn exec:java@transform -Dexec.args="/path/to/input /path/to/output"

where:
- `/path/to/input` is the input [caDSR XML file](https://wiki.nci.nih.gov/display/caDSR/caDSR+Hosted+Data+Standards%2C+Downloads%2C+and+Transformation+Utilities) location in your local machine, which can be either a directory or a file
- `/path/to/output` is the directory location to store the output files
    
To run the converter tool and upload the fields to CEDAR server:

    mvn exec:java@upload -Dexec.args="/path/to/input target-server folder-id 'cedar-apikey'"

where:
- `/path/to/input` is the input [caDSR XML file](https://wiki.nci.nih.gov/display/caDSR/caDSR+Hosted+Data+Standards%2C+Downloads%2C+and+Transformation+Utilities) location in your local machine, which can be either a directory or a file
- `target-server` is the CEDAR server types and the options are "local", "staging", "production"
- `folder-id` is the folder unique identifier found in the URL of your CEDAR workspace (e.g., 4aabbfc6-f953-4779-b667-e0d6a1234ce8)
- `cedar-apikey` is your CEDAR API key that will give the permission to upload files to the server (e.g., 'apiKey 0000111122223333444455556666777788889999aaaabbbbccccddddeeeeffff')
