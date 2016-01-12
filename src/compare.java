import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;

public class compare {

	public static void main(String[] args) {
		
		class documentSet {
			String filename;
			HashSet<String> set1;
			HashSet<String> set2;
			
			public documentSet(String filename, HashSet<String> set1, HashSet<String> set2){
				this.filename = filename;
				this.set1 = set1;
				this.set2 = set2;
			}
		}
		
        try{
        	String root1 = "/home/michael/Downloads/solr/plain/";
    		String root2 = "/home/michael/Downloads/solr/capisco/";
    		
    		ArrayList<documentSet> docs = new ArrayList<documentSet>();

            BufferedReader reader;
        	
            //loop through each file in directory 1 and build sets for this file and the matching file in the second directory
			for (File sub : FileUtils.listFiles(new File(root1), null, true))
			{
				File sub2 = new File(root2 + sub.getName()); //get the matching file in the other directory
				HashSet<String> set1 = new HashSet<String>();
				HashSet<String> set2 = new HashSet<String>();
				
				reader = new BufferedReader(new FileReader(sub));
				String line;
		        while ((line = reader.readLine()) != null){
		          set1.add(line);
		        }
		        
		        reader = new BufferedReader(new FileReader(sub2));
		        while ((line = reader.readLine()) != null){
		          set2.add(line);
		        }
		        
		        docs.add(new documentSet(sub.getName(), set1, set2));
			}
			
			System.out.println("Sets Built");
			HashSet<String> diff1total = new HashSet<String>();
			HashSet<String> diff2total = new HashSet<String>();
			HashSet<String> commontotal = new HashSet<String>();
			String statstotal = "";
			
			for(documentSet d : docs){
				HashSet<String> diff1 = new HashSet<String>();
				HashSet<String> diff2 = new HashSet<String>();
				HashSet<String> common = new HashSet<String>();
				
				PrintWriter diff1w = new PrintWriter("results/" + d.filename + "-plain-unique.txt", "UTF-8");
	    		PrintWriter diff2w = new PrintWriter("results/" + d.filename + "-capisco-unique.txt", "UTF-8");
	    		PrintWriter commonw = new PrintWriter("results/" + d.filename + "-common-set.txt", "UTF-8");
	    		PrintWriter statsw = new PrintWriter("results/" + d.filename + "-stats.txt", "UTF-8");
				
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
			
			String tmp = "";
			tmp += "TOTAL :: Solr Unique Element Count: " + diff1total.size() + "\n";
			tmp += "TOTAL :: Capisco Unique Element Count: " + diff2total.size() + "\n";
			tmp += "TOTAL :: Shared/Common Element Count: " + commontotal.size() + "\n\n";
			tmp += "********************************\n";
			statstotal = tmp + statstotal;
			
    		PrintWriter statsw = new PrintWriter("results/_TOTAL-stats.txt", "UTF-8");
    		statsw.write(statstotal);
    		statsw.close();
        }
        catch(Exception e){
        	e.printStackTrace();
        }
	}
}
