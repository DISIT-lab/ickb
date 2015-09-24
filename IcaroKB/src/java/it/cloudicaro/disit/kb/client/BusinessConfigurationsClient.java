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

/**
 * Jersey REST client generated for REST
 * resource:BusinessConfigurationCheckResource [businessConfigurationCheck]<br>
 * USAGE:
 * <pre>
 *        BusinessConfigurationCheckClient client = new BusinessConfigurationCheckClient();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 * @author bellini
 */
public class BusinessConfigurationsClient {

  private WebTarget webTarget;
  private Client client;
  private static final String BASE_URI = "http://localhost:8080/IcaroKB/api";

  public BusinessConfigurationsClient(String baseUri) {
    client = javax.ws.rs.client.ClientBuilder.newClient();
    webTarget = client.target(baseUri).path("businessConfiguration");
  }

  public BusinessConfigurationsClient(String baseUri, String username, String password) {
    this(baseUri);
    setUsernamePassword(username, password);
  }

  public String postXml(Object requestEntity) throws ClientErrorException {
    return webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_XML).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_XML), String.class);
  }

  public void close() {
    client.close();
  }

  public final void setUsernamePassword(String username, String password) {
    webTarget.register(new org.glassfish.jersey.client.filter.HttpBasicAuthFilter(username, password));
  }

}
