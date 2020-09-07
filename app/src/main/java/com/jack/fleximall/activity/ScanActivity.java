package com.jack.fleximall.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.jack.fleximall.R;
import com.jack.fleximall.ScanCommunication;
import com.jack.fleximall.barcode.barcodescanning.BarcodeScanningProcessor;
import com.jack.fleximall.barcode.common.CameraSource;
import com.jack.fleximall.barcode.common.CameraSourcePreview;
import com.jack.fleximall.barcode.common.GraphicOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScanActivity extends AppCompatActivity implements ScanCommunication {

    private CameraSource cameraSource;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        preview = (CameraSourcePreview)findViewById(R.id.scan_preview);
        if (preview == null)
            Toast.makeText(this, "preview not available", Toast.LENGTH_SHORT).show();

        graphicOverlay = (GraphicOverlay) findViewById(R.id.scan_overlay);
        if (graphicOverlay == null)
            Toast.makeText(this, "graphic overlay not available", Toast.LENGTH_SHORT).show();

        if (allPermissionsGranted()) {
            createCameraSource();
            startCameraSource();
        } else {
            getRuntimePermissions();
        }
    }

    @Override
    public void onBarcodeDetected(String barcode) {
        closeCamera();
        Intent data = new Intent();
        data.setData(Uri.parse(barcode));
        setResult(RESULT_OK,data);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (allPermissionsGranted())
            startCameraSource();
    }

    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }

        cameraSource.setMachineLearningFrameProcessor(new BarcodeScanningProcessor(this));
    }

    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Toast.makeText(this, "Preview is null", Toast.LENGTH_SHORT).show();
                }
                if (graphicOverlay == null) {
                    Toast.makeText(this, "graphOverlay is null", Toast.LENGTH_SHORT).show();
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Toast.makeText(this, "Unable to start camera source", Toast.LENGTH_SHORT).show();
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info = this.getPackageManager().getPackageInfo(this.getPackageName(),
                    PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (allPermissionsGranted()) {
            createCameraSource();
            startCameraSource();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void closeCamera(){

        if (cameraSource!=null)
            cameraSource.release();
        if (preview!=null)
            preview.release();
        if (graphicOverlay!=null)
            graphicOverlay.clear();
    }
}