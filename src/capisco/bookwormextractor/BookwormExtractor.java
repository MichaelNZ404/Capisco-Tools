package capisco.bookwormextractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

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
		
		Analyze(input, output);
		

	}
	
	public static void Analyze(String input, String output) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(input));
		
		String line;
		int accepted = 0;
		int removed = 0;
		File capiscodir = new File(output + "capisco/");
		capiscodir.mkdirs();
		File metadir = new File(output + "meta/");
		metadir.mkdirs();
		
		
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
				String date = datetime[1];
				
				BufferedWriter writer = new BufferedWriter(new FileWriter(new File(capiscodir.getPath() + "/" + id + ".txt")));
				writer.write(text);
				writer.close();
			
				JsonObject value = Json.createObjectBuilder()
					     .add("searchstring", "<a href=https://twitter.com/user/status/"+id+">Tweet: "+id+" From: "+user+"</a>")
					     .add("filename", id)
					     .add("date", date)
					     .add("time", time)
					     .build();
				//new FileWriter(new File(metadir.getPath() + "/" + id + ".json"))
				JsonWriterFactory factory = Json.createWriterFactory(value);
				JsonWriter jwriter = factory.createWriter(new FileWriter(new File(metadir.getPath() + "/data.json"), true));
				jwriter.writeObject(value);
				jwriter.close();
				accepted++;
			}
		}
		
		JsonObject tmp1 = Json.createObjectBuilder()
			.add("field", "searchstring")
			.add("datatype", "searchstring")
			.add("type", "text")
			.add("unique", true)
			.build();
		JsonObject tmp2 = Json.createObjectBuilder()
				.add("field", "filename")
				.add("datatype", "categorical")
				.add("type", "text")
				.add("unique", true)
				.build();;
		JsonObject tmp3 = Json.createObjectBuilder()
				.add("field", "date")
				.add("datatype", "categorical")
				.add("type", "text")
				.add("unique", true)
				.build();
		JsonObject tmp4 = Json.createObjectBuilder()
				.add("field", "time")
				.add("datatype", "categorical")
				.add("type", "text")
				.add("unique", true)
				.build();
		JsonArray structure = Json.createArrayBuilder()
			.add(tmp1)
			.add(tmp2)
			.add(tmp3)
			.add(tmp4)
			.build();

		//new FileWriter(new File(metadir.getPath() + "/" + id + ".json"))
		JsonWriterFactory factory = Json.createWriterFactory(null);
		JsonWriter jwriter = factory.createWriter(new FileWriter(new File(metadir.getPath() + "/structure.json"), true));
		jwriter.writeArray(structure);
		jwriter.close();
		
		System.out.println("Removed: " + removed);
		System.out.println("Accepted: " + accepted);
		reader.close();
	}

}
