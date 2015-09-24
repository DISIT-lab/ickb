Icaro Cloud Knowledge Base (ICKB).
Copyright (C) 2015 DISIT Lab http://www.disit.org - University of Florence

Installation instructions:
- install tomcat
- modify the tomcat-users.xml file and add two users as reported in the conf/tomcat-users-example.xml file
- restart tomcat
- deploy OWLIM Standard Edition 4.3 openrdf-sesame and openrdf-workbench
- create a repository 'icaro' using workbench
- put in the repository the ontology
- create directory icaro in the home of tomcat (e.g. /usr/share/tomcat7)
- copy in this directory the file conf/icaro/kb.properties
- deploy IcaroKB.war
- go to the status page on http://localhost:8080/IcaroKB/
- if all ok you should see the current configuration settings

- go in conf directory and execute init-apps.sh and init-metrics.sh

NOTES:
It has been tested only with OWLIM Standard Edition 4.3, with other RDF stores the upload and verification of data center/business configuration may fail.
In this case the kb-validation.xml file containing the query performed to validate a graph should be modified.
The queries in kb-validation.xml should return no rows if the graph is consistent or one or more rows in case of inconsistency.
