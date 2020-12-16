package pro.luisserrano.mistagram.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import pro.luisserrano.mistagram.R;
import pro.luisserrano.mistagram.data.adapter.UserAdapter;
import pro.luisserrano.mistagram.model.User;

import static com.google.firebase.database.FirebaseDatabase.getInstance;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;

    EditText search_bar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate( R.layout.fragment_search, container, false );

        recyclerView = view.findViewById( R.id.recycle_view );
        recyclerView.setHasFixedSize( true );
        recyclerView.setLayoutManager( new LinearLayoutManager( getContext() ) );

        search_bar = view.findViewById( R.id.search_bar );

        mUsers = new ArrayList<>();
        userAdapter = new UserAdapter( getContext(),mUsers, true );
        recyclerView.setAdapter( userAdapter );

        readUsers();
        //TODO(17): Añadir a search_bar un listener al vento addTextChanged que tenga el siguiente valor DONE
        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers( s.toString().toLowerCase() );
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    private void searchUsers(String s){
        Query query = getInstance().getReference("Users").orderByChild( "username" )
                .startAt( s )
                .endAt( s+"\uf0ff" );
        query.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                //TODO(18): Añadir al vector mUsers, todos los usuarios que se han encontrado DONE
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    User user = snapshot1.getValue( User.class );
                    mUsers.add( user );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void readUsers(){

        //TODO(19): Inicializar reference al hijo "Users DONE"
        DatabaseReference reference = getInstance().getReference().child("Users");
        //TODO(20): Añadir un listener al evento addVAlue con el siguiente valor DONE
         reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(search_bar.getText().toString().equals( "" )){
                    mUsers.clear();
                    for(DataSnapshot snapshot1 : snapshot.getChildren()){
                        User user = snapshot1.getValue( User.class );
                        mUsers.add( user );
                    }
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

