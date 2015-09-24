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

import it.cloudicaro.disit.kb.client.ServerMonitoringClient;
import it.cloudicaro.disit.kb.rdf.QueryResult;
import it.cloudicaro.disit.kb.rdf.RDFStoreInterface;
import it.cloudicaro.disit.kb.rdf.RDFStore;
import it.cloudicaro.disit.kb.rdf.ResultValue;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import org.apache.commons.codec.binary.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


/**
 * REST Web Service
 *
 * @author bellini
 */

public class BusinessConfigurationResource {
    private String name;
    /** Creates a new instance of BusinessConfigurationResource */
    private BusinessConfigurationResource(String name) {
        this.name=name;
    }

    /** Get instance of the BusinessConfigurationResource */
    public static BusinessConfigurationResource getInstance(String name) {
        // The user may use some kind of persistence mechanism
        // to store and restore instances of BusinessConfigurationResource class.
        return new BusinessConfigurationResource(name);
    }

  /**
   * Retrieves representation of an instance of it.cloudicaro.unifi.kb.BusinessConfigurationResource
   * @return an instance of java.lang.String
   */
  @GET
  @Produces("application/xml")
  public String getXml() throws Exception {
    RDFStoreInterface rdfStore=RDFStore.getInstance();

    String graph="urn:cloudicaro:context:BusinessConfiguration:"+this.name;
    return rdfStore.getStatements("application/rdf+xml", graph);
  }

  /**
   * PUT method for updating or creating an instance of BusinessConfigurationResource
   * @param content representation for the resource
   * @return an HTTP response with content of the updated or created resource.
   */
  @PUT
  @Consumes("application/xml")
  @Produces("application/xml")
  public String putXml(String content, @Context HttpServletRequest request) throws Exception {
    if(IcaroKnowledgeBase.isRecovering())
      throw new KbException("KB is recovering try later",400);

    return putBusinessConfiguration(this.name, content, request);
  }
  
