/* Icaro Cloud Knowledge Base (ICKB).
   Copyright (C) 2015 DISIT Lab http://www.disit.org - University of Florence

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. */

package it.cloudicaro.disit.kb;

import it.cloudicaro.disit.kb.rdf.QueryResult;
import it.cloudicaro.disit.kb.rdf.RDFStore;
import it.cloudicaro.disit.kb.rdf.RDFStoreInterface;
import it.cloudicaro.disit.kb.rdf.ResultValue;
import it.cloudicaro.disit.kb.xsd.ResourceResolver;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author bellini
 */
public class ValidateResource {
  static Document kbValidationDoc = null;
  
  static String validateContent(String content, String schemaXsd) {
    try {
      Source xmlSource = new StreamSource(new ByteArrayInputStream(content.getBytes("UTF-8")));
      InputStream schemaSource = Thread.currentThread().getContextClassLoader().getResourceAsStream("it/cloudicaro/disit/kb/xsd/"+schemaXsd);

      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setResourceResolver(new ResourceResolver());
      Schema schema;
      try {
        schema = schemaFactory.newSchema(new StreamSource(schemaSource));
      } catch (SAXException ex) {
        Logger.getLogger(DataCenterResource.class.getName()).log(Level.SEVERE, null, ex);
        return "SchemaFactory: "+ex.getLocalizedMessage();
      }

      Validator validator = schema.newValidator();
      try {
        validator.validate(xmlSource);
        return null;
      } catch (SAXParseException e) {
        return "XML NOT VALID: line " + e.getLineNumber() + " col " + e.getColumnNumber() + " reason: "+e.getLocalizedMessage();
      } catch (SAXException e) {
        return "XML NOT VALID: " + e.getLocalizedMessage();
      } catch (IOException e) {
        return "IOException: " + e.getLocalizedMessage();
      }
    } catch (UnsupportedEncodingException ex) {
      return "VALIDATE UNSUPPORTED ENCODING";
    }
  }

  static String validateBusinessConfiguration(String content) throws Exception{
    String validationResult;
    RDFStoreInterface rdfStore=RDFStore.getInstance();
    String graph="urn:cloudicaro:context:BusinessConfiguration:check:"+UUID.randomUUID();

    //post content on RDF store in the context
    rdfStore.addStatements(transformBlankNodes(content), "application/rdf+xml", graph);
    try {
      validationResult=validateBusinessConfigurationGraph(graph);
    }
    finally {
      rdfStore.removeGraph(graph);
    }

    return validationResult;
  }

  static String validateBusinessConfigurationGraph(String graph) throws Exception{
    return validateGraph(graph, "BusinessConfiguration");
  }

   static String validateDataCenterGraph(String graph) throws Exception{
    return validateGraph(graph, "DataCenter");
  }
  
  static String validateMetricTypeGraph(String graph) throws Exception{
    return validateGraph(graph, "MetricType");
  }
  
  static String validateGraph(String graph, String type) throws Exception{
    String validationResult="";
    RDFStoreInterface rdfStore=RDFStore.getInstance();
    Configuration conf=Configuration.getInstance();

    if(conf.get("kb.validation.enable", "true").equals("false")) {
      System.out.println("Validation disabled");
      return null;
    }

    if(kbValidationDoc==null) {
      InputStream xmlStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("it/cloudicaro/disit/kb/kb-validation.xml");
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      kbValidationDoc = dBuilder.parse(xmlStream);
    }
    NodeList queryList = kbValidationDoc.getElementsByTagName("query");
    for(int i=0; i<queryList.getLength(); i++) {
      Node query = queryList.item(i);
      if(query.getParentNode().getNodeName().equals(type)) {
        String spqlQuery = query.getTextContent();
        String name = query.getAttributes().getNamedItem("name").getNodeValue();
        String message = query.getAttributes().getNamedItem("message").getNodeValue();
        Node idNode = query.getAttributes().getNamedItem("id");
        String id = (idNode==null ? "" : idNode.getNodeValue());
        if(!conf.get("kb.validation."+id, "true").equals("true")) {
          System.out.println("Validation skipping query "+id);
        } else {
          spqlQuery=spqlQuery.replaceAll("%g", "<"+graph+">");
          //System.out.println(name+" "+spqlQuery+" "+message);

          QueryResult qr=rdfStore.query(IcaroKnowledgeBase.SPARQL_PREFIXES+spqlQuery);
          for(Map<String, ResultValue> r:qr.results()) {
            //System.out.println(r);
            String msg=message;
            for(String v: qr.getVariables()) {
              ResultValue rv = r.get(v);
              msg=msg.replaceAll("\\?"+v, rv==null ?
                "--" :
                "\""+rv.getValue().
                      replaceAll(IcaroKnowledgeBase.NS_ICARO_APPS, "app:").
                      replaceAll(IcaroKnowledgeBase.NS_ICARO_CORE, "icr:")+"\"");
            }
            validationResult += name+": "+msg+"\n";
          }
        }
      }
    }
    System.out.println("validationResult="+validationResult);

    if(validationResult.equals(""))
      return null;
    return validationResult;
  }

  static Document parseXml(String content) throws Exception {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    dbFactory.setNamespaceAware(true);
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    InputSource is = new InputSource(new StringReader(content));
    return dBuilder.parse(is);
  }
  /**
   * 
   * @param content
   * @return
   * @throws Exception 
   */
  static String transformBlankNodes(String content) throws Exception {
    return transformBlankNodes(parseXml(content));
  }  
  
  static String transformBlankNodes(Document doc) throws Exception {
    String[] sorted_tags = {
      "AvgOp",
      "ConstOp",
      "DivOp",
      "MetricMeasure",
      "MonitorInfo",
      "MulOp",
      "NetworkAdapter",
      "ServiceLevelAction",
      "ServiceLevelAndMetric",
      "ServiceLevelObjective",
      "ServiceLevelOrMetric",
      "ServiceLevelSimpleMetric",
      "ServiceMetric",
      "SubOp",
      "SumOp"
    };
    DocumentTraversal t=(DocumentTraversal) doc;
    NodeIterator it = t.createNodeIterator(doc.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);

    for (Node n = it.nextNode(); n != null; n = it.nextNode()) {
      Element e=(Element)n;
      String tag=e.getLocalName();
      if(Arrays.binarySearch(sorted_tags, tag)>=0) {
        if(!e.hasAttributeNS(IcaroKnowledgeBase.NS_RDF, "about"))
          e.setAttributeNS(IcaroKnowledgeBase.NS_RDF, "about", "urn:icarocloud:"+tag+"-"+UUID.randomUUID());        
      }
    }

    DOMSource domSource = new DOMSource(doc);
    StringWriter writer = new StringWriter();
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.transform(domSource, new StreamResult(writer));
    return writer.toString();
  }  
}
