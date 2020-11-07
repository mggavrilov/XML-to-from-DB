import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public class Main {
	static Connection connection = null;
	static Statement stmt = null;
	static ResultSet result = null;

	static Vendors vendors = new Vendors();
	static Products products = new Products();

	static File vendorsFile = new File("vendors.xml");
	static File productsFile = new File("products.xml");

	static List<Vendor> vendorList = new ArrayList<Vendor>();
	static List<Product> productList = new ArrayList<Product>();

	public static void main(String[] args) {
		readEntriesFromDB();
		convertDBEntriesToXMLFiles();

		// DTD Validation and XML files to DB entries
		try {
			String shopFile = "shop.xml";

			// if(validateDOM(shopFile)) {
			if (validateSAX(shopFile)) {
				// We've validated the XML, so extraction from the DB must've
				// been successful.
				// Deleting all entries from the database and repopulating it.
				deleteDBEntries();

				convertXMLFilesToDBEntries();
			} else {
				System.out
						.println("Couldn't validate the XML files against the DTD.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void readEntriesFromDB() {
		System.out.println("Reading entries from the database...");

		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");

			connection = DriverManager.getConnection(
					"jdbc:hsqldb:hsql://localhost/", "sa", "");

			stmt = connection.createStatement();

			result = stmt.executeQuery("SELECT * FROM vendors");

			while (result.next()) {
				String id = "v" + result.getInt("id");
				String name = result.getString("name");
				String address = result.getString("address");

				vendorList.add(new Vendor(id, name, address));
			}

			result = stmt.executeQuery("SELECT * FROM products");

			while (result.next()) {
				String id = "p" + result.getInt("id");
				String vendor_id = "v" + result.getInt("vendor_id");
				String name = result.getString("name");
				String description = result.getString("description");
				double price = result.getDouble("price");
				int quantity = result.getInt("quantity");

				productList.add(new Product(id, vendor_id, name, description,
						price, quantity));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Read " + vendorList.size()
				+ " vendors from the database.");
		System.out.println("Read " + productList.size()
				+ " products from the database.");

		vendors.setVendors(vendorList);
		products.setProducts(productList);
	}

	public static void convertDBEntriesToXMLFiles() {
		// DB entries to XML files
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Vendors.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

			jaxbMarshaller.marshal(vendors, vendorsFile);

			System.out.println("Saved DB vendors to " + vendorsFile.getName());

			jaxbContext = JAXBContext.newInstance(Products.class);
			jaxbMarshaller = jaxbContext.createMarshaller();

			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

			jaxbMarshaller.marshal(products, productsFile);

			System.out
					.println("Saved DB products to " + productsFile.getName());
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	public static void deleteDBEntries() throws SQLException {
		System.out
				.println("Successfully validated the XML files against the DTD");

		System.out.println("Deleting entries from the database...");

		stmt.executeQuery("DELETE FROM products");
		stmt.executeQuery("DELETE FROM vendors");

		result = stmt.executeQuery("SELECT COUNT(*) as count FROM vendors");
		result.next();
		System.out
				.println("Deleted all entries from the vendors table. Current count: "
						+ result.getInt("count"));

		result = stmt.executeQuery("SELECT COUNT(*) as count FROM products");
		result.next();
		System.out
				.println("Deleted all entries from the products table. Current count: "
						+ result.getInt("count"));

		System.out
				.println("You can now manually check the database to make sure the entries were in fact deleted.");
		System.out.print("Please press ENTER to continue.");
		Scanner sc = new Scanner(System.in);
		sc.nextLine();
		sc.close();
	}

	public static void convertXMLFilesToDBEntries() throws Exception {
		JAXBContext jaxbContext = JAXBContext.newInstance(Vendors.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		int initialVendors = vendors.getVendors().size();
		vendors = (Vendors) jaxbUnmarshaller.unmarshal(vendorsFile);

		System.out.println("Inserting XML entries to the DB...");

		String sql = "INSERT INTO vendors (id, name, address) VALUES (?, ?, ?)";
		PreparedStatement preparedStatement = connection.prepareStatement(sql);

		for (Vendor vendor : vendors.getVendors()) {
			preparedStatement.setInt(1,
					Integer.parseInt(vendor.getId().substring(1)));
			preparedStatement.setString(2, vendor.getName());
			preparedStatement.setString(3, vendor.getAddress());
			preparedStatement.executeUpdate();
		}

		jaxbContext = JAXBContext.newInstance(Products.class);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		int initialProducts = products.getProducts().size();
		products = (Products) jaxbUnmarshaller.unmarshal(productsFile);

		sql = "INSERT INTO products (id, vendor_id, name, description, price, quantity) VALUES (?, ?, ?, ?, ?, ?)";
		preparedStatement = connection.prepareStatement(sql);

		for (Product product : products.getProducts()) {
			preparedStatement.setInt(1,
					Integer.parseInt(product.getId().substring(1)));
			preparedStatement.setInt(2,
					Integer.parseInt(product.getVendorId().substring(1)));
			preparedStatement.setString(3, product.getName());
			preparedStatement.setString(4, product.getDescription());
			preparedStatement.setDouble(5, product.getPrice());
			preparedStatement.setInt(6, product.getQuantity());
			preparedStatement.executeUpdate();
		}

		System.out.println("Inserted " + vendors.getVendors().size()
				+ " vendors.");
		System.out.println("Inserted " + products.getProducts().size()
				+ " products.");

		if (initialVendors == vendors.getVendors().size()
				&& initialProducts == products.getProducts().size()) {
			System.out.println("Execution successful.");
		} else {
			System.out.println("Execution failed");
		}

		connection.close();
	}

	/*
	 * Reference: http://www.rgagnon.com/javadetails/java-0668.html
	 */
	// validate using DOM
	public static boolean validateDOM(String xml)
			throws ParserConfigurationException, IOException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setValidating(true);

			DocumentBuilder builder = factory.newDocumentBuilder();

			builder.setErrorHandler(new ErrorHandler() {
				public void warning(SAXParseException e) throws SAXException {
					System.out.println("WARNING : " + e.getMessage());
					e.printStackTrace();
				}

				public void error(SAXParseException e) throws SAXException {
					System.out.println("ERROR : " + e.getMessage());
					e.printStackTrace();

					throw e;
				}

				public void fatalError(SAXParseException e) throws SAXException {
					System.out.println("FATAL : " + e.getMessage());
					e.printStackTrace();

					throw e;
				}
			});
			builder.parse(new InputSource(xml));
			return true;
		} catch (ParserConfigurationException pce) {
			throw pce;
		} catch (IOException io) {
			throw io;
		} catch (SAXException se) {
			return false;
		}
	}

	// validate using SAX
	public static boolean validateSAX(String xml)
			throws ParserConfigurationException, IOException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(true);

			SAXParser parser = factory.newSAXParser();

			XMLReader reader = parser.getXMLReader();
			reader.setErrorHandler(new ErrorHandler() {
				public void warning(SAXParseException e) throws SAXException {
					System.out.println("WARNING : " + e.getMessage());
					e.printStackTrace();
				}

				public void error(SAXParseException e) throws SAXException {
					System.out.println("ERROR : " + e.getMessage());
					e.printStackTrace();
					throw e;
				}

				public void fatalError(SAXParseException e) throws SAXException {
					System.out.println("FATAL : " + e.getMessage());
					e.printStackTrace();
					throw e;
				}
			});
			reader.parse(new InputSource(xml));
			return true;
		} catch (ParserConfigurationException pce) {
			throw pce;
		} catch (IOException io) {
			throw io;
		} catch (SAXException se) {
			return false;
		}
	}
}
