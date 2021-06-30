package com.devutkarsh.gateway.filter;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StreamUtils;

import com.amazonaws.util.IOUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;


public class ResponseFilter extends ZuulFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseFilter.class);

    @Override
    public boolean shouldFilter() {
        return true;
    }

    /**
     * To catch timeout errors from default fallback service and logging response
     * from services
     * 
     * @see com.netflix.zuul.IZuulFilter#run()
     *
     */
    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();

        if (ctx.getResponse().getStatus() == HttpStatus.GATEWAY_TIMEOUT.value()) {
            LOGGER.error("ResponseFilter.run() : Response not recieved. Gateway Timed out for {}",
                    ctx.getRequest().getRequestURI());
            throw new ZuulException("Cannot connect to service ".concat(ctx.getRequest().getRequestURI()),
                    HttpStatus.GATEWAY_TIMEOUT.value(),
                    "Cannot connect to service ".concat(ctx.getRequest().getRequestURI()));
        }

        try {
            if (ctx.getResponseDataStream().available() == 0) {
                LOGGER.info("ResponseFilter.run() : No response body input stream.");
                return null;
            }
        } catch (Exception e) {
            LOGGER.warn("ResponseFilter.run() : No response stream available.");
            return null;
        }

        byte[] response;
        byte[] writeResponse;

        try (InputStream responseDataStream = ctx.getResponseDataStream()) {
            response = IOUtils.toByteArray(responseDataStream);
            writeResponse = response;
        } catch (Exception er) {
            LOGGER.error("ResponseFilter.run() : Error reading response input stream", er);
            throw new ZuulException(er, 500, er.getCause().getMessage());
        }

        try (ByteArrayInputStream responseBack = new ByteArrayInputStream(response)) {
            writeResponse(writeResponse);
            ctx.setResponseDataStream(responseBack);
        } catch (Exception e) {
            LOGGER.error("ResponseFilter.run() : Error reading response byte stream", e);
            throw new ZuulException(e, 500, e.getCause().getMessage());
        }

        return null;
    }

    /**
     * Used to write back the response from services for logging uses Async to have
     * faster response
     * 
     * @param responseData
     */
    @Async
    public void writeResponse(byte[] responseData) {
        ByteArrayInputStream logStream = new ByteArrayInputStream(responseData);
        if (isGZipped(logStream))
            writeGZipData(responseData);
        else
            writeData(responseData);
    }

    public void writeGZipData(byte[] response) {
        String responseAsString = "";
        try (InputStream zipData = new GZIPInputStream(new ByteArrayInputStream(response))) {
            responseAsString = StreamUtils.copyToString(zipData, Charset.defaultCharset());
        } catch (IOException e) {
            LOGGER.error("ResponseFilter.writeGZipData() : Cannot convert logging stream to byte array.");
        }
        LOGGER.info("ResponseFilter.writeGZipData() : Response Body : {}", responseAsString);
        LOGGER.info("----------END----------");
    }

    public void writeData(byte[] response) {
        String responseAsString = "";
        try (InputStream data = new ByteArrayInputStream(response)) {
            responseAsString = StreamUtils.copyToString(data, Charset.defaultCharset());
        } catch (IOException e) {
            LOGGER.error("ResponseFilter.writeData() : Cannot convert logging stream to byte array.");
        }
        LOGGER.info("ResponseFilter.writeData() : Response Body : {}", responseAsString);
        LOGGER.info("----------END----------");
    }

    public static boolean isGZipped(InputStream in) {
        if (!in.markSupported()) {
            in = new BufferedInputStream(in);
        }
        in.mark(2);
        int magic = 0;
        try {
            magic = in.read() & 0xff | ((in.read() << 8) & 0xff00);
            in.reset();
        } catch (IOException e) {
            LOGGER.error("Error checking GZIP format for stream ", e);
            return false;
        }
        return magic == GZIPInputStream.GZIP_MAGIC;
    }

    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 15;
    }

}
