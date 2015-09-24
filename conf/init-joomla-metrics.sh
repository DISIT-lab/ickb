echo Joomla Metrics
curl -X PUT "http://kb.cloudicaro.it:8080/IcaroKB/api/metricTypes/joomla-metrics" --data @joomla-metrics.xml --user kb-write:icaro -H "Content-type: application/xml"