  static public String putBusinessConfiguration(String name, String content, HttpServletRequest request) throws Exception {
    Date start=new Date();
    String op = (name.equals("") ? "POST" : "PUT");
    Configuration conf=Configuration.getInstance();
    String validationResult = null;
    String bcAbout="";
    boolean check = (request!=null || conf.get("kb.recover.force_checkBC", "false").equals("true"));
    
    if(check) {
      //validate 'content' using xml schema
      validationResult=ValidateResource.validateContent(content,"schema-icaro-kb-businessConfiguration.xsd");
      if(validationResult!=null) {
        IcaroKnowledgeBase.error(start, op, "BC", name, "FAILED-XML-VALIDATION", validationResult, content, request);
        throw new KbException(validationResult,400);
      }
    }

    //get the BusinessConfiguration rdf:about and check if the resource name from the url is the ending part of the rdf:about
    Document doc = ValidateResource.parseXml(content);
    NodeList bc = doc.getElementsByTagNameNS(IcaroKnowledgeBase.NS_ICARO_CORE, "BusinessConfiguration");
    if(bc.getLength()==1)
      bcAbout=bc.item(0).getAttributes().getNamedItemNS(IcaroKnowledgeBase.NS_RDF, "about").getNodeValue();

    String bcPrefix = conf.get("kb.bcPrefix", "urn:cloudicaro:BusinessConfiguration:");

    if(check && name.equals("")) {
      if(!bcAbout.startsWith(bcPrefix)) {
        String error = "BusinessConfiguration rdf:about='"+bcAbout+"' does not start with '"+bcPrefix+"'";
        IcaroKnowledgeBase.error(start, op, "BC", name, "FAILED-ID-CHECK1", error, content, request);
        throw new KbException(error,400);
      }
      name = bcAbout.substring(bcPrefix.length());
    }

    if(check && conf.get("kb.validateBCAbout", "true").equalsIgnoreCase("true") && !bcAbout.endsWith(name)) {
      String error = "BusinessConfiguration rdf:about='"+bcAbout+"' does not end with '"+name+"'";
      IcaroKnowledgeBase.error(start, op, "BC", name, "FAILED-ID-CHECK2", error, content, request);
      throw new KbException(error,400);
    }
    
    RDFStoreInterface rdfStore=RDFStore.getInstance();
    
    //check if the business configuration is already present or not
    Boolean toUpdate=false;
    QueryResult qr = rdfStore.query("SELECT * WHERE { <"+bcAbout+"> ?p ?o } LIMIT 1");
    toUpdate = (qr.results().size()>=1);

    //upload to a new temporary context, if ok rename the context otherwise remove
    String tmpGraph="urn:cloudicaro:context:BusinessConfiguration:tmp:"+UUID.randomUUID();
    String graph="urn:cloudicaro:context:BusinessConfiguration:"+name;

    String data = ValidateResource.transformBlankNodes(doc);
    rdfStore.addStatements(data, "application/rdf+xml", tmpGraph);
    try {
      if(check)
        validationResult=ValidateResource.validateBusinessConfigurationGraph(tmpGraph);
    }
    finally {
      if(validationResult!=null) {
        rdfStore.removeGraph(tmpGraph);
        IcaroKnowledgeBase.error(start, op, "BC", name, "FAILED-KB-VALIDATION", validationResult, content, request);
        throw new KbException(validationResult,400);
      } else {
        //remove context associated with id and then rename the graph to the new one
        rdfStore.removeGraph(graph);
        if(conf.get("kb.bcUpdateFix", "false").equals("false"))
          rdfStore.update("MOVE <"+tmpGraph+"> TO <"+graph+">");
        else {
          rdfStore.removeGraph(tmpGraph);
          rdfStore.addStatements(data, "application/rdf+xml", graph);
        }
      }
    }

    Date mid=new Date();
    //post content on RDF store in the context
    //???rdfStore.addStatements(content, "application/rdf+xml", graph);
    if(request!=null) {
      rdfStore.flush();
      // save the request to disk
      IcaroKnowledgeBase.storePut(start, content, name, "BC");

      if(conf.get("kb.sm.forward.BusinessConfiguration", "false").equals("true")) {
        try {
          String smUrl=conf.get("kb.sm.url", "http://192.168.0.37/icaro/api/configurator");

          if(toUpdate)
            smUrl = smUrl + "/" + bcAbout;

          ServerMonitoringClient client=new ServerMonitoringClient(smUrl);
          client.setUsernamePassword(conf.get("kb.sm.user", "test"), conf.get("kb.sm.passwd", "12345"));

          String result;
          if(toUpdate) {
            System.out.println("PUT BC " + bcAbout + " to SM");
            result=client.putXml(content);
          }
          else {
            System.out.println("POST BC to SM");
            result = client.postXml(content);
          }
          System.out.println("SM result:\n" + result);

          client.close();
          Date end=new Date();
          System.out.println(start+" KB-PERFORMANCE "+op+" BC "+name+" kb:"+(mid.getTime()-start.getTime())+"ms sm:"+(end.getTime()-mid.getTime())+"ms");
          IcaroKnowledgeBase.log(start, op, "BC", name, "OK", request);
          /*if(conf.get("kb.sce.forward.BusinessConfiguration", "false").equals("true")) {
            try {
              String sceUrl=conf.get("kb.sce.url", "");
            }
            catch(Exception e) {
              e.printStackTrace();
              IcaroKnowledgeBase.error(start, op, "BC", name, "FAILED-SCE", e.getLocalizedMessage(), content, request);
            }
          }*/
          return result;
        }
        catch(Exception e) {
          e.printStackTrace();
          IcaroKnowledgeBase.error(start, op, "BC", name, "FAILED-SM", e.getLocalizedMessage(), content, request);
          throw new KbException("Failed setting monitoring: "+e.getLocalizedMessage(),400);
        }
      }
      IcaroKnowledgeBase.log(start, op, "BC", name, "OK", request);
    }
    return "";
  }

  /**
   * DELETE method for resource BusinessConfigurationResource
   */
  @DELETE
  public void delete(@Context HttpServletRequest request) throws Exception {
    if(IcaroKnowledgeBase.isRecovering())
      throw new KbException("KB is recovering try later",400);

    deleteBusinessConfiguration(this.name, request);
  }
  
