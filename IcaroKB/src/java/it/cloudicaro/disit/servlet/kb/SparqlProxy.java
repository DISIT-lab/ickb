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

package it.cloudicaro.disit.servlet.kb;

import it.cloudicaro.disit.kb.Configuration;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.HttpClients;

/**

 */
public class SparqlProxy extends HttpServlet {

  private Log log = LogFactory.getLog(SparqlProxy.class);

  @Override
  public void doGet(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {
    String aSparqlEndpointQuery = theReq.getParameter("query");

    if (log.isDebugEnabled()) {
      log.debug(aSparqlEndpointQuery);
    }

    try {
      buildSparqlResponse(theReq, theResp);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void buildSparqlResponse(HttpServletRequest theReq, HttpServletResponse theResp) throws Exception {
    Date start=new Date();
    HttpClient httpclient = HttpClients.createDefault();
    HttpGet httpget = null;
    HttpResponse response = null;

    String theServerHost = Configuration.getInstance().get("kb.rdf.sparql_endpoint","http://192.168.0.106:8080/openrdf-sesame/repositories/icaro5");
    String query = theReq.getParameter("query");
    String aEncodedQuery = URLEncoder.encode(query, "UTF-8");

    String theReqUrl = theServerHost + "?query=" + aEncodedQuery;
    //System.out.println("request: " + theReqUrl);

    try {
      httpget = new HttpGet(theReqUrl);

      //System.out.println("request header:");
      Enumeration<String> aHeadersEnum = theReq.getHeaderNames();
      while (aHeadersEnum.hasMoreElements()) {
        String aHeaderName = aHeadersEnum.nextElement();
        String aHeaderVal = theReq.getHeader(aHeaderName);
        httpget.setHeader(aHeaderName, aHeaderVal);
        //System.out.println("  "+aHeaderName+": "+aHeaderVal);
      }

      if (log.isDebugEnabled()) {
        log.debug("executing request " + httpget.getURI());
      }

      // Create a response handler
      response = httpclient.execute(httpget);

      //System.out.println("response header:");
      // set the same Headers
      for (Header aHeader : response.getAllHeaders()) {
        if(!aHeader.getName().equals("Transfer-Encoding") || !aHeader.getValue().equals("chunked") )
          theResp.setHeader(aHeader.getName(), aHeader.getValue());
        //System.out.println("  "+aHeader.getName()+": "+aHeader.getValue());
      }

      // set the same locale
      if(response.getLocale()!=null)
        theResp.setLocale(response.getLocale());

      // set the content
      theResp.setContentLength((int) response.getEntity().getContentLength());
      theResp.setContentType(response.getEntity().getContentType().getValue());

      // set the same status
      theResp.setStatus(response.getStatusLine().getStatusCode());

      // redirect the output
      InputStream aInStream = null;
      OutputStream aOutStream = null;
      try {
        aInStream = response.getEntity().getContent();
        aOutStream = theResp.getOutputStream();

        byte[] buffer= new byte[1024];
        int r=0;
        do {
          r=aInStream.read(buffer);
          if(r!=-1)
            aOutStream.write(buffer,0,r);
        }
        while(r!=-1);
/*
        int data = aInStream.read();
        while (data != -1) {
          aOutStream.write(data);

          data = aInStream.read();
        }
 */
      } catch (IOException ioe) {
        ioe.printStackTrace();
      } finally {
        if (aInStream != null) {
          aInStream.close();
        }
        if (aOutStream != null) {
          aOutStream.close();
        }
      }
      Date end=new Date();
      System.out.println(start+" SPARQL-PERFORMANCE: "+(end.getTime()-start.getTime())+"ms "+query);
    } finally {
      httpclient.getConnectionManager().closeExpiredConnections();
      httpclient.getConnectionManager().shutdown();
    }
  }
}
