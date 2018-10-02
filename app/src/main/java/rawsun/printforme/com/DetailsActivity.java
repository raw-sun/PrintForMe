package rawsun.printforme.com;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailsActivity extends Activity {

    Button choose, show;
    int REQUEST_CODE_DOC = 1;
    private Uri mfilepath;
    private Spinner mColorSpinner, msidedSpinner;
    private Button msubmit;
    private StorageReference storageReference;
    private final String TAG="FireBase Upload";
    private StorageTask storageTask;

     int subTotal;
     double discountPrice, orderTotal, file_size;
     EditText minlimit, maxlimit, copies;
     String stringUri;
     String extension;

    public static String mUserId ;
    String mSellerId;
    String mSellerAdress;
    String mSellerPhone;
    String mSellerName;
    String mOrderId;
    FirebaseFirestore mFireStore;
    FirebaseFirestore mFireStoreforUser;

    //for uploading data to the firestore
    HashMap<String,String> orderdetail;
    HashMap<String,String> mOrderRefUser;
    String onlyDate;
    String normaltime;
    int totalpayable=0 ;
    String paytmpaymentinfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //getting the value of sellerId by intent
        mSellerId=getIntent().getStringExtra("SellerEmail");
        mSellerAdress=getIntent().getStringExtra("Adress");
        mSellerPhone=getIntent().getStringExtra("Phone");
        mSellerName=getIntent().getStringExtra("ShopName");

        Log.d(TAG,"Error :" +mSellerId);


        //getting user EmailId
        mUserId=MainActivity.mUserEmail;

        storageReference= FirebaseStorage.getInstance().getReference();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        //this is implemention of choose button. it chooses pdf and msword file from the storage via intent
        choose = (Button) findViewById(R.id.choose);
        choose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                //mimy code here paste it;
                String[] mimeTypes =
                        {"application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                                "application/pdf",
                        };

                intent.addCategory(Intent.CATEGORY_OPENABLE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
                    if (mimeTypes.length > 0) {
                        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                    }
                } else {
                    String mimeTypesStr = "";
                    for (String mimeType : mimeTypes) {
                        mimeTypesStr += mimeType + "|";
                    }
                    intent.setType(mimeTypesStr.substring(0, mimeTypesStr.length() - 1));
                }

                startActivityForResult(Intent.createChooser(intent, "Choose PDF or Doc file"), REQUEST_CODE_DOC);
            }
        });

        //implementation for color spinner
        mColorSpinner = (Spinner) findViewById(R.id.colour);

        ArrayAdapter<String> colourAdapter = new ArrayAdapter<String>(DetailsActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.names));

        colourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mColorSpinner.setAdapter(colourAdapter);

        //implementation for msidedSpinner spinner
        msidedSpinner = (Spinner) findViewById(R.id.sided);

        ArrayAdapter<String> sidedAdapter = new ArrayAdapter<String>(DetailsActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.sided));

        sidedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        msidedSpinner.setAdapter(sidedAdapter);

        //initialize editview
        minlimit = (EditText) findViewById(R.id.minlimit);

        //filter to limit values
        minlimit.setFilters(new InputFilter[]{new FilterEditview("1","2000")});

        maxlimit = (EditText) findViewById(R.id.maxlimit);
        maxlimit.setFilters(new InputFilter[]{new FilterEditview("1","2000")});

        copies = (EditText) findViewById(R.id.copies);
        copies.setFilters(new InputFilter[]{new FilterEditview("1","100")});

        //implement the msubmit button


        msubmit = (Button) findViewById(R.id.submit);
        msubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {


                AlertDialog.Builder builder=new AlertDialog.Builder(DetailsActivity.this);

                builder.setMessage("Order Confirm ?? ")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                generateCheckSum();
                            }
                        })
                        .setNegativeButton("No" , null);
                AlertDialog alear=builder.create();
                alear.show();

            }
        });//finish the button click of payable button

         final TextView TotalPayable=(TextView) findViewById(R.id.totalPayable);

        TotalPayable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkEditfield()==true)
                {

                    TotalPayable.setText("Total Payable :- "+String.valueOf(calculateprice()));

                }
            }
        });
    }


    public void payThroughPaytm()
    {
                        if (checkEditfield())
                        {
                            int temp=calculateprice();
                            //Start showing the progress dialog and setting the title of the ProgressDialog
                            final ProgressDialog progressDialog=new ProgressDialog(DetailsActivity.this);
                            progressDialog.setTitle("Processing......");
                            progressDialog.show();

                            //getting diffrent type of time

                            //getting time with milisecond for accurate order Id
                            Date c = Calendar.getInstance().getTime();
                            SimpleDateFormat time=new SimpleDateFormat("HH:mm:ss:SSS a");
                            String timewithmiliseconds=time.format(c);

                            //getting normal time without miliseconds
                            normaltime= DateFormat.getTimeInstance().format(new Date());

                            //getting only data
                            onlyDate=DateFormat.getDateInstance().format(new Date());

                            mOrderId = mUserId.substring(0,mUserId.indexOf('@')) + timewithmiliseconds.replaceAll(":| ","-");


                            orderdetail.put("Completed","0");
                            mOrderRefUser.put("Completed","0");
                            orderdetail.put("UserEmail",mUserId);
                            orderdetail.put("DateTime",onlyDate+" "+normaltime);
                            orderdetail.put("FileName",mOrderId+extension);


                            mOrderRefUser.put("Phone",mSellerPhone);
                            mOrderRefUser.put("ShopName",mSellerName);
                            mOrderRefUser.put("Adress",mSellerAdress);

                            Log.d(TAG,"paytminfo" + paytmpaymentinfo);
                            mOrderRefUser.put("Paytm Info",paytmpaymentinfo);
                            mOrderRefUser.put("DateTime",onlyDate+" "+normaltime);

                            //uploading pdf or word file to fireBase storage
                            StorageReference riversRef = storageReference.child("Files/"+mOrderId);

                            storageTask = riversRef.putFile(mfilepath)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {



                                            Uri url=taskSnapshot.getDownloadUrl();

                                            orderdetail.put("Storage",url.toString());
                                            mOrderRefUser.put("Storage",url.toString());

                                            // After file is uploaded to the firebase server then uploading the details to the firestore

                                            mFireStore= FirebaseFirestore.getInstance();
                                            mFireStore.collection("SellerId").document(mSellerId).collection("Order").document(mOrderId)
                                                    .set(orderdetail)
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                                                        }
                                                    });

                                            //Below code is adding order information to the user database

                                            //Long tsLong = System.currentTimeMillis();


                                            mFireStoreforUser=FirebaseFirestore.getInstance();
                                            mFireStoreforUser.collection("mUserId").document(mUserId).collection("Order").document(mOrderId)
                                                    .set(mOrderRefUser)
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                                                        }
                                                    });

                                            progressDialog.dismiss();
                                            Toast.makeText(getApplicationContext(),"Order placed",Toast.LENGTH_LONG).show();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            progressDialog.dismiss();
                                            Toast.makeText(getApplicationContext(),exception.getMessage(),Toast.LENGTH_LONG).show();

                                        }
                                    })
                                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                                            progressDialog.setCancelable(false);

                                            double progress=(100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                                            progressDialog.setMessage(((int) progress) +"% Uploading.....");
                                        }
                                    });

                        }

    }


    //this method executed when user selected file from chooser button
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_DOC && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mfilepath = data.getData();
            if (mfilepath != null)
            {
                File file= new File(mfilepath.getPath());
                extension=file.getName().substring(file.getName().lastIndexOf("."));

                Log.d(TAG,"file name" + mfilepath.toString());
                Toast.makeText(this, "File selected", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "No file chosen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public int  calculateprice()
    {
        int colorSpinnerValue=0;
        double sidedSpinnerValue=0.0;
        int copiesTextViewValue=0;
        int maxValue=0;
        int minValue=0;

        copiesTextViewValue=Integer.parseInt(copies.getText().toString());
        Log.d(TAG,"value" + totalpayable);
        maxValue= Integer.parseInt(maxlimit.getText().toString());
        minValue= Integer.parseInt(minlimit.getText().toString());

        if(mColorSpinner.getSelectedItemPosition()==0)
        {
            colorSpinnerValue= 1;
        }
        else
        {
             colorSpinnerValue= 5;
        }

        if(msidedSpinner.getSelectedItemPosition()==0)
        {
                 sidedSpinnerValue=  1.0;
        }
        else
        {
            sidedSpinnerValue=0.5;
        }


        try
        {
            // This line of code throws NullPointerException
            // because ptr is null
            totalpayable= (int) Math.ceil(( Math.abs(maxValue-minValue)+1) * copiesTextViewValue * colorSpinnerValue *sidedSpinnerValue);
            Log.d(TAG,"value" + totalpayable);
            orderdetail= new HashMap<>();

            orderdetail.put("Copies",Integer.toString(copiesTextViewValue));
            orderdetail.put("Pages",Integer.toString(minValue)+"-"+Integer.toString(maxValue));
            orderdetail.put("Sided",msidedSpinner.getSelectedItem().toString());
            orderdetail.put("Colour",mColorSpinner.getSelectedItem().toString());

            //this is for user order
            mOrderRefUser=new HashMap<>();
            mOrderRefUser.put("TotalPayable",String.valueOf(totalpayable));
            mOrderRefUser.put("Copies",Integer.toString(copiesTextViewValue));
            mOrderRefUser.put("Pages",Integer.toString(minValue)+"-"+Integer.toString(maxValue));
            mOrderRefUser.put("Sided",msidedSpinner.getSelectedItem().toString());
            mOrderRefUser.put("Colour",mColorSpinner.getSelectedItem().toString());
        }
        catch(NullPointerException e)
        {
            Log.d(TAG,"Null point exception" + totalpayable);
        }

        //setting value to the hash map for uploading data of the order
        //Uploading data to the FireStore database

        return  totalpayable;
    }

    public Boolean checkEditfield()
    {

        //check field it is empty or not

        if (!(mfilepath != null && !mfilepath.equals(Uri.EMPTY))){

            Toast.makeText(this, "Select File first ", Toast.LENGTH_SHORT).show();
            return false;

        }
        else if(TextUtils.isEmpty(copies.getText().toString()))
        {
            Toast.makeText(this, "Please Enter NO. of Copies ", Toast.LENGTH_SHORT).show();
            return false;
        }
       else if(TextUtils.isEmpty(minlimit.getText().toString()) && TextUtils.isEmpty(minlimit.getText().toString()))
        {
            Toast.makeText(this, "Please Enter the Page Numbers ", Toast.LENGTH_SHORT).show();
            return false;
        }

        else
        {
            return true;
        }
    }

    private void generateCheckSum() {

        //getting the tax amount first.
       //TODO set text amount
        String txnAmount = Integer.toString(totalpayable);

        //creating a retrofit object.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //creating the retrofit api service
        Api apiService = retrofit.create(Api.class);

        //creating paytm object
        //containing all the values required
        final Paytm paytm = new Paytm(
                Constants.M_ID,
                Constants.CHANNEL_ID,
                txnAmount,
                Constants.WEBSITE,
                Constants.CALLBACK_URL,
                Constants.INDUSTRY_TYPE_ID
        );

        //creating a call object from the apiService
        Call<Checksum> call = apiService.getChecksum(
                paytm.getmId(),
                paytm.getOrderId(),
                paytm.getCustId(),
                paytm.getChannelId(),
                paytm.getTxnAmount(),
                paytm.getWebsite(),
                paytm.getCallBackUrl(),
                paytm.getIndustryTypeId()
        );

        //making the call to generate checksum
        call.enqueue(new Callback<Checksum>() {
            @Override
            public void onResponse(Call<Checksum> call, Response<Checksum> response) {

                //once we get the checksum we will initiailize the payment.
                //the method is taking the checksum we got and the paytm object as the parameter


                try
                {
                    // This line of code throws NullPointerException

                    initializePaytmPayment(response.body().getChecksumHash(), paytm);
                    Toast.makeText(DetailsActivity.this,"Loging you in to Paytm",Toast.LENGTH_LONG).show();

                }
                catch(NullPointerException e)
                {
                    Toast.makeText(DetailsActivity.this,"Server is taking rest !! Will be back again within an hour !! ",Toast.LENGTH_LONG).show();
                }



            }

            @Override
            public void onFailure(Call<Checksum> call, Throwable t) {
                Toast.makeText(DetailsActivity.this,"Problem connecting to server !! ",Toast.LENGTH_LONG).show();

            }
        });
    }

    private void initializePaytmPayment(String checksumHash, Paytm paytm) {

        //getting paytm service
        PaytmPGService Service = PaytmPGService.getStagingService();

        //use this when using for production
        //PaytmPGService Service = PaytmPGService.getProductionService();

        //creating a hashmap and adding all the values required
        Map<String, String> paramMap = new HashMap<String,String>();
        paramMap.put("MID", Constants.M_ID);
        paramMap.put("ORDER_ID", paytm.getOrderId());
        paramMap.put("CUST_ID", paytm.getCustId());
        paramMap.put("CHANNEL_ID", paytm.getChannelId());
        paramMap.put("TXN_AMOUNT", paytm.getTxnAmount());
        paramMap.put("WEBSITE", paytm.getWebsite());
        paramMap.put("CALLBACK_URL", paytm.getCallBackUrl());
        paramMap.put("CHECKSUMHASH", checksumHash);
        paramMap.put("INDUSTRY_TYPE_ID", paytm.getIndustryTypeId());


        //creating a paytm order object using the hashmap
        PaytmOrder order = new PaytmOrder(paramMap);

        //intializing the paytm service
        Service.initialize(order, null);

        //finally starting the payment transaction
        Service.startPaymentTransaction(this, true, true, new PaytmPaymentTransactionCallback() {
            @Override
            public void onTransactionResponse(Bundle bundle)
            {
                Log.d("LOG", "Payment Transaction : " + bundle.toString());
                // Toast.makeText(getApplicationContext(), "Payment Transaction response "+bundle.toString(), Toast.LENGTH_LONG).show();

                String mCallBack=bundle.toString();
                if(mCallBack.substring(0,50).contains("STATUS=TXN_SUCCESS"))
                {
                   paytmpaymentinfo=mCallBack;
                    payThroughPaytm();

                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Transaction Failed", Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void networkNotAvailable()

            {
                Log.d("LOG", "network not available");
                Toast.makeText(getApplicationContext(), " Network not available ", Toast.LENGTH_LONG).show();
            }

            @Override
            public void clientAuthenticationFailed(String s)
            {
                Log.d("LOG", "client authentication failed.");
                Toast.makeText(getApplicationContext(), " Severside Error "+ s, Toast.LENGTH_LONG).show();

            }

            @Override
            public void someUIErrorOccurred(String s) {
                Log.d("LOG", "UI Error Occur.");
                Toast.makeText(getApplicationContext(), " UI Error Occur. ", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onErrorLoadingWebPage(int i, String s, String s1) {
                Log.d("LOG", "Error Loading WebPage");
                Toast.makeText(getApplicationContext(), " Error loding webpage ", Toast.LENGTH_LONG).show();


            }

            @Override
            public void onBackPressedCancelTransaction() {
                Log.d("LOG", "Back PressedCancel Transaction");
                Toast.makeText(getApplicationContext(), " Back Pressed Cancel Transaction ", Toast.LENGTH_LONG).show();


            }

            @Override
            public void onTransactionCancel(String s, Bundle bundle)
            {
                Log.d("LOG", "Payment Transaction Failed " + s);
                Toast.makeText(getBaseContext(), "Payment Transaction Failed ", Toast.LENGTH_LONG).show();

            }
        });


    }


}




