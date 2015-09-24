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

/**
 *
 * @author bellini
 */
public interface RDFStoreInterface {

  public void initialize(String url, String repository, String user, String passw);

  public int getSize() throws Exception;

  public String getStatements(String format, String graph) throws Exception;

  public String addStatements(String data, String format, String graph) throws Exception;

  public void flush() throws Exception;

  public void removeGraph(String graph) throws Exception;

  public String query(String query, String resultFormat) throws Exception;

  public QueryResult query(String query) throws Exception;

  public String update(String update) throws Exception;
  
  public String getLastQuery();
}
