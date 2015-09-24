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

import java.util.Date;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;

/**
 * REST Web Service
 *
 * @author bellini
 */
@Path("status")
public class StatusResource {

  @Context
  private UriInfo context;

  /**
   * Creates a new instance of StatusResource
   */
  public StatusResource() {
  }

  /**
   * Retrieves representation of an instance of it.cloudicaro.disit.kb.StatusResource
   * @return an instance of java.lang.String
   */
  @GET
  @Produces("application/xml")
  public String getXml(@Context HttpServletRequest request) throws Exception{
    Date start=new Date();
    String statusXML="<kb-status>";
    try{
      Map<String,String> status=it.cloudicaro.disit.kb.IcaroKnowledgeBase.getStatus();
      for(Map.Entry<String,String> e: status.entrySet()) {
        statusXML += "  <value type=\""+e.getKey()+"\">"+e.getValue()+"</value>\n";
      }
    }
    catch(Exception e) {
      statusXML += "  <exception>"+e.getLocalizedMessage()+"</exception>";
      e.printStackTrace();
    }
    statusXML+="</kb-status>";
    IcaroKnowledgeBase.log(start, "GET", "STATUS", "", "OK", request);
    return statusXML;
  }

}
