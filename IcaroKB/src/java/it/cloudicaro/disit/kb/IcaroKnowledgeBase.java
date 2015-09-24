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

import it.cloudicaro.disit.kb.rdf.QueryResult;
import it.cloudicaro.disit.kb.rdf.RDFStore;
import it.cloudicaro.disit.kb.rdf.RDFStoreInterface;
import it.cloudicaro.disit.kb.rdf.ResultValue;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author bellini
 */
public class IcaroKnowledgeBase {
  public static final String NS_ICARO_CORE="http://www.cloudicaro.it/cloud_ontology/core#";
  public static final String NS_ICARO_APPS="http://www.cloudicaro.it/cloud_ontology/applications#";
  public static final String NS_RDF="http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  public static final String SPARQL_PREFIXES="PREFIX icr:<"+NS_ICARO_CORE+">"+
          "PREFIX app:<"+NS_ICARO_APPS+">"+
          "PREFIX rdf:<"+NS_RDF+">"+
          "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"+
          "PREFIX foaf:<http://xmlns.com/foaf/0.1/>"+
          "PREFIX owl:<http://www.w3.org/2002/07/owl#>"+
          "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#> ";
  private static HashSet<String> metricNames = null;
  private static HashMap<String, MetricTypeResource.HLMType> metricCache = new HashMap<String, MetricTypeResource.HLMType>();
  private static String recoveringStatus = "No recover in progress";
  private static int recoveringTot = 0;
  private static Thread recoverThread = null;

