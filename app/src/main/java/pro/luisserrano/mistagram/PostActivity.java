package pro.luisserrano.mistagram;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import pro.luisserrano.mistagram.databinding.ActivityPostBinding;

public class PostActivity extends AppCompatActivity {

    ActivityPostBinding binding;

    Uri imageUri;
    String myUrl;
    StorageTask uploadTask;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        storageReference = FirebaseStorage.getInstance().getReference("Posts");

        binding.close.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent( pro.luisserrano.mistagram.PostActivity.this,MainActivity.class ) );
                finish();
            }
        } );

        binding.post.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        } );

        CropImage.activity()
                .setAspectRatio( 1,1 )
                .start(pro.luisserrano.mistagram.PostActivity.this);
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType( contentResolver.getType( uri ) );
    }

    private void uploadImage(){
        ProgressDialog progressDialog = showProgressDialog();

        if(imageUri!= null){
            final StorageReference filereference = storageReference.child( System.currentTimeMillis()+"."+getFileExtension( imageUri ));
            uploadTask = filereference.putFile( imageUri );

            uploadTask = filereference.putFile( imageUri );
            uploadTask.continueWithTask( new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return filereference.getDownloadUrl();
                }
            } ).addOnCompleteListener( new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

                        String postid = reference.push().getKey();
                        //Log.i("username",postid);
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put( "postid",postid );
                        hashMap.put( "postimage",myUrl);
                        hashMap.put("description",binding.description.getText().toString());
                        hashMap.put( "publisher", FirebaseAuth.getInstance().getCurrentUser().getUid() );

                        reference.child( postid ).setValue( hashMap );

                        progressDialog.dismiss();

                        startActivity( new Intent( pro.luisserrano.mistagram.PostActivity.this,MainActivity.class ) );
                        finish();
                    }else{
                        Toast.makeText( pro.luisserrano.mistagram.PostActivity.this,"Failed",Toast.LENGTH_SHORT ).show();
                    }
                }
            } ).addOnFailureListener( new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText( pro.luisserrano.mistagram.PostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT ).show();
                }
            } );
        }else{
            Toast.makeText( pro.luisserrano.mistagram.PostActivity.this,"No Image selected!",Toast.LENGTH_SHORT ).show();
        }
    }

    private ProgressDialog showProgressDialog(){
        final ProgressDialog progressDialog = new ProgressDialog( this );
        progressDialog.setMessage( "posting" );
        progressDialog.show();
        return progressDialog;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult( data );
            imageUri = result.getUri();

            binding.imageAdded.setImageURI( imageUri );
        }else {
            Toast.makeText( pro.luisserrano.mistagram.PostActivity.this,getString(R.string.wrong),Toast.LENGTH_SHORT ).show();
            startActivity( new Intent( pro.luisserrano.mistagram.PostActivity.this,MainActivity.class ) );
            finish();
        }
    }

}