  static public void deleteBusinessConfiguration(String name,@Context HttpServletRequest request) throws Exception {
    Date start=new Date();
    Configuration conf=Configuration.getInstance();
    //remove context associated with id
    RDFStoreInterface rdfStore=RDFStore.getInstance();

    String graph = "urn:cloudicaro:context:BusinessConfiguration:"+name;
    String bcAbout = "";
    if(conf.get("kb.sm.forward.BusinessConfiguration", "false").equals("true")) {
      //find the BusinessConfiguration id that is in this graph
      QueryResult qr = rdfStore.query(IcaroKnowledgeBase.SPARQL_PREFIXES+"SELECT ?bc WHERE { GRAPH <"+graph+"> { ?bc a icr:BusinessConfiguration } }");
      if(qr.results().size()==1) {
        bcAbout = qr.results().get(0).get("bc").getValue();
      }
      else if(qr.results().size()>1) {
        String error = "Not only one BC in graph "+graph;
        IcaroKnowledgeBase.error(start, "DELETE", "BC", name, "FAILED-ID-CHECK", error, null, request);
        throw new KbException(error, 400);
      }
    }
  
    rdfStore.removeGraph(graph);
    
    //request is null when the KB is recovered
    if(request!=null) {
      rdfStore.flush();
      IcaroKnowledgeBase.storeDelete(start, name, "BC");

      Date mid=new Date();
      if(!bcAbout.equals("")){
        //forward the delete operation to SM
        try {
          String smUrl=conf.get("kb.sm.url", "http://192.168.0.37/icaro/api/configurator") + "/" + bcAbout;        
          ServerMonitoringClient client=new ServerMonitoringClient(smUrl);
          client.setUsernamePassword(conf.get("kb.sm.user", "test"), conf.get("kb.sm.passwd", "12345"));

          System.out.println("DELETE BC "+bcAbout+" to SM");
          client.delete();
          client.close();
        }
        catch(Exception e) {
          e.printStackTrace();
          IcaroKnowledgeBase.error(start, "DELETE", "BC", name, "FAILED-SM", e.getLocalizedMessage(), null, request);
          throw new KbException("Failed deleting monitoring: "+e.getLocalizedMessage(),400);
        }
      }
      Date end=new Date();
      System.out.println(start+" KB-PERFORMANCE DELETE BC "+name+" kb:"+(mid.getTime()-start.getTime())+"ms sm:"+(end.getTime()-mid.getTime())+"ms");
      IcaroKnowledgeBase.log(start, "DELETE", "BC", name, "OK", request);
    }
  }
  
