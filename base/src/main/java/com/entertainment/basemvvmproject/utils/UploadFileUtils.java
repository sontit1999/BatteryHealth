package com.entertainment.basemvvmproject.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class UploadFileUtils {

    public static final int MAX_IMAGE_SIZE = 1200;

    public static Bitmap rotateImage(Bitmap source, float angle) {

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public static Bitmap resizeImage(Context context, Uri uri, final int requiredSize, boolean isPickedFromGallery) throws FileNotFoundException {

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExifInterface exif; // handle image auto rotate when capturing image from camera
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                exif = new ExifInterface(inputStream);
            } else {
                exif = new ExifInterface(uri.getPath());
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Bitmap rotatedBitmap;
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;
            }
            bitmap = rotatedBitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }

        float ratio = Math.min(
                (float) requiredSize / bitmap.getWidth(),
                (float) requiredSize / bitmap.getHeight());
        int width = Math.round((float) ratio * bitmap.getWidth());
        int height = Math.round((float) ratio * bitmap.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, width,
                height, true);

        return newBitmap;
    }

    public static MultipartBody.Part buildMultiPartFromBitmap(Context context, Bitmap bitmap, String nameFormData) { // convert to file in order to send multipart to server
        File filesDir = context.getFilesDir();
        File imageFile = new File(filesDir, "image" + System.currentTimeMillis() + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();

            RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part part = MultipartBody.Part.createFormData(nameFormData, imageFile.getName(), fileReqBody);

            return part;

        } catch (Exception e) {
            Log.e(context.getClass().getSimpleName(), "Error writing bitmap", e);
        }
        return null;
    }

    public static Bitmap getResizedImage(Activity activity, Intent data, Uri photoURI, boolean isPickedFromGallery) {

        if (isPickedFromGallery) {
            List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(data, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                activity.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
        try {
            return UploadFileUtils.resizeImage(activity, photoURI, MAX_IMAGE_SIZE, isPickedFromGallery);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File createImageFile(Context context) throws IOException {
        String imageFileName = "capture";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".png",
                storageDir
        );
        return image;
    }
}
