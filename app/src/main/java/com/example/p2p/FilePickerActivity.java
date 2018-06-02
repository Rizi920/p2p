package com.example.p2p;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;

public class FilePickerActivity extends AppCompatActivity {
    TextView im;
    Button bn;
    public String file_name;
    BufferedInputStream bis;
    ObjectOutputStream oos;
    String filename1;
    Float file_size;
    Handler handler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_picker);
        showFileChooser();
        im = (TextView) findViewById(R.id.fileNameTextview);
        bn = (Button) findViewById(R.id.sendFilebutton);
        bn.setClickable(false);

    }







    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"), 1);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Get the Uri of the selected file
            Uri selectedImageUri = data.getData( );
            final String filePath = getPath( getApplicationContext(), selectedImageUri );
            filename1=filePath.substring(filePath.lastIndexOf("/")+1);
            Log.d("Picture Path", filePath);
            File f=new File(filePath);
            file_size = Float.parseFloat(String.valueOf(f.length()/1024));
            Log.d("file size...........",Float.toString(file_size)+"Kb");

            im.setText(filename1+" "+"("+file_size+" Kb"+")" );
            bn.setClickable(true);
            bn.setClickable(true);
            bn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Socket ClientSoc;



                    try
                    {
                        Send_file(filePath);
                        finish();
                        onBackPressed();

                    }
                    catch(Exception ex)
                    {
                        System.out.println("problem in sendsocket...");
                    }
                }
            });


        }

    }


    public void Send_file(String filename){
        File f=new File(filename);
        if(file_size<11000){
        if(!f.exists())
        {
            try {
                System.out.println("File not Exists...");

                return;
            }catch(Exception x){

            }
        }else{
            try {

                System.out.println("File found.");
                System.out.println("Sending file...");
                final String Msg = "Sending File "+filename1+" ..." ;
                FilePickerActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(FilePickerActivity.this,
                                Msg,
                                Toast.LENGTH_LONG).show();
                    }});


                ObjectOutputStream oos = new ObjectOutputStream(ClientAndServerThread.socketftp.getOutputStream());
                DataOutputStream d = new DataOutputStream(oos);
                d.writeUTF(filename1);
                byte[] bytes = new byte[(int) f.length()];
                bis = new BufferedInputStream(new FileInputStream(f));
                bis.read(bytes, 0, bytes.length);
                oos.writeObject(bytes);
                oos.flush();
                System.out.println("File sent successfully.");
                final String sentMsg = "File sent Successfully" ;
                FilePickerActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(FilePickerActivity.this,
                                sentMsg,
                                Toast.LENGTH_LONG).show();
                    }});




            }catch(Exception x){
                System.out.println("sending failed."+x.getMessage());
            }
        }
    }else{
            final String sentMsg = "FIle Size limit (10Mb) exceed.. Sorry" ;
            FilePickerActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(FilePickerActivity.this,
                            sentMsg,
                            Toast.LENGTH_LONG).show();
                }});
            finish();
            onBackPressed();
        }
    }



    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id)
                );

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        }
        finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }



    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }








}

