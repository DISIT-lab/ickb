echo XLMS
curl -X PUT "http://localhost:8080/IcaroKB/api/applicationType/XLMS" --data @apps/xlms.xml --user kb-write:icaro -H "Content-type: application/xml"
echo MyXLMS
curl -X PUT "http://localhost:8080/IcaroKB/api/applicationType/MyXLMS" --data @apps/myxlms.xml --user kb-write:icaro -H "Content-type: application/xml"
echo Joomla
curl -X PUT "http://localhost:8080/IcaroKB/api/applicationType/Joomla" --data @apps/joomla.xml --user kb-write:icaro -H "Content-type: application/xml"
echo SharePoint
curl -X PUT "http://localhost:8080/IcaroKB/api/applicationType/SharePoint" --data @apps/sharepoint.xml --user kb-write:icaro -H "Content-type: application/xml"
echo LAMP
curl -X PUT "http://localhost:8080/IcaroKB/api/applicationType/LAMP" --data @apps/lamp.xml --user kb-write:icaro -H "Content-type: application/xml"
echo BizMonitor
curl -X PUT "http://localhost:8080/IcaroKB/api/applicationType/BizMonitor" --data @apps/bizmonitor.xml --user kb-write:icaro -H "Content-type: application/xml"
