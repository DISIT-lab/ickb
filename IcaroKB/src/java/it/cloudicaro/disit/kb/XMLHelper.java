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

public class XMLHelper {

  /**
   * Returns the string where all non-ascii and <, &, > are encoded as numeric entities. I.e. "&lt;A &amp; B &gt;"
   * .... (insert result here). The result is safe to include anywhere in a text field in an XML-string. If there was
   * no characters to protect, the original string is returned.
   *
   * @param originalUnprotectedString
   *            original string which may contain characters either reserved in XML or with different representation
   *            in different encodings (like 8859-1 and UFT-8)
   * @return
   */
  public static String protectSpecialCharacters(String originalUnprotectedString) {
    if (originalUnprotectedString == null) {
      return null;
    }
    boolean anyCharactersProtected = false;

    StringBuilder stringBuffer = new StringBuilder();
    for (int i = 0; i < originalUnprotectedString.length(); i++) {
      char ch = originalUnprotectedString.charAt(i);

      boolean controlCharacter = (ch < 32 && ch != 10);
      boolean unicodeButNotAscii = ch > 126;
      boolean characterWithSpecialMeaningInXML = ch == '<' || ch == '&' || ch == '>';

      if (characterWithSpecialMeaningInXML || unicodeButNotAscii || controlCharacter) {
        stringBuffer.append("&#").append((int) ch).append(";");
        anyCharactersProtected = true;
      } else {
        stringBuffer.append(ch);
      }
    }
    if (anyCharactersProtected == false) {
      return originalUnprotectedString;
    }

    return stringBuffer.toString();
  }
}
