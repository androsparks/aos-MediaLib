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

public class SearchMovieParser extends JSONStreamParser<List<SearchResult>, Integer>{
    private static final SearchMovieParser INSTANCE = new SearchMovieParser();
    public static SearchMovieParser getInstance() {
        return INSTANCE;
    }
    private SearchMovieParser() {
        // empty
    }

    private static final StringMatcher MATCHER = new StringMatcher();
    // top level keys
    private static final int KEY_RESULTS = 1;
    // item level keys
    private static final int KEY_ID = 3;
    private static final int KEY_TITLE = 4;

    static {
        MATCHER.addKey("results", KEY_RESULTS);
        MATCHER.addKey("id", KEY_ID);
        MATCHER.addKey("title", KEY_TITLE);
    }

    @Override
    protected List<SearchResult> getResult(JsonReader reader, Integer config) throws IOException {
        List<SearchResult> result = new LinkedList<SearchResult>();

        reader.beginObject();
        String name;
        while ((name = getNextNotNullName(reader)) != null) {
            switch(MATCHER.match(name)) {
                case KEY_RESULTS:
                    readResults(reader, result, config);
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();

        return result;
    }

    public static void readResults(JsonReader reader, List<SearchResult> result, int limit) throws IOException {
        reader.beginArray();
        while (hasNextSkipNull(reader)) {
            if (limit > 0 && result.size() >= limit) {
                reader.skipValue();
            } else {
                SearchResult item = readResult(reader);
                result.add(item);
            }
        }
        reader.endArray();
    }

    public static SearchResult readResult(JsonReader reader) throws IOException {
        SearchResult item = new SearchResult();

        reader.beginObject();
        String name;
        while ((name = getNextNotNullName(reader)) != null) {
            switch(MATCHER.match(name)) {
                case KEY_TITLE:
                    item.setTitle(reader.nextString());
                    break;
                case KEY_ID:
                    item.setId(reader.nextInt());
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();

        return item;
    }
}
