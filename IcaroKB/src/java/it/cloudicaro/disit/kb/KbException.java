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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 *
 * @author bellini
 */
public class KbException extends WebApplicationException {

  private String msg = "Unknown error (500)";
  /**
  * Create a HTTP 404 (Not Found) exception.
  */
  public KbException() {
    super(Response.status(500).build());
  }

  /**
  * Create a HTTP 404 (Not Found) exception.
  * @param message the String that is the entity of the 404 response.
  */
  public KbException(String message, int status) {
    super(Response.status(status).
    entity(message).type("text/plain").build());
    msg = "("+status+") "+message;
  }
  
  @Override
  public String getMessage() {
    return msg;
  }
}

