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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author bellini
 */
public class QueryResult {
  List<String> variables=new ArrayList<String>();
  ArrayList<Map<String,ResultValue>> results=new ArrayList<Map<String, ResultValue>>();

  public QueryResult(String data) throws Exception {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();

    DefaultHandler handler = new DefaultHandler() {
      String lastChars;
      String lastBingingName;
      String lastDatatype;
      String lastLang;
      Map<String,ResultValue> row;

      @Override
      public void startElement(String uri, String localName, String qName,
              Attributes attributes) throws SAXException {
        if(qName.equals("variable"))
          variables.add(attributes.getValue("name"));
        else if(qName.equals("binding"))
          lastBingingName=attributes.getValue("name");
        else if(qName.equals("result"))
          row=new HashMap<String, ResultValue>();
        else if(qName.equals("literal")) {
          lastDatatype=attributes.getValue("datatype");
          lastLang=attributes.getValue("lang");
        }
        lastChars=null;
      }

      @Override
      public void characters(char ch[], int start, int length) throws SAXException {
          lastChars=new String(ch, start, length);
      }

      @Override
      public void endElement(String uri, String localName,
              String qName) throws SAXException {
        if(qName.equals("literal"))
          row.put(lastBingingName, new ResultValue(lastChars, lastDatatype, lastLang));
        else if(qName.equals("uri"))
          row.put(lastBingingName, new ResultValue(ResultValue.Type.URI, lastChars));
        else if(qName.equals("bnode"))
          row.put(lastBingingName, new ResultValue(ResultValue.Type.BNODE, lastChars));
        else if(qName.equals("result")) {
          results.add(row);
          row=null;
        }
        else if(qName.equals("sparql")) {
          row=null;
          lastBingingName=null;
          lastChars=null;
          lastDatatype=null;
          lastLang=null;
        }
      }
    };

    InputStream is= new ByteArrayInputStream(data.getBytes());
    saxParser.parse(is, handler);
  }

  public List<String> getVariables() {
    return variables;
  }

  public List<Map<String,ResultValue>> results() {
    return results;
  }
}
