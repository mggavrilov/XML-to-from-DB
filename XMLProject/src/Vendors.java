import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Vendors {
	private List<Vendor> vendors;
	
	public Vendors() {}
	
	public Vendors(List<Vendor> vendors) {
		this.vendors = vendors;
	}

	public List<Vendor> getVendors() {
		return vendors;
	}

	@XmlElement(name = "vendor")
	public void setVendors(List<Vendor> vendors) {
		this.vendors = vendors;
	}
}
