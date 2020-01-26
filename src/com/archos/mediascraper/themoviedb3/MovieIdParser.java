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

import com.archos.mediascraper.MovieTags;
import com.archos.mediascraper.StringMatcher;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MovieIdParser extends JSONStreamParser<MovieTags, Void> {

    public static final MovieIdParser getInstance() {
        return INSTANCE;
    }
    private MovieIdParser () { /* empty */ }
    private static final MovieIdParser INSTANCE = new MovieIdParser();

    private static final StringMatcher MATCHER = new StringMatcher();

    private static final int KEY_ID = 1;
    private static final int KEY_NAME = 2;
    private static final int KEY_GENRES = 3;
    private static final int KEY_IMDB_ID = 4;
    private static final int KEY_OVERVIEW = 5;
    private static final int KEY_PRODUCTION_COMPANIES = 6;
    private static final int KEY_RELEASE_DATE = 7;
    private static final int KEY_TITLE = 8;
    private static final int KEY_VOTE_AVERAGE = 9;
    private static final int KEY_RUNTIME = 19;

    // casts
    private static final int KEY_CASTS = 10;
    private static final int KEY_CAST = 11;
    private static final int KEY_CREW = 12;
    private static final int KEY_CHARACTER = 13;
    private static final int KEY_JOB = 14;

    // certification
    private static final int KEY_RELEASES = 15;
    private static final int KEY_COUNTRIES = 16;
    private static final int KEY_ISO_3166 = 17;
    private static final int KEY_CERTIFICATION = 18;

    static {
        MATCHER.addKey("id", KEY_ID);
        MATCHER.addKey("name", KEY_NAME);
        MATCHER.addKey("genres", KEY_GENRES);
        MATCHER.addKey("imdb_id", KEY_IMDB_ID);
        MATCHER.addKey("overview", KEY_OVERVIEW);
        MATCHER.addKey("production_companies", KEY_PRODUCTION_COMPANIES);
        MATCHER.addKey("release_date", KEY_RELEASE_DATE);
        MATCHER.addKey("title", KEY_TITLE);
        MATCHER.addKey("vote_average", KEY_VOTE_AVERAGE);
        MATCHER.addKey("runtime", KEY_RUNTIME);

        // casts
        MATCHER.addKey("casts", KEY_CASTS);
        MATCHER.addKey("cast", KEY_CAST);
        MATCHER.addKey("crew", KEY_CREW);
        MATCHER.addKey("character", KEY_CHARACTER);
        MATCHER.addKey("job", KEY_JOB);

        // releases
        MATCHER.addKey("releases", KEY_RELEASES);
        MATCHER.addKey("countries", KEY_COUNTRIES);
        MATCHER.addKey("iso_3166_1", KEY_ISO_3166);
        MATCHER.addKey("certification", KEY_CERTIFICATION);
    }

    private static final String COUNTRY_US = "US";

    private static final String DIRECTOR = "Director";

    @Override
    protected MovieTags getResult(JsonReader reader, Void config) throws IOException {
        reader.beginObject();

        MovieTags result = new MovieTags();
        String name;
        while ((name = getNextNotNullName(reader)) != null) {
            switch (MATCHER.match(name)) {
                case KEY_ID:
                    result.setOnlineId(reader.nextLong());
                    break;
                case KEY_GENRES:
                    readGenres(reader, result);
                    break;
                case KEY_IMDB_ID:
                    result.setImdbId(reader.nextString());
                    break;
                case KEY_OVERVIEW:
                    result.setPlot(reader.nextString());
                    break;
                case KEY_PRODUCTION_COMPANIES:
                    readProductionCompanies(reader, result);
                    break;
                case KEY_RELEASE_DATE:
                    result.setYear(getYear(reader.nextString()));
                    break;
                case KEY_TITLE:
                    result.setTitle(reader.nextString());
                    break;
                case KEY_VOTE_AVERAGE:
                    result.setRating((float) reader.nextDouble());
                    break;
                case KEY_CASTS:
                    readCasts(reader, result);
                    break;
                case KEY_RELEASES:
                    readReleases(reader, result);
                    break;
                case KEY_RUNTIME:
                    result.setRuntime(reader.nextLong(), TimeUnit.MINUTES);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }

        reader.endObject();

        return result;
    }

    private static void readGenres(JsonReader reader, MovieTags target) throws IOException {
        reader.beginArray();
        while (hasNextSkipNull(reader)) {
            String item = readName(reader);
            target.addGenreIfAbsent(item);
        }
        reader.endArray();
    }

    private static void readProductionCompanies(JsonReader reader, MovieTags target) throws IOException {
        reader.beginArray();
        while (hasNextSkipNull(reader)) {
            String item = readName(reader);
            target.addStudioIfAbsent(item);
        }
        reader.endArray();
    }

    private static String readName(JsonReader reader) throws IOException {
        reader.beginObject();
        String name;
        String returnValue = null;
        while ((name = getNextNotNullName(reader)) != null) {
            switch(MATCHER.match(name)) {
                case KEY_NAME:
                    returnValue = reader.nextString();
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
        return returnValue;
    }

    private static int getYear(String string) {
        // Quick extraction of year from "2011-12-31"
        if (string.length() > 4) {
            String year = string.substring(0, 4);
            try {
                return Integer.parseInt(year);
            } catch (NumberFormatException e) {
                // ignore, return 0 below
            }
        }
        return 0;
    }

    //-------------------- ACTORS & DIRECTORS --------------------------------//
    private static void readCasts(JsonReader reader, MovieTags target) throws IOException {
        reader.beginObject();
        String name;
        while ((name = getNextNotNullName(reader)) != null) {
            switch(MATCHER.match(name)) {
                case KEY_CAST:
                    readActors(reader, target);
                    break;
                case KEY_CREW:
                    readDirectors(reader, target);
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
    }

    private static void readDirectors(JsonReader reader, MovieTags target) throws IOException {
        reader.beginArray();
        while (hasNextSkipNull(reader)) {
            String item = readDirector(reader);
            target.addDirectorIfAbsent(item);
        }
        reader.endArray();
    }

    private static String readDirector(JsonReader reader) throws IOException {
        reader.beginObject();
        String name;
        String personName = null;
        boolean isDirector = false;
        while ((name = getNextNotNullName(reader)) != null) {
            switch(MATCHER.match(name)) {
                case KEY_NAME:
                    personName = reader.nextString();
                    break;
                case KEY_JOB:
                    if (DIRECTOR.equals(reader.nextString()))
                        isDirector = true;
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
        return isDirector ? personName : null;
    }


    private static void readActors(JsonReader reader, MovieTags target) throws IOException {
        reader.beginArray();
        while (hasNextSkipNull(reader)) {
            readActor(reader, target);
        }
        reader.endArray();
    }

    private static void readActor(JsonReader reader, MovieTags target) throws IOException {
        reader.beginObject();
        String name;
        String personName = null;
        String personRole = null;
        while ((name = getNextNotNullName(reader)) != null) {
            switch(MATCHER.match(name)) {
                case KEY_NAME:
                    personName = reader.nextString();
                    break;
                case KEY_CHARACTER:
                    personRole = reader.nextString();
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
        target.addActorIfAbsent(personName, personRole);
    }

    // --------------------- RELEASES -------------------------------------- //
    private static void readReleases(JsonReader reader, MovieTags target) throws IOException {
        reader.beginObject();
        String name;
        while ((name = getNextNotNullName(reader)) != null) {
            switch(MATCHER.match(name)) {
                case KEY_COUNTRIES:
                    target.setContentRating(readCountries(reader));
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
    }

    private static String readCountries(JsonReader reader) throws IOException {
        reader.beginArray();
        String result = null;
        while (hasNextSkipNull(reader)) {
            String item = readCountry(reader);
            if (item != null) {
                result = item;
            }
        }
        reader.endArray();
        return result;
    }

    private static String readCountry(JsonReader reader) throws IOException {
        String certification = null;
        boolean isUS = false;

        reader.beginObject();
        String name;
        while ((name = getNextNotNullName(reader)) != null) {
            switch(MATCHER.match(name)) {
                case KEY_CERTIFICATION:
                    certification = reader.nextString();
                    break;
                case KEY_ISO_3166:
                    if (COUNTRY_US.equals(reader.nextString()))
                        isUS = true;
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();

        return isUS ? certification : null;
    }
}
