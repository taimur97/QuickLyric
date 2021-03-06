/*
 * *
 *  * This file is part of QuickLyric
 *  * Created by geecko
 *  *
 *  * QuickLyric is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * QuickLyric is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  * You should have received a copy of the GNU General Public License
 *  * along with QuickLyric.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.geecko.QuickLyric.lyrics;

import com.geecko.QuickLyric.annotations.Reflection;
import com.geecko.QuickLyric.utils.Net;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

@Reflection
public class Bollywood {

    @Reflection
    public static final String domain = "quicklyric.netii.net/bollywood/";

    public static ArrayList<Lyrics> search(String query) {
        ArrayList<Lyrics> results = new ArrayList<>();
        String searchUrl = "http://quicklyric.netii.net/bollywood/search.php?q=%s";
        try {
            String jsonText;
            jsonText = Net.getUrlAsString(String.format(searchUrl, URLEncoder.encode(query, "utf-8")));
            JSONObject jsonResponse = new JSONObject(jsonText);
            JSONArray lyricsResults = jsonResponse.getJSONArray("lyrics");
            for (int i = 0; i < lyricsResults.length(); ++i) {
                JSONObject lyricsResult = lyricsResults.getJSONObject(i);
                JSONArray tags = lyricsResult.getJSONArray("tags");
                Lyrics lyrics = new Lyrics(Lyrics.SEARCH_ITEM);
                lyrics.setTitle(lyricsResult.getString("name"));
                for (int j = 0; i < tags.length(); ++j) {
                    JSONObject tag = tags.getJSONObject(j);
                    if (tag.getString("tag_type").equals("Singer")) {
                        lyrics.setArtist(tag.getString("name").trim());
                        break;
                    }
                }
                lyrics.setURL("http://quicklyric.netii.net/bollywood/get.php?id=" + lyricsResult.getInt("id"));
                results.add(lyrics);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return results;
    }

    @Reflection
    public static Lyrics fromMetaData(String artist, String title) {
        ArrayList<Lyrics> searchResults = search(artist + " " + title);
        for (Lyrics result : searchResults) {
            if (artist.contains(result.getArtist()) && title.equalsIgnoreCase(result.getTrack()))
                return fromAPI(result.getURL(), artist, result.getTrack());
        }
        return new Lyrics(Lyrics.NO_RESULT);
    }

    // TODO handle urls
    @Reflection
    public static Lyrics fromURL(String url, String artist, String song) {
        return new Lyrics(Lyrics.NO_RESULT);
    }

    public static Lyrics fromAPI(String url, String artist, String title) {
        Lyrics lyrics = new Lyrics(Lyrics.POSITIVE_RESULT);
        lyrics.setArtist(artist);
        lyrics.setTitle(title);
        // fixme no public url
        try {
            String jsonText = Net.getUrlAsString(url);
            JSONObject lyricsJSON = new JSONObject(jsonText);
            lyrics.setText(lyricsJSON.getString("body").trim());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return new Lyrics(Lyrics.ERROR);
        }
        lyrics.setSource(domain);
        return lyrics;
    }
}
