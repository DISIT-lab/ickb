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

import it.cloudicaro.disit.kb.rdf.RDFStoreInterface;
import it.cloudicaro.disit.kb.rdf.RDFStore;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * REST Web Service
 *
 * @author bellini
 */

@Path("serviceMetric")
public class ServiceMetricResource {
  @Context
  private UriInfo context;

    /** Creates a new instance of ServiceMetricResource */
  public ServiceMetricResource() {
  }


  /**
   * POST method for updating or creating an instance of ServiceMetricResource
   * @param content representation for the resource
   * @return an HTTP response with content of the updated or created resource.
   */
  @POST
  @Consumes("application/xml")
  public void postXml(String content, @Context HttpServletRequest request) throws Exception {
    if(IcaroKnowledgeBase.isRecovering())
      throw new KbException("KB is recovering try later",400);

    postServiceMetric(content, request);
  }
  
  static public void postServiceMetric(String content, @Context HttpServletRequest request) throws Exception {
    Date start=new Date();
    Configuration conf=Configuration.getInstance();
    String validationResult = null;
    boolean check = (request!=null || conf.get("kb.recover.force_checkSM", "false").equals("true"));
    
    if(check) {
      //validate 'content' using xml schema
      validationResult=ValidateResource.validateContent(content,"schema-icaro-kb-serviceMetric.xsd");
      if(validationResult!=null) {
        IcaroKnowledgeBase.error(start, "POST", "SM", "", "FAILED-XML-VALIDATION", validationResult, content, request);
        throw new KbException(validationResult,400);
      }
    }

    if(check) {
      //check metric names
      Document doc = ValidateResource.parseXml(content);
      NodeList metricNames = doc.getElementsByTagNameNS(IcaroKnowledgeBase.NS_ICARO_CORE, "hasMetricName");
      for(int i=0; i<metricNames.getLength(); i++) {
        String mname=metricNames.item(i).getTextContent();
        //System.out.println("checking metric name "+mname);
        if(!IcaroKnowledgeBase.checkMetricName(mname)) {
          String error = "Metric '"+mname+"' does not exists as KB metric type name";
          IcaroKnowledgeBase.error(start, "POST", "SM", "", "FAILED-METRIC-NAME", error, content, request);
          throw new KbException(error,400);
        } 
      }
    }
    
    //remove context associated with id
    RDFStoreInterface rdfStore=RDFStore.getInstance();

    //post content on RDF store in the context
    rdfStore.addStatements(ValidateResource.transformBlankNodes(content), "application/rdf+xml", "urn:cloudicaro:context:ServiceMetric");
    
    //request is null when the KB is recovering
    if(request!=null) {
      rdfStore.flush();
      IcaroKnowledgeBase.storeSubmission(start, content, "POST", "", "SM");
      IcaroKnowledgeBase.log(start, "POST", "SM", "", "OK", request);
    }
  }
}
