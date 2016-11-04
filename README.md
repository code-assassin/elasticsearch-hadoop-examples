# elasticsearch-hadoop-examples
Elasticsearch Hadoop Examples

X=`ls -d1m ~/src/andrew/elastic/djia/target/dependency/*.jar` && export LIBJARS=`echo $X | tr -d ' '` && export HADOOP_CLASSPATH=`echo $LIBJARS | sed s/,/:/g` && unset X
