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

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;

/**
 * REST Web Service
 *
 * @author bellini
 */
@Path("/metricTypes")
public class MetricTypesResource {

  @Context
  private UriInfo context;

  /**
   * Creates a new instance of MetricTypesResource
   */
  public MetricTypesResource() {
  }

  /**
   * Retrieves representation of an instance of it.cloudicaro.disit.kb.MetricTypesResource
   * @return an instance of java.lang.String
   */
  @GET
  @Produces("application/xml")
  public String getXml() {
    //TODO return proper representation object
    throw new UnsupportedOperationException();
  }

  /**
   * Sub-resource locator method for {name}
   */
  @Path("{name}")
  public MetricTypeResource getMetricTypeResource(@PathParam("name") String name) {
    return MetricTypeResource.getInstance(name);
  }
}
