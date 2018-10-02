package rawsun.printforme.com;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

/**
 * Created by ASUS on 16-03-2018.
 */

/*
R
 */

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<OrderDetails> orderList;

    public OrderAdapter(Context context, List<OrderDetails> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.order_details_activity,null);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {

        final OrderDetails orders=orderList.get(position);
        holder.datetime.setText(orders.getDateTime());
        holder.shopName.setText(orders.getShopName());

        String[] latlong=null;
        String mainAdress=null;

        try {
            latlong=orders.getAdress().split(",");
            Geocoder geocoder=new Geocoder(context, Locale.getDefault());
            List<Address> addresslist=geocoder.getFromLocation(Double.parseDouble(latlong[0]),Double.parseDouble(latlong[1]),1);
            mainAdress=addresslist.get(0).getAddressLine(0);
        } catch (Exception e) {
            e.printStackTrace();
        }



        holder.shopAdress.setText(mainAdress);

        holder.shopAdress.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                String temp="geo:"+ orders.getAdress()+"?q="+orders.getAdress();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(temp));
                context.startActivity(intent);
                if(intent.resolveActivity(context.getPackageManager())!=null)
                {
                    context.startActivity(intent);
                }

                else
                {
                    Toast toast=Toast.makeText(context,"Could Not Happen",Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });


        holder.phone.setText(orders.getPhone());

        holder.phone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                String ph=orders.getPhone();
                intent.setData(Uri.parse("tel:"+ph));
                context.startActivity(intent);
            }
        });


        holder.copies.setText(orders.getCopies() +" Copies");
        holder.pageNo.setText("From "+orders.getPages());
        holder.sided.setText(orders.getSided());
        holder.coloured.setText(orders.getColour());
        holder.totalpayable.setText(orders.getTotalPayable());

        //check the value of complete textview and change the value of textview if value is one
        if(orders.getCompleted().equals("1"))
        {

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(0,0,50,0);
            holder.completed.setLayoutParams(params);
            holder.completed.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            holder.completed.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.group10,0);
            holder.completed.setTextColor(Color.parseColor("#6601E8"));
            holder.completed.setText("Completed");
        }

        else
            holder.completed.setText("Order In-Progress");

        holder.downloadfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(orders.getStorage()));
                context.startActivity(i);

            }
        });
        }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder{

        TextView datetime,shopName,shopAdress,phone,copies,pageNo,sided,coloured,totalpayable,completed;
        Button downloadfile;

        public OrderViewHolder(View itemView) {
            super(itemView);

            datetime=itemView.findViewById(R.id.dateTime);
            shopName=itemView.findViewById(R.id.ShopName_textView);
            shopAdress=itemView.findViewById(R.id.adress_textView);
            phone=itemView.findViewById(R.id.phone_textView);
            copies=itemView.findViewById(R.id.copies_textView);
            pageNo=itemView.findViewById(R.id.pages_textView);
            sided=itemView.findViewById(R.id.sided_textView);
            coloured=itemView.findViewById(R.id.colour_textView);
            totalpayable=itemView.findViewById(R.id.Total_textView);
            completed=itemView.findViewById(R.id.completed_textview);
            downloadfile=itemView.findViewById(R.id.download_button);

            }
    }
}
