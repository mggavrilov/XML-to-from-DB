import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Products {
	private List<Product> products;

	Products() {
	}

	Products(List<Product> products) {
		this.products = products;
	}

	public List<Product> getProducts() {
		return products;
	}

	@XmlElement(name = "product")
	public void setProducts(List<Product> products) {
		this.products = products;
	}
}
