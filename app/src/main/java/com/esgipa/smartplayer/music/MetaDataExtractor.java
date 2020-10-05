package com.esgipa.smartplayer.music;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.esgipa.smartplayer.data.model.Song;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class MetaDataExtractor {
    private MediaMetadataRetriever metadataRetriever;
    private FFmpegMediaMetadataRetriever mmr;

    private Context context;
    public MetaDataExtractor(Context context) {
        this.context = context;
        metadataRetriever = new MediaMetadataRetriever();
        mmr = new FFmpegMediaMetadataRetriever();
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
        mmr.setDataSource(musicUrl);
        String musictTitle = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_TITLE);
        String albumTitle = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);
        String artist = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
        byte[] albumArt = mmr.getEmbeddedPicture();
        String durationString = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);

        if(musictTitle == null) {
            musictTitle = "Unknown";
        }
        if(albumTitle == null) {
            albumTitle = "Unknown";
        }
        if(artist == null) {
            artist = "Unknown";
        }
        long duration = Long.parseLong(durationString.trim());
        Song newSong = new Song(musicUrl, artist, musictTitle, albumTitle, albumArt, duration);
        newSong.setFileName(musicUrl.substring(musicUrl.lastIndexOf("/")));
        return newSong;
    }
}
