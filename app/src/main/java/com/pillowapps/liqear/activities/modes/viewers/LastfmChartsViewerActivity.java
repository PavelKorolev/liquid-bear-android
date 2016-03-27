package com.pillowapps.liqear.activities.modes.viewers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.pillowapps.liqear.LBApplication;
import com.pillowapps.liqear.R;
import com.pillowapps.liqear.activities.base.PagerResultActivity;
import com.pillowapps.liqear.adapters.pagers.PagesPagerAdapter;
import com.pillowapps.liqear.callbacks.SimpleCallback;
import com.pillowapps.liqear.components.viewers.LastfmArtistViewerPage;
import com.pillowapps.liqear.components.viewers.LastfmTracksViewerPage;
import com.pillowapps.liqear.components.viewers.base.ViewerPage;
import com.pillowapps.liqear.entities.Page;
import com.pillowapps.liqear.entities.Track;
import com.pillowapps.liqear.entities.lastfm.LastfmArtist;
import com.pillowapps.liqear.entities.lastfm.LastfmTrack;
import com.pillowapps.liqear.helpers.PreferencesScreenManager;
import com.pillowapps.liqear.helpers.ErrorNotifier;
import com.pillowapps.liqear.models.lastfm.LastfmChartModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class LastfmChartsViewerActivity extends PagerResultActivity {
    public static final int TOP_TRACKS = 0;
    public static final int TOP_ARTISTS = 1;
    //    public static final int HYPED_TRACKS = 1;
//    public static final int MOST_LOVED = 4;
    public static final int PAGES_NUMBER = 2;

    @Inject
    LastfmChartModel chartsModel;
    @Inject
    PreferencesScreenManager preferencesManager;

    public static Intent startIntent(Context context) {
        return new Intent(context, LastfmChartsViewerActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LBApplication.get(this).applicationComponent().inject(this);

        setContentView(R.layout.viewer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.charts);
        }
        initUi();
    }

    private void initUi() {
        initViewPager();
        int defaultIndex = TOP_TRACKS;
        changeViewPagerItem(defaultIndex);
    }

    private void initViewPager() {
        List<Page> pages = new ArrayList<>(PAGES_NUMBER);
//        pages.add(createHypedArtistsPage());
//        pages.add(createHypedTracksPage());
        pages.add(createTopTracksPage());
        pages.add(createTopArtistsPage());
//        pages.add(createLovedTracksPage());
        setPages(pages);
        final PagesPagerAdapter adapter = new PagesPagerAdapter(pages);
        injectViewPager(adapter);
        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int index) {
                invalidateOptionsMenu();
                ViewerPage viewer = getViewer(index);
                if (viewer.isNotLoaded()) {
                    viewer.showProgressBar(true);
                    viewer.onLoadMore();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    private ViewerPage createLovedTracksPage() {
        final LastfmTracksViewerPage viewer = new LastfmTracksViewerPage(this,
                View.inflate(this, R.layout.list_tab, null),
                R.string.loved_tracks);
        viewer.setOnLoadMoreListener(() -> getMostLoved(viewer)
        );
        viewer.setItemClickListener(trackClickListener);
        viewer.setItemLongClickListener(trackLongClickListener);
        addViewer(viewer);
        return viewer;
    }

    private ViewerPage createTopArtistsPage() {
        final LastfmArtistViewerPage viewer = new LastfmArtistViewerPage(this,
                View.inflate(this, R.layout.list_tab, null),
                R.string.top_artists, preferencesManager.isDownloadImagesEnabled());
        viewer.setOnLoadMoreListener(() -> getTopArtists(viewer));
        viewer.setItemClickListener(artistClickListener);
        addViewer(viewer);
        return viewer;
    }

    private ViewerPage createTopTracksPage() {
        final LastfmTracksViewerPage viewer = new LastfmTracksViewerPage(this,
                View.inflate(this, R.layout.list_tab, null),
                R.string.top_tracks);
        viewer.setOnLoadMoreListener(() -> getTopTracks(viewer));
        viewer.setItemClickListener(trackClickListener);
        viewer.setItemLongClickListener(trackLongClickListener);
        addViewer(viewer);
        return viewer;
    }

    private LastfmTracksViewerPage createHypedTracksPage() {
        final LastfmTracksViewerPage viewer = new LastfmTracksViewerPage(this,
                View.inflate(this, R.layout.list_tab, null),
                R.string.hyped_artists);
        viewer.setOnLoadMoreListener(() -> getHypedTracks(viewer));
        viewer.setItemClickListener(trackClickListener);
        viewer.setItemLongClickListener(trackLongClickListener);
        addViewer(viewer);
        return viewer;
    }

    private ViewerPage createHypedArtistsPage() {
        final LastfmArtistViewerPage viewer = new LastfmArtistViewerPage(this,
                View.inflate(this, R.layout.list_tab, null),
                R.string.hyped_artists, preferencesManager.isDownloadImagesEnabled());
        viewer.setOnLoadMoreListener(() -> getHypedArtists(viewer));
        viewer.setItemClickListener(artistClickListener);
        addViewer(viewer);
        return viewer;
    }

    private void changeViewPagerItem(int currentItem) {
        pager.setCurrentItem(currentItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        final int index = pager.getCurrentItem();
        switch (index) {
//            case HYPED_TRACKS:
//            case MOST_LOVED:
            case TOP_TRACKS: {
                inflater.inflate(R.menu.to_playlist_menu, menu);
            }
            break;
            default: {
                inflater.inflate(R.menu.empty_menu, menu);
            }
            break;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.to_playlist: {
                LastfmTracksViewerPage viewer = (LastfmTracksViewerPage) getViewer(pager.getCurrentItem());
                if (viewer.isNotLoaded()) return true;
                List<Track> items = viewer.getItems();
                addToMainPlaylist(items);
                Toast.makeText(LastfmChartsViewerActivity.this, R.string.added, Toast.LENGTH_SHORT).show();
            }
            return true;
            case R.id.save_as_playlist: {
                LastfmTracksViewerPage viewer = (LastfmTracksViewerPage) getViewer(pager.getCurrentItem());
                if (viewer.isNotLoaded()) return true;
                List<Track> items = viewer.getItems();
                saveAsPlaylist(items);
            }
            return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getMostLoved(final LastfmTracksViewerPage viewer) {
        chartsModel.getLovedTracksChart(getPageSize(),
                viewer.getPage(), new SimpleCallback<List<LastfmTrack>>() {
                    @Override
                    public void success(List<LastfmTrack> lastfmTracks) {
                        viewer.fill(lastfmTracks);
                    }

                    @Override
                    public void failure(String errorMessage) {
                        ErrorNotifier.showError(LastfmChartsViewerActivity.this, errorMessage);
                    }
                });
    }

    private void getTopArtists(final LastfmArtistViewerPage viewer) {
        chartsModel.getTopArtists(getPageSize(),
                viewer.getPage(), new SimpleCallback<List<LastfmArtist>>() {
                    @Override
                    public void success(List<LastfmArtist> lastfmArtists) {
                        viewer.fill(lastfmArtists);
                    }

                    @Override
                    public void failure(String errorMessage) {
                        ErrorNotifier.showError(LastfmChartsViewerActivity.this, errorMessage);
                    }
                });
    }

    private void getTopTracks(final LastfmTracksViewerPage viewer) {
        chartsModel.getTopTracksChart(getPageSize(),
                viewer.getPage(), new SimpleCallback<List<LastfmTrack>>() {
                    @Override
                    public void success(List<LastfmTrack> lastfmTracks) {
                        viewer.fill(lastfmTracks);
                    }

                    @Override
                    public void failure(String errorMessage) {
                        ErrorNotifier.showError(LastfmChartsViewerActivity.this, errorMessage);
                    }
                });
    }

    private void getHypedTracks(final LastfmTracksViewerPage viewer) {
        chartsModel.getHypedTracks(getPageSize(),
                viewer.getPage(), new SimpleCallback<List<LastfmTrack>>() {
                    @Override
                    public void success(List<LastfmTrack> lastfmTracks) {
                        viewer.fill(lastfmTracks);
                    }

                    @Override
                    public void failure(String errorMessage) {
                        ErrorNotifier.showError(LastfmChartsViewerActivity.this, errorMessage);
                    }
                });
    }

    private void getHypedArtists(final LastfmArtistViewerPage viewer) {
        chartsModel.getHypedArtists(getPageSize(),
                viewer.getPage(), new SimpleCallback<List<LastfmArtist>>() {
                    @Override
                    public void success(List<LastfmArtist> lastfmArtists) {
                        viewer.fill(lastfmArtists);
                    }

                    @Override
                    public void failure(String errorMessage) {
                        ErrorNotifier.showError(LastfmChartsViewerActivity.this, errorMessage);
                    }
                });
    }
}