<%-- 
   Icaro Cloud Knowledge Base (ICKB).
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
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
String baseUrl="http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ICARO-CLOUD Knowledge Base</title>
        <script type="text/javascript" src="flint/jquery-1.5.2.min.js">//</script>
        <script type="text/javascript" src="flint/lib/codemirror.js">//</script>
        <script type="text/javascript" src="flint/sparql10querymode_ll1.js">//</script>
        <script type="text/javascript" src="flint/sparql11querymode_ll1.js">//</script>
        <script type="text/javascript" src="flint/sparql11updatemode_ll1.js">//</script>
        <script type="text/javascript" src="flint/init-local.js">//</script>
        <script type="text/javascript" src="flint/flint-editor.js">//</script>
        <link rel="stylesheet" href="flint/lib/codemirror.css"/>
        <link rel="stylesheet" href="flint/css/sparqlcolors.css"/>
        <link rel="stylesheet" href="flint/css/docs.css"/>
        <link rel="stylesheet" type="text/css" href="style.css">
    </head>
    <body>
      <div class="center" style="width:90%">
        <h1><img src="img/logo.png" id="logo">ICARO CLOUD Knowledge Base <a href="http://www.disit.org" target="_blank"><img src="img/logo-disit.png" class="grayscale" id="logo-disit"></a></h1>
        <p><a href="index.jsp">Home</a> | <a href="status.jsp">Current status</a> | <a href="query-editor.jsp"><b>SPARQL query</b></a></p>
        <div id="flint-test"></div>
      </div>
    </body>
</html>
