package capisco.bookwormextractor;

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

public class BookwormExtractor {
	
	/**
	 * 
	 * @param args[0] input text file of tweets
	 * @param args[1] output directory
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String input = args[0];
		String output = args[1];
		
		if(!output.endsWith("/")){
			output = output + "/";
		}
		
		DickensAnalyze(input, output);
	}
	
	public static void DickensAnalyze(String input, String output) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String delimiter = "CHAPTER "; //if this occurs in the text then thats bad
		
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
	
	public static void TweetAnalyze(String input, String output) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(input));
		
		String line;
		int accepted = 0;
		int removed = 0;
		File capiscodir = new File(output + "capisco/");
		capiscodir.mkdirs();
		File metadir = new File(output + "meta/");
		metadir.mkdirs();
		//this writer creates a vanilla input in the meta directory for comparison
		BufferedWriter iwriter = new BufferedWriter(new FileWriter(new File(metadir.getPath() + "/input.txt")));
		
		
		while((line = reader.readLine()) != null){
			String[] exploded = line.split("\t");
			String code = exploded[0];
			String id = exploded[6];
			String user = exploded[7];
			String text = exploded[8];
			
			if(code.equals("404") || text.equals("null")){
				//System.out.println("invalid");
				removed++;
			}
			else{	
				String[] datetime = exploded[9].split(" - ");
				String time = datetime[0];
				time = time.substring(0, 2) + "00" + time.substring(5, time.length());
				String date = datetime[1];
				
				BufferedWriter cwriter = new BufferedWriter(new FileWriter(new File(capiscodir.getPath() + "/" + id + ".txt")));
				iwriter.write(id + ".txt\t" + text.replace('\n', ' ').replace('\t', ' ') + "\n");
				cwriter.write(text);
				cwriter.close();
			
				JsonObject value = Json.createObjectBuilder()
					     .add("searchstring", "<a href=https://twitter.com/user/status/"+id+">Tweet: "+id+" From: "+user+"</a>")
					     .add("filename", id + ".txt")
					     .add("date", date)
					     .add("time", time)
					     .build();
				//new FileWriter(new File(metadir.getPath() + "/" + id + ".json"))
				JsonWriterFactory factory = Json.createWriterFactory(value);
				FileWriter fwriter = new FileWriter(new File(metadir.getPath() + "/jsoncatalog.txt"), true);
				JsonWriter jwriter = factory.createWriter(fwriter);
				jwriter.writeObject(value);
				fwriter.write("\n");
				jwriter.close();
				accepted++;
			}
		}
		iwriter.close();
		JsonObject tmp1 = Json.createObjectBuilder()
			.add("field", "searchstring")
			.add("datatype", "searchstring")
			.add("type", "text")
			.add("unique", true)
			.build();
		JsonObject tmp2 = Json.createObjectBuilder()
				.add("field", "date")
				.add("datatype", "categorical")
				.add("type", "text")
				.add("unique", true)
				.build();
		JsonObject tmp3 = Json.createObjectBuilder()
				.add("field", "time")
				.add("datatype", "categorical")
				.add("type", "text")
				.add("unique", true)
				.build();
		JsonArray structure = Json.createArrayBuilder()
			.add(tmp1)
			.add(tmp2)
			.add(tmp3)
			.build();

		//new FileWriter(new File(metadir.getPath() + "/" + id + ".json"))
		JsonWriterFactory factory = Json.createWriterFactory(null);
		JsonWriter jwriter = factory.createWriter(new FileWriter(new File(metadir.getPath() + "/field_descriptions.json"), true));
		jwriter.writeArray(structure);
		jwriter.close();
		
		System.out.println("Removed: " + removed);
		System.out.println("Accepted: " + accepted);
		reader.close();
	}

}
