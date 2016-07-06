caDSR to CEDAR Convertor
========================

This project converts XML-encoded [caDSR](https://wiki.nci.nih.gov/display/caDSR/caDSR+Wiki) [11179-based](http://metadata-standards.org/11179/)
common data elements (CDEs) to instances of a JSON-encoded CEDAR template that represents those elements.

The format of the caDSR CDEs is described by an [XML Schema document]
(https://github.com/metadatacenter/cadsr2cedar/blob/master/src/main/resources/xsd/DataElement_V4.0.xsd).
The [JAXB](http://www.oracle.com/technetwork/articles/javase/index-140168.html) library uses this document to generates Java classes to read the XML-encoded instances of caDSR CDEs.

Similarly, the [jsonchema2pojo](http://www.jsonschema2pojo.org/) library uses a
[JSON Schema-encoded CEDAR CDE](https://github.com/metadatacenter/cadsr2cedar/blob/master/src/main/resources/json-schema/CDE.json)
template to automatically generate Java classes that can serialize JSON instances conforming to this template.

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

To run:

    mvn exec:java
