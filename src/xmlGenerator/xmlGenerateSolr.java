package xmlGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import capisco.lucene_extractor.luceneInfo;

public class xmlGenerateSolr {
	private String source;
	public xmlGenerateSolr(String source){
			this.source = new String(source);	
	}
	
	public Document buildDocument(String docPath, luceneInfo Info, String index, String namefield)
	{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try{
			docBuilder = docFactory.newDocumentBuilder();
		}catch(Exception e){ System.out.println("builder exception");}
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("Archive");
		doc.appendChild(rootElement);
		
		Element baseSection = doc.createElement("Section");
		rootElement.appendChild(baseSection);
		Element baseDescription = doc.createElement("Description");
		baseSection.appendChild(baseDescription);
		String forIE =Info.name.substring(Info.name.indexOf("=") + 1, Info.name.lastIndexOf("=")) + "/" + 
				Info.name.substring(Info.name.lastIndexOf("=") + 1, Info.name.length());
		
		String fordocName = Info.name.substring(0,Info.name.indexOf("+")) + ":/" + Info.name.substring(Info.name.indexOf("=") + 1, Info.name.lastIndexOf("=")) + "/" + 
				Info.name.substring(Info.name.lastIndexOf("=") + 1, Info.name.length());
		String forIdentifier = Info.name.substring(0,Info.name.indexOf("+")) + Info.name.substring(Info.name.indexOf("=") + 1, Info.name.lastIndexOf("=")) +
				Info.name.substring(Info.name.lastIndexOf("=") + 1, Info.name.length());
		
		String[] characs = {"docName", "docNameIE", "dc.Title", "Identifier", "gsdlsourcefilename", "gsdldoctype"};
		String[] values = {fordocName, forIE, forIdentifier, forIdentifier, "import/test1.xml", "indexed_doc"};
		for(int j = 0; j < characs.length; ++j){
			Element baseMeta1 = doc.createElement("Metadata");
			Attr baseAttr1 = doc.createAttribute("name");
			baseAttr1.setValue(characs[j]);
			baseMeta1.setAttributeNode(baseAttr1);
			baseMeta1.appendChild(doc.createTextNode(values[j]));
			baseDescription.appendChild(baseMeta1);
		}
		
		File root = new File(source);
		try{
			for (File sub : FileUtils.listFiles(root, null, true)){
				if(sub.getName().substring(0, sub.getName().lastIndexOf(".")).equalsIgnoreCase(Info.name)){
					ZipFile zf = new ZipFile(sub);	
					Enumeration<? extends ZipEntry> e = zf.entries();
					ZipEntry ze = e.nextElement();
					int i = 1;
					for (; e.hasMoreElements();) {
						String pageContent = null;
						ze = e.nextElement();
						long size = ze.getSize();
						if (size > 0) 		
							pageContent = fromStream(zf.getInputStream(ze));
	
						Element pageSectionEle = doc.createElement("Section");
						baseSection.appendChild(pageSectionEle);
		
						Element pageDescription = doc.createElement("Description");
						pageSectionEle.appendChild(pageDescription);		
						{
							Element pageNumMeta = doc.createElement("Metadata");
							Attr pageNumAttr = doc.createAttribute("name");
							pageNumAttr.setValue("Title");
							pageNumMeta.setAttributeNode(pageNumAttr);
							pageNumMeta.appendChild(doc.createTextNode("Page" + i));
							pageDescription.appendChild(pageNumMeta);
						}
						
						ArrayList<String> conceptsList = this.getConceptsForXML(Info.name, new Integer(i).toString(), index, namefield);
						
						if(conceptsList != null){
							for(String concept : conceptsList){
								Element conceptEle = doc.createElement("Metadata");
								Attr conceptAttr = doc.createAttribute("name");
								conceptAttr.setValue("concept");
								conceptEle.setAttributeNode(conceptAttr);
								conceptEle.appendChild(doc.createTextNode(concept));
								pageDescription.appendChild(conceptEle);
							}
						}
						
						Element pageContentEle = doc.createElement("Content");	
						pageContentEle.appendChild(doc.createTextNode(pageContent));
						pageSectionEle.appendChild(pageContentEle);		
						++i;
					}
					zf.close();
				
				}
			
			}
		}catch(Exception e){ 
			System.out.println("IOException happens at here");
		}		
		return doc;
	}
	
	private ArrayList<String> getConceptsForXML(String docName, String pageNum, String index, String namefield) throws ParseException, IOException
	{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index).toPath()));
		IndexSearcher searcher = new IndexSearcher(reader);
		//System.out.println(docName);
		Term term = new Term(namefield, docName);
		TermQuery tq = new TermQuery(term);		
		TopDocs results = searcher.search(tq, 1000);
		
		ScoreDoc[] hits = results.scoreDocs;
		//System.out.println(hits.length);
		String[] resultConcepts = null;
		for (int i = 0; i < hits.length; ++i) 
		{
			org.apache.lucene.document.Document doc = searcher.doc(hits[i].doc);
			String pagek = doc.getValues("pageNum")[0];
			//System.out.println(pagek);
			int forpagek = 0;
			while(pagek.charAt(forpagek) == '0')
				++forpagek;
			//System.out.println(forpagek);
			String cutit = pagek.substring(forpagek, pagek.length());
	//		System.out.println(cutit);
			
			
			if(cutit.equalsIgnoreCase(pageNum)){
				resultConcepts = doc.getValues("topics");			
			}
		}

		if(resultConcepts == null){
			return null;		
		}
		else{
			ArrayList<String> conceptsArray = new ArrayList<String>();
			for(int j = 0; j < resultConcepts.length; ++j){
				conceptsArray.add(resultConcepts[j]);
			}

		return conceptsArray;	
		}
	}
	
	private String fromStream(InputStream in) throws IOException
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

	public void saveXML(Document doc, String outputPath, String docName) throws Exception
	{
		try{
			DOMImplementation impl = doc.getImplementation();
			DOMImplementationLS implLS = (DOMImplementationLS) impl.getFeature("LS", "3.0");
			LSSerializer ser = implLS.createLSSerializer();
			ser.getDomConfig().setParameter("format-pretty-print", true);
			LSOutput out = implLS.createLSOutput();
			out.setEncoding("UTF-8");	
			
			File outputdir = new File(outputPath + "/" + docName + ".dir");
			if(!outputdir.exists())
				outputdir.mkdir();	
			File outputfile = new File(outputPath + "/" + docName + ".dir/doc.xml");
			outputfile.createNewFile();
			Path path = outputfile.toPath();
			out.setByteStream(Files.newOutputStream(path));
			ser.write(doc, out);
			System.out.println("XML file saved at" + outputPath + "/" + docName + "successfully");
		}catch(Exception e){
			System.out.println("Saving Exception happens at here");
			e.printStackTrace();
			e.notifyAll();
		}	
	}
}
