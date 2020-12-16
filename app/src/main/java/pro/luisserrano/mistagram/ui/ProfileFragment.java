package pro.luisserrano.mistagram.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import pro.luisserrano.mistagram.EditProfileActivity;
import pro.luisserrano.mistagram.FollowersActivity;
import pro.luisserrano.mistagram.OptionsActivity;
import pro.luisserrano.mistagram.R;
import pro.luisserrano.mistagram.data.adapter.MyFotoAdapter;
import pro.luisserrano.mistagram.model.Post;
import pro.luisserrano.mistagram.model.User;

import static com.google.firebase.database.FirebaseDatabase.getInstance;

public class ProfileFragment extends Fragment {

    ImageView image_Profile, options;
    TextView posts,followers,following,fullname,bio,username;
    Button edit_profile;

    private List<String> mySaves;

    RecyclerView recyclerView_saves;
    MyFotoAdapter myFotoAdapter_saves;
    List<Post> postList_saves;

    RecyclerView recyclerView;
    MyFotoAdapter myFotoAdapter;
    List<Post> postList;

    FirebaseUser firebaseUser;
    String profileid;

    ImageButton my_fotos, saved_fotos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.fragment_profile, container, false );


        //TODO(21): Inicializar firebseUser al actual usuario en la base de datos DONE
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences prefs = getContext().getSharedPreferences( "PREFS", Context.MODE_PRIVATE );
        profileid = prefs.getString( "profileid",  firebaseUser.getUid() );

        image_Profile = view.findViewById( R.id.image_profile );
        options = view.findViewById( R.id.options );
        posts = view.findViewById( R.id.posts );
        followers = view.findViewById( R.id.followers );
        following = view.findViewById( R.id.following );
        fullname = view.findViewById( R.id.fullname );
        bio = view.findViewById( R.id.bio );
        username = view.findViewById( R.id.username );
        edit_profile = view.findViewById( R.id.edit_profile );
        my_fotos = view.findViewById( R.id.my_fotos );
        saved_fotos = view.findViewById( R.id.saved_fotos );

        recyclerView = view.findViewById( R.id.recycle_view );
        recyclerView.setHasFixedSize( true );
        LinearLayoutManager linearLayoutManager = new GridLayoutManager( getContext(),3 );
        recyclerView.setLayoutManager( linearLayoutManager );
        postList = new ArrayList<>();
        myFotoAdapter = new MyFotoAdapter( getContext(), postList );
        recyclerView.setAdapter( myFotoAdapter );

        recyclerView_saves = view.findViewById( R.id.recycle_view_save );
        recyclerView_saves.setHasFixedSize( true );
        LinearLayoutManager linearLayoutManager_saves = new GridLayoutManager( getContext(),3 );
        recyclerView_saves.setLayoutManager( linearLayoutManager_saves );
        postList_saves = new ArrayList<>();
        myFotoAdapter_saves = new MyFotoAdapter( getContext(), postList_saves );
        recyclerView_saves.setAdapter( myFotoAdapter_saves );

        recyclerView.setVisibility( View.VISIBLE );
        recyclerView_saves.setVisibility( View.GONE );

        userInfo();
        getFollowers();
        getNrPosts();
        myFotos();
        mysaves();

        if (profileid.equals( firebaseUser.getUid() )){
            edit_profile.setText(getContext().getString(R.string.edit_profile));
        }else{
           checkFollow();
           saved_fotos.setVisibility( View.GONE );
        }

        edit_profile.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btn = edit_profile.getText().toString();

                if(btn.equals(getContext().getString(R.string.edit_profile))){
                    startActivity( new Intent( getContext(), EditProfileActivity.class ) );
                }else if(btn.equals( "follow" )){
                    getInstance().getReference().child( "Follow" ).child( firebaseUser.getUid() )
                            .child( "following" ).child( profileid ).setValue( true );
                    getInstance().getReference().child( "Follow" ).child( profileid )
                            .child( "followers" ).child( firebaseUser.getUid() ).setValue( true );
                    addNotifications();

                }else if(btn.equals( "following" )){
                    getInstance().getReference().child( "Follow" ).child( firebaseUser.getUid() )
                            .child( "following" ).child( profileid ).removeValue();
                    getInstance().getReference().child( "Follow" ).child( profileid )
                            .child( "followers" ).child( firebaseUser.getUid() ).removeValue();
                }
            }
        } );

        options.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( getContext(), OptionsActivity.class );
                startActivity( intent );
            }
        } );

        my_fotos.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility( View.VISIBLE );
                recyclerView_saves.setVisibility( View.GONE );
            }
        } );

        saved_fotos.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility( View.GONE );
                recyclerView_saves.setVisibility( View.VISIBLE );
            }
        } );

        followers.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO(22): Crear un intent, al que se le pase como extras "id", con el valor del id del usuario y "title" con el valor "followers DONE?"
                Intent intent = new Intent(getActivity(), FollowersActivity.class);
                intent.putExtra("id", firebaseUser.getUid());
                intent.putExtra("title", "followers");
                startActivity( intent );
            }
        } );

        following.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO(22): Crear un intent, al que se le pase como extras "id", con el valor del id del usuario y "title" con el valor "following DONE?"
                Intent intent = new Intent(getActivity(), FollowersActivity.class);
                intent.putExtra("id", firebaseUser.getUid());
                intent.putExtra("title",  "following");
                startActivity( intent );
            }
        } );

        return view;
    }

    private void addNotifications(){
        DatabaseReference reference = getInstance().getReference("Notifications").child( profileid );

        /*TODO(23): Crear un objeto de tipo HashMap<String,Object> que contenga como pares "userid", con el id del usuario firegbase, "text DONE?"
        con el valor "started following you", "postid" con valor "" e "ispot" con valor false*/
        HashMap <String,Object> hashMap = new HashMap<String,Object>();
        hashMap.put("userid",firebaseUser.getUid());
        hashMap.put("text","@string/start_follow");
        hashMap.put("postid","");
        hashMap.put("ispost",false);
        reference.push().setValue( hashMap );
    }

    private void userInfo(){
        DatabaseReference reference = getInstance().getReference("Users").child( profileid );
        reference.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(getContext() == null){
                    return;
                }

                User user = snapshot.getValue( User.class );

                //TODO(24): Mostrar los datos del usuario user (imagen, nombre usuario, nombre completo, bio) en los correspondientes elementos de la vista DONE
                Glide.with(getContext()).load(user.getImageurl()).into(image_Profile);
                username.setText(user.getUsername());
                fullname.setText(user.getFullname());
                bio.setText(user.getBio());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void checkFollow(){
        //TODO(25) Inicializar reference a los usuarios que sigue el actual usuario /Follow/Uid/following DONE

        DatabaseReference reference = getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child( profileid ).exists()){
                    edit_profile.setText( "following" );
                }else{
                    edit_profile.setText( "follow" );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void getFollowers(){

        //TODO(26) Inicializar reference a los usuarios que siguen al  usuario /Follow/Uid/followers DONE?
        DatabaseReference reference = getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("followers");
        reference.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers.setText( ""+snapshot.getChildrenCount() );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );

        //TODO(26) Inicializar reference a los usuarios que siguen al  usuario /Follow/profileid/followers DONE -ESTO ESTABA MAL-
        DatabaseReference reference1 = getInstance().getReference().child("Follow").child(profileid).child("following");
        reference1.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following.setText( ""+snapshot.getChildrenCount() );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void getNrPosts(){
        DatabaseReference reference = getInstance().getReference("Posts");
        reference.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i=0;
                //TODO(27): Contar los posts del usuario, para ello recorrer los posts que hay almacenados en dataSnapshot, y si el usario que lo publico es igual al usuario actual(profileid) incrementar i DONE
                for(DataSnapshot snapshot1:dataSnapshot.getChildren()){
                    Post post = snapshot1.getValue( Post.class );
                    if(post.getPublisher().equals( profileid )){
                        i++;
                    }
                }
                posts.setText( ""+i );

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void myFotos(){
        DatabaseReference reference = getInstance().getReference("Posts");
        reference.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    Post post = snapshot1.getValue( Post.class );
                    if(post.getPublisher().equals( profileid )){
                        postList.add( post );
                    }
                }
                Collections.reverse( postList );
                myFotoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void mysaves(){
        mySaves = new ArrayList<>();
        DatabaseReference reference = getInstance().getReference("Saves")
                .child( firebaseUser.getUid() );
        reference.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    mySaves.add( snapshot1.getKey() );
                }
                readSaves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }
    private void readSaves(){
        DatabaseReference reference = getInstance().getReference("Posts");
        reference.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList_saves.clear();
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    Post post = snapshot1.getValue( Post.class );

                    for(String id : mySaves){
                        if(post.getPostid().equals( id )){
                            postList_saves.add( post );
                        }
                    }
                }
                myFotoAdapter_saves.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );

    }
}