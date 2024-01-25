package com.amg.reducenoise;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {


    private static String AD_UNIT_ID = "";
    private static final String TAG = "MyActivity";
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;
    private AdView adView;
    private FrameLayout adContainerView;
    private AtomicBoolean initialLayoutComplete = new AtomicBoolean(false);

    private String outputFolder;
    Operation current;

    int volume = 0;
    private ProgressDialog progressDialog;

    private boolean adIsLoading;
    private InterstitialAd interstitialAd;

    private int adsCount = 0;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AD_UNIT_ID = getString(R.string.banner1);

        adContainerView = findViewById(R.id.ad_view_container);

        // Log the Mobile Ads SDK version.

        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        this.preferences = defaultSharedPreferences;
        adsCount = preferences.getInt("ADS",0);

        googleMobileAdsConsentManager =
                GoogleMobileAdsConsentManager.getInstance(getApplicationContext());
        googleMobileAdsConsentManager.gatherConsent(
                this,
                consentError -> {
                    if (consentError != null) {
                        // Consent not obtained in current session.
                        Log.w(
                                TAG,
                                String.format("%s: %s", consentError.getErrorCode(), consentError.getMessage()));
                    }

                    if (googleMobileAdsConsentManager.canRequestAds()) {
                        initializeMobileAdsSdk();
                    }

                    if (googleMobileAdsConsentManager.isPrivacyOptionsRequired()) {
                        // Regenerate the options menu to include a privacy setting.
                        invalidateOptionsMenu();
                    }
                });

        // This sample attempts to load ads using consent obtained in the previous session.
        if (googleMobileAdsConsentManager.canRequestAds()) {
            initializeMobileAdsSdk();
        }

        // Since we're loading the banner based on the adContainerView size, we need to wait until this
        // view is laid out before we can get the width.
        adContainerView
                .getViewTreeObserver()
                .addOnGlobalLayoutListener(
                        () -> {
                            if (!initialLayoutComplete.getAndSet(true)
                                    && googleMobileAdsConsentManager.canRequestAds()) {
                                loadBanner();
                                if (adsCount % 3 == 0)
                                    loadInter();
                            }
                        });


        MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345")).build());

        if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VIDEO") != 0 &&ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_AUDIO") != 0 && ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != 0 && ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_MEDIA_VIDEO","android.permission.READ_MEDIA_AUDIO","android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 1234);
        } else {

            initializeOutput();

            initializeViews();
        }

    }

    private void loadBanner() {
        // Create a new ad view.
        adView = new AdView(this);
        adView.setAdUnitId(AD_UNIT_ID);
        adView.setAdSize(getAdSize());

        // Replace ad container with new ad view.
        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        // Start loading the ad in the background.
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return;
        }

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(
                this,
                new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {}
                });

        // Load an ad.
        if (initialLayoutComplete.get()) {
            //loadBanner();
        }
    }

    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = adContainerView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1234) {
            try {
                if (grantResults.length == 0 || grantResults[0] != 0) {
                    return;
                }
                initializeOutput();

                initializeViews();
            } catch (Exception unused) {
            }
        }
    }

    private void initializeViews() {

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Please wait..");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        findViewById(R.id.video_noise_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current = Operation.NoiseVideo;
                selectVideo();
            }
        });

        findViewById(R.id.audio_noise_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current = Operation.NoiseAudio;
                selectAudio();
            }
        });

        findViewById(R.id.volume_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current = Operation.SetVolume;
                selectVideo();
            }
        });

        findViewById(R.id.video_audio_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current = Operation.VideoToAudio;
                selectVideo();
            }
        });

        findViewById(R.id.output_layout).setOnClickListener(new View.OnClickListener() { // from class: com.amg.compressaudio.MainActivity.7
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                OutputsActivity.launch(MainActivity.this, new File(MainActivity.this.outputFolder));
            }
        });
    }

    void initializeOutput(){
        if (Build.VERSION.SDK_INT > 29) {
            this.outputFolder = getExternalFilesDir(null).getAbsolutePath() + "/ReduceNoise/";
        } else {
            this.outputFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ReduceNoise/";
        }
        File file = new File(this.outputFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    void showVolumeDialog(String input, String output,Uri mediaUri){
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.volume_dialog);
        EditText editInput = dialog.findViewById(R.id.editInput);
        dialog.findViewById(R.id.increase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = 1;
                try {
                    int value2 = Integer.parseInt(editInput.getText().toString());
                    value = value2 + 1;
                } catch (Exception e) {
                }
                editInput.setText(value + "");
            }
        });
        dialog.findViewById(R.id.decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = 1;
                try {
                    int value2 = Integer.parseInt(editInput.getText().toString());
                    value = value2 - 1;
                } catch (Exception e) {
                }
                editInput.setText(value + "");
            }
        });
        dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int volume = Integer.parseInt(editInput.getText().toString());
                String command = "-y -i "+ input+" -af \"volume="+volume+"dB\" "+output;
                executeCommand(command,mediaUri);
                dialog.dismiss();
                progressDialog.show();
            }
        });
        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(true);
        dialog.show();
    }

    private ActivityResultLauncher<Intent> selectVideoAct = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri selectedVideoUri = data.getData();
                        String selectedFilePath = RealPathUtil.getRealPath(MainActivity.this,selectedVideoUri);

                        if (selectedFilePath != null) {
                            String command = "";
                            String fileName = "";
                            String output = "";
                            String input = selectedFilePath.replace(" ","\' \'");
                            switch (current){
                                case NoiseVideo:
                                    progressDialog.show();
                                    fileName = RealPathUtil.getFileName(input);
                                    output = RealPathUtil.newFilePath("DeNoise_vid",fileName,outputFolder);
                                    command = "-y -i "+ input+" -af afftdn=nf=-25:nr=97 " + output;
                                    executeCommand(command,selectedVideoUri);
                                    break;
                                case NoiseAudio:
                                    progressDialog.show();
                                    fileName = RealPathUtil.getFileName(input);
                                    output = RealPathUtil.newFilePath("DeNoise_aud",fileName,outputFolder);
                                    command = "-y -i "+ input+" -af afftdn=nf=-25:nr=97 " + output;
                                    executeCommand(command,selectedVideoUri);
                                    break;
                                case VideoToAudio:
                                    progressDialog.show();
                                    fileName = RealPathUtil.getFileName(input);
                                    output = RealPathUtil.newFilePath("_aud",fileName,outputFolder,"mp3");
                                    command = "-y -i "+ input+" -q:a 0 -map a " + output;
                                    executeCommand(command,selectedVideoUri);
                                    break;
                                case SetVolume:
                                    fileName = RealPathUtil.getFileName(input);
                                    output = RealPathUtil.newFilePath("SetVolume_vid",fileName,outputFolder);
                                    showVolumeDialog(input,output,selectedVideoUri);
                                    break;
                            }
                        }
                    }
                }
            }
    );

    private void executeCommand(String command, Uri mediaUri) {

        final int duration = MediaPlayer.create(MainActivity.this, mediaUri).getDuration();
        Config.enableStatisticsCallback(new StatisticsCallback() {
            public void apply(Statistics newStatistics) {
                float parseFloat = (Float.parseFloat(String.valueOf(newStatistics.getTime())) / duration) * 100.0f;
                float f = parseFloat < 99.0f ? parseFloat : 100.0f;
                progressDialog.setMessage((int) f+"%");
            }
        });
        long executionId = FFmpeg.executeAsync(command, new ExecuteCallback() {

            @Override
            public void apply(final long executionId, final int returnCode) {
                progressDialog.dismiss();
                if (returnCode == RETURN_CODE_SUCCESS) {
                    showFinishDialog();
                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.i(Config.TAG, "Async command execution cancelled by user.");
                } else {
                    Log.i(Config.TAG, String.format("Async command execution failed with returnCode=%d.", returnCode));
                }
            }
        });
    }

    void selectVideo(){

        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        selectVideoAct.launch(Intent.createChooser(intent,"Select Video"));
    }

    void selectAudio(){

        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        selectVideoAct.launch(Intent.createChooser(intent,"Select Audio"));
    }

    void showFinishDialog(){
        AlertDialog.Builder title = new AlertDialog.Builder(MainActivity.this).setTitle(MainActivity.this.getString(R.string.completed));
        title.setMessage(MainActivity.this.getString(R.string.folder) + "\n" + MainActivity.this.outputFolder).setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.amg.compressaudio.MainActivity.CompressAudios.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if (adsCount % 3 == 0){
                    showInterstitial();
                }
            }
        }).show();
    }

    public void loadInter() {
        // Request a new ad if one isn't already loaded.
        if (adIsLoading || interstitialAd != null) {
            return;
        }
        adIsLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(
                this,
                getString(R.string.inter),
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        MainActivity.this.interstitialAd = interstitialAd;
                        adIsLoading = false;
                        Log.i(TAG, "onAdLoaded");
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        // Called when fullscreen content is dismissed.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        MainActivity.this.interstitialAd = null;
                                        Log.d("TAG", "The ad was dismissed.");
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        // Called when fullscreen content failed to show.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        MainActivity.this.interstitialAd = null;
                                        Log.d("TAG", "The ad failed to show.");
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        // Called when fullscreen content is shown.
                                        Log.d("TAG", "The ad was shown.");
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        interstitialAd = null;
                        adIsLoading = false;

                        String error =
                                String.format(
                                        java.util.Locale.US,
                                        "domain: %s, code: %d, message: %s",
                                        loadAdError.getDomain(),
                                        loadAdError.getCode(),
                                        loadAdError.getMessage());

                    }
                });
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise restart the game.
        if (interstitialAd != null) {
            interstitialAd.show(this);
            adsCount++;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("ADS",adsCount);
            editor.apply();
        } /*else {

            if (googleMobileAdsConsentManager.canRequestAds()) {
                loadInter();
            }
        }
        */
    }

    @Override
    public void onDestroy() {
        deleteTempFile();
        super.onDestroy();
    }

    private void deleteTempFile() {
        final File[] files = getCacheDir().listFiles();
        if (files != null) {
            for (final File file : files) {
                //if (file.getName().contains(TEMP_FILE)) {
                file.delete();
                Log.d("DELETE",file.getPath());
                //}
            }
        }
    }
}