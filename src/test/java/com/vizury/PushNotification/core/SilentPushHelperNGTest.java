package com.vizury.PushNotification.core;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anurag on 10/29/15.
 */
public class SilentPushHelperNGTest {

    @DataProvider(name = "handleUnInstallCookiesDataProvider")
    public Object[][] handleUnInstallCookiesDataProvider() {
        return new Object[][] {
                {   "1506", "AIzaSyDFEiYIBXw-zFIfvocJ2iIfIarLPnbq0uM","",
                        new ArrayList<String>() {{
                            add("viz_a_cb80835a-a7f0-411b-9d6b-1c90b1043269");
                            add("viz_a_f4cf97e7-6b6f-4713-8187-bef6d80ad0f2");
                        }}
                }
        };
    }

    @Test(dataProvider = "handleUnInstallCookiesDataProvider")
    public void testHandleUnInstallCookies(String campaignId, String apiKey,
                                           String path, List<String> cookieList) {
        SilentPushHelper silentPushHelper = new SilentPushHelper(campaignId, apiKey, path,1000);
        silentPushHelper.handleUnInstallCookies(cookieList);
    }
}
