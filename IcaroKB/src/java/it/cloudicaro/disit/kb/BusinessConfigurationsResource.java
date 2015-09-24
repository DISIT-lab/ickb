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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;

/**
 * REST Web Service
 *
 * @author bellini
 */

@Path("/businessConfiguration")
public class BusinessConfigurationsResource {
    @Context
    private UriInfo context;

    /** Creates a new instance of BusinessConfigurationsResource */
    public BusinessConfigurationsResource() {
    }

  /**
   * Retrieves representation of an instance of it.cloudicaro.unifi.kb.BusinessConfigurationsResource
   * @return an instance of java.lang.String
   */
  @GET
  @Produces("application/xml")
  public String getXml() {
    //TODO return proper representation object
    throw new UnsupportedOperationException();
  }

  /**
   * POST method for updating or creating an instance of ServiceMetricResource
   * @param content representation for the resource
   * @return an HTTP response with content of the updated or created resource.
   */
  @POST
  @Consumes("application/xml")
  @Produces("application/xml")
  public String postXml(String content, @Context HttpServletRequest request) throws Exception {
    return BusinessConfigurationResource.getInstance("").putXml(content, request);
  }

  /**
   * Sub-resource locator method for {name}
   */
  @Path("{name}")
  public BusinessConfigurationResource getBusinessConfigurationResource(@PathParam("name")
  String name) {
    return BusinessConfigurationResource.getInstance(name);
  }
}
