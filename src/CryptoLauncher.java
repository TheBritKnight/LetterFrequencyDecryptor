import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;

public class CryptoLauncher extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;
	JTextField userInput, key, outputField;
	JLabel luserInput, lkey, characterWarnings, decryptionWarnings, loutput;
	JButton encrypt, decrypt, learnFromPlaintext;
	
	CryptoLauncher() {
		
		super("MyCryptoGraphyEngine");
		
		this.init();
		
		//Create window
		this.pack();
		this.setResizable(false);
		this.setVisible(true);

	} //End Main
	
	private void init() {
		//Initialize Labels
		luserInput = new JLabel("Input");
		lkey = new JLabel("Key");
		characterWarnings = new JLabel("Encrypt alphabetical characters and spaces only");
		decryptionWarnings = new JLabel("Decryption w/out key requires a sample of sufficient length to determine frequency profile");
		loutput = new JLabel("Output");
		
		//Initialize fields
		userInput = new JTextField(50);
		key = new JTextField(3);
		outputField = new JTextField(50);
		userInput.setHorizontalAlignment(JTextField.LEFT);
		outputField.setHorizontalAlignment(JTextField.LEFT);
			
		//Initialize Buttons
		encrypt = new JButton("Encrypt");
		decrypt = new JButton("Decrypt");
		learnFromPlaintext = new JButton("Learn Plaintext");
		encrypt.addActionListener(this);
		decrypt.addActionListener(this);
		learnFromPlaintext.addActionListener(this);
		
		//Initialize panel for Warnings
		JPanel warningPanel = new JPanel(new GridLayout(2, 1));
		
		//Add warnings to warningPanel
		warningPanel.add(characterWarnings);
		warningPanel.add(decryptionWarnings);
		
		//Initialize panel for labels and fields (left and right)
		JPanel inputPanelLeft = new JPanel(new GridLayout(4, 2));
		JPanel inputPanelRight = new JPanel(new GridLayout(2, 2));
		
		//Add stuff to inputPanel
		
		inputPanelLeft.add(luserInput);
		inputPanelRight.add(lkey);
		inputPanelLeft.add(userInput);
		inputPanelRight.add(key);
		inputPanelLeft.add(loutput);
		inputPanelLeft.add(outputField);
		
		//Initialize panel for buttons
		JPanel buttonPanel = new JPanel();
		
		//Add stuff to buttonPanel
		buttonPanel.add(encrypt);
		buttonPanel.add(decrypt);
		buttonPanel.add(learnFromPlaintext);
		
		//Set up window
		this.setLayout(new BorderLayout());
		this.add(warningPanel, BorderLayout.NORTH);
		this.add(inputPanelLeft, BorderLayout.WEST);
		this.add(inputPanelRight, BorderLayout.EAST);
		this.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == encrypt) {
			int k = Integer.parseInt(key.getText());
			String encryptedText = Encryptor.encrypt(userInput.getText(), k);
			outputField.setText(encryptedText);
		} else if(e.getSource() == decrypt) {
			try{
				int k = Integer.parseInt(key.getText());
				String decryptedText = Decryptor.decryptWithKey(userInput.getText(), k);
				outputField.setText(decryptedText);
			} catch (NumberFormatException nfex) {
				String decryptedText = Decryptor.decryptWithoutKey(userInput.getText());
				outputField.setText(decryptedText);
			} catch (Exception ex) {
				System.out.println("Unknown Exception");
				ex.printStackTrace();
			} // End try/catch
		} else {
			String toLearn = userInput.getText();
			Decryptor.saveFrequencyChart(toLearn);
			Decryptor.loadFrequencyChart();
		} //End if
	} // End actionPerformed
	
	public static void main(String[] args) {
		CryptoLauncher cl = new CryptoLauncher();
		cl.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

} //End CryptoLauncher Class

class Decryptor{

