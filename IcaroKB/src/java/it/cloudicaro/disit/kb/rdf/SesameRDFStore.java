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

import it.cloudicaro.disit.kb.Configuration;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

/**
 *
 * @author bellini
 */
public class SesameRDFStore implements RDFStoreInterface {
  private String _url=null;
  private String _repos=null;
  private String _user=null;
  private String _passwd=null;
  private String _lastQuery="";
  
  SesameRDFStore() {
    Configuration cnf = Configuration.getInstance();
    this._url= cnf.get("kb.rdf.url", null);
    this._repos=cnf.get("kb.rdf.repos", null);
    this._user= cnf.get("kb.rdf.user", null);
    this._passwd=cnf.get("kb.rdf.passwd", null);
  }

  public void initialize(String url, String repository, String user, String passw) {
    this._url=url;
    this._user=user;
    this._passwd=passw;
    this._repos=repository;
  }

  public int getSize() throws Exception {
    String size = HttpUtil.get(new URL(_url + "/repositories/" + _repos + "/size"), null, this._user, this._passwd);
    return Integer.parseInt(size);
  }

  public String getStatements(String format, String graph) throws Exception {
    if(format==null)
      format="application/rdf+xml";
    Date start = new Date();
    String result = HttpUtil.get(new URL(_url+ "/repositories/" + _repos+ "/rdf-graphs/service"+(graph==null?"":"?graph="+graph)),format, this._user, this._passwd);
    Date end = new Date();
    System.out.println(start+" RDF-PERFORMANCE: get "+Thread.currentThread().getStackTrace()[2].toString()+" "+(end.getTime()-start.getTime())+"ms");
    return result; 
  }

  public String addStatements(String data, String format, String graph) throws Exception {
    if(format==null)
      format="application/rdf+xml";
    Date start = new Date();
    String result = HttpUtil.post(new URL(_url+ "/repositories/" + _repos+ "/rdf-graphs/service"+(graph==null?"":"?graph="+graph)),data,format, this._user, this._passwd);
    Date end = new Date();
    System.out.println(start+" RDF-PERFORMANCE: add "+Thread.currentThread().getStackTrace()[2].toString()+" "+(end.getTime()-start.getTime())+"ms");
    return result;
  }
  
  public void flush() throws Exception {
    addStatements("<http://www.example.com> <http://www.ontotext.com/owlim/system#flush> \"\" .", "text/plain", "urn:flush");
  }

  public String query(String query, String resultFormat) throws Exception {
    Date start = new Date();
    String result = HttpUtil.get(new URL(_url + "/repositories/" + _repos+"?query="+URLEncoder.encode(query, "UTF8")), resultFormat, this._user, this._passwd);
    Date end = new Date();
    _lastQuery = query;
    System.out.println(start+" RDF-PERFORMANCE: query "+Thread.currentThread().getStackTrace()[2].toString()+" "+(end.getTime()-start.getTime())+"ms");
    return result;
  }

  public QueryResult query(String query) throws Exception {
    Date start = new Date();
    String result= HttpUtil.get(new URL(_url + "/repositories/" + _repos+"?query="+URLEncoder.encode(query, "UTF8")), "application/sparql-results+xml", this._user, this._passwd);
    Date end = new Date();
    _lastQuery = query;
    System.out.println(start+" RDF-PERFORMANCE: query "+Thread.currentThread().getStackTrace()[2].toString()+" "+(end.getTime()-start.getTime())+"ms");
    return new QueryResult(result);
  }

  public void removeGraph(String graph) throws Exception {
    Date start = new Date();
    HttpUtil.delete(new URL(_url+ "/repositories/" + _repos+ "/rdf-graphs/service"+(graph==null?"":"?graph="+graph)), this._user, this._passwd);
    Date end = new Date();
    System.out.println(start+" RDF-PERFORMANCE: removeGraph "+Thread.currentThread().getStackTrace()[2].toString()+" "+(end.getTime()-start.getTime())+"ms");
  }

  public String update(String update) throws Exception {
    Date start = new Date();
    String result = HttpUtil.post(new URL(_url + "/repositories/" + _repos + "/statements"), "update="+URLEncoder.encode(update, "UTF8"), "application/x-www-form-urlencoded", this._user, this._passwd);
    Date end = new Date();
    System.out.println(start+" RDF-PERFORMANCE: RDF update "+Thread.currentThread().getStackTrace()[2].toString()+" "+(end.getTime()-start.getTime())+"ms");
    return result;
  }

  public String getLastQuery() {
    return _lastQuery;
  }
}