  @Path("hlmetrics")
  @GET
  @Produces("application/xml")
  public String getHlmMetrics(@QueryParam("format") String format,@Context HttpServletRequest request) throws Exception {
    if(IcaroKnowledgeBase.isRecovering())
      throw new KbException("KB is recovering try later",400);

    String xml="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    Date start=new Date();
    RDFStoreInterface rdfStore=RDFStore.getInstance();
    
    //determina il grafo della BC
    String graph;
    if(this.name.startsWith("urn:") || this.name.startsWith("http:")) {
      QueryResult query = rdfStore.query(IcaroKnowledgeBase.SPARQL_PREFIXES+" SELECT ?g WHERE {"
            + " GRAPH ?g {"
            + "   <"+this.name+"> a icr:BusinessConfiguration."
            + " }}");
      if(query.results().size()>0)
        graph = query.results().get(0).get("g").getValue();
      else 
        throw new KbException("BusinessConfiguration \""+this.name+"\" not found", 400);
    }
    else
      graph = "urn:cloudicaro:context:BusinessConfiguration:"+this.name;
    
    //prende dalla config i nomi delle metriche di alto livello da calcolare comunque
    Configuration conf=Configuration.getInstance();
    String defaultMetrics = conf.get("kb.hlm.default_bc_metrics", "");
    String union="";
    if(defaultMetrics.trim().length()>0) {
      String[] bc_metrics=defaultMetrics.split(";");
      StringBuilder filter = new StringBuilder();
      filter.append( bc_metrics[0] );
      for ( int x=1; x < bc_metrics.length; ++x )
      {
        filter.append("\",\"").append(bc_metrics[x]);
      }
      union = " UNION {"
        + "  ?mt a icr:HighLevelMetricType;"
        + "  icr:hasMetricName ?mn."
        + "  FILTER (?mn IN (\""+filter.toString()+"\")) }";
    }
    if(format==null || format.equalsIgnoreCase("HLMXML")) {
      //cerca le metriche di alto livello per macchina usate nella configurazione, ordinate per gruppo
      QueryResult query = rdfStore.query(IcaroKnowledgeBase.SPARQL_PREFIXES+" SELECT DISTINCT ?grp ?mt WHERE {"
            + " { GRAPH <"+graph+"> {"
            + "   ?s icr:hasMetricName ?mn."
            + " }"
            + " ?mt a icr:MachineHighLevelMetricType;"
            + "   icr:hasMetricName ?mn. } "
            + union.replace("icr:HighLevelMetricType", "icr:MachineHighLevelMetricType")
            + " ?mt  icr:forGroup ?grp."  
            + " } ORDER BY ?grp");
      xml += "<metrics>\n"+
              "<hostgroupmetrics>\n";
      String prvGrp = "";
      for(Map<String,ResultValue> r : query.results()) {
        String grp=r.get("grp").getValue();
        String mt=r.get("mt").getValue();
        if(!grp.equals(prvGrp)) {
          if(!prvGrp.equals(""))
            xml += "</hostgroup>\n";
          xml += "<hostgroup group=\""+grp+"\">\n";
        }
        xml += IcaroKnowledgeBase.getHighLevelMetricType(mt).getHlmXml();
        prvGrp = grp;
      }
      if(!prvGrp.equals(""))
        xml += "</hostgroup>\n";
      
      //cerca le metriche di alto livello per servizi usate nella configurazione, ordinate per gruppo
      query = rdfStore.query(IcaroKnowledgeBase.SPARQL_PREFIXES+" SELECT DISTINCT ?grp ?mt WHERE {"
            + " { GRAPH <"+graph+"> {"
            + "   ?s icr:hasMetricName ?mn."
            + " }"
            + " ?mt a icr:ServiceHighLevelMetricType;"
            + "   icr:hasMetricName ?mn. }"
            + union.replace("icr:HighLevelMetricType", "icr:ServiceHighLevelMetricType")
            + "  ?mt icr:forGroup ?grp."  
            + " } ORDER BY ?grp");
      xml += "</hostgroupmetrics>\n"+
              "<servicemetrics>\n";
      prvGrp = "";
      for(Map<String,ResultValue> r : query.results()) {
        String grp=r.get("grp").getValue();
        String mt=r.get("mt").getValue();
        if(!grp.equals(prvGrp)) {
          if(!prvGrp.equals(""))
            xml += "</servicegroup>\n";
          xml += "<servicegroup group=\""+grp+"\">\n";
        }
        xml += IcaroKnowledgeBase.getHighLevelMetricType(mt).getHlmXml();
        prvGrp = grp;
      }
      if(!prvGrp.equals(""))
        xml += "</servicegroup>\n";
      xml += "</servicemetrics>\n";
      xml += "</metrics>\n";
    }
    else {      
      //cerca le metriche usate nella configurazione
      QueryResult query = rdfStore.query(IcaroKnowledgeBase.SPARQL_PREFIXES+" SELECT DISTINCT ?mt WHERE {"
            + " { GRAPH <"+graph+"> {"
            + "   ?s icr:hasMetricName ?mn."
            + " }"
            + " ?mt a icr:HighLevelMetricType;"
            + "   icr:hasMetricName ?mn. }"+union+"}");
    
      xml+="<rdf:RDF xmlns:app=\"http://www.cloudicaro.it/cloud_ontology/applications#\" "
              + "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "
              + "xmlns:icr=\"http://www.cloudicaro.it/cloud_ontology/core#\" "
              + "xmlns:foaf=\"http://xmlns.com/foaf/0.1/\">\n";
      //carica la definizione delle metriche di alto livello usate
      //produce l'xml per calcolo HLM
      for(Map<String,ResultValue> r : query.results()) {
        String mt=r.get("mt").getValue();
        xml+=IcaroKnowledgeBase.getHighLevelMetricType(mt).getRdfXml();
      }
      xml+="</rdf:RDF>\n";
    }
    IcaroKnowledgeBase.log(start, "GET-HLM", "BC", name, "OK", request);
    return xml;
  }
}
