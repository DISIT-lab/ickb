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

package it.cloudicaro.disit.kb.rdf;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author bellini
 */
public class HttpUtil {
  
  public static String get(URL url,String accept) throws Exception {
    return get(url,accept,null,null);
  }

  public static String get(URL url,String accept, String user, String passwd) throws Exception {
    //System.out.println("GET "+url);
    HttpClient client = HttpClients.createDefault();
    HttpGet request = new HttpGet(url.toURI());
    if(accept!=null)
      request.addHeader("Accept", accept);

    HttpClientContext context = HttpClientContext.create();
    if(user!=null && passwd!=null) {
      CredentialsProvider credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(
              AuthScope.ANY,
              new UsernamePasswordCredentials(user, passwd));

      /*// Create AuthCache instance
      AuthCache authCache = new BasicAuthCache();
      // Generate BASIC scheme object and add it to the local auth cache
      BasicScheme basicAuth = new BasicScheme();
      authCache.put(targetHost, basicAuth);*/

      // Add AuthCache to the execution context
      context.setCredentialsProvider(credsProvider);
    }
    HttpResponse response = client.execute(request,context);

    StatusLine s=response.getStatusLine();
    int code=s.getStatusCode();
    //System.out.println(code);
    if(code!=200)
      throw new Exception("failed access to "+url.toString()+" code: "+code+" "+s.getReasonPhrase());

    Reader reader = null;
    try {
      reader = new InputStreamReader(response.getEntity().getContent());

      StringBuilder sb = new StringBuilder();
      {
        int read;
        char[] cbuf = new char[1024];
        while ((read = reader.read(cbuf)) != -1) {
          sb.append(cbuf, 0, read);
        }
      }

      return sb.toString();
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static String post(URL url, String data, String contentType, String user, String passwd) throws Exception {
    //System.out.println("POST "+url);
    HttpClient client = HttpClients.createDefault();
    HttpPost request = new HttpPost(url.toURI());
    request.setEntity(new StringEntity(data,ContentType.create(contentType, "UTF-8")));

    HttpClientContext context = HttpClientContext.create();
    if(user!=null && passwd!=null) {
      CredentialsProvider credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(
              AuthScope.ANY,
              new UsernamePasswordCredentials(user, passwd));
      context.setCredentialsProvider(credsProvider);
    }

    HttpResponse response = client.execute(request,context);

    StatusLine s=response.getStatusLine();
    int code=s.getStatusCode();
    //System.out.println(code);
    if(code==204)
      return "";
    if(code!=200)
      throw new Exception("failed access to "+url.toString()+" code: "+code+" "+s.getReasonPhrase());

    Reader reader = null;
    try {
      reader = new InputStreamReader(response.getEntity().getContent());

      StringBuilder sb = new StringBuilder();
      {
        int read;
        char[] cbuf = new char[1024];
        while ((read = reader.read(cbuf)) != -1) {
          sb.append(cbuf, 0, read);
        }
      }

      return sb.toString();
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void delete(URL url, String user, String passwd) throws Exception {
    //System.out.println("DELETE "+url);
    HttpClient client = HttpClients.createDefault();
    HttpDelete request = new HttpDelete(url.toURI());

    HttpClientContext context = HttpClientContext.create();
    if(user!=null && passwd!=null) {
      CredentialsProvider credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(
              AuthScope.ANY,
              new UsernamePasswordCredentials(user, passwd));
      context.setCredentialsProvider(credsProvider);
    }

    HttpResponse response = client.execute(request, context);

    StatusLine s=response.getStatusLine();
    int code=s.getStatusCode();
    //System.out.println(code);
    if(code!=204 && code!=404)
      throw new Exception("failed access to "+url.toString()+" code: "+code+" "+s.getReasonPhrase());
  }
}
