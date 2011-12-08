package com.steganography;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.steganography.F5.Extract;
import com.steganography.F5.JpegEncoder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.util.Log;

public class steganography extends Activity {
    /** Called when the activity is first created. */
    
    public static final String LOG_TAG = "Steganography";
    
    private static int TAKE_PICTURE = 1;
    private static int CONTACT_PICKER_RESULT = 2;
    private static int FILE_BROWSE = 3;

    private Uri capturedImageURI;
    private Uri receivedPictureFilePath;
    
    private String encryptionPassPhrase;
    private String decryptionPassPhrase;
    private String hiddenText;
    
    public void onCreate(Bundle savedInstanceState)
    {
        Log.v(LOG_TAG, "Inside onCreate");
        super.onCreate(savedInstanceState);    
        setContentView(R.layout.main);
    }
    
    public void captureEncrypt(View view)
    {
        Log.v(LOG_TAG, "Inside captureEncrypt");
        takePhoto();
    }
    
    public void Decrypt(View view)
    {
        Log.v(LOG_TAG, "Inside Decrypt");
        browseForImage();
    }
    
    private void takePhoto()
    {        
        Log.v(LOG_TAG, "Inside takePhoto");
        String fileName = "capture-" + System.currentTimeMillis() + ".jpg";
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory(), fileName);
 
