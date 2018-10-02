package rawsun.printforme.com;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {

    RecyclerView recyclerView;
    ShopDetailAdapter adapter;
    List<ShopDetails> shopList=null;
    FirebaseFirestore db;
    private static final String TAG="FireLog";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final int RC_SIGN_IN=1;
     static String mUserEmail;
    private SwipeRefreshLayout swipeRefreshLayout;

    String temp="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        swipeRefreshLayout=(SwipeRefreshLayout) findViewById(R.id.refreshShop);
        swipeRefreshLayout.setOnRefreshListener(MainActivity.this);

        loadShop();

        //code for authStateListener for checking the state that user is loged in or not

        //Code for authentication of the user
        mFirebaseAuth=FirebaseAuth.getInstance();
        mAuthStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user= firebaseAuth.getCurrentUser() ;
                if(user != null){
                    //user is signed in
                    mUserEmail=user.getEmail();
                   // Toast.makeText(MainActivity.this,"Signed in !! ",Toast.LENGTH_SHORT).show();
                }
                else {
                    //user is signed out
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }

            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            if(resultCode==RESULT_OK){
                Toast.makeText(this,"Signed in !!",Toast.LENGTH_SHORT).show();
            }
            else if (resultCode==RESULT_CANCELED){
                Toast.makeText(this,"Sign in canceled",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.Logout:
                AuthUI.getInstance().signOut(this);
                return true;
            case R.id.Orders:
                Toast.makeText(this,"Orders item is clicked",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, Orders_Activity.class);
                startActivity(intent);
                return true;
            case R.id.Help:
                Toast.makeText(this,"Help is clicked ",Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }


    public void loadShop()
    {

        shopList = new ArrayList<>();
        adapter = new ShopDetailAdapter(this, shopList);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db=FirebaseFirestore.getInstance();

        db.collection("SellerId")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if(e!=null)
                        {
                            Log.d(TAG,"Error :" +e.getMessage());
                        }
                        for(DocumentChange doc :documentSnapshots.getDocumentChanges())
                        {
                            if(doc.getType()==DocumentChange.Type.ADDED){
                                ShopDetails shopDetails=doc.getDocument().toObject(ShopDetails.class);
                                shopList.add(shopDetails);

                                adapter.notifyDataSetChanged();
                            }

                        }
                    }
                });

        if(swipeRefreshLayout.isRefreshing())
        {
            swipeRefreshLayout.setRefreshing(false);
        }
        recyclerView.setAdapter(adapter);



    }
    @Override
    public void onRefresh()
    {
        loadShop();

    }
}






















