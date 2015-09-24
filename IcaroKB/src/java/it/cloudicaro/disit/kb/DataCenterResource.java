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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * REST Web Service
 *
 * @author bellini
 */

public class DataCenterResource {
    private String id;
    /** Creates a new instance of ItemResource */
    private DataCenterResource(String id) {
        this.id=id;
    }

    /** Get instance of the ItemResource */
    public static DataCenterResource getInstance(String id) {
        // The user may use some kind of persistence mechanism
        // to store and restore instances of ItemResource class.
        return new DataCenterResource(id);
    }

  /**
   * Retrieves representation of an instance of it.cloudicaro.unifi.kb.ItemResource
   * @return an instance of java.lang.String
   */
  @GET
  @Produces("application/xml")
  public String getXml() throws Exception {
    RDFStoreInterface rdfStore=RDFStore.getInstance();

    String graph="urn:cloudicaro:context:DataCenter:"+this.id;
    return rdfStore.getStatements("application/rdf+xml", graph);
  }

  /**
   * PUT method for updating or creating an instance of ItemResource
   * @param content representation for the resource
   * @return an HTTP response with content of the updated or created resource.
   */
  @PUT
  @Consumes("application/xml")
  @Produces("application/xml")
  public String putXml(String content, @Context HttpServletRequest request) throws Exception {
    if(IcaroKnowledgeBase.isRecovering())
      throw new KbException("KB is recovering try later",400);

    return putDataCenter(id, content, request);
  }
  
  static public String putDataCenter(String id, String content, @Context HttpServletRequest request) throws Exception {
    Date start=new Date();
    Configuration conf=Configuration.getInstance();
    boolean check = (request!=null || conf.get("kb.recover.force_checkDC", "false").equals("true"));
    String validationResult = null;
    
    if(check) {
      //validate 'content' using xml schema
      validationResult=ValidateResource.validateContent(content,"schema-icaro-kb-dataCenter.xsd");
      if(validationResult!=null) {
        IcaroKnowledgeBase.error(start, "PUT", "DC", id, "FAILED-XML-VALIDATION", validationResult, content, request);
        throw new KbException(validationResult,400);
      }
    }

    //remove context associated with id
    RDFStoreInterface rdfStore=RDFStore.getInstance();

    //get the BusinessConfiguration rdf:about and check if the resource name from the url is the ending part of the rdf:about
    Document doc = ValidateResource.parseXml(content);
    NodeList dc = doc.getElementsByTagNameNS(IcaroKnowledgeBase.NS_ICARO_CORE, "DataCenter");
    String dcAbout="";
    if(dc.getLength()==1)
      dcAbout=dc.item(0).getAttributes().getNamedItemNS(IcaroKnowledgeBase.NS_RDF, "about").getNodeValue();
    
    if(check && conf.get("kb.validateDCAbout", "true").equalsIgnoreCase("true") && !dcAbout.endsWith(id)) {
      String error = "DataCenter rdf:about='"+dcAbout+"' does not end with '"+id+"'";
      IcaroKnowledgeBase.error(start, "PUT", "DC", id, "FAILED-ID-CHECK", error, content, request);
      throw new KbException(error,400);
    }

    String graph="urn:cloudicaro:context:DataCenter:"+id;
    
    //check if the datacenter is already present or not
    Boolean toUpdate=false;
    QueryResult qr = rdfStore.query("SELECT * WHERE { <"+dcAbout+"> ?p ?o } LIMIT 1");
    toUpdate = (qr.results().size()>=1);

    rdfStore.removeGraph(graph);

    //post content on RDF store in the context
    rdfStore.addStatements(ValidateResource.transformBlankNodes(doc), "application/rdf+xml", graph);

    //request is null when the KB is recovered
    if(request!=null) {
      rdfStore.flush();
      IcaroKnowledgeBase.storePut(start, content, id, "DC");
    
      if(conf.get("kb.sm.forward.DataCenter", "false").equals("true")) {
        try {
          String smUrl=conf.get("kb.sm.url", "http://192.168.0.37/icaro/api/configurator") + "/system";

          if(toUpdate)
            smUrl = smUrl + "/" + dcAbout;

          ServerMonitoringClient client=new ServerMonitoringClient(smUrl);
          client.setUsernamePassword(conf.get("kb.sm.user", "test"), conf.get("kb.sm.passwd", "12345"));

          String result;
          if(toUpdate) {
            System.out.println("PUT DC " + dcAbout + " to SM");
            result=client.putXml(content);
          }
          else {
            System.out.println("POST DC to SM");
            result = client.postXml(content);
          }
          System.out.println("SM result:\n" + result);

          client.close();
          IcaroKnowledgeBase.log(start, "PUT", "DC", id, "OK", request);
          return result;
        }
        catch(Exception e) {
          e.printStackTrace();
          IcaroKnowledgeBase.error(start, "PUT", "DC", id, "FAILED-SM", e.getLocalizedMessage(), content, request);
          throw new KbException("Failed setting monitoring: "+e.getLocalizedMessage(),400);
        }
      }
      IcaroKnowledgeBase.log(start, "PUT", "DC", id, "OK", request);
    }
    return"";
  }

