package com.pillowapps.liqear.models.lastfm;

import android.content.Context;
import android.graphics.Bitmap;

import com.pillowapps.liqear.callbacks.SimpleCallback;
import com.pillowapps.liqear.callbacks.retrofit.LastfmCallback;
import com.pillowapps.liqear.entities.Album;
import com.pillowapps.liqear.entities.lastfm.LastfmAlbum;
import com.pillowapps.liqear.entities.lastfm.roots.LastfmAlbumRoot;
import com.pillowapps.liqear.entities.lastfm.roots.LastfmAlbumSearchResultsRoot;
import com.pillowapps.liqear.models.ImageModel;
import com.pillowapps.liqear.network.service.LastfmApiService;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

public class LastfmAlbumModel {
    private Context context;

    private LastfmApiService lastfmService;
    private ImageModel imageModel;

    @Inject
    public LastfmAlbumModel(Context context, LastfmApiService api, ImageModel imageModel) {
        this.context = context;
        this.lastfmService = api;
        this.imageModel = imageModel;
    }

    public void getAlbumInfo(Album album, final SimpleCallback<LastfmAlbum> callback) {
        lastfmService.getAlbumInfo(
                album.getArtist(),
                album.getTitle(),
                new LastfmCallback<LastfmAlbumRoot>() {
                    @Override
                    public void success(LastfmAlbumRoot lastfmAlbumRoot) {
                        callback.success(lastfmAlbumRoot.getAlbum());
                    }

                    @Override
                    public void failure(String error) {
                        callback.failure(error);
                    }
                }
        );
    }

    public Observable<LastfmAlbumRoot> getAlbumInfo(Album album) {
        return lastfmService.getAlbumInfo(
                album.getArtist(),
                album.getTitle()
        );
    }

    public void searchAlbum(String query, int limit, int page,
                            final SimpleCallback<List<LastfmAlbum>> callback) {
        lastfmService.searchAlbum(query, limit, page, new LastfmCallback<LastfmAlbumSearchResultsRoot>() {
            @Override
            public void success(LastfmAlbumSearchResultsRoot root) {
                callback.success(root.getResults().getAlbums().getAlbums());
            }

            @Override
            public void failure(String error) {
                callback.failure(error);
            }
        });
    }

    public Observable<Bitmap> getCover(final Album album) {
        return Observable.create(subscriber -> {
            if (album == null) {
                subscriber.onNext(null);
                subscriber.onCompleted();
                return;
            }
            imageModel.loadImage(context, album.getImageUrl(), bitmap -> {
                subscriber.onNext(bitmap);
                subscriber.onCompleted();
            });
        });
    }

}
