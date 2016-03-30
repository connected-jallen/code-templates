package com.connectedlab.unittest;

import com.connectedlab.templates.logging.LogUtil;
import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class WebQueueDispatcher extends Dispatcher {

    private Map<String, Queue<MockResponse>> mResponseQueue = new HashMap<>();

    public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

        String requestLine = request.getRequestLine();
        for (Map.Entry<String, Queue<MockResponse>> entry : mResponseQueue.entrySet()){
            if(requestLine.matches(entry.getKey())){
                return entry.getValue().poll();
            }
        }
        LogUtil.i("Missing response for %s", requestLine);
        return new MockResponse().setResponseCode(200);
    }

    public void enqueue(String regex, MockResponse response){
        Iterator it = mResponseQueue.entrySet().iterator();
        Queue<MockResponse> list = mResponseQueue.get(regex);
        if(list == null){
            Queue<MockResponse> mockResponseQueue = new LinkedList<>();
            mockResponseQueue.add(response);
            mResponseQueue.put(regex, mockResponseQueue);

        }
        else list.add(response);
    }
}
