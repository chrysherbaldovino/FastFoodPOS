package FastFoodPOS;

//This stores basic details like the item's ID, name, price, and visual icon
//Additionally, this class has the items in the menu 
public class MenuItem {
    private int id; //unique identifier for the item
    private String name; //display name of the food/drink
    private double price; //the cost of the item
    private String icon; //the emoji icon used in the UI
    
    //this creates a new MenuItem object with the provided details
    public MenuItem(int id, String name, double price, String icon) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.icon = icon;
    }
    
    //this allows other parts of the program to read these private variables
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getIcon() { return icon; }
    
    //This overrides how the object looks when printed as text
    //instead of printing a memory address, it prints the icon as well as the name of the food and the amount of it
    @Override
    public String toString() {
        return icon + " " + name + " - ₱" + String.format("%.2f", price);
    }
}