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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Context;
import org.w3c.dom.Document;

/**
 * REST Web Service
 *
 * @author bellini
 */
public class MetricTypeResource {

  private String name;

  /**
   * Creates a new instance of MetricTypeResource
   */
  private MetricTypeResource(String name) {
    this.name = name;
  }

  /**
   * Get instance of the MetricTypeResource
   */
  public static MetricTypeResource getInstance(String name) {
        // The user may use some kind of persistence mechanism
    // to store and restore instances of MetricTypeResource class.
    return new MetricTypeResource(name);
  }

  /**
   * Retrieves representation of an instance of it.cloudicaro.disit.kb.MetricTypeResource
   * @return an instance of java.lang.String
   */
  @GET
  @Produces("application/xml")
  public String getXml() throws Exception {
    RDFStoreInterface rdfStore=RDFStore.getInstance();

    String graph="urn:cloudicaro:context:MetricType:"+this.name;
    return rdfStore.getStatements("application/rdf+xml", graph);
  }

  /**
   * PUT method for updating or creating an instance of MetricTypeResource
   * @param content representation for the resource
   * @return an HTTP response with content of the updated or created resource.
   */
  @PUT
  @Consumes("application/xml")
  public void putXml(String content, @Context HttpServletRequest request) throws Exception {
    if(IcaroKnowledgeBase.isRecovering())
      throw new KbException("KB is recovering try later",400);

    putMetricType(name, content, request);
  }
  
  static public void putMetricType(String name, String content, @Context HttpServletRequest request) throws Exception {
    Date start=new Date();
    String op = (name.equals("") ? "POST" : "PUT");
    Configuration conf=Configuration.getInstance();
    boolean check = (request!=null || conf.get("kb.recover.force_checkMT", "false").equals("true"));
    String validationResult = null;
    
    if(check) {
      //validate 'content' using xml schema
      validationResult=ValidateResource.validateContent(content,"schema-icaro-kb-metricType.xsd");
      if(validationResult!=null) {
        IcaroKnowledgeBase.error(start, op, "MT", name, "FAILED-XML-VALIDATION", validationResult, content, request);
        throw new KbException(validationResult,400);
      }
    }
    
    //get the BusinessConfiguration rdf:about and check if the resource name from the url is the ending part of the rdf:about
    Document doc = ValidateResource.parseXml(content);
    RDFStoreInterface rdfStore=RDFStore.getInstance();
    
    //upload to a new temporary context, if ok rename the context otherwise remove
    String tmpGraph="urn:cloudicaro:context:MetricType:tmp:"+UUID.randomUUID();
    String graph="urn:cloudicaro:context:MetricType:"+name;

    rdfStore.addStatements(ValidateResource.transformBlankNodes(doc), "application/rdf+xml", tmpGraph);
    try {
      if(check)
        validationResult=ValidateResource.validateMetricTypeGraph(tmpGraph);
    }
    finally {
      if(validationResult!=null) {
        rdfStore.removeGraph(tmpGraph);
        IcaroKnowledgeBase.error(start, op, "MT", name, "FAILED-KB-VALIDATION", validationResult, content, request);
        throw new KbException(validationResult,400);
      } else {
        //remove context associated with id and then rename the graph to the new one
        rdfStore.removeGraph(graph);
        rdfStore.update("MOVE <"+tmpGraph+"> TO <"+graph+">");
      }
      IcaroKnowledgeBase.resetMetricNames();
    }

    //request is null when the KB is recovered
    if(request!=null) {
      rdfStore.flush();
      IcaroKnowledgeBase.storePut(start, content, name, "MT");
    
      IcaroKnowledgeBase.log(start, op, "MT", name, "OK", request);
    }
  }

  /**
   * DELETE method for resource MetricTypeResource
   */
  @DELETE
  public void delete(@Context HttpServletRequest request) throws Exception {
    if(IcaroKnowledgeBase.isRecovering())
      throw new KbException("KB is recovering try later",400);

    deleteMetricType(name, request);
  }
  
