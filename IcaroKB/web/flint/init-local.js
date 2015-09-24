$(document)
		.ready(
				function() {
					var sampleQuery1 = "PREFIX icr: <http://www.cloudicaro.it/cloud_ontology/core#>\nSELECT * WHERE {\n  ?vm a icr:VirtualMachine.\n} LIMIT 100";

					var sampleQuery2 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n\nCONSTRUCT {?s ?p ?o}\nWHERE {\n\tGRAPH <http://www.legislation.gov.uk/id/uksi/2010/2581>\n\t{?s ?p ?o}\n}";

					var sampleQuery3 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\nPREFIX gzt: <http://www.gazettes-online.co.uk/ontology#>\n\nSELECT ?n WHERE {\n\t?n a gzt:Notice .\n\t?n gzt:hasPublicationDate ?d .\n\tFILTER (?d >= '2010-09-01'^^xsd:date)\n}\nORDER BY ?d\nLIMIT 100";

					var flintConfig = {
						"interface" : {
							"toolbar" : false,
							"menu" : false
						},
						"namespaces" : [
								{
									"name" : "ICARO Cloud Core Ontology",
									"prefix" : "icr",
									"uri" : "http://www.cloudicaro.it/cloud_ontology/core#"
								},
								{
									"name" : "ICARO Cloud App Ontology",
									"prefix" : "app",
									"uri" : "http://www.cloudicaro.it/cloud_ontology/applications#"
								},
								{
									"name" : "Friend of a friend",
									"prefix" : "foaf",
									"uri" : "http://xmlns.com/foaf/0.1/"
								},
								{
									"name" : "XML schema",
									"prefix" : "xsd",
									"uri" : "http://www.w3.org/2001/XMLSchema#"
								},
								{
									"name" : "Resource Description Framework",
									"prefix" : "rdf",
									"uri" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
								},
								{
									"name" : "Resource Description Framework schema",
									"prefix" : "rdfs",
									"uri" : "http://www.w3.org/2000/01/rdf-schema#"
								},
								{
									"name" : "Web Ontology Language",
									"prefix" : "owl",
									"uri" : "http://www.w3.org/2002/07/owl#"
								}
						],
						"defaultEndpointParameters" : {
							"queryParameters" : {
								"format" : "output",
								"query" : "query",
								"update" : "update"
							},
							"selectFormats" : [ {
								"name" : "SPARQL-XML",
								"format" : "sparql",
								"type" : "application/sparql-results+xml"
							} ],
							"constructFormats" : [ {
								"name" : "RDF/XML",
								"format" : "rdfxml",
								"type" : "application/rdf+xml"
							} ]            
						},
						"endpoints" : [
								{
									"name" : "ICARO Cloud",
									"uri" : "/IcaroKB/sparql",
									"modes" : ["sparql11"],
									queries : [
											{
												"name" : "All virtual machines",
												"description" : "Select all virtual machines",
												"query" : sampleQuery1
											} ]
								}],
						"defaultModes" : [  {
							"name" : "SPARQL 1.1 Query",
							"mode" : "sparql11query"
						} ]
					}

					var flintEd = new FlintEditor("flint-test", "flint/images", flintConfig);
				});
