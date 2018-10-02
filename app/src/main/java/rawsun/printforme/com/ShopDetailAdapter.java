package rawsun.printforme.com;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

/**
 * Created by ASUS on 10-02-2018.
 */

public class ShopDetailAdapter extends RecyclerView.Adapter<ShopDetailAdapter.ShopDetailViewHolder> {

    private Context ctx;
    private List<ShopDetails> shopDetailsList;

    public ShopDetailAdapter(Context ctx, List<ShopDetails> shopDetailsList) {
        this.ctx = ctx;
        this.shopDetailsList = shopDetailsList;
    }

    @Override
    public ShopDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(ctx);
        View view=inflater.inflate(R.layout.homepage_shoplist,null);
        return new ShopDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ShopDetailViewHolder holder, int position) {

        final ShopDetails shopDetails=shopDetailsList.get(position);

        //binding the data with the viewholder views
        holder.textViewShopName.setText(shopDetails.getShopName());

        String[] latlong=null;
        String mainAdress=null;

        try {
            latlong=shopDetails.getAdress().split(",");
            Geocoder geocoder=new Geocoder(ctx, Locale.getDefault());
            List<Address> addresslist=geocoder.getFromLocation(Double.parseDouble(latlong[0]),Double.parseDouble(latlong[1]),1);
            mainAdress=addresslist.get(0).getAddressLine(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.textViewAdress.setText(mainAdress);
        holder.textViewAdress.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                String temp="geo:"+ shopDetails.getAdress()+"?q="+shopDetails.getAdress();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(temp));
                ctx.startActivity(intent);
                if(intent.resolveActivity(ctx.getPackageManager())!=null)
                {
                    ctx.startActivity(intent);
                }

                else
                {
                    Toast toast=Toast.makeText(ctx,"Could Not Happen",Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        holder.textViewPhoneNo.setText(shopDetails.getPhone());

        holder.textViewPhoneNo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                String ph=shopDetails.getPhone();

                intent.setData(Uri.parse("tel:"+ph));
                ctx.startActivity(intent);
            }
        });

        holder.printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx, DetailsActivity.class);
                intent.putExtra("SellerEmail",shopDetails.getSellerId());
                intent.putExtra("ShopName",shopDetails.getShopName());
                intent.putExtra("Phone",shopDetails.getPhone());
                intent.putExtra("Adress",shopDetails.getAdress());


                ctx.startActivity(intent);


            }
        });


    }

    @Override
    public int getItemCount() {
        return shopDetailsList.size();
    }

    class ShopDetailViewHolder extends RecyclerView.ViewHolder{


        TextView textViewShopName,textViewAdress,textViewPhoneNo;
        Button printButton;
        public ShopDetailViewHolder(View itemView) {
            super(itemView);

            textViewShopName= itemView.findViewById(R.id.shopNameTextView);
            textViewAdress = itemView.findViewById(R.id.adressTextView);
            textViewPhoneNo = itemView.findViewById(R.id.phoneNoTextView);
            printButton = itemView.findViewById(R.id.printButton);

        }
    }


}
