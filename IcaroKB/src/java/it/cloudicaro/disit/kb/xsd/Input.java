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

package it.cloudicaro.disit.kb.xsd;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import org.w3c.dom.ls.LSInput;

/**
 *
 * @author bellini
 */
public class Input implements LSInput {

  private String publicId;
  private String systemId;

  public String getPublicId() {
    return publicId;
  }

  public void setPublicId(String publicId) {
    this.publicId = publicId;
  }

  public String getBaseURI() {
    return null;
  }

  public InputStream getByteStream() {
    return null;
  }

  public boolean getCertifiedText() {
    return false;
  }

  public Reader getCharacterStream() {
    return null;
  }

  public String getEncoding() {
    return null;
  }

  public String getStringData() {
    synchronized (inputStream) {
      try {
        byte[] input = new byte[inputStream.available()];
        inputStream.read(input);
        String contents = new String(input);
        return contents;
      } catch (IOException e) {
        e.printStackTrace();
        System.out.println("Exception " + e);
        return null;
      }
    }
  }

  public void setBaseURI(String baseURI) {
  }

  public void setByteStream(InputStream byteStream) {
  }

  public void setCertifiedText(boolean certifiedText) {
  }

  public void setCharacterStream(Reader characterStream) {
  }

  public void setEncoding(String encoding) {
  }

  public void setStringData(String stringData) {
  }

  public String getSystemId() {
    return systemId;
  }

  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }

  public BufferedInputStream getInputStream() {
    return inputStream;
  }

  public void setInputStream(BufferedInputStream inputStream) {
    this.inputStream = inputStream;
  }
  private BufferedInputStream inputStream;

  public Input(String publicId, String sysId, InputStream input) {
    this.publicId = publicId;
    this.systemId = sysId;
    this.inputStream = new BufferedInputStream(input);
  }
}
