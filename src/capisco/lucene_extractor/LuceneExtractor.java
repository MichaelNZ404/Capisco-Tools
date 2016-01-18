package capisco.lucene_extractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class LuceneExtractor
{
	final static String namefield = "path";
	final static String valuefield = "topics";
	final static String pagefield = "pageNum";
	final static String countfield = "count";
	
	private static String index;
	private static String output;
	private static String source;
	private static luceneInfo information;
	
	private static class luceneInfo{
		public String name;
		public String[] concepts;
		public String[] counts;
		public String[] pages;
		
		luceneInfo(String name, String[] concepts, String[] counts, String[] pages){
			this.name = name;
			this.concepts = concepts;
			this.counts = counts;
			this.pages = pages;
		}
	}
	
	/**
	 * 
	 * @param args[0] directory of index
	 * @param args[1] directory of output
	 * 
	 * @param args[2] directory of source files -- optional for bookworm
	 * 
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception 
	{
		index = args[0];
		output = args[1];
		
		build();
		textDump();
		bookwormDump();
		if(args.length > 2){
			source = args[2];
			greenstoneDump();
		}
	}   
    
	private static void build() throws IOException{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index).toPath()));
		IndexSearcher searcher = new IndexSearcher(reader);
		
		//get all page/volume names in index
		HashSet<String> docNames = new HashSet<String>();
		for (int i=0; i<reader.maxDoc(); i++) {
		    Document doc = reader.document(i);
		    docNames.add(doc.get(namefield));
		}
		
		//for each name, get the associated term
		for(String docName : docNames){	
			String docTitle = docName.substring(docName.lastIndexOf("/")+1) + ".txt";
			
			Term term = new Term(namefield, docName);
			TermQuery tq = new TermQuery(term);		
			TopDocs results = searcher.search(tq, 1000);
			ScoreDoc[] hits = results.scoreDocs;
			
        	Document doc = searcher.doc(hits[0].doc);
			information = new luceneInfo(docTitle, doc.getValues(valuefield), doc.getValues(countfield), doc.getValues(pagefield));
		}
	}
	
	private static void textDump() throws IOException
	{	
		File docDir = new File(output);
		if (!docDir.exists()){
			docDir.mkdir();
		}
	
		File writename = new File(output + information.name);
		BufferedWriter out = new BufferedWriter(new FileWriter(writename,true));
		if(information.pages.length > 0){
			for(int j = 0; j < information.concepts.length; j++){					 
        	out.write(information.concepts[j] + "\t" + information.counts[j] + "\t" + information.pages[j] + "\n");
			}
		}
		else{
			for(int j = 0; j < information.concepts.length; j++){					 
            	//out.write(resultConcepts[j] + "\t" + resultCounts[j] + "\n");
            	out.write(information.concepts[j] + "\n");
			}
		}
		out.close();
	}
	
	public static void greenstoneDump(){
		
	}
	public static void bookwormDump(){
		
	}
}
