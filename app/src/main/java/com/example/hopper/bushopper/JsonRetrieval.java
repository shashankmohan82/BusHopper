package com.example.hopper.bushopper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by shashank on 4/16/2016.
 */
public class JsonRetrieval {

    private JSONObject curLocation;
    private String routeId;
    private JSONObject route;
    private JSONObject stopSeqNo;
    private JSONObject lastLocation;
    private JSONObject busStop;
    private String mbusstopLat;
    private String mbusstopLong;
    private String ebusstopLat;
    private String ebusstopLong;
    private String eseqNo;
    private String mseqNo;
    private String curLat;
    private String curLong;
    private String seqNo;

    public JSONObject getCurLocation(JSONObject json) throws JSONException {
        JSONObject object = json.getJSONObject("curLocation");
        return object;
    }

    public JSONObject getRoute(JSONObject json) throws JSONException {
        JSONObject object = json.getJSONObject("route");
        return object;
    }

    public JSONObject getStopSeqNo(JSONObject json) throws JSONException {
        JSONObject object = json.getJSONObject("stopSeqNo");
        return object;
    }
    public JSONObject getLastLocation(JSONObject json) throws JSONException {
        JSONObject object = json.getJSONObject("lastLocation");
        return object;
    }
    public JSONObject getBusStop(JSONObject json) throws JSONException {
        JSONObject object = json.getJSONObject("busStop");
        return object;
    }


    public String getRouteId(JSONObject json) throws JSONException {
        String jsonString = getRoute(json).optString("routeId");
        return jsonString;

    }
    public String getMbusstopLat(JSONObject json) throws JSONException{
        String jsonString = getBusStop(json).optString("mbusstopLat");
        return jsonString;

    }
    public String getEbusstopLat(JSONObject json) throws JSONException{
        String jsonString = getBusStop(json).optString("ebusstopLat");
        return jsonString;
    }
    public String getMbusstopLong(JSONObject json) throws JSONException{
        String jsonString = getBusStop(json).optString("mbusstopLong");
        return jsonString;

    }
    public String getEbusstopLong(JSONObject json) throws JSONException{
        String jsonString = getBusStop(json).optString("ebusstopLong");
        return jsonString;
    }
    public String geteseqNo(JSONObject json) throws JSONException{
        String jsonString = getStopSeqNo(json).optString("eseqNo");
        return jsonString;
    }
    public String getmseqNo(JSONObject json) throws JSONException{

        String jsonString = getStopSeqNo(json).optString("mseqNo");
        return jsonString;
    }
    public String getCurLat(JSONObject json) throws JSONException{
        String jsonString = getCurLocation(json).optString("curLat");
        return jsonString;
    }
    public String getCurLong(JSONObject json) throws JSONException{
        String jsonString = getCurLocation(json).optString("curLong");
        return jsonString;
    }
    public String getSeqNo(JSONObject json) throws JSONException{
        String jsonString = getLastLocation(json).optString("lastbusstopseqNo");
        return jsonString;
    }



}
