// Copyright 2020 Courville Software
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.archos.mediascraper.thetvdb;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import com.archos.mediascraper.ScraperImage;
import com.uwetrottmann.thetvdb.entities.SeriesImageQueryResult;
import com.uwetrottmann.thetvdb.entities.SeriesImageQueryResultResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ShowIdPostersParser {

    private static final String TAG = ShowIdPostersParser.class.getSimpleName();
    private static final boolean DBG = false;

    final static String BANNERS_URL = "https://www.thetvdb.com/banners/";

    public static ShowIdPostersResult getResult(String showTitle,
                                                SeriesImageQueryResultResponse postersResponse,
                                                SeriesImageQueryResultResponse seasonsResponse,
                                                SeriesImageQueryResultResponse globalPostersResponse,
                                                SeriesImageQueryResultResponse globalSeasonsResponse,
                                                boolean basicShow, boolean basicEpisode,
                                                String language, Context context) {

        ShowIdPostersResult result = new ShowIdPostersResult();

        if (DBG) Log.d(TAG, "getResults: basicShow=" + basicShow + ", basicEpisode=" + basicEpisode);

        // posters
        List<ScraperImage> posters = new ArrayList<>();
        List<Pair<SeriesImageQueryResult, String>> tempPosters = new ArrayList<>();

        if (!basicEpisode) {
            if (postersResponse != null)
                if (!postersResponse.data.isEmpty())
                    for (SeriesImageQueryResult poster : postersResponse.data)
                        tempPosters.add(Pair.create(poster, language));
            if (!language.equals("en"))
                if (globalPostersResponse != null)
                    if (!globalPostersResponse.data.isEmpty())
                        for (SeriesImageQueryResult poster : globalPostersResponse.data)
                            tempPosters.add(Pair.create(poster, "en"));
        }
        if (!basicShow) {
            if (seasonsResponse != null)
                if (! seasonsResponse.data.isEmpty())
                    for(SeriesImageQueryResult season : seasonsResponse.data)
                        tempPosters.add(Pair.create(season, language));
            if (!language.equals("en"))
                if (globalSeasonsResponse != null)
                    if (!globalSeasonsResponse.data.isEmpty())
                        for (SeriesImageQueryResult season : globalSeasonsResponse.data)
                            tempPosters.add(Pair.create(season, "en"));
        }
        Collections.sort(tempPosters, new Comparator<Pair<SeriesImageQueryResult, String>>() {
            @Override
            public int compare(Pair<SeriesImageQueryResult, String> p1, Pair<SeriesImageQueryResult, String> p2) {
                return - Double.compare(p1.first.keyType.equals("season") ? p1.first.ratingsInfo.average - 11 : p1.first.ratingsInfo.average, p2.first.keyType.equals("season") ? p2.first.ratingsInfo.average - 11 : p2.first.ratingsInfo.average);
            }
        });
        for(Pair<SeriesImageQueryResult, String> poster : tempPosters) {
            if (DBG) Log.d(TAG,"ScrapeDetailResult: generating ScraperImage for poster for " + showTitle + ", large=" + BANNERS_URL + poster.first.fileName + ", thumb=" + BANNERS_URL + poster.first.fileName);
            ScraperImage image = new ScraperImage(poster.first.keyType.equals("season") ? ScraperImage.Type.EPISODE_POSTER : ScraperImage.Type.SHOW_POSTER, showTitle);
            image.setLanguage(poster.second);
            image.setThumbUrl(BANNERS_URL + poster.first.thumbnail);
            image.setLargeUrl(BANNERS_URL + poster.first.fileName);
            image.generateFileNames(context);
            try {
                image.setSeason(poster.first.keyType.equals("season") ? Integer.parseInt(poster.first.subKey) : -1);
            } catch (Throwable t) {
                image.setSeason(-1);
                Log.w(TAG, "getResult: parseInt(" + poster.first.subKey + ") error on season for " + showTitle);
            }
            posters.add(image);
        }
        /*
        ScraperImage genericImage = null;
        if(!posters.isEmpty())
            genericImage = posters.get(0);
         */
        result.posters = posters;
        return result;
    }
}
