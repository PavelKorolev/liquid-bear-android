package com.pillowapps.liqear.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.pillowapps.liqear.R;
import com.pillowapps.liqear.connection.GetResponseCallback;
import com.pillowapps.liqear.connection.Params;
import com.pillowapps.liqear.connection.QueryManager;
import com.pillowapps.liqear.connection.ReadyResult;
import com.pillowapps.liqear.helpers.AuthActivityAdapter;
import com.pillowapps.liqear.helpers.AuthorizationInfoManager;
import com.pillowapps.liqear.helpers.Constants;
import com.pillowapps.liqear.helpers.PreferencesManager;
import com.pillowapps.liqear.helpers.Utils;
import com.pillowapps.liqear.models.ErrorResponseLastfm;
import com.pillowapps.liqear.models.ErrorResponseVk;
import com.pillowapps.liqear.models.Session;
import com.pillowapps.liqear.models.User;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.List;

public class AuthActivity extends TrackedActivity {
    private static final int VK_INDEX = 0;
    private static final int LASTFM_INDEX = 1;
    private View vkTab;
    private ViewPager pager;
    private View lastfmTab;
    private TitlePageIndicator indicator;
    private Button authorizeVkButton;
    private Button authorizeLastfmButton;
    private EditText loginLastfmEditText;
    private EditText passwordLastfmEditText;
    private View authVkPanel;
    private View authLastfmPanel;
    private TextView vkNameTextView;
    private TextView lastfmNameTextView;
    private ImageView avatarVkImageView;
    private ImageView avatarLastfmImageView;
    private DisplayImageOptions options = new DisplayImageOptions.Builder()
            .cacheOnDisc()
            .bitmapConfig(Bitmap.Config.RGB_565)
            .displayer(new FadeInBitmapDisplayer(500))
            .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
            .build();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private boolean firstStart;
    private TextView errorVkTextView;
    private TextView errorLastfmTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_layout);
        setTitle(R.string.authorizations);
        firstStart = getIntent().getBooleanExtra(Constants.FIRST_START, false);
        initViewPager();
        initUi();
        initListeners();
        showSaves();
        int authProblemsState = getIntent().getIntExtra(Constants.AUTH_PROBLEMS, 0);
        switch (authProblemsState) {
            case 1 /*Lastfm*/:
                pager.setCurrentItem(LASTFM_INDEX);
                errorLastfmTextView.setVisibility(View.VISIBLE);
                break;
            case 2 /*VK*/:
                pager.setCurrentItem(VK_INDEX);
                errorVkTextView.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
        if (!firstStart) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void showSaves() {
        if (AuthorizationInfoManager.isAuthorizedOnVk()) {
            imageLoader.displayImage(AuthorizationInfoManager.getVkAvatar(),
                    avatarVkImageView, options);
            vkNameTextView.setText(AuthorizationInfoManager.getVkName());
            authVkPanel.setVisibility(View.VISIBLE);
        }
        if (AuthorizationInfoManager.isAuthorizedOnLastfm()) {
            lastfmNameTextView.setText(AuthorizationInfoManager.getLastfmName());
            loginLastfmEditText.setText(AuthorizationInfoManager.getLastfmName());
            authLastfmPanel.setVisibility(View.VISIBLE);
            imageLoader.displayImage(AuthorizationInfoManager.getLastfmAvatar(),
                    avatarLastfmImageView, options);
        }
    }

    private void initViewPager() {
        final LayoutInflater inflater = LayoutInflater.from(this);
        final List<View> views = new ArrayList<View>();
        vkTab = inflater.inflate(R.layout.auth_vk_layout, null);
        views.add(vkTab);
        lastfmTab = inflater.inflate(R.layout.auth_lastfm_layout, null);
        views.add(lastfmTab);
        pager = (ViewPager) findViewById(R.id.pager);
        AuthActivityAdapter adapter = new AuthActivityAdapter(views);
        pager.setAdapter(adapter);
        indicator = (TitlePageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        indicator.setCurrentItem(0);
        indicator.setFooterColor(getResources().getColor(R.color.blue_darken));
        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private void initUi() {
        authorizeVkButton = (Button) vkTab.findViewById(R.id.authorize_vk_button);
        authorizeLastfmButton = (Button) lastfmTab.findViewById(R.id.authorize_lastfm_button);
        authVkPanel = vkTab.findViewById(R.id.auth_panel);
        loginLastfmEditText = (EditText) lastfmTab.findViewById(R.id.login_edit_text);
        passwordLastfmEditText = (EditText) lastfmTab.findViewById(R.id.password_edit_text);
        authLastfmPanel = lastfmTab.findViewById(R.id.auth_panel);
        vkNameTextView = (TextView) vkTab.findViewById(R.id.name_text_view);
        lastfmNameTextView = (TextView) lastfmTab.findViewById(R.id.name_text_view);
        avatarVkImageView = (ImageView) vkTab.findViewById(R.id.avatar);
        avatarLastfmImageView = (ImageView) lastfmTab.findViewById(R.id.avatar);
        errorVkTextView = (TextView) vkTab.findViewById(R.id.auth_error_text_view);
        errorLastfmTextView = (TextView) lastfmTab.findViewById(R.id.auth_error_text_view);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == Constants.AUTH_VK_REQUEST) {
            QueryManager.getInstance().getUsersInfoVk(AuthorizationInfoManager.getVkUserId(),
                    new GetResponseCallback() {
                @Override
                public void onDataReceived(ReadyResult result) {
                    if (checkForError(result, Params.ApiSource.VK)) {
                        return;
                    }
                    authVkPanel.setVisibility(View.VISIBLE);
                    List<User> users = (List<User>) result.getObject();
                    User user = users.get(0);
                    AuthorizationInfoManager.setVkAvatar(user.getImageUrl());
                    imageLoader.displayImage(user.getImageUrl(), avatarVkImageView, options);
                    vkNameTextView.setText(user.getName());
                    AuthorizationInfoManager.setVkName(user.getName());
                    invalidateOptionsMenu();
                    if (firstStart && AuthorizationInfoManager.isAuthorizedOnLastfm()) {
                        Utils.startMainActivity(AuthActivity.this);
                        finish();
                    }
                }
            });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initListeners() {
        authorizeVkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorVkTextView.setVisibility(View.GONE);
                AuthorizationInfoManager.signOutVk();
                authVkPanel.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(AuthActivity.this, LoginActivity.class);
                startActivityForResult(intent, Constants.AUTH_VK_REQUEST);
            }
        });
        authorizeLastfmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorLastfmTextView.setVisibility(View.GONE);
                AuthorizationInfoManager.signOutLastfm();
                authLastfmPanel.setVisibility(View.INVISIBLE);
                loginLastfmEditText.setVisibility(View.VISIBLE);
                passwordLastfmEditText.setVisibility(View.VISIBLE);
                QueryManager.getInstance().getMobileSession(loginLastfmEditText.getText().toString(),
                        passwordLastfmEditText.getText().toString(),
                        new GetResponseCallback() {
                            @Override
                            public void onDataReceived(ReadyResult result) {
                                if (result.isOk()) {
                                    Session session = (Session) result.getObject();
                                    String name = session.getName();
                                    SharedPreferences.Editor editor = PreferencesManager
                                            .getLastfmPreferences(AuthActivity.this).edit();
                                    editor.putString(Constants.LASTFM_NAME, name);
                                    editor.putString(Constants.LASTFM_KEY, session.getKey());
                                    editor.commit();
                                    lastfmNameTextView.setText(name);
                                    authLastfmPanel.setVisibility(View.VISIBLE);
                                    QueryManager.getInstance().getUserInfo(name, new GetResponseCallback() {
                                        @Override
                                        public void onDataReceived(ReadyResult result) {
                                            if (result.isOk()) {
                                                String url = (String) result.getObject();
                                                AuthorizationInfoManager.setLastfmAvatar(url);
                                                imageLoader.displayImage(url,
                                                        avatarLastfmImageView, options);
                                            }
                                        }
                                    });
                                    invalidateOptionsMenu();
                                    if (firstStart && AuthorizationInfoManager.isAuthorizedOnVk()) {
                                        finish();
                                        Utils.startMainActivity(AuthActivity.this);
                                    }
                                } else {
                                    ErrorResponseLastfm errorResponse =
                                            (ErrorResponseLastfm) result.getObject();
                                    Toast.makeText(AuthActivity.this, errorResponse.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                );
            }
        });
    }

    private boolean checkForError(ReadyResult result, Params.ApiSource source) {
        final boolean error = !result.isOk();
        int errorCode = -1;
        if (result.getObject() instanceof ErrorResponseLastfm) {
            errorCode = ((ErrorResponseLastfm) result.getObject()).getError();
        } else if (result.getObject() instanceof ErrorResponseVk) {
            errorCode = ((ErrorResponseVk) result.getObject()).getErrorCode();
        }
        if (error && errorCode != 0) {
            Utils.showErrorDialog(result, AuthActivity.this, source);
        }
        return error;
    }

    private void signUpVk() {
        String url = "http://vk.com/join";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void signUpLastfm() {
        String url = "http://www.lastfm.ru/join";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getSupportMenuInflater();
        switch (pager.getCurrentItem()) {
            case VK_INDEX: {
                if (AuthorizationInfoManager.isAuthorizedOnVk()) {
                    inflater.inflate(R.menu.menu_auth, menu);
                } else {
                    inflater.inflate(R.menu.menu_sign_up, menu);
                }
            }
            break;
            case LASTFM_INDEX:
                if (AuthorizationInfoManager.isAuthorizedOnLastfm()) {
                    inflater.inflate(R.menu.menu_auth, menu);
                } else {
                    inflater.inflate(R.menu.menu_sign_up, menu);
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        int itemId = item.getItemId();
        int currentPage = pager.getCurrentItem();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
            case R.id.sign_up_button: {
                if (currentPage == VK_INDEX) {
                    signUpVk();
                } else if (currentPage == LASTFM_INDEX) {
                    signUpLastfm();
                }
            }
            return true;
            case R.id.skip_button: {
                AuthorizationInfoManager.skipAuth();
                Utils.startMainActivity(AuthActivity.this);
                finish();
            }
            return true;
            case R.id.sign_out_button: {
                if (currentPage == VK_INDEX) {
                    AuthorizationInfoManager.signOutVk();
                    authVkPanel.setVisibility(View.INVISIBLE);
                } else if (currentPage == LASTFM_INDEX) {
                    AuthorizationInfoManager.signOutLastfm();
                    authLastfmPanel.setVisibility(View.INVISIBLE);
                    loginLastfmEditText.setVisibility(View.VISIBLE);
                    passwordLastfmEditText.setVisibility(View.VISIBLE);
                }
                invalidateOptionsMenu();
            }
            return true;
            default:
                break;
        }
        return false;
    }
}
