import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder = { "name", "description", "price", "quantity" })
public class Product {
	private String id;
	private String vendor_id;
	private String name;
	private String description;
	private double price;
	private int quantity;

	public Product() {
	}

	public Product(String id, String vendor_id, String name,
			String description, double price, int quantity) {
		this.id = id;
		this.vendor_id = vendor_id;
		this.name = name;
		this.description = description;
		this.price = price;
		this.quantity = quantity;
	}

	public String getName() {
		return name;
	}

	@XmlElement
	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	@XmlElement
	public void setDescription(String description) {

		this.description = description;
	}

	public double getPrice() {
		return price;
	}

	@XmlElement
	public void setPrice(double price) {
		this.price = price;
	}

	public int getQuantity() {
		return quantity;
	}

	@XmlElement
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getId() {
		return id;
	}

	@XmlAttribute
	public void setId(String id) {
		this.id = id;
	}

	public String getVendorId() {
		return vendor_id;
	}

	@XmlAttribute
	public void setVendorId(String vendor_id) {
		this.vendor_id = vendor_id;
	}
}
