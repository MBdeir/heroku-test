package com.example.wildsight;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class JsonArrayPostRequest extends Request<JSONArray> {
    private final Response.Listener<JSONArray> listener;
    private final JSONObject requestBody;

    public JsonArrayPostRequest(int method, String url, JSONObject requestBody,
                                Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = listener;
        this.requestBody = requestBody;
    }

    @Override
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONArray(jsonString), HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(JSONArray response) {
        listener.onResponse(response);
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return requestBody == null ? null : requestBody.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getBodyContentType() {
        return "application/json; charset=utf-8";
    }
}
