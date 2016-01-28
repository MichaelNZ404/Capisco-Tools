package capisco.bookwormextractor.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;

/**
 * 
 * @author mjc62
 * Used to analyze the dickens texts, by splitting on a certain line to divide into chapters.
 */
public class DelimiterSplit{
	
	public void RunDelimiterSplit(String input, String output) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String delimiter = "Chapter "; //if this occurs in the text then thats bad
		
		String line;
		File capiscodir = new File(output + "capisco/");
		capiscodir.mkdirs();
		File metadir = new File(output + "meta/");
		metadir.mkdirs();
		//this writer creates a vanilla input in the meta directory for comparison
		BufferedWriter iwriter = new BufferedWriter(new FileWriter(new File(metadir.getPath() + "/input.txt")));
		
		int count = 1;
		BufferedWriter cwriter = null;
		String chapterbuff = reader.readLine() + "\n";
		while((line = reader.readLine()) != null){
			//if this is a new chapter create a new file
			if(line.startsWith(delimiter)){
				cwriter = new BufferedWriter(new FileWriter(new File(capiscodir.getPath() + "/" + count + ".txt")));
				cwriter.write(chapterbuff);
				cwriter.close();
				iwriter.write(count + ".txt\t" + chapterbuff.replace('\n', ' ').replace('\t', ' ') + "\n");
				
				JsonObject value = Json.createObjectBuilder()
					     .add("searchstring", "<a href=https://www.gutenberg.org/ebooks/766>David Copperfield</a>")
					     .add("filename", count + ".txt")
					     .add("chapter", count)
					     .add("author", "Charles Dickens")
					     .build();
				JsonWriterFactory factory = Json.createWriterFactory(value);
				FileWriter fwriter = new FileWriter(new File(metadir.getPath() + "/jsoncatalog.txt"), true);
				JsonWriter jwriter = factory.createWriter(fwriter);
				jwriter.writeObject(value);
				fwriter.write("\n");
				jwriter.close();
				
				count++;
				chapterbuff = "";
			}
			chapterbuff += line + "\n";
		}
		//write the final chapter
		cwriter = new BufferedWriter(new FileWriter(new File(capiscodir.getPath() + "/" + count + ".txt")));
		cwriter.write(chapterbuff);
		cwriter.close();
		iwriter.write(count + ".txt\t" + chapterbuff.replace('\n', ' ').replace('\t', ' ') + "\n");
		iwriter.close();
		
		JsonObject value = Json.createObjectBuilder()
			     .add("searchstring", "<a href=https://www.gutenberg.org/ebooks/766>David Copperfield</a>")
			     .add("filename", count + ".txt")
			     .add("chapter", count)
			     .add("author", "Charles Dickens")
			     .build();
		//new FileWriter(new File(metadir.getPath() + "/" + id + ".json"))
		JsonWriterFactory factory = Json.createWriterFactory(value);
		FileWriter fwriter = new FileWriter(new File(metadir.getPath() + "/jsoncatalog.txt"), true);
		JsonWriter jwriter = factory.createWriter(fwriter);
		jwriter.writeObject(value);
		fwriter.write("\n");
		jwriter.close();
	
		JsonObject tmp1 = Json.createObjectBuilder()
			.add("field", "searchstring")
			.add("datatype", "searchstring")
			.add("type", "text")
			.add("unique", true)
			.build();
		JsonObject tmp2 = Json.createObjectBuilder()
				.add("field", "chapter")
				.add("datatype", "categorical")
				.add("type", "text")
				.add("unique", true)
				.build();
		JsonObject tmp3 = Json.createObjectBuilder()
				.add("field", "author")
				.add("datatype", "categorical")
				.add("type", "text")
				.add("unique", true)
				.build();
		JsonArray structure = Json.createArrayBuilder()
			.add(tmp1)
			.add(tmp2)
			.add(tmp3)
			.build();
	
		JsonWriterFactory ffactory = Json.createWriterFactory(null);
		JsonWriter fjwriter = ffactory.createWriter(new FileWriter(new File(metadir.getPath() + "/field_descriptions.json"), true));
		fjwriter.writeArray(structure);
		fjwriter.close();
		reader.close();
	}
}
