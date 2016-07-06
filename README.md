caDSR to CEDAR Convertor
========================

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