  static public void deleteMetricType(String name, @Context HttpServletRequest request) throws Exception {
    Date start=new Date();
    Configuration conf=Configuration.getInstance();
    
    //remove context associated with id
    RDFStoreInterface rdfStore=RDFStore.getInstance();

    String graph="urn:cloudicaro:context:MetricType:"+name;
    rdfStore.removeGraph(graph);
    IcaroKnowledgeBase.resetMetricNames();
    
    if(request!=null) {
      rdfStore.flush();
      IcaroKnowledgeBase.storeDelete(start, name, "MT");
    
      IcaroKnowledgeBase.log(start, "DELETE", "MT", name, "OK", request);
    }
  }

  static public String getRdfXmlLowLevelMetricType(String llmt) throws Exception {
    HashMap<String,String> props=new HashMap<String,String>();
    RDFStoreInterface rdfStore=RDFStore.getInstance();
    QueryResult query = rdfStore.query("SELECT ?p ?o WHERE {"
            + " GRAPH ?g { <"+llmt+"> ?p ?o } } ");
    for(Map<String,ResultValue> r : query.results()) {
      String p = namespace(r.get("p").getValue());
      props.put(p, r.get("o").getValue());
    }
    String type=namespace(props.get("rdf:type"));
    String xml="<"+type+" rdf:about=\""+llmt+"\">\n";
    xml += rdfxmlLiteral(props,"icr:hasMetricName");
    xml += rdfxmlLiteral(props,"icr:hasDescription");
    xml += rdfxmlLiteral(props,"icr:hasMetricUnit");
    xml+="</"+type+">\n";
    return xml;    
  }
  
  static private String namespace(String uri) {
    if(uri==null)
      return uri;
    if(uri.startsWith(IcaroKnowledgeBase.NS_ICARO_CORE))
      uri = "icr:"+uri.substring(IcaroKnowledgeBase.NS_ICARO_CORE.length());
    else if(uri.startsWith(IcaroKnowledgeBase.NS_RDF))
      uri = "rdf:"+uri.substring(IcaroKnowledgeBase.NS_RDF.length());    
    return uri;
  }
  
  static private String rdfxmlLiteral(Map<String,String> props, String p) {
    if(props.containsKey(p))
      return "<"+p+">"+props.get(p)+"</"+p+">\n";
    return "";
  }
  
  static private String rdfxmlLiteral(String p, String v, boolean decimal) {
    return "<"+p+(decimal?" rdf:datatype=\"http://www.w3.org/2001/XMLSchema#decimal\"":"")+">"+v+"</"+p+">\n";
  }

  static private String rdfxmlUri(Map<String,String> props, String p) {
    if(props.containsKey(p))
      return rdfxmlUri(p, props.get(p));
    return "";
  }
  
  static private String rdfxmlUri(String p, String uri) {
    return "<"+p+" rdf:resource=\""+uri+"\" />\n";
  }

  static private String getRdfXmlDescription(String tag, String uri) throws Exception {
    RDFStoreInterface rdfStore=RDFStore.getInstance();
    QueryResult query = rdfStore.query(IcaroKnowledgeBase.SPARQL_PREFIXES+" SELECT ?p ?o WHERE {"
            + " <"+uri+"> ?p ?o } ");
    String xml="<rdf:Description rdf:about=\""+uri+"\">";
    for(Map<String,ResultValue> r : query.results()) {
      String p=namespace(r.get("p").getValue());
      String o=r.get("o").getValue();
      switch(r.get("o").getType()) {
        case URI:
          xml+="<"+p+" rdf:resource=\""+o+"\" />";
          break;
        case LITERAL:
          xml+="<"+p+">"+o+"</"+p+">";
          break;
      }
    }
    xml+="</rdf:Description>";
    return xml;
  }
  
  static public class Resource {
    public String uri;
    public ArrayList<String> types = new ArrayList<String>();
    public HashMap<String, String> properties = new HashMap<String, String>();
    
    public void load(String _uri) throws Exception{
      RDFStoreInterface rdfStore=RDFStore.getInstance();
      uri = _uri;
      QueryResult query = rdfStore.query("SELECT ?p ?o WHERE {"
              + " GRAPH ?g { <"+uri+"> ?p ?o } }");
      for(Map<String,ResultValue> r : query.results()) {
        String p = namespace(r.get("p").getValue());
        String o = r.get("o").getValue();
        if(p.equals("rdf:type"))
          types.add(namespace(o));
        else
          properties.put(p, o);
      }      
    }
  }
  