  public static Map<String,String> getStatus() throws Exception {
    Map<String,String> s=new LinkedHashMap<String, String>();

    RDFStoreInterface store=RDFStore.getInstance();
    Configuration conf = Configuration.getInstance();
    QueryResult qr;
    qr=store.query(SPARQL_PREFIXES+"SELECT (COUNT(*) AS ?c) WHERE { ?x a icr:BusinessConfiguration }");
    s.put("BusinessConfiguration count", qr.results().get(0).get("c").getValue());
    qr=store.query(SPARQL_PREFIXES+"SELECT (COUNT(*) AS ?c) WHERE { ?x a icr:HostMachine }");
    s.put("HostMachine count", qr.results().get(0).get("c").getValue());
    qr=store.query(SPARQL_PREFIXES+"SELECT (COUNT(*) AS ?c) WHERE { ?x a icr:VirtualMachine }");
    s.put("VirtualMachine count", qr.results().get(0).get("c").getValue());
    qr=store.query(SPARQL_PREFIXES+"SELECT (COUNT(*) AS ?c) WHERE { ?x a icr:IcaroApplication }");
    s.put("IcaroApplication count", qr.results().get(0).get("c").getValue());
    qr=store.query(SPARQL_PREFIXES+"SELECT (COUNT(*) AS ?c) WHERE { ?x a icr:IcaroService }");
    s.put("IcaroService count", qr.results().get(0).get("c").getValue());
    if(conf.get("kb.status.serviceMetricCount", "true").equals("true")) {
      qr=store.query(SPARQL_PREFIXES+"SELECT (COUNT(*) AS ?c) WHERE { ?x a icr:ServiceMetric }");
      s.put("ServiceMetric count", qr.results().get(0).get("c").getValue());
    }
    qr=store.query(SPARQL_PREFIXES+"SELECT (COUNT(*) AS ?c) WHERE { ?x a icr:LowLevelMetricType }");
    s.put("LowLevelMetricType count", qr.results().get(0).get("c").getValue());
    qr=store.query(SPARQL_PREFIXES+"SELECT (COUNT(*) AS ?c) WHERE { ?x a icr:HighLevelMetricType }");
    s.put("HighLevelMetricType count", qr.results().get(0).get("c").getValue());
    qr=store.query(SPARQL_PREFIXES+"SELECT ?t WHERE { ?s a icr:ServiceMetric; icr:atTime ?t. } ORDER BY DESC(?t) LIMIT 1");
    if(qr.results().size()>0 && qr.results().get(0).get("t")!=null) {
      String lastUpdate=qr.results().get(0).get("t").getValue();
      Calendar d=javax.xml.bind.DatatypeConverter.parseDateTime(lastUpdate);
      String mt=(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")).format(d.getTime());
      s.put("ServiceMetric last update", mt+" ("+dateDiff(d.getTime(),new Date())+"ago)");
    }
    else
      s.put("ServiceMetric last update", "NA");
    /*
    qr=store.query(SPARQL_PREFIXES+"select (max(?t) as ?mt) where { ?x a icr:ServiceMetric; icr:atTime ?t; icr:dependsOn ?d. } group by ?d order by ?mt limit 1");
    if(qr.results().get(0).get("mt")!=null) {
      String lastUpdate=qr.results().get(0).get("mt").getValue();
      Calendar d=javax.xml.bind.DatatypeConverter.parseDateTime(lastUpdate);
      s.put("ServiceMetric oldest update", dateDiff(d.getTime(), new Date())+"ago");
    }
    else
      s.put("ServiceMetric oldest update", "NA");
    */
    //qr=store.query(SPARQL_PREFIXES+"SELECT (COUNT(*) AS ?c) WHERE { ?s ?p ?o }");
    //s.put("full triple count", qr.results().get(0).get("c").getValue());
    s.put("RDF Store size", ""+store.getSize());
    return s;
  }

  public static String dateDiff(Date d1, Date d2) {
    long diff=(d2.getTime()-d1.getTime())/1000;
    String ago="";
    int days=(int)(diff/(60*60*24));
    diff=diff%(60*60*24);
    if(days>0)
      ago += days+" days ";
    int hours=(int)(diff/(60*60));
    diff=diff%(60*60);
    if(hours>0)
      ago += hours+" hours ";
    int mins=(int)(diff/(60));
    diff=diff%(60);
    if(mins>0)
      ago += mins+" mins ";
    if(diff>0)
      ago += diff+"s ";
    return ago;
  }
  
  public static List<String> getDataCenters() throws Exception {
    List<String> dc=new ArrayList();
    
    RDFStoreInterface store=RDFStore.getInstance();
    QueryResult qr;
    qr=store.query(SPARQL_PREFIXES+"SELECT ?dc WHERE { ?dc a icr:DataCenter } ORDER BY ?dc");
    for(Map<String, ResultValue> r:qr.results()) {
      dc.add(r.get("dc").getValue());
    }
    return dc;
  }

  public static List<String> getBusinessConfigurations() throws Exception {
    List<String> bc=new ArrayList();
    
    RDFStoreInterface store=RDFStore.getInstance();
    QueryResult qr;
    qr=store.query(SPARQL_PREFIXES+"SELECT ?bc WHERE { ?bc a icr:BusinessConfiguration } ORDER BY ?bc");
    for(Map<String, ResultValue> r:qr.results()) {
      bc.add(r.get("bc").getValue());
    }
    return bc;
  }

  public static List<String> getApplicationTypes() throws Exception {
    List<String> apps=new ArrayList();
    
    RDFStoreInterface store=RDFStore.getInstance();
    QueryResult qr;
    qr=store.query(SPARQL_PREFIXES+"SELECT ?app WHERE { ?app a owl:Class FILTER(STRSTARTS(STR(?app),\""+NS_ICARO_APPS+"\")) FILTER NOT EXISTS {?app rdfs:subClassOf icr:IcaroApplicationModule}} ORDER BY ?app");
    for(Map<String, ResultValue> r:qr.results()) {
      apps.add(r.get("app").getValue());
    }
    return apps;
  }

  public static List<String> getLowLevelMetricTypes() throws Exception {
    List<String> mt=new ArrayList();
    
    RDFStoreInterface store=RDFStore.getInstance();
    QueryResult qr;
    qr=store.query(SPARQL_PREFIXES+"SELECT ?mt ?mn WHERE { ?mt a icr:LowLevelMetricType; icr:hasMetricName ?mn } ORDER BY ?mn");
    for(Map<String, ResultValue> r:qr.results()) {
      mt.add(r.get("mt").getValue()+";"+r.get("mn").getValue());
    }
    return mt;
  }
  
  public static List<String> getHighLevelMetricTypes() throws Exception {
    List<String> mt=new ArrayList();
    
    RDFStoreInterface store=RDFStore.getInstance();
    QueryResult qr;
    qr=store.query(SPARQL_PREFIXES+"SELECT ?mt ?mn WHERE { ?mt a icr:HighLevelMetricType; icr:hasMetricName ?mn } ORDER BY ?mn");
    for(Map<String, ResultValue> r:qr.results()) {
      mt.add(r.get("mt").getValue()+";"+r.get("mn").getValue());
    }
    return mt;
  }

  static boolean checkMetricName(String mname) throws Exception {
    if( metricNames== null) {
      metricNames = new HashSet<String>();
      RDFStoreInterface rdfStore=RDFStore.getInstance();
      QueryResult r = rdfStore.query(IcaroKnowledgeBase.SPARQL_PREFIXES+" SELECT ?name WHERE { ?m a icr:ServiceMetricType; icr:hasMetricName ?name}");
      for (Map<String,ResultValue> x : r.results()) {
        String name = x.get("name").getValue();
        System.out.println("metric name: "+name);
        metricNames.add(name);
      }
    }
    return metricNames.contains(mname);
  }
  
  static void resetMetricNames() {
    metricNames = null;
    metricCache = new HashMap<String, MetricTypeResource.HLMType>();
  }

  static public MetricTypeResource.HLMType getHighLevelMetricType(String hlmtUri) throws Exception {
    MetricTypeResource.HLMType hlmt=metricCache.get(hlmtUri);
    if(hlmt==null) {
      hlmt=new MetricTypeResource.HLMType();
      hlmt.load(hlmtUri);
      metricCache.put(hlmtUri, hlmt);
    }
    return hlmt;
  }
  
  static void storePut(Date start, String content, String id, String type) throws IOException {
    storeSubmission(start, content, "PUT", type, id);
  }
  
  static void storeDelete(Date start, String id, String type) throws IOException {
    storeSubmission(start, "", "DELETE", type, id);
  }
  
  static void storeSubmission(Date start, String content, String op, String type, String id) throws IOException {
    String submissionPath=Configuration.getInstance().get("kb.store_submission_path", System.getProperty("user.home")+"/icaro/submissions");
    Writer writer = null;

    try {
      writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
              submissionPath+"/"+start.getTime()+"-"+op+"-"+type+
                      (!id.equals("")? "-"+URLEncoder.encode(id,"UTF-8") : "")+
                      ".xml"), "utf-8"));
      writer.write(content);
    } catch (IOException ex) {
      ex.printStackTrace();
      throw ex;
    } finally {
      if(writer!=null) 
        writer.close();
    }
  }
  
  static void log(Date start, String op, String type, String id, String result, HttpServletRequest request) throws IOException {
    Writer writer = null;
    Date end = new Date();
    Configuration conf=Configuration.getInstance();
    String log=conf.get("kb.logs.access_log", System.getProperty("user.home")+"/icaro/access.log");
    try {
      writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(log, true),"utf-8"));
      writer.write(start+"; "+request.getRemoteAddr()+"; "+request.getUserPrincipal().getName()+"; "+op+" "+type+" "+id+"; "+result+"; "+(end.getTime()-start.getTime())+"ms \r\n");
    } catch (IOException ex) {
      ex.printStackTrace();
      throw ex;
    } finally {
      if(writer!=null) 
        writer.close();
    }
    
    if(!op.startsWith("GET") && result.equals("OK") && 
            (conf.get("kb.sendmail.onSUCCESS", "true").equals("true") || 
             conf.get("kb.sendmail.onSUCCESS", "true").contains(type))) 
      sendMail("ICARO KB@"+request.getServerName()+" "+op+" "+type+"("+id+"): "+result, 
            "URL: "+request.getRequestURL()+"\n"
            + "from: "+request.getRemoteAddr()+"\n"
            + "user: "+request.getUserPrincipal().getName()+"\n"
            + "op: "+op+"\n"
            + "result: "+result+"\n"
            + "date: "+start+"\n"
            + "time: "+(end.getTime()-start.getTime())/1000.0+"s");
  }
  
  static void error(Date start, String op, String type, String id, String shortResult, String result, String input, HttpServletRequest request) throws IOException {   
    if(request!=null) {
      log(start, op, type, id, shortResult, request);
    
      Configuration conf = Configuration.getInstance();
      Writer writer = null;
      Date end = new Date();
      String log = conf.get("kb.logs.error_log", System.getProperty("user.home")+"/icaro/error.log");
      try {
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(log, true),"utf-8"));
        writer.write("------\r\n"+start+"; "+request.getRemoteAddr()+"; "+request.getUserPrincipal().getName()+"; "+op+" "+type+" "+request.getRequestURL()+"; "+shortResult+"; "+(end.getTime()-start.getTime())+"ms \r\n"+result+"\r\n\r\n------------------------------------\r\n"+input+"\r\n");
      } catch (IOException ex) {
        ex.printStackTrace();
        throw ex;
      } finally {
        if(writer!=null) 
          writer.close();
      }
      if(conf.get("kb.sendmail.onERROR", "true").equals("true")) 
        sendMail("ERROR icaro kb@"+request.getServerName()+" "+op+" "+type+"("+id+"): "+shortResult, 
              "URL: "+request.getRequestURL()+"\n"
              + "from: "+request.getRemoteAddr()+"\n"
              + "user: "+request.getUserPrincipal().getName()+"\n"
              + "op: "+op+"\n"
              + "result: "+shortResult+"\n"
              + "date: "+start+"\n"
              + "details:\n\n"
              + result
              + "\n\n"
              + "---------------------------------------------------------------\n"
              + "input\n"
              + "---------------------------------------------------------------\n"
              + input
        );
    }
    else {
      Configuration conf = Configuration.getInstance();
      Writer writer = null;
      Date end = new Date();
      String log = conf.get("kb.logs.recover_log", System.getProperty("user.home")+"/icaro/recover.log");
      try {
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(log, true),"utf-8"));
        writer.write("------\r\n"+start+"; "+op+" "+type+" "+id+"; "+shortResult+"; "+(end.getTime()-start.getTime())+"ms \r\n"+result+"\r\n");
      } catch (IOException ex) {
        ex.printStackTrace();
        throw ex;
      } finally {
        if(writer!=null) 
          writer.close();
      }
    }
  }

  static public void sendMail(String subject, String text){
    Configuration conf = Configuration.getInstance();
    String from = conf.get("kb.sendmail.from", "kb@cloudicaro.it");
    String to = conf.get("kb.sendmail.to", "pierfrancesco.bellini@unifi.it");
    
    Properties props = new Properties();
    props.put("mail.smtp.host", conf.get("kb.sendmail.smtp.host", "musicnetwork.dsi.unifi.it"));
    props.put("mail.smtp.port", conf.get("kb.sendmail.smtp.port", "25"));
    Session mailSession = Session.getDefaultInstance(props);
    Message simpleMessage = new MimeMessage(mailSession);
    InternetAddress fromAddress = null;
    InternetAddress toAddress = null;
    try {
        fromAddress = new InternetAddress(from);
    } catch (AddressException e) {
        e.printStackTrace();
        return;
    }

    try {
        simpleMessage.setFrom(fromAddress);
        String[] toAddrs=to.split(",");
        for (String addr : toAddrs) {
          try {
            toAddress = new InternetAddress(addr);
            simpleMessage.addRecipient(RecipientType.TO, toAddress);
          }catch (AddressException e) {
            e.printStackTrace();
          }
        }
        simpleMessage.setSubject(subject);
        simpleMessage.setText(text);
        System.out.println("sending message to "+to);
        Transport.send(simpleMessage);
    } catch (MessagingException e) {
        e.printStackTrace();
    }
  }
  
  static public synchronized String startRecover(final long from, final long to) {
    System.out.println("recover "+recoveringTot);
    if(recoveringTot==0) {
      System.out.println("Starting recover thread");
      recoveringStatus = "START RECOVER KB <br>";
      recoveringTot=1;
      recoverThread = new Thread() {
        @Override
        public void run() {
          System.out.println("RECOVER START");
          Date start = new Date();
          String submissionPath=Configuration.getInstance().get("kb.store_submission_path", System.getProperty("user.home")+"/icaro/submissions");
          File folder = new File(submissionPath);
          File[] files = folder.listFiles(); 

          try {            
            recoveringTot = files.length;
            Arrays.sort(files);
            for (int i = 0; i < recoveringTot; i++) 
            {
              if (files[i].isFile()) 
              {
                System.out.println("RECOVER FILE "+i);
                if(!recoverFile(i, files[i], from, to))
                  break;
                recoveringStatus +="<br>";
              }
            }
            Date end = new Date();
            long time = (end.getTime()-start.getTime())/1000;
            recoveringStatus +="RECOVER END in "+(time/3600)+"h "+((time%3600)/60)+"m "+((time%3600)%60)+"s <br>";
            recoveringTot = 0;
            System.out.println("RECOVER END");
          }
          catch(Exception e) {
            e.printStackTrace();
          }
        }
      };
      recoverThread.start();
      
      return recoveringStatus;
    }
    else {
      return recoveringStatus;
    }
  }
  
  static public synchronized String listRecover() throws Exception {
    String submissionPath=Configuration.getInstance().get("kb.store_submission_path", System.getProperty("user.home")+"/icaro/submissions");
    File folder = new File(submissionPath);
    File[] files = folder.listFiles(); 
    String out = "";
    Pattern p = Pattern.compile("([0-9]*)-([A-Z]*)-([A-Z]*)-?(.*)\\.xml");
    
    Arrays.sort(files);
    for (int i = 0; i < files.length; i++) 
    {
      if (files[i].isFile()) 
      {
        String file = files[i].getName();
        Matcher m = p.matcher(file);

        if(m.find()) {
          long time=Long.parseLong(m.group(1));
          Date date=new Date(time);
          String op=m.group(2);
          String type=m.group(3);
          String id=java.net.URLDecoder.decode(m.group(4), "UTF-8");
          if(op.equals("POST") && id.equals("SM") && type.equals("")) {
            type="SM";
            id="";
          }
          out += i+"/"+(files.length)+" "+date+" ("+time+") "+op+" "+type+" "+id+"<br>\n";
        }
        else
          out += file+" NOT MATCH<br>\n";
      }
    }
    return out;
  }

  static public String getRecoverStatus() {
    return recoveringStatus;
  }

  static public synchronized boolean isRecovering() {
    return recoveringTot>0;
  }

  static public boolean recoverFile(int i, File f, long from, long to) throws Exception {
    String file = f.getName();
    Pattern p = Pattern.compile("([0-9]*)-([A-Z]*)-([A-Z]*)-?(.*)\\.xml");
    Matcher m = p.matcher(file);

    if(m.find()) {
      long time=Long.parseLong(m.group(1));
      Date date=new Date(time);
      String op=m.group(2);
      String type=m.group(3);
      String id=java.net.URLDecoder.decode(m.group(4), "UTF-8");
      if(op.equals("POST") && id.equals("SM") && type.equals("")) {
        type="SM";
        id="";
      }
      String content = null;
      recoveringStatus += i+"/"+recoveringTot+" "+date+" "+type+" "+op+" "+id; //+" "+file;
      if(from>0 && time<from) {
        recoveringStatus += " SKIPPED";
        return true;
      }
      if(to>0 && time>to)
        return false;
      if(op.equals("PUT") || op.equals("POST")) {
        byte[] encoded = Files.readAllBytes(f.toPath());
        content = new String(encoded, "UTF-8");
      }
      Date start = new Date();
      try {
        if(type.equals("AT")) {
          if(op.equals("PUT"))
            ApplicationTypeResource.putApplicationType(id, content, null);
          else if(op.equals("DELETE"))
            ApplicationTypeResource.deleteApplicationType(id, null);
        }
        else if(type.equals("DC")) {
          if(op.equals("PUT"))
            DataCenterResource.putDataCenter(id, content, null);
          else if(op.equals("DELETE"))
            DataCenterResource.deleteDataCenter(id, null);
        }
        else if(type.equals("BC")) {
          if(op.equals("PUT"))
            BusinessConfigurationResource.putBusinessConfiguration(id, content, null);
          else if(op.equals("DELETE"))
            BusinessConfigurationResource.deleteBusinessConfiguration(id, null);
        }
        else if(type.equals("MT")) {
          if(op.equals("PUT"))
            MetricTypeResource.putMetricType(id, content, null);
          else if(op.equals("DELETE"))
            MetricTypeResource.deleteMetricType(id, null);
        }
        else if(type.equals("SM")) {
          if(op.equals("POST"))
            ServiceMetricResource.postServiceMetric(content, null);
        }
        else {
          recoveringStatus += " type \""+type+"\" not recognized"; 
        }
        Date end = new Date();
        recoveringStatus += " OK "+(end.getTime()-start.getTime())/1000.0+"s";
      }
      catch(Exception e) {
        recoveringStatus +=" <b>Exception <br>"+e.toString().replaceAll("\\n", "<br>")+"</b>";
        if(e.getMessage()==null)
          e.printStackTrace();
      }
    }
    return true;
  } 
}