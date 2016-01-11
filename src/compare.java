import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

public class compare {

	public static void main(String[] args) {
		
        try{
        	String path1 = "/home/michael/Downloads/solr/plain";
    		String path2 = "/home/michael/Downloads/solr/capisco";
    		
    		PrintWriter diff1w = new PrintWriter("plain-unique.txt", "UTF-8");
    		PrintWriter diff2w = new PrintWriter("capisco-unique.txt", "UTF-8");
    		PrintWriter commonw = new PrintWriter("common-set.txt", "UTF-8");
    		PrintWriter statsw = new PrintWriter("stats.txt", "UTF-8");
    		
    		HashSet<String> set1 = new HashSet<String>();
    		HashSet<String> set2 = new HashSet<String>();
    		//ArrayList<HashSet> set1 = new ArrayList<HashSet>();
    		//ArrayList<HashSet> set2 = new ArrayList<HashSet>();

            BufferedReader reader1;
            BufferedReader reader2;
        	
			for (File sub : FileUtils.listFiles(new File(path1), null, true))
			{
				reader1 = new BufferedReader(new FileReader(sub));
				String line;
		        while ((line = reader1.readLine()) != null){
		          set1.add(line);
		        }
			}
			
			for (File sub : FileUtils.listFiles(new File(path2), null, true))
			{
				reader2 = new BufferedReader(new FileReader(sub));
				String line;
		        while ((line = reader2.readLine()) != null){
		          set2.add(line);
		        }
			}
       
			System.out.println("Sets Built");
			
			HashSet<String> diff1 = new HashSet<String>();
			HashSet<String> diff2 = new HashSet<String>();
			HashSet<String> common = new HashSet<String>();
			
			diff1.addAll(set1);
			diff1.removeAll(set2);
			System.out.println("Difference in set 1:");
			System.out.println(diff1.toString());
			diff1w.write(diff1.toString().replaceAll(",", "\n"));
			diff1w.close();
			
			diff2.addAll(set2);
			diff2.removeAll(set1);
			System.out.println("Difference in set 2:");
			System.out.println(diff2.toString());
			diff2w.write(diff2.toString().replaceAll(",", "\n"));
			diff2w.close();
			
			common.addAll(set1);
			common.addAll(set2);
			common.removeAll(diff1);
			common.removeAll(diff2);
			System.out.println("Common Elements:");
			System.out.println(common.toString());
			commonw.write(common.toString().replaceAll(",", "\n"));
			commonw.close();
			
			statsw.write("Solr Unique Element Count: " + diff1.size() + "\n");
			statsw.write("Capisco Unique Element Count: " + diff2.size() + "\n");
			statsw.write("Shared/Common Element Count: " + common.size() + "\n");
			statsw.close();
        }
        catch(Exception e){
        	e.printStackTrace();
        }
	}
}
