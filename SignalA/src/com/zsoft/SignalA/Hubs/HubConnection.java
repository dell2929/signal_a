package com.zsoft.SignalA.Hubs;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.OperationApplicationException;
import android.util.Log;

import com.zsoft.SignalA.ConnectionState;
import com.zsoft.SignalA.Transport.ITransport;
import com.zsoft.SignalA.Transport.StateBase;

public class HubConnection extends com.zsoft.SignalA.ConnectionBase {
	
    private static final String TAG = null;
	private final Map<String, HubProxy> mHubs = new HashMap<String, HubProxy>();
    private final Map<String, HubInvokeCallback> mCallbacks = new HashMap<String, HubInvokeCallback>();
    private int mCallbackId = 0;

	public HubConnection(String url, Context context, ITransport transport) {
		super(url, context, transport);
		setUrl(GetUrl(url, true));
	}

	@Override
	public void setMessage(JSONObject message) {

		JSONObject info = message.optJSONObject("I");

		if (info != null)
        {
			HubResult result = new HubResult(message);
        	HubInvokeCallback callback = null;

            synchronized (mCallbacks) {
                if(mCallbacks.containsKey(result.Id))
                	callback = mCallbacks.remove(result.Id);
                else
                {
                	Log.d(TAG, "Callback with id " + result.Id + " not found!");
                }
            }

            if (callback != null)
            {
                //callback.OnResult(true, result);
            }
        }
        else
        {
//        	HubInvocation invocation = new HubInvocation(message);
//            HubProxy hubProxy;
//            if (mHubs.containsKey(invocation.getHubName()))
//            {
//            	/* ToDo. Handle state
//                if (invocation.State != null)
//                {
//                    foreach (var state in invocation.State)
//                    {
//                        hubProxy[state.Key] = state.Value;
//                    }
//                }
//				*/
//            	hubProxy = mHubs.get(invocation.getHubName());
//            	hubProxy.InvokeEvent(invocation.getMethod(), invocation.getArgs());
//            }
//
            super.setMessage(message);
        }
		
	}

	@Override
	public void OnError(Exception exception) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnStateChanged(StateBase oldState, StateBase newState) {
		// TODO Auto-generated method stub
		
	}

	
	public IHubProxy CreateHubProxy(String hubName) throws OperationApplicationException
    {
        if (getCurrentState().getState() != ConnectionState.Disconnected)
        {
            throw new OperationApplicationException("Proxies cannot be added when connection is started");
        }

        HubProxy hubProxy;
        if(mHubs.containsKey(hubName))
        	hubProxy = mHubs.get(hubName);
        else
        {
            hubProxy = new HubProxy(this, hubName);
            mHubs.put(hubName, hubProxy);
        }
        return hubProxy;
    }	
	
	public String RegisterCallback(HubInvokeCallback callback)
	{
        synchronized (mCallbacks) {
            String id = Integer.toString(mCallbackId);
            mCallbacks.put(id, callback);
            mCallbackId++;
            return id;
        }
	}
	
	public boolean RemoveCallback(final String callbackId)
	{
        synchronized (mCallbacks) {
            if(mCallbacks.containsKey(callbackId))
            {
            	mCallbacks.remove(callbackId);
            	return true;
            }
            else
            {
            	Log.d(TAG, "Callback with id " + callbackId + " not found!");
            }
        }
        return false;
	}
	
	
	public String GetUrl(String url, boolean useDefaultUrl)
	{
        if (!url.endsWith("/"))
        {
            url += "/";
        }

        if (useDefaultUrl)
        {
            return url + "signalr";
        }

        return url;

	}
	
	// return the connectiondata....[{"Name":"chat"}]
	@Override
	public String OnSending()
	{
		JSONArray arr = new JSONArray();
		for (Entry<String, HubProxy> entry : mHubs.entrySet())
		{
		    JSONObject jsonObj= new JSONObject();
		    try {
				jsonObj.put("name", entry.getKey());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		    arr.put(jsonObj);
		}
		
		return arr.toString();
	}
}