  static public class HLMType extends Resource {
    public MetricTypeExpression expression;
    
    public void load(String hlmt) throws Exception {
      RDFStoreInterface rdfStore=RDFStore.getInstance();
      uri = hlmt;
      QueryResult query = rdfStore.query("SELECT ?p ?o WHERE {"
              + " GRAPH ?g { <"+hlmt+"> ?p ?o } }");
      for(Map<String,ResultValue> r : query.results()) {
        String p = namespace(r.get("p").getValue());
        String o = r.get("o").getValue();
        if(p.equals("rdf:type"))
          types.add(namespace(o));
        else if(p.equals("icr:hasExpression")) {
          expression = new MetricTypeExpression();
          expression.load(o);
        }
        else
          properties.put(p, o);
      }
    }
    
    public String getRdfXml() {
      String type = types.get(0); //TODO scegliere il tipo più specifico
      String xml="<"+type+" rdf:about=\""+uri+"\">\n";
      xml += rdfxmlLiteral(properties,"icr:hasMetricName");
      xml += rdfxmlLiteral(properties,"icr:hasDescription");
      xml += rdfxmlLiteral(properties,"icr:hasMetricUnit");
      xml += rdfxmlLiteral(properties,"icr:forGroup");
      xml += "<icr:hasExpression>"+expression.getRdfXml()+"</icr:hasExpression>\n";
      xml += "</"+type+">\n";
      return xml;
    }

    public String getHlmXml() {
      String xml="<metric name=\""+properties.get("icr:hasMetricName")+"\" unit=\""+properties.get("icr:hasMetricUnit")+"\">\n";
      xml += expression.getHlmXml();
      xml += "</metric>\n";
      return xml;
    }
  }
  
  static public class MetricTypeExpression extends Resource {
    public MetricTypeExpression[] args = null;
    public Resource useTimeInterval = null;
    public Resource onLowLevelMetricType = null;
    
    @Override
    public void load(String expr) throws Exception {
      RDFStoreInterface rdfStore=RDFStore.getInstance();
      uri = expr;
      QueryResult query = rdfStore.query(IcaroKnowledgeBase.SPARQL_PREFIXES+" SELECT ?p ?o WHERE {"
            + " <"+expr+"> ?p ?o } ");
      for(Map<String,ResultValue> r : query.results()) {
        String p = namespace(r.get("p").getValue());
        String o = r.get("o").getValue();
        if(p.equals("rdf:type"))
          types.add(namespace(o));
        else if(p.equals("icr:args")) {
          QueryResult queryArgs = rdfStore.query(IcaroKnowledgeBase.SPARQL_PREFIXES+"SELECT ?f WHERE {\n" +
              "<"+expr+"> icr:args ?a.\n" +
              "?a rdf:rest* [ rdf:first ?f].\n" +
              "}");
          args=new MetricTypeExpression[queryArgs.results().size()];
          int i=0;
          for(Map<String,ResultValue> arg : queryArgs.results()) {
            String f=arg.get("f").getValue();
            args[i]=new MetricTypeExpression();
            args[i].load(f);
            i++;
          }      
        }
        else if(p.equals("icr:useTimeInterval")) {
            QueryResult queryTI = rdfStore.query(IcaroKnowledgeBase.SPARQL_PREFIXES+"SELECT ?v ?u WHERE {\n" +
                      "<"+expr+"> icr:useTimeInterval ?ti.\n" +
                      "?ti icr:hasValue ?v.\n" +
                      "?ti icr:hasUnit ?u.\n" +
                      "}");
            useTimeInterval = new Resource();
            for(Map<String,ResultValue> x : queryTI.results()) {
              useTimeInterval.properties.put("icr:hasValue", x.get("v").getValue());
              useTimeInterval.properties.put("icr:hasUnit", x.get("u").getValue());
            }          
        }
        else if(p.equals("icr:onLowLevelMetricType")) {
          onLowLevelMetricType = new Resource();
          onLowLevelMetricType.load(o);
        }
        else
          properties.put(p, o);
      }
    }
    