  /**
   * DELETE method for resource ItemResource
   */
  @DELETE
  public void delete(@Context HttpServletRequest request) throws Exception {
    if(IcaroKnowledgeBase.isRecovering())
      throw new KbException("KB is recovering try later",400);

    deleteDataCenter(id, request);
  }
  
  static public void deleteDataCenter(String id, @Context HttpServletRequest request) throws Exception {
    Date start=new Date();
    Configuration conf=Configuration.getInstance();
    
    //remove context associated with id
    RDFStoreInterface rdfStore=RDFStore.getInstance();

    String graph="urn:cloudicaro:context:DataCenter:"+id;
    String dcAbout = "";
    if(conf.get("kb.sm.forward.DataCenter", "false").equals("true")) {
      //find the DataCenter id that is in this graph
      QueryResult qr = rdfStore.query(IcaroKnowledgeBase.SPARQL_PREFIXES+"SELECT ?dc WHERE { GRAPH <"+graph+"> { ?dc a icr:DataCenter } }");
      if(qr.results().size()==1) {
        dcAbout = qr.results().get(0).get("dc").getValue();
      }
      else if(qr.results().size()>1) {
        String error = "Not only one DataCenter in graph "+graph;
        IcaroKnowledgeBase.error(start, "DELETE", "DC", id, "FAILED-ID-CHECK", error, "", request);
        throw new KbException(error, 400);
      }
    }
  
    rdfStore.removeGraph(graph);
    
    //request is null when the KB is recovered
    if(request!=null) {
      rdfStore.flush();
      IcaroKnowledgeBase.storeDelete(start, id, "DC");
    
      if(!dcAbout.equals("")){
        //forward the delete operation to SM
        try {
          String smUrl=conf.get("kb.sm.url", "http://192.168.0.37/icaro/api/configurator") + "/" + dcAbout;        
          ServerMonitoringClient client=new ServerMonitoringClient(smUrl);
          client.setUsernamePassword(conf.get("kb.sm.user", "test"), conf.get("kb.sm.passwd", "12345"));

          System.out.println("DELETE DC "+dcAbout+" to SM");
          client.delete();
          client.close();
        }
        catch(Exception e) {
          e.printStackTrace();
          IcaroKnowledgeBase.error(start, "DELETE", "DC", id, "FAILED-SM", e.getLocalizedMessage(), "", request);
          throw new KbException("Failed deleting monitoring: "+e.getLocalizedMessage(),400);
        }
      }
      IcaroKnowledgeBase.log(start, "DELETE", "DC", id, "OK", request);
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
    if(this.id.startsWith("urn:") || this.id.startsWith("http:")) {
      QueryResult query = rdfStore.query(IcaroKnowledgeBase.SPARQL_PREFIXES+" SELECT ?g WHERE {"
            + " GRAPH ?g {"
            + "   <"+this.id+"> a icr:DataCenter."
            + " }}");
      if(query.results().size()>0)
        graph = query.results().get(0).get("g").getValue();
      else 
        throw new KbException("DataCenter \""+this.id+"\" not found", 400);
    }
    else
      graph = "urn:cloudicaro:context:DataCenter:"+this.id;
    
    //prende dalla config i nomi delle metriche di alto livello da calcolare comunque
    Configuration conf=Configuration.getInstance();
    String defaultMetrics = conf.get("kb.hlm.default_dc_metrics", "");
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
    IcaroKnowledgeBase.log(start, "GET-HLM", "DC", id, "OK", request);
    return xml;
  }
}
