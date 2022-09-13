package processor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

public class CustomerOrder implements Runnable {

	private TreeMap<String, Integer> totalOrders;
	private TreeMap<String, String> itemData;
	private TreeMap<String, Integer> itemsOrdered;
	private int customerId;
	private String itemFile;
	private String fileName;
	private Object lock;

	public CustomerOrder(String fileName, String itemFile, 
			TreeMap<String, Integer> totalOrders, Object lock) {
		this.fileName = fileName;
		this.itemFile = itemFile;
		this.totalOrders = totalOrders;
		itemsOrdered = new TreeMap<String, Integer>();
		itemData = new TreeMap<String, String>();
		this.lock = lock;
	}

	//create a map with names of items and their corresponding cost
	public void itemDataProcessor(String itemFile) {
		try {
			FileInputStream file = new FileInputStream(itemFile);
			Scanner fileReader = new Scanner(file);
			while (fileReader.hasNextLine()) {
				itemData.put(fileReader.next(), fileReader.next());
			}
			fileReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	//creates a map with name and amount of each item purchased for one client
	public void processOrders() {
		try {
			FileInputStream file = new FileInputStream(fileName);
			Scanner fileReader = new Scanner(file);

			fileReader.skip("ClientId: ");
			customerId = fileReader.nextInt();

			while (fileReader.hasNextLine()) {
				String next = fileReader.next();

				if(itemsOrdered.containsKey(next)) {
					itemsOrdered.put(next, itemsOrdered.get(next) + 1);

				} else {
					itemsOrdered.put(next, 1);
				}
				fileReader.nextLine();
			}
			fileReader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	//creates a map containing the name and amount of items purchased by all clients
	public void processTotalOrders() {
		Set<String> keys = itemsOrdered.keySet();
		for(String key: keys) {
			if(totalOrders.containsKey(key)) {
				totalOrders.put(key, totalOrders.get(key) 
						+ itemsOrdered.get(key));
			} else {
				totalOrders.put(key, itemsOrdered.get(key));
			}
		}
	}

	//creates a string for the customers purchased items
	public String toString() {
		DecimalFormat decimalFormat = new DecimalFormat("#.00");
	    decimalFormat.setGroupingUsed(true);
	    decimalFormat.setGroupingSize(3);
		
		Double totalCostVal = 0.0;
		StringBuilder order = new StringBuilder("");
		Set<String> keys = itemsOrdered.keySet();
		for(String key: keys) {
			order.append("Item's name: " + key + ", Cost per item: $" 
					+ itemData.get(key) + ", " + "Quantity: " 
					+ itemsOrdered.get(key) + ", Cost: $");
					
			Double itemCostVal = Double.parseDouble(itemData.get(key))
					* itemsOrdered.get(key);
			
			String itemCost = decimalFormat.format(itemCostVal);
					
			if (itemCost.length() - itemCost.indexOf('.') 
					- 1 >= 2) {
				order.append(itemCost + "\n");
			} else {
				order.append(itemCost + "0\n");
			}
			
			totalCostVal += Double.parseDouble(itemData.get(key)) * itemsOrdered.get(key);
		}

		String totalCost = decimalFormat.format(totalCostVal);
		
		if(totalCost.length() - totalCost.indexOf('.' ) - 1 < 2) {
			totalCost += "0";
		}
		
		return "----- Order details for client with Id: " + customerId 
				+ " -----\n" + order + "Order Total: $" + totalCost + "\n";
	}

	//creates a string for the items purchased by all clients
	public String orderSummary() {
		DecimalFormat decimalFormat = new DecimalFormat("#.00");
	    decimalFormat.setGroupingUsed(true);
	    decimalFormat.setGroupingSize(3);
	    
		Double totalCostVal = 0.0;
		StringBuilder order = new StringBuilder("");
		Set<String> keys = totalOrders.keySet();
		for(String key: keys) {
			order.append("Summary - Item's name: " + key + ", Cost per item: $" 
					+ itemData.get(key) + ", "
					+ "Number sold: " + totalOrders.get(key) + ", Item's Total: $");
					
			Double itemCostVal = Double.parseDouble(itemData.get(key))
					* totalOrders.get(key);
			
			String itemCost = decimalFormat.format(itemCostVal);
					
			if (itemCost.length() - itemCost.indexOf('.') 
					- 1 >= 2) {
				order.append(itemCost + "\n");
			} else {
				order.append(itemCost + "0\n");
			}
			
			totalCostVal += Double.parseDouble(itemData.get(key)) * totalOrders.get(key);
		}

		String totalCost = decimalFormat.format(totalCostVal);
		
		if(totalCost.length() - totalCost.indexOf('.' ) - 1 < 2) {
			totalCost += "0";
		}
		

		return "***** Summary of all orders *****\n" + order 
				+ "Summary Grand Total: $" + totalCost + "\n";
	}



	@Override
	public void run() {
		this.processOrders();
		System.out.print("Reading order for client with id: " + customerId + "\n");
		this.itemDataProcessor(itemFile);
		synchronized(lock) {
			this.processTotalOrders();
		}
	}
}