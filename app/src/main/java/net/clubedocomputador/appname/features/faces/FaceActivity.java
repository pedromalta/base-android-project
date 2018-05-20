package net.clubedocomputador.appname.features.faces;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

import javax.inject.Inject;

import net.clubedocomputador.appname.R;
import net.clubedocomputador.appname.data.local.PreferencesHelper;
import net.clubedocomputador.appname.data.model.LoggedIn;
import net.clubedocomputador.appname.data.model.Login;
import net.clubedocomputador.appname.databinding.ActivityFaceBinding;
import net.clubedocomputador.appname.features.base.BaseActivity;
import net.clubedocomputador.appname.features.faces.widget.CameraSourcePreview;
import net.clubedocomputador.appname.features.faces.widget.FaceGraphic;
import net.clubedocomputador.appname.features.faces.widget.GraphicOverlay;
import net.clubedocomputador.appname.injection.component.ActivityComponent;
import net.clubedocomputador.appname.util.AppLogger;
import net.clubedocomputador.appname.util.Util;
import butterknife.BindView;


public class FaceActivity extends BaseActivity implements FaceMvpView {

    private static final String EXTRA_STUDENT_ID = "extra_student_id";
    private static final String EXTRA_INSTRUCTOR_ID = "extra_instructor_id";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 69;

    @Inject
    FacePresenter presenter;

    @Inject
    PreferencesHelper preferences;

    @BindView(R.id.shutter)
    FrameLayout shutter;

    @BindView(R.id.fade_camera)
    FrameLayout fadeCamera;

    @BindView(R.id.loading)
    ProgressBar progressBar;

    private CameraSource mCameraSource = null;

    @BindView(R.id.faceOverlay)
    GraphicOverlay mGraphicOverlay;

    @BindView(R.id.preview)
    CameraSourcePreview mPreview;

    private boolean loading = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                setupCameraPreview();
            }
        } else {
            setupCameraPreview();
        }



    }


    private void setupCameraPreview() {
        loading = false;
        fadeCamera.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .setProminentFaceOnly(true) // optimize for single, relatively large face
                .setTrackingEnabled(true) // enable face tracking
                .setClassificationType(/* eyes open and smile */ FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.FAST_MODE) // for one face this is OK
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());


        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            AppLogger.e("Face detector dependencies are not yet available.");
            Util.DialogFactory.createGenericErrorDialog(this, R.string.dialog_error_message_no_camera);
        }

        mCameraSource = new CameraSource.Builder(getApplicationContext(), detector)
                .setRequestedPreviewSize(1024, 768)
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();

        startCameraSource();


    }

    private void startCameraSource() {
        if (mCameraSource != null) {
            try {

                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                //Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private void takePicture() {

        mCameraSource.takePicture(
                () -> {
                    shutter.setVisibility(View.VISIBLE);
                    MediaActionSound sound = new MediaActionSound();
                    sound.play(MediaActionSound.SHUTTER_CLICK);
                    //Handler handler = new Handler();
                    //Runnable runnable = () -> shutter.setVisibility(View.GONE);
                    //handler.postDelayed(runnable, 500);

                },
                (bytes) -> {
                    shutter.setVisibility(View.GONE);
                    presenter.recognise(new Login(bytes));

                });

    }




    @Override
    public void recognized(LoggedIn loggedIn) {
        //Success result!
        setResult(RESULT_OK);
        Dialog dialog = Util.DialogFactory.createSimpleOkDialog(this,
                R.string.dialog_title_face,
                R.string.dialog_message_face
        );
        dialog.setOnDismissListener((d) -> {
            hideLoading();
            finish();
        });
        dialog.show();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        mPreview.release();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupCameraPreview();
                } else {
                    Util.DialogFactory.createGenericErrorDialog(this, R.string.dialog_error_message_no_camera);
                }
        }
    }

    @Override
    public int getLayout() {
        return R.layout.activity_face;
    }

    @Override
    protected void inject(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    protected void attachView() {
        presenter.attachView(this);
    }

    @Override
    protected void detachPresenter() {
        presenter.detachView();
    }

    public static Intent getStartIntent(Context context, String instructorId, String studentId) {
        Intent intent = new Intent(context, FaceActivity.class);
        intent.putExtra(EXTRA_INSTRUCTOR_ID, instructorId);
        intent.putExtra(EXTRA_STUDENT_ID, studentId);
        return intent;
    }

    public void showLoading() {
        loading = true;
        fadeCamera.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        //Hold new pictures being taken by 3s to avoid unnecessary taking pictures
        final Handler handler = new Handler();
        handler.postDelayed(() -> loading = false, 3000);
        fadeCamera.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    public void showError(Throwable error) {

    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;
        private int elapsedFrameCount = 0;
        private static final int frameCountForPicture = 10;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
            elapsedFrameCount++;
            if (!loading && elapsedFrameCount > frameCountForPicture) {
                elapsedFrameCount = 0;
                takePicture();
            }

        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }

    }


}

