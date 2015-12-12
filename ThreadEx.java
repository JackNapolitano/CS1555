/*
 * Java Threading Example
 */

import java.util.Random;
 
public class ThreadEx extends Thread {
	private static int NUM_OF_THREADS = 10;
	private static Random rand = new Random();

	int id;
	
	public static void main (String args []) {
		try {
			// Create the threads
			Thread[] threadList = new Thread[NUM_OF_THREADS];

			// spawn threads
			for (int i = 0; i < NUM_OF_THREADS; i++) {
				threadList[i] = new ThreadEx(i);
				threadList[i].start();
			}
		
			// Start everyone at the same time
			setGreenLight ();

			// wait for all threads to end
			for (int i = 0; i < NUM_OF_THREADS; i++) {
				threadList[i].join();
			}

		}
		catch (Exception e) {
			 e.printStackTrace();
		}
	
	}	

	public ThreadEx(int i) {
		 super();
		 id = i;
	}

	public void run() {
		try {
			// wait for the go-ahead
			while (!getGreenLight())
				yield();

			int c = 0;
			for (int i = 0; i < 10; i++) {
				System.out.println(id);
				
				c = rand.nextInt();
				if (c < 33)
					yield();
			}
		}
		catch (Exception e) {
			System.out.println("!! Exception: " + e + "!!");
			e.printStackTrace();
			return;
		}
	}

	static boolean greenLight = false;
	static synchronized void setGreenLight () { greenLight = true; }
	synchronized boolean getGreenLight () { return greenLight; }
}