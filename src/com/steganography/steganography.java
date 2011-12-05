package com.steganography;

import java.io.File;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

public class steganography extends Activity {
    /** Called when the activity is first created. */
    
    public static final String LOG_TAG = "Steganography";
    
    private static int TAKE_PICTURE = 1;
    private static int CONTACT_PICKER_RESULT = 2;
    private static int FILE_BROWSE = 3;
    private Uri outputFileUri;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
    }
    
    public void caputreEncrypt(View view) {
        takePhoto();
    }
    
    public void Decrypt(View view)
    {
        browseForImage();
    }
    
    private void browseForImage()
    {
        Intent intent = new Intent("org.openintents.action.PICK_FILE");
        startActivityForResult(intent, FILE_BROWSE);
    }
    private void takePhoto()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory(), "test.jpg");
 
        outputFileUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent, TAKE_PICTURE);
    }
    @Override
    //outputFileUri.uriString
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
 
        if (requestCode == TAKE_PICTURE)
        {
            {
                int i;
                Intent sendIntent = new Intent(Intent.ACTION_SEND); 
                sendIntent.putExtra("sms_body", "some text"); 
                sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(outputFileUri.toString()));
                sendIntent.setType("image/png");  
                startActivityForResult(sendIntent, CONTACT_PICKER_RESULT);
            }
        }
        else if( requestCode == CONTACT_PICKER_RESULT)
        {
            
        }
        else if( requestCode == FILE_BROWSE )
        {
            // TheFilePath contains filepath to the browsed file.
            if (resultCode==RESULT_OK && data!=null && data.getData()!=null) {
                String theFilePath = data.getData().getPath();
                
            }
        }
    }
}