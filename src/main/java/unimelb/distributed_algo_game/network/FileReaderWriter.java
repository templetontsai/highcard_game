package unimelb.distributed_algo_game.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Arjun
 *
 */
public class FileReaderWriter {
	public int totalNodes = 0;
	public Map<String, List<String>> map = new HashMap<String, List<String>>();

	/**
	 * This method is used to read config file and then print its content after
	 * storing it in a map and then returns total number of nodes
	 **/
	public int readConfig() {
		try (BufferedReader br = new BufferedReader(new FileReader("playersMachineParameters.txt"))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				if (!sCurrentLine.startsWith("#")) {
					String[] tokens = sCurrentLine.split(" ");
					if (tokens[1].equals("#")) {
						totalNodes = Integer.parseInt(tokens[0]);
						System.out.println(totalNodes);
					} else {
						// storing the hostname and port corresponding to
						// machine id
						List<String> valueList = new ArrayList<String>();
						valueList.add(tokens[1]);
						valueList.add(tokens[2]);
						map.put(tokens[0], valueList);
					}
				}
			}

			// Testing the HashMap output
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				String key = entry.getKey();
				List<String> values = entry.getValue();
				System.out.println("Key = " + key);
				System.out.println("Values = " + values);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return totalNodes;
	}

	/**
	 * Returns the socket connection details for the client
	 * 
	 * @param id
	 * @return
	 */
	public List getClientDetails(int id) {
		List<String> valueList = new ArrayList<String>();
		valueList = (List) map.get(id);
		return valueList;
	}

}
