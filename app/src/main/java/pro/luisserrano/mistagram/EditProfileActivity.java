package pro.luisserrano.mistagram;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import pro.luisserrano.mistagram.model.User;
import pro.luisserrano.mistagram.ui.ProfileFragment;

import static java.security.AccessController.getContext;

public class EditProfileActivity extends AppCompatActivity {

    ImageView close, image_profile;
    TextView save, tv_change;
    MaterialEditText fullname, username, bio;

    FirebaseUser firebaseUser;

    private Uri mImageUri;
    private StorageTask uploadTask;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_edit_profile );

        close = findViewById( R.id.close );
        image_profile = findViewById( R.id.image_profile );
        save = findViewById( R.id.save );
        tv_change = findViewById( R.id.tv_change );
        fullname = findViewById( R.id.fullname );
        username = findViewById( R.id.username );
        bio = findViewById( R.id.bio );

        //TODO(29): Inicializar firebaseUser al usuario actual de la base de datos
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("uploads");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child( firebaseUser.getUid() );
        reference.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User user = snapshot.getValue( User.class );
                //TODO(30): Mostrar los datos de user en los campos de la vista DONE
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(image_profile);
                username.setText(user.getUsername());
                fullname.setText(user.getFullname());
                bio.setText(user.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );

        close.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        } );

        tv_change.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio( 1,1 )
                        .setCropShape( CropImageView.CropShape.OVAL )
                        .start( pro.luisserrano.mistagram.EditProfileActivity.this );
            }
        } );
        image_profile.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio( 1,1 )
                        .setCropShape( CropImageView.CropShape.OVAL )
                        .start( pro.luisserrano.mistagram.EditProfileActivity.this );
            }
        } );

        save.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(fullname.getText().toString(),
                        username.getText().toString(),
                        bio.getText().toString());
                finish();


            }
        } );

    }
    private void updateProfile(String fullname,String username, String bio){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child( firebaseUser.getUid() );
        HashMap<String,Object>hashMap = new HashMap<>();
        hashMap.put( "fullname",fullname );
        hashMap.put( "username",username );
        hashMap.put( "bio",bio );

        reference.updateChildren( hashMap );

    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType( contentResolver.getType( uri ) );
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog( this );
        pd.setMessage( "Uploading" );
        pd.show();

        if(mImageUri!=null){
            final StorageReference fileReference = storageRef.child( System.currentTimeMillis() + "."+ getFileExtension( mImageUri ));

            uploadTask = fileReference.putFile( mImageUri );
            uploadTask.continueWithTask( new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {

                    if(!task.isSuccessful()){
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            } ).addOnCompleteListener( new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){

                        Uri downloadUri = task.getResult();
                        String myUrl = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child( firebaseUser.getUid() );

                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put( "imageurl",""+myUrl );

                        reference.updateChildren( hashMap );
                        pd.dismiss();
                    }else{
                        Toast.makeText( pro.luisserrano.mistagram.EditProfileActivity.this,"Failed",Toast.LENGTH_SHORT ).show();
                    }
                }
            } ).addOnFailureListener( new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText( pro.luisserrano.mistagram.EditProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT ).show();
                }
            } );
        } else {
            Toast.makeText( this,"No image selected",Toast.LENGTH_SHORT ).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult( data );
            mImageUri = result.getUri();

            uploadImage();
        }else {
            Toast.makeText( this,getString(R.string.wrong),Toast.LENGTH_SHORT ).show();
        }
    }
}