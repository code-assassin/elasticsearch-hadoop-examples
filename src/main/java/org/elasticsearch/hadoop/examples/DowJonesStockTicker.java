/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.elasticsearch.hadoop.examples;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import org.elasticsearch.hadoop.mr.EsOutputFormat;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Map-reduce program for importing historical ticker data for stocks in the Dow Jones Industrial Average.
 *
 * Files are named as: table_SYMBOL.csv.
 *
 * Format:
 * date     time open    high    low     close   volume splits earnings dividends
 * 20031103	930	 25.6338 25.6479 25.5488 25.6196 305780	1	   0	    0
 */
public class DowJonesStockTicker extends Configured implements Tool
{
    private static final String ES_NODES = "192.168.1.48:9200";
    private static final String ES_INDEX = "djia/ticker";

    public static void main(String[] args) throws Exception
    {
        int res = ToolRunner.run(new Configuration(), new DowJonesStockTicker(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception
    {
        Configuration conf = this.getConf();

        conf.setBoolean("mapred.map.tasks.speculative.execution", false);
        conf.setBoolean("mapred.reduce.tasks.speculative.execution", false);
        conf.set("es.nodes", ES_NODES);
        conf.set("es.resource", ES_INDEX);

        Job job = Job.getInstance(conf, "stock ticker");

        job.setJarByClass(DowJonesStockTicker.class);
        job.setMapperClass(StockTickerMapper.class);
        job.setOutputFormatClass(EsOutputFormat.class);
        job.setMapOutputKeyClass(NullWritable.class);
        job.setMapOutputValueClass(MapWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
        return 0;
    }

    private static class StockTickerMapper extends Mapper<Object, Text, NullWritable, MapWritable>
    {
//        private static final Pattern pattern = Pattern.compile("table_([a-zA-Z]+)\\.csv");

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException
        {
//            FileSplit split = (FileSplit) context.getInputSplit();
//            String name = split.getPath().getName();

//            Matcher matcher = pattern.matcher(name);
//            if (!matcher.matches()) {
//                return;
//            }

//            String symbol = matcher.group(1);
            String[] values = value.toString().split(",");
//            if (values.length != 10) {
//                return; // Malformed document
//            }

//            LocalDate date = LocalDate.parse(values[0], DateTimeFormatter.BASIC_ISO_DATE);
//            LocalTime time = LocalTime.parse(values[1], DateTimeFormatter.ofPattern("kmm"));
//            ZonedDateTime zoned = ZonedDateTime.of(date, time, ZoneId.of("America/New_York"));

            MapWritable doc = new MapWritable();

//            doc.put(new Text("symbol"), new Text(symbol));
            doc.put(new Text("timestamp"), new Text(values[0]));
            doc.put(new Text("open"), new Text(values[1]));
            doc.put(new Text("high"), new Text(values[2]));
            doc.put(new Text("low"), new Text(values[3]));
            doc.put(new Text("close"), new Text(values[4]));
            doc.put(new Text("volume"), new Text(values[5]));
//            doc.put(new Text("splits"), new Text(values[6]));
//            doc.put(new Text("earnings"), new Text(values[8]));
//            doc.put(new Text("dividends"), new Text(values[9]));

            System.out.println(values[0]);

            context.write(NullWritable.get(), doc);
        }
    }
}
