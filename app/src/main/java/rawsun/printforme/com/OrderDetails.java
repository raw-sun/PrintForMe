package rawsun.printforme.com;

/**
 * Created by ASUS on 16-03-2018.
 */

public class OrderDetails
{
    private String ShopName,Adress,Phone,Colour,Copies,DateTime,Pages,Sided,Storage,TotalPayable,Completed;

    public OrderDetails(){

    }

    public OrderDetails(String ShopName, String Adress, String Phone, String Colour, String Copies, String DateTime, String Pages, String Sided, String Storage, String TotalPayable, String Completed) {
        this.ShopName = ShopName;
        this.Adress = Adress;
        this.Phone = Phone;
        this.Colour = Colour;
        this.Copies = Copies;
        this.DateTime = DateTime;
        this.Pages = Pages;
        this.Sided = Sided;
        this.Storage = Storage;
        this.TotalPayable = TotalPayable;
        this.Completed = Completed;
    }

    public String getShopName() {
        return ShopName;
    }

    public  String getAdress() {
        return Adress;
    }

    public String getPhone() {
        return Phone;
    }

    public String getColour() {
        return Colour;
    }

    public String getCopies() {
        return Copies;
    }

    public String getDateTime() {
        return DateTime;
    }

    public String getPages() {
        return Pages;
    }

    public String getSided() {
        return Sided;
    }

    public String getStorage() {
        return Storage;
    }

    public String getTotalPayable() {
        return TotalPayable;
    }

    public String getCompleted() {
        return Completed;
    }
}
