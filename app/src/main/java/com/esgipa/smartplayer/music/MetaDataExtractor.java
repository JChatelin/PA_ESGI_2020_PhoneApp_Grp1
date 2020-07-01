package com.esgipa.smartplayer.music;

import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import com.esgipa.smartplayer.data.model.Song;

public class MetaDataExtractor {
    private MediaMetadataRetriever metadataRetriever;
    private Context context;
    public MetaDataExtractor(Context context) {
        this.context = context;
        metadataRetriever = new MediaMetadataRetriever();
    }

    public Song extract(int resourceId) {
        Uri dataSource = Uri.parse("android.resource://com.esgipa.smartplayer/" + resourceId);
        metadataRetriever.setDataSource(context, dataSource);
        String musicTile = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String albumTitle = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        String artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String durationString = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long duration = Long.parseLong(durationString.trim());
        return new Song(dataSource.toString(), artist, musicTile, albumTitle, null, duration);
    }

    public Song extract(String musicUrl) {
        metadataRetriever.setDataSource(musicUrl);
        String musicTile = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String albumTitle = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        String artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String durationString = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long duration = Long.parseLong(durationString.trim());
        return new Song(musicUrl, artist, musicTile, albumTitle, null, duration);
    }
}
