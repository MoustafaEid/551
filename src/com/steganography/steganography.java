package com.steganography;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

import com.steganography.F5.Extract;
import com.steganography.F5.JpegEncoder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

    private Uri capturedImageURI;
    private String receivedPictureFilePath;
    
    private String encryptionPassPhrase;
    private String decryptionPassPhrase;
    private String hiddenText;
    
    /* Method that creates the main view */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);    
        setContentView(R.layout.main);
    }
    
    /* This method is triggered when the Capture and Encrypt button is clicked */
    public void caputreEncrypt(View view)
    {
        takePhoto();
    }
    
    /* This method is triggered when the Decrypt button is clicked */
    public void Decrypt(View view)
    {
        browseForImage();
    }
    
    /* This method triggers the camera to start taking a picture, saving it into the SD card with the
     * name "capture-<ms>.jpg" where <ms> is the the difference, measured in milliseconds, between the current time and 
     * midnight, January 1, 1970 UTC. To avoid overwriting another file with the same name
     */
    private void takePhoto()
    {        
        String fileName = "capture-" + System.currentTimeMillis() + ".jpg";
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory(), fileName);
 
        capturedImageURI = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageURI);
        startActivityForResult(intent, TAKE_PICTURE);
    }
    
    /* This method triggers the intent for a file manager to start browsing for a 
     * JPEG image to decrypt it
     */
    private void browseForImage()
    {
        Intent intent = new Intent("org.openintents.action.PICK_FILE");
        startActivityForResult(intent, FILE_BROWSE);
    }
    
    /* This method display a dialog that prompts the user for the text to be hidden in the picture */
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
                // User Cancelled the action, delete the captured image as there is no use for it.
                deleteCapturedImage();
            }
        }).show();
    }
    
    /* This method display a dialog that prompts the user for a passphrase to use to hide the text
     * inside the captured image */
    public void promptForEncryptionPassPhrase()
    {
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
                // User Cancelled the action, delete the captured image as there is no use for it.
                deleteCapturedImage();
            }
        }).show();
    }

    /* This method display a dialog that prompts the user for the passphrase that was used to encrypt text withing the
     * the selected image. The passphrase will be used to decrypt the text */
    public void promptForDecryptionPassPhrase()
    {
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
                // User cancelled the action, the image has not been decrypted yet. Do nothing.
            }
        }).show();
    }
    
    /* This method encrypts the text into the captured picture */
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
        int quality    = 80; // Default for compression ratio
        String comment = ""; // Irrelavent
        
        // Load the camera shot
        try {
            FileOutputStream dataOut = new FileOutputStream(capturedImageURI.toString()+".out");
            
            Bitmap image = BitmapFactory.decodeFile(capturedImageURI.getPath());
            
            // Embed the message
            JpegEncoder jpg = new JpegEncoder(image, quality, dataOut, comment);
            jpg.Compress(hiddenText, encryptionPassPhrase);
            
            // Delete the original and change the resource to point to the embedded image
            File capturedImage = new File(capturedImageURI.getPath());
            capturedImage.delete();
            capturedImageURI = Uri.parse(capturedImageURI.toString()+".out");
            
            // send picture
            sendImage();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /* This method extracts hidden text from a selected image using the passphrase that used to encrypt the text in
     * the image */
    private void extractHiddenTextFromPicture()
    {
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
            imageReader = new FileInputStream(receivedPictureFilePath);
            Extract.extract(imageReader, receivedPictureFilePath.length(), decryptedText, decryptionPassPhrase);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        showDecryptedText(decryptedText);
    }
    
    /* This method displays the extracted text from a selected image to the user */
    private void showDecryptedText(String decryptedText)
    {
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
    
    
    /* This method prompts the user to send captured image with the encrypted text */
    private void sendImage()
    {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);  
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(capturedImageURI.toString()));
        sendIntent.setType("image/jpg");  
        startActivityForResult(sendIntent, CONTACT_PICKER_RESULT);
    }
    
    /* This method deletes the captured image from the SD card */
    private void deleteCapturedImage()
    {
        File capturedImage = new File(capturedImageURI.getPath());
        capturedImage.delete();
    }
    
    /* This method deletes the received image */
    private void deleteReceivedImage()
    {
        File receivedImage = new File(receivedPictureFilePath);
        receivedImage.delete();        
    }
    @Override
    //outputFileUri.uriString
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
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
                receivedPictureFilePath = data.getData().getPath();
                promptForDecryptionPassPhrase();                
            }
        }
    }
}