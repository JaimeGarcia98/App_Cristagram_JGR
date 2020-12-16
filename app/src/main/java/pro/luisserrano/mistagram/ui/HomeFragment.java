package pro.luisserrano.mistagram.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import pro.luisserrano.mistagram.LoginActivity;
import pro.luisserrano.mistagram.MessagesActivity;
import pro.luisserrano.mistagram.R;
import pro.luisserrano.mistagram.data.adapter.PostAdapter;
import pro.luisserrano.mistagram.data.adapter.StoryAdapter;
import pro.luisserrano.mistagram.databinding.FragmentHomeBinding;
import pro.luisserrano.mistagram.model.Post;
import pro.luisserrano.mistagram.model.Story;

import static com.google.firebase.database.FirebaseDatabase.getInstance;


public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postLists;

    private RecyclerView recyclerView_story;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;

    private  List<String> followingList;
    ImageView inbox;
    ProgressBar progressBar;

    private FragmentHomeBinding binding;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //ViewDataBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container,false);
        View view = inflater.inflate( R.layout.fragment_home,container,false );

        recyclerView = view.findViewById( R.id.recycle_view );
        recyclerView.setHasFixedSize( true );
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager( getContext() );
        linearLayoutManager.setReverseLayout( true );
        linearLayoutManager.setStackFromEnd( true );
        recyclerView.setLayoutManager( linearLayoutManager );
        postLists = new ArrayList<>();
        postAdapter = new PostAdapter( getContext(),postLists );
        recyclerView.setAdapter( postAdapter );

        recyclerView_story = view.findViewById( R.id.recycle_view_story );
        recyclerView_story.setHasFixedSize( true );
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager( getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        recyclerView_story.setLayoutManager( linearLayoutManager1 );
        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter( getContext(), storyList );
        recyclerView_story.setAdapter( storyAdapter );

        progressBar = view.findViewById( R.id.progress_circle );

        checkFollowing();

        inbox = view.findViewById(R.id.inbox);
        //TODO(11): colocar un listener a inbox que lance MessageActivity en caso de hacer click DONE?
        inbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mes = new Intent(getActivity(), MessagesActivity.class);
                startActivity(mes);
            }
        });
        return view;
        //return binding.getRoot();
    }


    private void checkFollowing(){
        followingList = new ArrayList<>(  );

        //TODO(12): Inicializar reference al valor del hijo "Follow", dentro de este al actual usuario, y dentro de este al hijo "following DONE"
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = getInstance().getReference().child("Follow").child(user.getUid()).child("following");
        reference.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    followingList.add( snapshot1.getKey() );
                }
                readPosts();
                readStory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void readPosts(){

        //TODO(13): Inicializar reference al valor del hijo "Posts DONE"
        DatabaseReference reference = getInstance().getReference().child("Posts");
        //TODO(14): Añadir a reference un listener sobre el evento addValue con el siguiente valor DONE
         reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postLists.clear();
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    Post post = snapshot1.getValue( Post.class );
                    for(String id : followingList){
                        if(post.getPublisher().equals( id )){
                            postLists.add( post );


                        }
                    }
                }

                postAdapter.notifyDataSetChanged();
                progressBar.setVisibility( View.GONE );

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void readStory(){
        //TODO(15): Inicializar reference al valor del hijo "Story" DONE
        DatabaseReference reference = getInstance().getReference().child("Story");
        //TODO(16): Añadir a reference un listener sobre el evento addValue con el siguiente valor DONE
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long timecurrent = System.currentTimeMillis();
                storyList.clear();
                storyList.add( new Story("",0,0,"",
                        FirebaseAuth.getInstance().getCurrentUser().getUid()) );
                Log.i( "followinglist", String.valueOf( followingList.size() ) );
                for(String id : followingList){
                    int countStory = 0;
                    Story story = null;
                    for (DataSnapshot snapshot1 : snapshot.child( id ).getChildren()){
                        story = snapshot1.getValue( Story.class );
                        if(timecurrent > story.getTimestart() && timecurrent < story.getTimeend()){
                            countStory++;
                        }
                    }
                    if(countStory>0){
                        storyList.add( story );
                    }
                }
                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}