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
<%@page import="it.cloudicaro.disit.kb.Configuration"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="java.net.URLEncoder"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
Configuration c=Configuration.getInstance();
String statusHTML="<div class=\"status\"><ul class=\"list\">\n";
try{
  Map<String,String> status=it.cloudicaro.disit.kb.IcaroKnowledgeBase.getStatus();
  for(Map.Entry<String,String> e: status.entrySet()) {
    statusHTML += "<li><b>"+e.getKey()+"</b>: "+e.getValue()+"</li>\n";
  }
  String logViewer=c.get("kb.log_url", 
          "http://log.disit.org/service/index.php?sparql=%sparql&uri=%uri")
          .replaceAll("%sparql", c.get("kb.rdf.sparql_endpoint", ""));
  statusHTML+="</ul>\n<h2>Data centers</h2>\n<ul class=\"list\">\n";
  List<String> dc=it.cloudicaro.disit.kb.IcaroKnowledgeBase.getDataCenters();
  for(String d:dc) {
    statusHTML += "<li><a href=\""+logViewer.replaceAll("%uri", URLEncoder.encode(d, "UTF-8"))+"\" target=\"_new\">"+d+"</a></li>\n";
  }
  statusHTML+="</ul>\n<h2>Business Configurations</h2>\n<ul class=\"list\">\n";
  List<String> bc=it.cloudicaro.disit.kb.IcaroKnowledgeBase.getBusinessConfigurations();
  for(String b:bc) {
    statusHTML += "<li><a href=\""+logViewer.replaceAll("%uri", URLEncoder.encode(b,"UTF-8"))+"\" target=\"_new\">"+b+"</a></li>\n";
  }
  statusHTML+="</ul>\n<h2>Application Types</h2>\n<ul class=\"list\">\n";
  List<String> apps=it.cloudicaro.disit.kb.IcaroKnowledgeBase.getApplicationTypes();
  for(String a:apps) {
    statusHTML += "<li><a href=\""+logViewer.replaceAll("%uri", URLEncoder.encode(a, "UTF-8"))+"\" target=\"_new\">"+a+"</a></li>\n";
  }
  statusHTML+="</ul>\n<h2>High Level Metric Types</h2>\n<ul class=\"list\">\n";
  List<String> hlmt=it.cloudicaro.disit.kb.IcaroKnowledgeBase.getHighLevelMetricTypes();
  for(String mt:hlmt) {
    String[] x=mt.split(";");
    statusHTML += "<li><a href=\""+logViewer.replaceAll("%uri", URLEncoder.encode(x[0], "UTF-8"))+"\" target=\"_new\">"+x[1]+"</a></li>\n";
  }
  statusHTML+="</ul>\n<h2>Low Level Metric Types</h2>\n<ul class=\"list\">\n";
  List<String> llmt=it.cloudicaro.disit.kb.IcaroKnowledgeBase.getLowLevelMetricTypes();
  for(String mt:llmt) {
    String[] x=mt.split(";");
    statusHTML += "<li><a href=\""+logViewer.replaceAll("%uri", URLEncoder.encode(x[0], "UTF-8"))+"\" target=\"_new\">"+x[1]+"</a></li>\n";
  }
}
catch(Exception e) {
  statusHTML+="<li>EXCEPTION: "+e.getMessage()+"</li>";
}
statusHTML+="</ul>";
statusHTML+="<h2>Configuration</h2>";
statusHTML+=c.asHtml();
statusHTML+="</div>";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Current status</title>
        <link rel="StyleSheet" type="text/css" href="style.css">
    </head>
    <body>
      <div class="center">
        <h1><img src="img/logo.png" id="logo">Icaro KB Current status  <a href="http://www.disit.org" target="_blank"><img src="img/logo-disit.png" class="grayscale" id="logo-disit"></a></h1>
        <p><a href="index.jsp">Home</a> | <a href="status.jsp"><b>Current status</b></a> | <a href="query-editor.jsp">SPARQL query</a></p>
        <%=statusHTML%>
      </div>
    </body>
</html>
