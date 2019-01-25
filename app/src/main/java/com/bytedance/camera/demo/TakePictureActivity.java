package com.bytedance.camera.demo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.bytedance.camera.demo.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.bytedance.camera.demo.utils.Utils.NUM_180;
import static com.bytedance.camera.demo.utils.Utils.NUM_270;
import static com.bytedance.camera.demo.utils.Utils.NUM_90;

public class TakePictureActivity extends AppCompatActivity {

    private ImageView imageView;
    private File imgFile;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static int REQUEST_PERSSION_CODE=1;
    private static String[] PERMISSION_STORAGE={
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    private static final int REQUEST_EXTERNAL_STORAGE = 101;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);
        imageView = findViewById(R.id.img);
        findViewById(R.id.btn_picture).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(TakePictureActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(TakePictureActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //todo 在这里申请相机、存储的权限
                if (ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,PERMISSION_STORAGE,REQUEST_PERSSION_CODE);
                }
                if (ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,PERMISSION_STORAGE,REQUEST_PERSSION_CODE);
                }
                takePicture();
            } else {
                takePicture();
            }
        });

    }

    private void takePicture() {
        //todo 打开相机
        Intent takePictureIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imgFile = Utils.getOutputMediaFile(Utils.MEDIA_TYPE_IMAGE);
        if(imgFile!=null){
            Uri fileUri;
            if(Build.VERSION.SDK_INT>=24) {
               fileUri = FileProvider.getUriForFile(this, "com.bytedance.camera.demo", imgFile);
            }
            else {
                fileUri= Uri.fromFile(imgFile);
            }
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);
            startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK ) {
            try {
                setPic();
            } catch (Exception e) {
                e.printStackTrace();
            }
//            Bundle extra = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extra.get("data");
//            imageView.setImageBitmap(imageBitmap);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setPic() throws Exception {
        //todo 根据imageView裁剪
        int targetH = imageView.getMaxHeight();
        int targetW = imageView.getMaxWidth();
        //todo 根据缩放比例读取文件，生成Bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgFile.getAbsolutePath(),bmOptions);
        int photoH=bmOptions.outHeight;
        int photoW=bmOptions.outWidth;
        int scaleFactor=Math.min(photoH/targetH,photoW/targetW);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable=true;

        Bitmap bmp=BitmapFactory.decodeFile(imgFile.getAbsolutePath(),bmOptions);
        //todo 如果存在预览方向改变，进行图片旋转
        //todo 如果存在预览方向改变，进行图片旋转
        rotataImage(bmp,imgFile.getAbsolutePath());

        imageView.setImageBitmap(bmp);
    }

    public static Bitmap rotataImage(Bitmap bitmap,String path) throws Exception{
        ExifInterface srcExif = new ExifInterface(path);
        Matrix matrix=new Matrix();
        int angle=0;
        int orientation = srcExif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
        switch (orientation){
            case ExifInterface.ORIENTATION_ROTATE_90:
                angle=NUM_90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                angle=NUM_180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                angle=NUM_270;
                break;
            default :
                    break;
        }
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                //todo 判断权限是否已经授予
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    takePicture();
                }
                break;
            }
        }
    }
}
