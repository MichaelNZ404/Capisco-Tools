import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;

public class compare {

	public static void main(String[] args) {	
		//args[0] location of first directory - solr
		//args[1] location of second directory - capisco
		//args[2] location of the output directory
		
		class documentSet {
			String filename; //name of the file in both directories
			HashSet<String> set1; //the set from one directory
			HashSet<String> set2; //the set from the second directory
			
			public documentSet(String filename, HashSet<String> set1, HashSet<String> set2){
				this.filename = filename;
				this.set1 = set1;
				this.set2 = set2;
			}
		}
		
        try{
        	String root1 = args[0]; //location of first directory
    		String root2 = args[1]; //location of second directory
    		String output = args[2]; //location of the output directory
        	
    		ArrayList<documentSet> docs = new ArrayList<documentSet>();
            BufferedReader reader;
        	
            //loop through each file in directory 1 and build sets for this file and the matching file in the second directory
			for (File sub : FileUtils.listFiles(new File(root1), null, true))
			{
				File sub2 = new File(root2 + sub.getName()); //get the matching file in the other directory
				HashSet<String> set1 = new HashSet<String>();
				HashSet<String> set2 = new HashSet<String>();
				
				//read all from first file and add to set1
				reader = new BufferedReader(new FileReader(sub));
				String line;
		        while ((line = reader.readLine()) != null){
		          set1.add(line);
		        }
		        
		        //read all from second file and add to set2
		        reader = new BufferedReader(new FileReader(sub2));
		        while ((line = reader.readLine()) != null){
		          set2.add(line);
		        }
		        docs.add(new documentSet(sub.getName(), set1, set2)); //store object in arraylist
			}
			
			System.out.println("Sets Built");
			
			//objects to hold total difference and statistics for all files
			HashSet<String> diff1total = new HashSet<String>();
			HashSet<String> diff2total = new HashSet<String>();
			HashSet<String> commontotal = new HashSet<String>();
			String statstotal = "";
			
			//for each document create sets and print to results directory
			for(documentSet d : docs){
				File docDir = new File(output + d.filename);
				if (!docDir.exists()){
					docDir.mkdir();
				}
				
				HashSet<String> diff1 = new HashSet<String>();
				HashSet<String> diff2 = new HashSet<String>();
				HashSet<String> common = new HashSet<String>();
				
				PrintWriter diff1w = new PrintWriter(docDir + "/" + d.filename + "-plain-unique.txt", "UTF-8");
	    		PrintWriter diff2w = new PrintWriter(docDir+ "/" + d.filename + "-capisco-unique.txt", "UTF-8");
	    		PrintWriter commonw = new PrintWriter(docDir + "/" + d.filename + "-common-set.txt", "UTF-8");
	    		PrintWriter statsw = new PrintWriter(docDir + "/" + d.filename + "-stats.txt", "UTF-8");
				
				diff1.addAll(d.set1);
				diff1.removeAll(d.set2);
				diff1w.write(diff1.toString().replaceAll(",", "\n"));
				diff1w.close();
				
				diff2.addAll(d.set2);
				diff2.removeAll(d.set1);
				diff2w.write(diff2.toString().replaceAll(",", "\n"));
				diff2w.close();
				
				common.addAll(d.set1);
				common.addAll(d.set2);
				common.removeAll(diff1);
				common.removeAll(diff2);
				commonw.write(common.toString().replaceAll(",", "\n"));
				commonw.close();
				
				statsw.write("Solr Unique Element Count: " + diff1.size() + "\n");
				statsw.write("Capisco Unique Element Count: " + diff2.size() + "\n");
				statsw.write("Shared/Common Element Count: " + common.size() + "\n");
				statsw.close();
				
				diff1total.addAll(diff1);
				diff2total.addAll(diff2);
				commontotal.addAll(common);
				statstotal += d.filename + ":: Solr Unique Element Count: " + diff1.size() + "\n";
				statstotal += d.filename + ":: Capisco Unique Element Count: " + diff2.size() + "\n";
				statstotal += d.filename + ":: Shared/Common Element Count: " + common.size() + "\n\n";
			}
			
			//create total stats file
			String tmp = "";
			tmp += "TOTAL :: Solr Unique Element Count: " + diff1total.size() + "\n";
			tmp += "TOTAL :: Capisco Unique Element Count: " + diff2total.size() + "\n";
			tmp += "TOTAL :: Shared/Common Element Count: " + commontotal.size() + "\n\n";
			tmp += "********************************\n\n";
			statstotal = tmp + statstotal;
    		PrintWriter statsw = new PrintWriter(output + "_TOTAL-stats.txt", "UTF-8");
    		statsw.write(statstotal);
    		statsw.close();
    		
    		System.out.println("Completed!");
        }
        catch(Exception e){
        	e.printStackTrace();
        }
	}
}
