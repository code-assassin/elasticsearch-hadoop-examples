# elasticsearch-hadoop-examples
Elasticsearch Hadoop Examples

add ~/.m2/settings.xml
```
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 http://maven.apache.org/xsd/settings-1.2.0.xsd">
    <mirrors>
        <mirror>
            <mirrorOf>bestsolution</mirrorOf>
            <url>http://maven.bestsolution.at/efxclipse-releases/</url>

            <id>bestsolution-unblocked</id>
            <name></name>
            <blocked>false</blocked>
        </mirror>
    </mirrors>
</settings>

```

build the apps with maven
```
mvn --settings /root/.m2/settings.xml clean compile install
```

run hadoop
```
yarn jar /opt/stocks-1.0-SNAPSHOT-jar-with-dependencies.jar table_stocks.csv output
```
