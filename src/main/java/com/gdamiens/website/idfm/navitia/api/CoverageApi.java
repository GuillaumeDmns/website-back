/*
 * navitia
 *      navitia.io is the open API for building cool stuff with mobility data. It provides the following services      * journeys computation     * line schedules     * next departures     * exploration of public transport data / search places     * and sexy things such as isochrones      navitia is a HATEOAS API that returns JSON formated results
 *
 * OpenAPI spec version: v15.75.4
 * Contact: navitia@googlegroups.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.gdamiens.website.idfm.navitia.api;

import io.swagger.client.ApiCallback;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.Configuration;
import io.swagger.client.Pair;
import io.swagger.client.ProgressRequestBody;
import io.swagger.client.ProgressResponseBody;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;


import java.math.BigDecimal;
import io.swagger.client.model.Coverages;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoverageApi {
    private ApiClient apiClient;
    private Map<String, String> headers;

    public CoverageApi() {
        this(Configuration.getDefaultApiClient());
    }

    public CoverageApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void setHeadersOverrides(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Build call for getCoverage
     * @param disableGeojson hide the coverage geojson to reduce response size (optional)
     * @param progressListener Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public com.squareup.okhttp.Call getCoverageCall(Boolean disableGeojson, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/coverage/";

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (disableGeojson != null)
        localVarQueryParams.addAll(apiClient.parameterToPair("disable_geojson", disableGeojson));

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if(progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new com.squareup.okhttp.Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(com.squareup.okhttp.Interceptor.Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                    .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                    .build();
                }
            });
        }

        String[] localVarAuthNames = new String[] { "basicAuth" };
        if (headers != null) {
            localVarHeaderParams.putAll(headers);
        }
        return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    @SuppressWarnings("rawtypes")
    private com.squareup.okhttp.Call getCoverageValidateBeforeCall(Boolean disableGeojson, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {


        com.squareup.okhttp.Call call = getCoverageCall(disableGeojson, progressListener, progressRequestListener);
        return call;

    }

    /**
     *
     *
     * @param disableGeojson hide the coverage geojson to reduce response size (optional)
     * @return Coverages
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public Coverages getCoverage(Boolean disableGeojson) throws ApiException {
        ApiResponse<Coverages> resp = getCoverageWithHttpInfo(disableGeojson);
        return resp.getData();
    }

    /**
     *
     *
     * @param disableGeojson hide the coverage geojson to reduce response size (optional)
     * @return ApiResponse&lt;Coverages&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<Coverages> getCoverageWithHttpInfo(Boolean disableGeojson) throws ApiException {
        com.squareup.okhttp.Call call = getCoverageValidateBeforeCall(disableGeojson, null, null);
        Type localVarReturnType = new TypeToken<Coverages>(){}.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     *  (asynchronously)
     *
     * @param disableGeojson hide the coverage geojson to reduce response size (optional)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call getCoverageAsync(Boolean disableGeojson, final ApiCallback<Coverages> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = new ProgressResponseBody.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    callback.onDownloadProgress(bytesRead, contentLength, done);
                }
            };

            progressRequestListener = new ProgressRequestBody.ProgressRequestListener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                    callback.onUploadProgress(bytesWritten, contentLength, done);
                }
            };
        }

        com.squareup.okhttp.Call call = getCoverageValidateBeforeCall(disableGeojson, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<Coverages>(){}.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }
    /**
     * Build call for getCoverageLonLat
     * @param lon The longitude of where the coord you want to query (required)
     * @param lat The latitude of where the coord you want to query (required)
     * @param disableGeojson hide the coverage geojson to reduce response size (optional)
     * @param progressListener Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public com.squareup.okhttp.Call getCoverageLonLatCall(BigDecimal lon, BigDecimal lat, Boolean disableGeojson, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/coverage/{lon};{lat}/"
            .replaceAll("\\{" + "lon" + "\\}", apiClient.escapeString(lon.toString()))
            .replaceAll("\\{" + "lat" + "\\}", apiClient.escapeString(lat.toString()));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (disableGeojson != null)
        localVarQueryParams.addAll(apiClient.parameterToPair("disable_geojson", disableGeojson));

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if(progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new com.squareup.okhttp.Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(com.squareup.okhttp.Interceptor.Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                    .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                    .build();
                }
            });
        }

        String[] localVarAuthNames = new String[] { "basicAuth" };
        if (headers != null) {
            localVarHeaderParams.putAll(headers);
        }
        return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    @SuppressWarnings("rawtypes")
    private com.squareup.okhttp.Call getCoverageLonLatValidateBeforeCall(BigDecimal lon, BigDecimal lat, Boolean disableGeojson, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {

        // verify the required parameter 'lon' is set
        if (lon == null) {
            throw new ApiException("Missing the required parameter 'lon' when calling getCoverageLonLat(Async)");
        }

        // verify the required parameter 'lat' is set
        if (lat == null) {
            throw new ApiException("Missing the required parameter 'lat' when calling getCoverageLonLat(Async)");
        }


        com.squareup.okhttp.Call call = getCoverageLonLatCall(lon, lat, disableGeojson, progressListener, progressRequestListener);
        return call;

    }

    /**
     *
     *
     * @param lon The longitude of where the coord you want to query (required)
     * @param lat The latitude of where the coord you want to query (required)
     * @param disableGeojson hide the coverage geojson to reduce response size (optional)
     * @return Coverages
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public Coverages getCoverageLonLat(BigDecimal lon, BigDecimal lat, Boolean disableGeojson) throws ApiException {
        ApiResponse<Coverages> resp = getCoverageLonLatWithHttpInfo(lon, lat, disableGeojson);
        return resp.getData();
    }

    /**
     *
     *
     * @param lon The longitude of where the coord you want to query (required)
     * @param lat The latitude of where the coord you want to query (required)
     * @param disableGeojson hide the coverage geojson to reduce response size (optional)
     * @return ApiResponse&lt;Coverages&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<Coverages> getCoverageLonLatWithHttpInfo(BigDecimal lon, BigDecimal lat, Boolean disableGeojson) throws ApiException {
        com.squareup.okhttp.Call call = getCoverageLonLatValidateBeforeCall(lon, lat, disableGeojson, null, null);
        Type localVarReturnType = new TypeToken<Coverages>(){}.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     *  (asynchronously)
     *
     * @param lon The longitude of where the coord you want to query (required)
     * @param lat The latitude of where the coord you want to query (required)
     * @param disableGeojson hide the coverage geojson to reduce response size (optional)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call getCoverageLonLatAsync(BigDecimal lon, BigDecimal lat, Boolean disableGeojson, final ApiCallback<Coverages> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = new ProgressResponseBody.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    callback.onDownloadProgress(bytesRead, contentLength, done);
                }
            };

            progressRequestListener = new ProgressRequestBody.ProgressRequestListener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                    callback.onUploadProgress(bytesWritten, contentLength, done);
                }
            };
        }

        com.squareup.okhttp.Call call = getCoverageLonLatValidateBeforeCall(lon, lat, disableGeojson, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<Coverages>(){}.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }
    /**
     * Build call for getCoverageRegion
     * @param region The region you want to query (required)
     * @param disableGeojson hide the coverage geojson to reduce response size (optional)
     * @param progressListener Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public com.squareup.okhttp.Call getCoverageRegionCall(String region, Boolean disableGeojson, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/coverage/{region}/"
            .replaceAll("\\{" + "region" + "\\}", apiClient.escapeString(region.toString()));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (disableGeojson != null)
        localVarQueryParams.addAll(apiClient.parameterToPair("disable_geojson", disableGeojson));

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if(progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new com.squareup.okhttp.Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(com.squareup.okhttp.Interceptor.Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                    .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                    .build();
                }
            });
        }

        String[] localVarAuthNames = new String[] { "basicAuth" };
        if (headers != null) {
            localVarHeaderParams.putAll(headers);
        }
        return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    @SuppressWarnings("rawtypes")
    private com.squareup.okhttp.Call getCoverageRegionValidateBeforeCall(String region, Boolean disableGeojson, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {

        // verify the required parameter 'region' is set
        if (region == null) {
            throw new ApiException("Missing the required parameter 'region' when calling getCoverageRegion(Async)");
        }


        com.squareup.okhttp.Call call = getCoverageRegionCall(region, disableGeojson, progressListener, progressRequestListener);
        return call;

    }

    /**
     *
     *
     * @param region The region you want to query (required)
     * @param disableGeojson hide the coverage geojson to reduce response size (optional)
     * @return Coverages
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public Coverages getCoverageRegion(String region, Boolean disableGeojson) throws ApiException {
        ApiResponse<Coverages> resp = getCoverageRegionWithHttpInfo(region, disableGeojson);
        return resp.getData();
    }

    /**
     *
     *
     * @param region The region you want to query (required)
     * @param disableGeojson hide the coverage geojson to reduce response size (optional)
     * @return ApiResponse&lt;Coverages&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<Coverages> getCoverageRegionWithHttpInfo(String region, Boolean disableGeojson) throws ApiException {
        com.squareup.okhttp.Call call = getCoverageRegionValidateBeforeCall(region, disableGeojson, null, null);
        Type localVarReturnType = new TypeToken<Coverages>(){}.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     *  (asynchronously)
     *
     * @param region The region you want to query (required)
     * @param disableGeojson hide the coverage geojson to reduce response size (optional)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call getCoverageRegionAsync(String region, Boolean disableGeojson, final ApiCallback<Coverages> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = new ProgressResponseBody.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    callback.onDownloadProgress(bytesRead, contentLength, done);
                }
            };

            progressRequestListener = new ProgressRequestBody.ProgressRequestListener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                    callback.onUploadProgress(bytesWritten, contentLength, done);
                }
            };
        }

        com.squareup.okhttp.Call call = getCoverageRegionValidateBeforeCall(region, disableGeojson, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<Coverages>(){}.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }
}
