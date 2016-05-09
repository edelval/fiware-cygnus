package com.telefonica.iot.cygnus.sources;

import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Sink;
import org.apache.flume.SinkRunner;
import org.apache.flume.ChannelSelector;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.channel.ReplicatingChannelSelector;
import org.apache.flume.conf.Configurables;
import org.apache.flume.sink.DefaultSinkProcessor;
import org.apache.flume.sink.LoggerSink;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author jpalanca
 */
@RunWith(MockitoJUnitRunner.class)
public class TwitterSourceTest {

    private String consumerKey = "iAtYJ4HpUVfIUoNnif1DA";
    private String consumerSecret = "172fOpzuZoYzNYaU3mMYvE8m8MEyLbztOdbrUolU";
    private String accessToken = "zxcvbnm";
    private String accessTokenSecret = "1234567890";
    private String top_left_latitude = "40.748433";
    private String top_left_longitude = "-73.985656";
    private String bottom_right_latitude = "40.758611";
    private String bottom_right_longitude = "-73.979167";
    private String keywords = "keywords, more_keywords";
    private double[][] coordinates = {{-73.985656, 40.748433}, {-73.979167, 40.758611}};
    private String[] keywords_array = {"keywords", "more_keywords"};


    @Test
    public void testConfigure() {
        Context context = new Context();
        context.put("consumerKey", consumerKey);
        context.put("consumerSecret", consumerSecret);
        context.put("accessToken", accessToken);
        context.put("accessTokenSecret", accessTokenSecret);
        context.put("top_left_latitude", top_left_latitude);
        context.put("top_left_longitude", top_left_longitude);
        context.put("bottom_right_latitude", bottom_right_latitude);
        context.put("bottom_right_longitude", bottom_right_longitude);
        context.put("keywords", keywords);

        context.put("maxBatchDurationMillis", "1000");

        TwitterSource source = new TwitterSource();
        source.configure(context);

        assertEquals(consumerKey, source.getConsumerKey());
        assertEquals(consumerSecret, source.getConsumerSecret());
        assertEquals(accessToken, source.getAccessToken());
        assertEquals(accessTokenSecret, source.getAccessTokenSecret());
        assertArrayEquals(coordinates, source.getCoordinates());
        assertArrayEquals(keywords_array, source.getKeywords());

    }

    // From flume v1.7.0
    @Test
    public void testBasic() throws Exception {
        Context context = new Context();
        context.put("consumerKey", consumerKey);
        context.put("consumerSecret", consumerSecret);
        context.put("accessToken", accessToken);
        context.put("accessTokenSecret", accessTokenSecret);
        context.put("maxBatchDurationMillis", "1000");

        TwitterSource source = new TwitterSource();
        source.configure(context);

        Map<String, String> channelContext = new HashMap();
        channelContext.put("capacity", "1000000");
        channelContext.put("keep-alive", "0"); // for faster tests
        Channel channel = new MemoryChannel();
        Configurables.configure(channel, new Context(channelContext));

        Sink sink = new LoggerSink();
        sink.setChannel(channel);
        sink.start();
        DefaultSinkProcessor proc = new DefaultSinkProcessor();
        proc.setSinks(Collections.singletonList(sink));
        SinkRunner sinkRunner = new SinkRunner(proc);
        sinkRunner.start();

        ChannelSelector rcs = new ReplicatingChannelSelector();
        rcs.setChannels(Collections.singletonList(channel));
        ChannelProcessor chp = new ChannelProcessor(rcs);
        source.setChannelProcessor(chp);
        source.start();

        Thread.sleep(500);
        source.stop();
        sinkRunner.stop();
        sink.stop();
    }
}