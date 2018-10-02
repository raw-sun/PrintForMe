package rawsun.printforme.com;

/**
 * Created by ASUS on 10-02-2018.
 */

public class ShopDetails {

    private String SellerId;
    public ShopDetails(){

    }

    public String getSellerId() {
        return SellerId;
    }

    public String getShopName() {
        return ShopName;
    }

    public String getAdress() {
        return Adress;
    }

    public String getPhone() {
        return Phone;
    }

    private String ShopName, Adress, Phone;

    public ShopDetails(String SellerId, String ShopName, String Adress, String Phone) {
        this.SellerId = SellerId;
        this.ShopName = ShopName;
        this.Adress = Adress;
        this.Phone = Phone;

    }


}
