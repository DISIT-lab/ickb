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
 * Jersey REST client generated for REST resource:MetricTypeResource<br>
 * USAGE:
 * <pre>
 *        MetricTypeClient client = new MetricTypeClient();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 * @author bellini
 */
public class MetricTypeClient {
  private WebTarget webTarget;
  private Client client;

  public MetricTypeClient(String baseUri, String name) {
    client = javax.ws.rs.client.ClientBuilder.newClient();
    String resourcePath = java.text.MessageFormat.format("metricTypes/{0}", new Object[]{name});
    webTarget = client.target(baseUri).path(resourcePath);
  }

  public void setResourcePath(String baseUri, String name) {
    String resourcePath = java.text.MessageFormat.format("metricTypes/{0}", new Object[]{name});
    webTarget = client.target(baseUri).path(resourcePath);
  }

  public String getXml() throws ClientErrorException {
    WebTarget resource = webTarget;
    return resource.request(javax.ws.rs.core.MediaType.APPLICATION_XML).get(String.class);
  }

  public void delete() throws ClientErrorException {
    webTarget.request().delete();
  }

  public void putXml(Object requestEntity) throws Exception {
    //webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_XML).put(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_XML));
    Response response = webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_XML).put(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_XML));
    if (response.getStatus() >= 300) {
      throw new Exception("HTTP " + response.getStatus() + " " + response.getStatusInfo() + " " + response.readEntity(String.class));
    }
}

  public void close() {
    client.close();
  }

  public final void setUsernamePassword(String username, String password) {
    webTarget.register(new org.glassfish.jersey.client.filter.HttpBasicAuthFilter(username, password));
  }
  
}
