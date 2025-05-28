package com.ifi.ifi;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.util.Base64;

public abstract class VolleyMultipartRequest extends com.android.volley.Request<NetworkResponse> {

    private final Response.Listener<NetworkResponse> mListener;
    private final Map<String, String> mHeaders = new HashMap<>();

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders;
    }

    public void setCustomHeader(String key, String value) {
        mHeaders.put(key, value);
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    //@Override
    public abstract Map<String, DataPart> getByteData() throws IOException;

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    private final String boundary = "apiclient-" + System.currentTimeMillis();

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            for (Map.Entry<String, String> entry : getParams().entrySet()) {
                bos.write(("--" + boundary + "\r\n").getBytes());
                bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n").getBytes());
                bos.write((entry.getValue() + "\r\n").getBytes());
            }

            for (Map.Entry<String, DataPart> entry : getByteData().entrySet()) {
                bos.write(("--" + boundary + "\r\n").getBytes());
                bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + entry.getValue().getFileName() + "\"\r\n").getBytes());
                bos.write(("Content-Type: " + entry.getValue().getType() + "\r\n\r\n").getBytes());
                bos.write(entry.getValue().getContent());
                bos.write("\r\n".getBytes());
            }

            bos.write(("--" + boundary + "--\r\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bos.toByteArray();
    }

    public abstract Map<String, String> getParams();

    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String name, byte[] data, String mimeType) {
            this.fileName = name;
            this.content = data;
            this.type = mimeType;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public String getType() {
            return type;
        }
    }
}
