import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.BreakIterator;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

public class luceneBuilder
{
	File root;
	IndexWriter writer;
	
	public luceneBuilder(File root, File index) throws Exception
	{
		this.root = root;
		writer = createIndex(index);
	}

	public static String fromStream(InputStream in) throws IOException
	{
	    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	    StringBuilder out = new StringBuilder();
	    String newLine = System.getProperty("line.separator");
	    String line;
	    while ((line = reader.readLine()) != null) {
	        out.append(line);
	        out.append(newLine);
	    }
	    return out.toString();
	}
	
	public void run()
	{
		try
		{	
			for (File sub : FileUtils.listFiles(root, null, true))
			{
				if(sub.getName().endsWith(".zip")) //zipfile handler
				{
					ZipFile zf = new ZipFile(sub);
					String name = null;
					for (Enumeration<? extends ZipEntry> e = zf.entries(); e.hasMoreElements();) {//zip entries havent been closed, the same as the stream. hard to decide where to close them 
						ZipEntry ze = e.nextElement();
						long size = ze.getSize();

						if (size > 0) {
							name = ze.getName();
							String tit = name.substring(name.lastIndexOf("/")+1);
							
							String wholeContent = fromStream(zf.getInputStream(ze));
							BreakIterator wordIterator = BreakIterator.getWordInstance(); 
							wordIterator.setText(wholeContent);							
							printEachForward(wordIterator, wholeContent, tit);
											
						}
					}
					zf.close();
				}							
			}
			System.out.println("All files queued for processing, awaiting termination");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void printEachForward(BreakIterator boundary, String source, String name)throws IOException {
		int start = boundary.first();
	     for (int end = boundary.next();
	          end != BreakIterator.DONE;
	          start = end, end = boundary.next()) {
	    	  this.save(source.substring(start,end), name);
	     }
	 }
	
	public void close() throws IOException
	{
		System.out.println("Closing lucene index writer");
		writer.commit();
		System.out.println("writer is closed");
	}
	
	protected void save(String retravls, String path)   //create lucene index here
	{		
		Document doc = new Document();
		doc.add(new StringField("path", path, Field.Store.YES));
		doc.add(new StringField("topics", retravls, Field.Store.YES));		
		try
		{
			writer.addDocument(doc);
		}
		catch (IOException e)
		{
			String message = "Failed to add " + path + " to index";
			throw new RuntimeException(message, e);
		}
	}
	
	static IndexWriter createIndex(File location) throws IOException
	{
		try
		{
			FSDirectory dir = FSDirectory.open(location.toPath());
			Analyzer analyzer = new EnglishAnalyzer();
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			config.setOpenMode(IndexWriterConfig.OpenMode.valueOf("CREATE_OR_APPEND"));
			return new IndexWriter(dir, config);
		}
		catch (IOException e)
		{
			System.err.format("Could not create index writer (%1$s)", e.getMessage());
			throw e;
		}
	}
	
	public static void main(String[] args)
	{
		// arg[0] = input documents directory path
		// arg[1] = where to create/append to the index
		
		if (args.length < 2)
		{
			System.out.println("Use: WMindexer <server URI> <server port> <input documents path> <index path> [number of workers] [database]");
			return;
		}
		
		File documents = new File(args[0]);
		File index = new File(args[1]);
		
		try
		{
			luceneBuilder indexer = new luceneBuilder(documents, index);
			indexer.run();
			indexer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}