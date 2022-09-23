redis-server
zookeeper-server-start /opt/homebrew/opt/kafka/libexec/config/zookeeper.properties
sleep 6s
kafka-server-start /opt/homebrew/opt/kafka/libexec/config/server.properties
sleep 6s
sh /usr/local/elasticsearch-7.17.4/bin/elasticsearch