        capturedImageURI = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageURI);
        startActivityForResult(intent, TAKE_PICTURE);
    }
    
    private void browseForImage()
    {
        Log.v(LOG_TAG, "Inside browseForImage");
        
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/jpeg");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, FILE_BROWSE);
    }
    
    private void promptForHiddenText()
    {
        final EditText input = new EditText(this);
        
        new AlertDialog.Builder(this)
        .setTitle("Enter the text you wish to hide")
        .setView(input)
        .setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                hiddenText = input.getText().toString();
                embedHiddenTextIntoPicture();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int whichButton) 
            {
                deleteCapturedImage();
            }
        }).show();
    }
    
    public void promptForEncryptionPassPhrase()
    {
        Log.v(LOG_TAG, "Inside promptForEncryptionPassPhrase");
        final EditText input = new EditText(this);
        
        new AlertDialog.Builder(this)
        .setTitle("Enter a Passphrase")
        .setView(input)
        .setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                encryptionPassPhrase = input.getText().toString();
                promptForHiddenText();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                deleteCapturedImage();
            }
        }).show();
    }

    public void promptForDecryptionPassPhrase()
    {
        Log.v(LOG_TAG, "Inside promptForDecryptionPassPhrase");
        final EditText input = new EditText(this);
        
        new AlertDialog.Builder(this)
        .setTitle("Enter a Passphrase")
        .setView(input)
        .setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                decryptionPassPhrase = input.getText().toString();
                extractHiddenTextFromPicture();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                deleteCapturedImage();
            }
        }).show();
    }
    
    private Bitmap loadDownScaledBitmap(Uri path)
    {
        Bitmap largeBitmap = BitmapFactory.decodeFile(path.getPath());
        
        // Set it to scale down by 50 percent
        Matrix matrix = new Matrix();
        matrix.postScale(0.10f, 0.10f);
        
        return Bitmap.createBitmap(largeBitmap, 0, 0, largeBitmap.getWidth(), 
                                   largeBitmap.getHeight(), matrix, false);
    }
    
    private void embedHiddenTextIntoPicture()
    {
        /* 
         * Manipulate picture here
         * Global variables to use:
         *    private Uri capturedImageURI => Contains the path to the captured image (capturedImageURI.getPath())
         *    private String encryptionPassPhrase => User's passphrase to encrypt the text into the image
         *    private String hiddenText => Hidden text that should be embedded in the captured images
         *    All the variables above should be populated and valid at this point
         */
        int quality    = 100; // For the sake of speed
        String comment = ""; // Irrelavent
        
        try {
            FileOutputStream dataOut = new FileOutputStream(new File("/sdcard/embeddedImage.jpg"));
          
            // Load the camera shot
            Log.v(LOG_TAG, "Starting load down scaled bitmap...");
            Bitmap image = loadDownScaledBitmap(capturedImageURI);
            
            // Embed the message
            JpegEncoder jpg = new JpegEncoder(image, quality, dataOut, comment);
            Log.v(LOG_TAG, "Starting compression...");
            jpg.Compress(hiddenText, encryptionPassPhrase);
            
            // Delete the original and change the resource to point to the embedded image
            Log.v(LOG_TAG, "File location '" + this.getCacheDir() + "/embeddedImage.tmp'");
            //File capturedImage = new File(capturedImageURI.getPath());
            //capturedImage.delete();
            capturedImageURI = Uri.parse("file:///sdcard/embeddedImage.jpg");
            
            // Send picture in MMS
            Log.v(LOG_TAG, "Sending MMS...");
            sendMMS();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void extractHiddenTextFromPicture()
    {
        Log.v(LOG_TAG, "Inside extractHiddenTextFromPicture");
        /* 
         * Manipulate picture here
         * Global variables to use:
         *    private File receivedPictureFilePath => The path to the image that needs to be encrypted
         *    private String decryptionPassPhrase => User's passphrase to encrypt the text into the image
         *    All the variables above should be populated and valid at this point
         */

        String decryptedText = "";
        FileInputStream imageReader;
        try {
            File file = new File(receivedPictureFilePath.getPath());
            imageReader = new FileInputStream(file);
            decryptedText = Extract.extract(imageReader, file.length(), decryptionPassPhrase);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        showDecryptedText(decryptedText);
    }
    
    private void showDecryptedText(String decryptedText)
    {
        Log.v(LOG_TAG, "Inside showDecryptedText");
        new AlertDialog.Builder(this)
        .setTitle("Decrypted Text")
        .setMessage(decryptedText)
        .setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                // Exit dialog
            }
        }).setNegativeButton("Delete Image", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                // delete browsed image
                deleteReceivedImage();
            }
        }).show();
    }
    
    private void sendMMS()
    {
        Log.v(LOG_TAG, "Inside sendMMS");
        
        Intent picMessageIntent = new Intent(android.content.Intent.ACTION_SEND);
        picMessageIntent.setType("image/jpeg");
        picMessageIntent.putExtra(Intent.EXTRA_STREAM, capturedImageURI);
        
        Intent htcIntent = new Intent("android.intent.action.SEND_MSG");
        htcIntent.setType("image/jpeg");
        htcIntent.putExtra(Intent.EXTRA_STREAM, capturedImageURI);
        
        Intent chooser = Intent.createChooser(picMessageIntent, "Send Method");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {htcIntent});
        startActivity(chooser);
    }
    
    private void deleteCapturedImage()
    {
        Log.v(LOG_TAG, "Inside deleteCapturedImage");
        File capturedImage = new File(capturedImageURI.getPath());
        capturedImage.delete();
    }
    
    private void deleteReceivedImage()
    {
        Log.v(LOG_TAG, "Inside deleteReceivedImage");
        File receivedImage = new File(receivedPictureFilePath.getPath());
        receivedImage.delete();        
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.v(LOG_TAG, "Inside onActivityResult");
        
        int i=1;
        if (requestCode == TAKE_PICTURE)
        {
            promptForEncryptionPassPhrase();
        }
        else if( requestCode == CONTACT_PICKER_RESULT)
        {
            deleteCapturedImage();
        }
        else if( requestCode == FILE_BROWSE )
        {
            // TheFilePath contains file path to the browsed file.
            if (resultCode==RESULT_OK && data!=null && data.getData()!=null) 
            {
                receivedPictureFilePath = data.getData();
                promptForDecryptionPassPhrase();                
            }
        }
    }
}