    public String getRdfXml() {
      String type=namespace(types.get(0));
      if(!type.startsWith("icr:"))
        type=types.get(1);
      String xml="<"+type+" rdf:about=\""+uri+"\">\n";
      if(types.contains("icr:MetricMeasure")) {
        xml += "<icr:onLowLevelMetricType>\n";
        //TODO rivedere, basato su ipotesi ordinamento tipi 0 +specifico 1 -specifico
        String llmtype1=onLowLevelMetricType.types.get(0);
        String llmtype2=null;
        if(onLowLevelMetricType.types.size()==2) {
          llmtype1=onLowLevelMetricType.types.get(1);
          llmtype2=onLowLevelMetricType.types.get(0).replace("icr:", IcaroKnowledgeBase.NS_ICARO_CORE);
        }
        xml += "<"+llmtype1+" rdf:about=\""+onLowLevelMetricType.uri+"\">\n";
        if(llmtype2!=null)
          xml += rdfxmlUri("rdf:type", llmtype2);
        xml += rdfxmlLiteral(onLowLevelMetricType.properties,"icr:hasMetricName");
        xml += rdfxmlLiteral(onLowLevelMetricType.properties,"icr:hasPerfData");
        xml += rdfxmlLiteral(onLowLevelMetricType.properties,"icr:hasDescription");
        xml += rdfxmlLiteral(onLowLevelMetricType.properties,"icr:hasMetricUnit");
        xml+="</"+llmtype1+">\n";
        xml += "</icr:onLowLevelMetricType>\n";
        xml += rdfxmlLiteral(properties, "icr:useOperator");
        xml += rdfxmlLiteral(properties, "icr:useMultiValueOp");
        if(useTimeInterval!=null) {
          xml += "<icr:useTimeInterval rdf:parseType=\"Resource\">\n";
          xml += rdfxmlLiteral("icr:hasValue", useTimeInterval.properties.get("icr:hasValue"), true);
          xml += rdfxmlLiteral("icr:hasUnit", useTimeInterval.properties.get("icr:hasUnit"), false);
          xml += "</icr:useTimeInterval>\n";
        }
      }
      else if(types.contains("icr:ConstOp")) {
        xml += rdfxmlLiteral(properties,"icr:hasValue");      
      }
      else if(args!=null) {
        for (MetricTypeExpression arg : args) {
          xml += arg.getRdfXml();      
        }
      }
      xml+="</"+type+">\n";
      return xml;      
    }
    
    public String getHlmXml() {
      String type=namespace(types.get(0));
      String tag="xxx";
      String attr="";
      if(types.contains("icr:MulOp"))
        tag = "mul";
      else if(types.contains("icr:SumOp"))
        tag = "sum";
      else if(types.contains("icr:AvgOp"))
        tag = "avg";
      else if(types.contains("icr:SubOp"))
        tag = "sub";
      else if(types.contains("icr:DivOp"))
        tag = "div";
      else if(types.contains("icr:ConstOp"))
        tag = "const";
      else if(types.contains("icr:MetricMeasure")) {
        tag = "measure";
        attr = " multivalue=\""+properties.get("icr:useMultiValueOp")+"\"";
      }
      String xml="<"+tag+attr+">\n";
      if(tag.equals("measure")) {
        xml+="<lmetric>"+onLowLevelMetricType.properties.get("icr:hasMetricName")+"</lmetric>\n";
        String perfData=onLowLevelMetricType.properties.get("icr:hasPerfData");
        if(perfData==null)
          perfData = "";
        xml+="<perfdata>"+perfData+"</perfdata>\n";
        if(useTimeInterval!=null)
          xml+="<timeinterval unit=\""+useTimeInterval.properties.get("icr:hasUnit")+"\">"
                  + useTimeInterval.properties.get("icr:hasValue")+"</timeinterval>\n";
        xml+="<operator>"+properties.get("icr:useOperator")+"</operator>\n";
      }
      else if(tag.equals("const")){
        xml += "<val>"+properties.get("icr:hasValue")+"</val>\n";
      }
      else if(args!=null) {
        for (MetricTypeExpression arg : args) {
          xml += arg.getHlmXml();      
        }
      }
      xml += "</"+tag+">\n";
      return xml;
    }
  }
}
