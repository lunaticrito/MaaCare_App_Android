package com.cse2200.maacare.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton Retrofit HTTP Client instance.
 */
public class ApiClient {

    private static final String BASE_URL = "https://raw.githubusercontent.com/cse2200-maacare/api/main/";
    private static Retrofit retrofit = null;

    public static MaaCareApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(MaaCareApiService.class);
    }
}
