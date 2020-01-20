package com.tutsplus.code.android.tutsplusupload;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static com.tutsplus.code.android.tutsplusupload.R.drawable.common_google_signin_btn_icon_dark;

public class MainActivity extends AppCompatActivity {

  //Variables
  private Button btnChoose, btnUpload, downloadBtn;
  private ImageView imageView;
  public TextView downloadURL;


  private Uri filePath;

  private final int PICK_IMAGE_REQUEST = 10;

  //Firebase
  FirebaseStorage storage;
  StorageReference storageReference;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    storage = FirebaseStorage.getInstance();
    storageReference = storage.getReference();

    //Initialize Views
    btnChoose = (Button) findViewById(R.id.btnChoose);
    btnUpload = (Button) findViewById(R.id.btnUpload);
    imageView = (ImageView) findViewById(R.id.imgView);
    downloadBtn = (Button) findViewById(R.id.downloadBtn);
    downloadURL = (TextView) findViewById(R.id.downloadURL);

    btnChoose.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        chooseImage();
      }
    });

    btnUpload.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        uploadImage();
      }
    });
  }

  private void chooseImage() {
    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
            && data != null && data.getData() != null )
    {
      filePath = data.getData();
      try {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
        imageView.setImageBitmap(bitmap);
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }

  public void uploadImage() {

    if(filePath != null)
    {
      final ProgressDialog progressDialog = new ProgressDialog(this);
      progressDialog.setTitle("Uploading...");
      progressDialog.show();

      final StorageReference ref = storageReference.child("images/"+ UUID.randomUUID().toString());
      ref.putFile(filePath)
              .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                  progressDialog.dismiss();
                  Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                  imageView.setImageResource(0);
                  //TODO; download URL getting
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                          final String downURL = uri.toString();
                          downloadBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                              Picasso.get().load(downURL).into(imageView);
                            }
                          });

                        }
                    });
                }
              })
              .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                  progressDialog.dismiss();
                  Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
              })
              .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                  double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                          .getTotalByteCount());
                  progressDialog.setMessage("Uploaded "+(int)progress+"%");
                }
              });
    }
  }

}
