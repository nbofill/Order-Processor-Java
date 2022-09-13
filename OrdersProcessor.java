package processor;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;

public class OrdersProcessor {

	public static void main(String[] args) {

		Scanner input = new Scanner(System.in);
		System.out.print("Enter item's data file name: ");

		String fileName = input.next();

		boolean multiThread = false;
		System.out.print
		("Enter 'y' for multiple threads, any other character otherwise: ");

		if (input.next().equals("y")) {
			multiThread = true;
		}

		System.out.print("Enter number of orders to process: ");
		int numOfOrders = input.nextInt();

		System.out.print("Enter order's base filename: ");
		String customerFile = input.next();

		System.out.print("Enter result's filename: ");
		String resultsFile = input.next();

		input.close();

		long startTime = System.currentTimeMillis();
		TreeMap<String, Integer> totalOrders = new TreeMap<String, Integer>();

		if (multiThread) {
			Object lock = new Object();
			ArrayList<CustomerOrder> orders = new ArrayList<CustomerOrder>();
			Thread[] threads = new Thread[numOfOrders];
			for (int i = 0; i < numOfOrders; i++) {
				orders.add(i, new CustomerOrder
				(customerFile + (i + 1) + ".txt", fileName, totalOrders, lock ));
				threads[i] = new Thread(orders.get(i));
			}

			for (int i = 0; i < orders.size(); i++) {
				threads[i].start();
			}

			for(Thread currentThread: threads) {
				try {
					currentThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			StringBuilder orderResult = new StringBuilder("");
			for(CustomerOrder order: orders) {
				orderResult.append(order.toString());
			}
			
			orderResult.append(orders.get(0).orderSummary());
			
			 try {
					FileWriter orderSummary = new FileWriter(resultsFile);
					orderSummary.write(orderResult.toString());
					orderSummary.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

		} else {
			Object lock = new Object();
			ArrayList<CustomerOrder> orders = new ArrayList<CustomerOrder>();
			Thread[] threads = new Thread[numOfOrders];
			for (int i = 0; i < numOfOrders; i++) {
				orders.add(i, new CustomerOrder
				(customerFile + (i + 1) + ".txt", fileName, totalOrders, lock ));
				threads[i] = new Thread(orders.get(i));
			}

			for (int i = 0; i < orders.size(); i++) {
				threads[i].run(); 
			}

			for(Thread currentThread: threads) {
				try {
					currentThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			StringBuilder orderResult = new StringBuilder("");
			for(CustomerOrder order: orders) {
				orderResult.append(order.toString());
			}
			
			orderResult.append(orders.get(0).orderSummary());
			
			 try {
					FileWriter orderSummary = new FileWriter(resultsFile);
					orderSummary.write(orderResult.toString());
					orderSummary.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("Processing time (msec): " + (endTime - startTime));

		System.out.print("Results can be found in the file: " + resultsFile);
	}
}
