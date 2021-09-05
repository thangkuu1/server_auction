package com.main.auc.config;


import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;


public class MyHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private byte[] rawData;
    private HttpServletRequest request;
    private MyServletInputStream myServletInputStream;

    public MyHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.request = request;
        this.myServletInputStream = new MyServletInputStream();
    }

    public boolean isFileUploadRequest(HttpServletRequest aRequest){
        return aRequest.getContentType().startsWith("multipart/form-data");
    }

    public void resetInputStream(byte[] newRawData) {
        rawData = newRawData;
        myServletInputStream.inputStream = new ByteArrayInputStream(newRawData);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (rawData == null) {
            rawData = IOUtils.toByteArray(this.request.getInputStream());
            myServletInputStream.inputStream = new ByteArrayInputStream(rawData);
        }
        return myServletInputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        // Create a reader from cachedContent and return it
        if (rawData == null) {
            rawData = IOUtils.toByteArray(this.request.getInputStream());
            myServletInputStream.inputStream = new ByteArrayInputStream(rawData);
        }
        return new BufferedReader(new InputStreamReader(myServletInputStream));
    }

    private class MyServletInputStream extends ServletInputStream {

        private InputStream inputStream;

        @Override
        public boolean isFinished() {
            try {
                return inputStream.available() == 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener arg0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }
    }

}
