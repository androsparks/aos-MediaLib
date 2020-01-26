// Copyright 2017 Archos SA
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

package com.archos.mediascraper.themoviedb3;

import android.util.JsonReader;

import com.archos.mediascraper.SearchResult;
import com.archos.mediascraper.StringMatcher;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SearchMovieTrailerParser extends JSONStreamParser<List<SearchMovieTrailerResult.TrailerResult>, Integer>{
    private static final SearchMovieTrailerParser INSTANCE = new SearchMovieTrailerParser();
    public static SearchMovieTrailerParser getInstance() {
        return INSTANCE;
    }
    private SearchMovieTrailerParser() {
        // empty
    }

    private static final StringMatcher MATCHER = new StringMatcher();
    // top level keys
    private static final int KEY_RESULTS = 1;
    // item level keys
    private static final int KEY_TITLE = 4;
    private static final int KEY_TRAILER = 5;
    private static final int KEY_LANGUAGE = 6;
    private static final int KEY_SERVICE = 7;
    private static final int KEY_NAME = 8;
    private static final int KEY_TYPE = 9;
    static {
        MATCHER.addKey("results", KEY_RESULTS);
        MATCHER.addKey("key", KEY_TRAILER);
        MATCHER.addKey("language",KEY_LANGUAGE);
        MATCHER.addKey("site",KEY_SERVICE);
        MATCHER.addKey("type",KEY_TYPE);
        MATCHER.addKey("name",KEY_NAME);
    }

    @Override
    protected List<SearchMovieTrailerResult.TrailerResult> getResult(JsonReader reader, Integer config) throws IOException {
        List<SearchMovieTrailerResult.TrailerResult> result = new LinkedList<SearchMovieTrailerResult.TrailerResult>();

        reader.beginObject();
        String name;
        while ((name = getNextNotNullName(reader)) != null) {
            switch(MATCHER.match(name)) {
                case KEY_RESULTS:
                    readResults(reader, result, 40);
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();

        return result;
    }

    public static void readResults(JsonReader reader, List<SearchMovieTrailerResult.TrailerResult> result, int limit) throws IOException {
        reader.beginArray();
        while (hasNextSkipNull(reader)) {
            if (limit > 0 && result.size() >= limit) {
                reader.skipValue();
            } else {
                SearchMovieTrailerResult.TrailerResult item = readResult(reader);
                result.add(item);
            }
        }
        reader.endArray();
    }

    public static SearchMovieTrailerResult.TrailerResult readResult(JsonReader reader) throws IOException {
        SearchMovieTrailerResult.TrailerResult item = new SearchMovieTrailerResult.TrailerResult();

        reader.beginObject();
        String name;
        while ((name = getNextNotNullName(reader)) != null) {
            switch(MATCHER.match(name)) {
                case KEY_SERVICE:
                    item.setService(reader.nextString());
                    break;
                case KEY_LANGUAGE:
                    item.setLanguage(reader.nextString());
                    break;
                case KEY_TRAILER:
                    item.setKey(reader.nextString());
                    break;
                case KEY_NAME:
                    item.setName(reader.nextString());
                    break;
                case KEY_TYPE:
                    item.setType(reader.nextString());
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();

        return item;
    }
}
