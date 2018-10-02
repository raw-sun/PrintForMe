package rawsun.printforme.com;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Orders_Activity extends Activity implements SwipeRefreshLayout.OnRefreshListener {

    FirebaseFirestore db;
    FirebaseFirestore orderdb;
    String TAG="123";
    DocumentReference docRef;

    RecyclerView ordersRecyclerView;
    OrderAdapter orderAdapter;
    List<OrderDetails> orderList=null;
    private SwipeRefreshLayout swipeRefreshLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_);


        swipeRefreshLayout=(SwipeRefreshLayout) findViewById(R.id.refreshOrder);
        swipeRefreshLayout.setOnRefreshListener(Orders_Activity.this);

        loadOrders();


    }

    public void loadOrders()
    {

        orderList=new ArrayList<>();
        orderAdapter=new OrderAdapter(this,orderList);
        ordersRecyclerView=(RecyclerView) findViewById(R.id.orderRecyclerView);
        ordersRecyclerView.setHasFixedSize(true);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        db=FirebaseFirestore.getInstance();

        db.collection("mUserId").document("raushansingh116@gmail.com").collection("Order")
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
                                OrderDetails orderDetails=doc.getDocument().toObject(OrderDetails.class);
                                orderList.add(orderDetails);

                                orderAdapter.notifyDataSetChanged();
                            }

                        }
                    }
                });

        if(swipeRefreshLayout.isRefreshing())
        {
            swipeRefreshLayout.setRefreshing(false);
        }
        ordersRecyclerView.setAdapter(orderAdapter);

    }

    @Override
    public void onRefresh()
    {
        loadOrders();

    }
}
