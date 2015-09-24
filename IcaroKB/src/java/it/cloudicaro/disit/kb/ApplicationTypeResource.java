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

import it.cloudicaro.disit.kb.rdf.RDFStore;
import it.cloudicaro.disit.kb.rdf.RDFStoreInterface;
import java.security.Principal;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;


/**
 * REST Web Service
 *
 * @author bellini
 */

public class ApplicationTypeResource {
    @Context UriInfo uriInfo;
    private String name;
    /** Creates a new instance of ApplicationTypeResource */
    private ApplicationTypeResource(String name) {
        this.name=name;
    }

    /** Get instance of the ApplicationTypeResource */
    public static ApplicationTypeResource getInstance(String name) {
        // The user may use some kind of persistence mechanism
        // to store and restore instances of ApplicationTypeResource class.
        return new ApplicationTypeResource(name);
    }

  /**
   * Retrieves representation of an instance of it.cloudicaro.disit.kb.ApplicationTypeResource
   * @return an instance of java.lang.String
   */
  @GET
  @Produces("application/xml")
  public String getXml() throws Exception {
    RDFStoreInterface rdfStore=RDFStore.getInstance();

    String graph="urn:cloudicaro:context:ApplicationType:"+this.name;
    return rdfStore.getStatements("application/rdf+xml", graph);
  }

  /**
   * PUT method for updating or creating an instance of ApplicationTypeResource
   * @param content representation for the resource
   * @return an HTTP response with content of the updated or created resource.
   */
  @PUT
  @Consumes("application/xml")
  public String putXml(String content, @Context HttpServletRequest request) throws Exception {
    if(IcaroKnowledgeBase.isRecovering())
      throw new KbException("KB is recovering try later",400);

    return putApplicationType(name, content, request);
  }
  
  static public String putApplicationType(String name, String content, @Context HttpServletRequest request) throws Exception {
    //remove context associated with id
    Date start=new Date();
    RDFStoreInterface rdfStore=RDFStore.getInstance();

    String graph="urn:cloudicaro:context:ApplicationType:"+name;
    rdfStore.removeGraph(graph);

    //post content on RDF store in the context
    rdfStore.addStatements(content, "application/rdf+xml", graph);
    
    //request is null when the KB is recovered
    if(request!=null) {
      rdfStore.flush();
      IcaroKnowledgeBase.storePut(start, content, name, "AT");
      IcaroKnowledgeBase.log(start, "PUT", "AT", name, "OK", request);
    }
    return "";
  }

  /**
   * DELETE method for resource ApplicationTypeResource
   */
  @DELETE
  public void delete(@Context HttpServletRequest request) throws Exception {
    if(IcaroKnowledgeBase.isRecovering())
      throw new KbException("KB is recovering try later",400);

    deleteApplicationType(name, request);
  }
  
  static public void deleteApplicationType(String name, @Context HttpServletRequest request) throws Exception {
    Date start=new Date();
      //remove context associated with id
    RDFStoreInterface rdfStore=RDFStore.getInstance();

    String graph="urn:cloudicaro:context:ApplicationType:"+name;
    rdfStore.removeGraph(graph);
    
    //request is null when the KB is recovered
    if(request!=null) {
      rdfStore.flush();
      IcaroKnowledgeBase.storeDelete(start, name, "AT");
      IcaroKnowledgeBase.log(start, "DELETE", "AT", name, "OK", request);
    }
  }
}
