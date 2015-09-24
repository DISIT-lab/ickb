echo Low Level Metrics
curl -X PUT "http://kb.cloudicaro.it:8080/IcaroKB/api/metricTypes/core-llm" --data @llmetrics.xml --user kb-write:icaro -H "Content-type: application/xml"
echo High Level Metrics
curl -X PUT "http://kb.cloudicaro.it:8080/IcaroKB/api/metricTypes/core-hlm" --data @hlmetrics.xml --user kb-write:icaro -H "Content-type: application/xml"
