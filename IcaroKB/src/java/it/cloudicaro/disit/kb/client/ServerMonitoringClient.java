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

package it.cloudicaro.disit.kb.client;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * Jersey REST client for REST resource:ServiceMetricResource
 * [serviceMetric]<br>
 * USAGE:
 * <pre>
 *        ServiceMetricClient client = new ServiceMetricClient();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 * @author bellini
 */
public class ServerMonitoringClient {

  private WebTarget webTarget;
  private Client client;

  public ServerMonitoringClient(String baseUri) {
    client = javax.ws.rs.client.ClientBuilder.newClient();
    webTarget = client.target(baseUri);
  }

  public ServerMonitoringClient(String baseUri, String username, String password) {
    this(baseUri);
    setUsernamePassword(username, password);
  }

  public String postXml(Object requestEntity) throws Exception {
    Response response = webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_XML).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_XML));
    if (response.getStatus() >= 300) {
      throw new Exception("Failed POST to SM: " + response.readEntity(String.class));
    }
    if (response.getStatus() == 204) {
      return "";
    }
    return response.readEntity(String.class);
  }

  public String putXml(Object requestEntity) throws Exception {
    Response response = webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_XML).put(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_XML));
    if (response.getStatus() >= 300) {
      throw new Exception("Failed PUT to SM: " + response.readEntity(String.class));
    }
    if (response.getStatus() == 204) {
      return "";
    }
    return response.readEntity(String.class);
  }
  
  public void delete() throws Exception {
    Response response = webTarget.request().delete();
    System.out.println("SM delete "+response.getStatus());
    if (response.getStatus() >= 300) {
      throw new Exception("Failed DELETE to SM: " + response.readEntity(String.class));
    }
  }
  
  public void close() {
    client.close();
  }

  public final void setUsernamePassword(String username, String password) {
    webTarget.register(new org.glassfish.jersey.client.filter.HttpBasicAuthFilter(username, password));
  }

}
