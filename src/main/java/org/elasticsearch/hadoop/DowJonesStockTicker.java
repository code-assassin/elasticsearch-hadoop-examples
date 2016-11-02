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
package org.elasticsearch.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.Arrays;
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
public class DowJonesStockTicker
{
    private static final String ES_NODES = "127.0.0.1:9200";
    private static final String ES_INDEX = "djia/ticker";

    private static class StockTickerMapper extends Mapper<Object, Text, Text, Text>
    {
        private static final Pattern pattern = Pattern.compile("table_([a-zA-Z]+)\\.csv");

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException
        {
            FileSplit split = (FileSplit) context.getInputSplit();
            String name = split.getPath().getName();

            Matcher matcher = pattern.matcher(name);
            if (!matcher.matches()) {
                return;
            }

            String symbol = matcher.group(1);
            String[] values = value.toString().split("\\s");

            context.write(new Text(symbol), new Text(Arrays.toString(values)));
        }
    }

    public static void main(String[] args) throws Exception
    {
        Configuration conf = new Configuration();
        conf.setBoolean("mapred.map.tasks.speculative.execution", false);
        conf.setBoolean("mapred.reduce.tasks.speculative.execution", false);
        conf.set("es.nodes", ES_NODES);
        conf.set("es.resource", ES_INDEX);

        Job job = Job.getInstance(conf, "stock ticker");
        job.setJarByClass(DowJonesStockTicker.class);
        job.setMapperClass(StockTickerMapper.class);
        job.setOutputKeyClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
