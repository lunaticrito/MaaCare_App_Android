package com.cse2200.maacare.network;

import com.cse2200.maacare.data.model.MaternalHealthDataResponse;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Retrofit REST API interface for remote maternal health tips, advisories, and emergency directories.
 */
public interface MaaCareApiService {

    @GET("api/v1/maternal-health-data.json")
    Call<MaternalHealthDataResponse> getMaternalHealthData();
}