	private static ArrayList<String> frequencyChart = new ArrayList<String>(Arrays.asList(" ", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "."));
	private static ArrayList<Integer> frequencyChartHistory = new ArrayList<Integer>(Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1));
	private static ArrayList<String> charsInCiphertext = new ArrayList<String>(); /*This array will hold the chars
	 																		*that correspond to the frequency
	 																		* in freqInCiphertext with the same index
	 																		*/
	private static ArrayList<Integer> freqInCiphertext = new ArrayList<Integer>();	

	
	
	public static String decryptWithKey(String ciphertext, int key) {
		char[] array = ciphertext.toCharArray();
		
		for(int index = 0; index < array.length; index++){
			array[index] -= key;
		}// End For
		
		String plaintext = new String(array);
		return plaintext;
	}// End decryptWithKey
	
	public static String decryptWithoutKey(String ciphertext) {
		System.out.println("Running Brute Force Crack...");
		
		Decryptor.loadFrequencyChart();
		
		//Begin iteration for frequency analysis
		System.out.println("Running Frequency Analysis");
		
		for(int index = 0; index < ciphertext.length(); index++){
			String c = Character.toString(ciphertext.charAt(index));
			if(charsInCiphertext.contains(c)) {
				int cIndex = charsInCiphertext.indexOf(c);
				int count = freqInCiphertext.get(cIndex);
				count ++;
				freqInCiphertext.set(charsInCiphertext.indexOf(c), count);
			} else {
				int count = 1;
				charsInCiphertext.add(c);
				freqInCiphertext.add(charsInCiphertext.indexOf(c) , count);
			} // End If
		}// End for
		
		//Begin Sorting through frequency chart, preparing for substitution;
		System.out.println("Running quickSort");
		quickSort(0, (freqInCiphertext.size() - 1));
		
		//Begin substitution
		System.out.println("Running Substitution");
		
		//Make charsInCiphertext & frequencyChart the same size
		while(charsInCiphertext.size() < frequencyChart.size()) {
			charsInCiphertext.add(null);
		}
		
		//Cast charsInCiphertext & frequencyChart as arrays
		String[] charsInCiphertextArray = charsInCiphertext.toArray(new String[charsInCiphertext.size()]);
		String[] frequencyChartArray = frequencyChart.toArray(new String[frequencyChart.size()]);
		
		try{
			ciphertext = StringUtils.replaceEach(ciphertext, charsInCiphertextArray, frequencyChartArray);
		} catch(IndexOutOfBoundsException ex) {
			System.out.println("Invalid character entered. Check input for upper case and non alphabetic characters such as numbers and punctuation");
		} catch(IllegalArgumentException ex) {
			System.out.println("Invalid character entered. Check input for upper case and non alphabetic characters such as numbers and punctuation");
		}
		
		System.out.println(charsInCiphertext);
		System.out.println(frequencyChart);
		
		//Return decrypted text
		return ciphertext;
		
	} // End decryptWithoutKey

	private static void swap(int i, int j) {
		int tempInt = freqInCiphertext.get(i);
		String tempString = charsInCiphertext.get(i);
		
		freqInCiphertext.set(i, freqInCiphertext.get(j));
		charsInCiphertext.set(i, charsInCiphertext.get(j));
		
		freqInCiphertext.set(j, tempInt);
		charsInCiphertext.set(j, tempString);
	}// End swap
	
	private static void quickSort(int low, int high){
 
		/* quickSort code derived from the method named
		 * quickSortInDescendingOrder that can be found
		 * at http://www.softwaredevelopersworld.com/samplecodes/
		 * java/beginners/sorting/QuickSortDescending.php 
		 */
		
        int i = low;
        int j = high;
        long middle = freqInCiphertext.get((low+high)/2);
 
        while (i < j) {
            while (freqInCiphertext.get(i) > middle) {
                i++;
            } //End while
            while (freqInCiphertext.get(j) < middle) {
                j--;
            }//End while
            if (j >= i) {
                swap(i, j);
                i++;
                j--;
            } // End if
        } // End while
 
 
        if (low<j) {
            quickSort(low, j);
        } 
        
        if (i<high) {
            quickSort(i, high);
        } //end if
    }//End quickSort

	private static void quickSortFC(int low, int high){
		
		/* 
		 * quickSort code derived from the method named
		 * quickSortInDescendingOrder that can be found
		 * at http://www.softwaredevelopersworld.com/samplecodes/
		 * java/beginners/sorting/QuickSortDescending.php 
		 */
		
        int i = low;
        int j = high;
        int middle = frequencyChartHistory.get(((low+high)/2));
 
        while (i < j) {
            while (frequencyChartHistory.get(i) > middle) {
                i++;
            } //End while
            while (frequencyChartHistory.get(j) < middle) {
                j--;
            }//End while
            if (j >= i) {
                swapFC(i, j);
                i++;
                j--;
            } // End if
        } // End while
 
 
        if (low < j) {
            quickSortFC(low, j);
        }
        if (i < high) {
            quickSortFC(i, high);
        } //end if
	}
	
	private static void swapFC(int i, int j){
		int tempInt = frequencyChartHistory.get(i);
		String tempString = frequencyChart.get(i);
		
		frequencyChartHistory.set(i, frequencyChartHistory.get(j));
		frequencyChart.set(i, frequencyChart.get(j));
		
		frequencyChartHistory.set(j, tempInt);
		frequencyChart.set(j, tempString);
	}
	
	@SuppressWarnings("unchecked")
	public static void loadFrequencyChart() {
		
		ObjectOutputStream outputStream = null;
		ObjectInputStream inputStream = null;
		
		try {
            inputStream = new ObjectInputStream(new FileInputStream("frequencyChartHistory.dat"));
            frequencyChartHistory = (ArrayList<Integer>) inputStream.readObject();
		} catch (FileNotFoundException e) {
            System.out.println("FNF Error: " + e.getMessage());
		} catch (IOException e) {
            System.out.println("IO Error: " + e.getMessage());
		} catch (ClassNotFoundException e) {
            System.out.println("CNF Error: " + e.getMessage());
		} finally {
            try {
            	if (outputStream != null) {
            		outputStream.flush();
            		outputStream.close();
            	}
            } catch (IOException e) {
            	System.out.println("IO Error: " + e.getMessage());	
            }//End Catch
		}//End Catch
		
		try {
            inputStream = new ObjectInputStream(new FileInputStream("frequencyChart.dat"));
            frequencyChart = (ArrayList<String>) inputStream.readObject();
		} catch (FileNotFoundException e) {
            System.out.println("FNF Error: " + e.getMessage());
		} catch (IOException e) {
            System.out.println("IO Error: " + e.getMessage());
		} catch (ClassNotFoundException e) {
            System.out.println("CNF Error: " + e.getMessage());
		} finally {
            try {
            	if (outputStream != null) {
            		outputStream.flush();
            		outputStream.close();
            	}
            } catch (IOException e) {
            	System.out.println("IO Error: " + e.getMessage());	
            }//End Catch
		}//End Catch
		
		quickSortFC(0, frequencyChartHistory.size()-1);
		System.out.println(frequencyChart);
		System.out.println(frequencyChartHistory);
		
	} //End loadFrequencyChart
	
	public static void saveFrequencyChart(String toLearn){
		//Begin Save Preparation
		//Format Strings (i.e. remove upper case and punctuation)
		toLearn = toLearn.toLowerCase();
		toLearn = toLearn.replaceAll("[^a-z]", " ");
		System.out.println(toLearn);
		
		System.out.println("Beggining save");
		
		ArrayList<String> tempChart = new ArrayList<String>();
		ArrayList<Integer> tempHistory = new ArrayList<Integer>();
		
		for(int index = 0; index < toLearn.length(); index++){
			String c = Character.toString(toLearn.charAt(index));
			if(tempChart.contains(c)) {
				int count = tempHistory.get(tempChart.indexOf(c));
				count ++;
				tempHistory.set(tempChart.indexOf(c), count);
			} else {
				int count = 1;
				tempChart.add(c);
				tempHistory.add(tempChart.indexOf(c), count);
			} // End If
		}// End for
		System.out.println("Count done");
		
		System.out.println(tempChart);
		System.out.println(tempHistory);
		
		Decryptor.loadFrequencyChart();
		if(frequencyChart != null){
			for(int i = 0; i < frequencyChart.size(); i++) {
				String s = frequencyChart.get(i);
				if(tempChart.contains(s) && frequencyChart.contains(s)){
					int count = frequencyChartHistory.get(i) + tempHistory.get(tempChart.indexOf(s));
					frequencyChartHistory.set(i, count);
				} else if (tempChart.contains(s) && !(frequencyChart.contains(s))) {
					frequencyChart.add(s);
					frequencyChartHistory.add(1);
				}//End if
			} // End for
		} else {
			frequencyChart = tempChart;
			frequencyChartHistory = tempHistory;
		}
		
		quickSortFC(0, frequencyChartHistory.size() - 1);

        System.out.println(frequencyChart);
        System.out.println(frequencyChartHistory);
		
		//Begin Save

        //Initialising an in and outputStream for working with the file
        ObjectOutputStream outputStream = null;
		
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream("frequencyChartHistory.dat"));
            System.out.println("Saving...");
            outputStream.writeObject(frequencyChartHistory);
            System.out.println("Save done");
        } catch (FileNotFoundException e) {
            System.out.println("FNF Error: " + e.getMessage() + ",the program will try and make a new file");
        } catch (IOException e) {
            System.out.println("IO Error: " + e.getMessage());
        } finally {
            try {
            	if (outputStream != null) {
            		outputStream.flush();
                    outputStream.close();
            	}//End if
            } catch (IOException e) {
            	System.out.println("Error: " + e.getMessage());
            } //End catch
        }//End catch
        
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream("frequencyChart.dat"));
            System.out.println("Saving...");
            outputStream.writeObject(frequencyChart);
            System.out.println("Save done");
        } catch (FileNotFoundException e) {
            System.out.println("FNF Error: " + e.getMessage() + ",the program will try and make a new file");
        } catch (IOException e) {
            System.out.println("IO Error: " + e.getMessage());
        } finally {
            try {
            	if (outputStream != null) {
            		outputStream.flush();
                    outputStream.close();
            	}//End if
            } catch (IOException e) {
            	System.out.println("Error: " + e.getMessage());
            } //End catch
        }//End catch
        
	}//End saveFrequencyChart
}// End Decryptor Class

class Encryptor {
	public static String encrypt(String toEncrypt, int offset) {
		
		//Format Strings (i.e. remove upper case and punctuation)
		toEncrypt = toEncrypt.toLowerCase();
		toEncrypt = toEncrypt.replaceAll("[^a-z0-9]", " ");
		System.out.println(toEncrypt);
		
		char[] array = toEncrypt.toCharArray();
		for(int index = 0; index < array.length; index++){
			array[index] += offset;
		}// End For
		String encryptedString = new String(array);
		return encryptedString;
	}// End encrypt
}// End Encryptor
