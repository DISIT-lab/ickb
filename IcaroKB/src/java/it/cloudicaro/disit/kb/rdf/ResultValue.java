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
public class ResultValue {
  public enum Type { LITERAL, BNODE, URI} ;
  private String value;
  private Type type = null;
  private String datatype = null; //only for literals
  private String lang = null; //only for literals

  ResultValue(Type t, String value) {
    this.type=t;
    this.value=value;
  }

  ResultValue(String value, String datatype, String lang) {
    this.type=Type.LITERAL;
    this.value=value;
    this.datatype=datatype;
    this.lang=lang;
  }

  public String getValue() {
    return value;
  }

  public Type getType() {
    return type;
  }

  public String getDatatype() {
    return datatype;
  }

  public String getLang() {
    return lang;
  }

  public String toString() {
    return type+" "+value+(datatype!=null?" "+datatype:"")+(lang!=null?" "+lang:"");
  }
}
