package capisco.bookwormextractor.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;

public class LettersAnalyze {
	
	public static String getYearFromString(String s, String curr){
		Pattern pattern = Pattern.compile("\\d{4}");
		Matcher matcher = pattern.matcher(s);
		if (matcher.find())
		{
		    return(matcher.group(0));
		}
		else{
			return curr;
		}
	}
	
	public static String getMonthFromString(String s, String curr){
		String[] tmp = s.split(",");
		Pattern pattern = Pattern.compile("(th)|(rd)|(st)");		
		for(int i = 0; i< tmp.length; i++){
			tmp[i] = tmp[i].trim();
			Matcher matcher = pattern.matcher(tmp[i]);
			if (matcher.find())
			{
				String tmpresult = tmp[i].split(" ")[0];
				tmpresult = tmpresult.replace("_", "");
				tmpresult = tmpresult.replace(".", "");
				Pattern pattern2 = Pattern.compile("(Jan)|(Feb)|(Mar)|(Apr)|(May)|(Jun)|(Jul)|(Aug)|(Sep)|(Oct)|(Nov)|(Dec)");
				Matcher matcher2 = pattern2.matcher(tmpresult);
				if (matcher2.find())
				{
					return(tmpresult.substring(0, 3));
				}
			}
		}
		return curr;
	}
	
	public static void DickensLettersAnalyze(String input, String output) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String delimiter = "[Sidenote: "; //if this occurs in the text then thats bad
		
		String line;
		File capiscodir = new File(output + "capisco/");
		capiscodir.mkdirs();
		File metadir = new File(output + "meta/");
		metadir.mkdirs();
		BufferedWriter iwriter = new BufferedWriter(new FileWriter(new File(metadir.getPath() + "/input.txt")));
		
		int count = 1;
		BufferedWriter cwriter = null;
		String chapterbuff = reader.readLine() + "\n"; //consume the first delimiter
		reader.readLine(); //consume white line;
		String dateline = reader.readLine();
		String year = getYearFromString(dateline, null); //null while we do not know the date.
		String month = getMonthFromString(dateline, null); //null while we do not know the date.
		
		Boolean discardMode = false;
		
		while((line = reader.readLine()) != null){
			
			//if this is a new chapter create a new file
			if(line.startsWith(delimiter)){
				cwriter = new BufferedWriter(new FileWriter(new File(capiscodir.getPath() + "/" + count + ".txt")));
				cwriter.write(chapterbuff);
				cwriter.close();
				iwriter.write(count + ".txt\t" + chapterbuff.replace('\n', ' ').replace('\t', ' ') + "\n");
				
				if(year == null || year.equals("undefined")){
					year = "undefined";
				}
				if(month == null || month.equals("undefined")){
					month = "undefined";
				}
				
				JsonObject value = Json.createObjectBuilder()
					     .add("searchstring", "<a href=https://www.gutenberg.org/ebooks/766>David Copperfield</a>")
					     .add("filename", count + ".txt")
					     .add("chapter", count)
					     .add("author", "Charles Dickens")
					     .add("year", year)
					     .add("month", month)
					     .add("monthyear", month + "-" + year)
					     .build();
				JsonWriterFactory factory = Json.createWriterFactory(value);
				FileWriter fwriter = new FileWriter(new File(metadir.getPath() + "/jsoncatalog.txt"), true);
				JsonWriter jwriter = factory.createWriter(fwriter);
				jwriter.writeObject(value);
				fwriter.write("\n");
				jwriter.close();
				
				count++;
				chapterbuff = "";
				discardMode = false;
				
				reader.readLine(); //consume white line;
				dateline = reader.readLine();
				year = getYearFromString(dateline, year); //null while we do not know the date.
				month = getMonthFromString(dateline, month); //null while we do not know the date.
			}
			
			if(line.startsWith("NOTE.") || line.startsWith("NARRATIVE.")){
				discardMode = true;
			}
			
			if(discardMode == false){
				chapterbuff += line + "\n";
			}
		}
		
		
		//write the final chapter
		cwriter = new BufferedWriter(new FileWriter(new File(capiscodir.getPath() + "/" + count + ".txt")));
		cwriter.write(chapterbuff);
		cwriter.close();
		iwriter.write(count + ".txt\t" + chapterbuff.replace('\n', ' ').replace('\t', ' ') + "\n");
		
		if(year == null || year.equals("undefined")){
			year = "undefined";
		}
		if(month == null || month.equals("undefined")){
			month = "undefined";
		}
		
		JsonObject value = Json.createObjectBuilder()
			     .add("searchstring", "<a href=https://www.gutenberg.org/ebooks/766>David Copperfield</a>")
			     .add("filename", count + ".txt")
			     .add("chapter", count)
			     .add("author", "Charles Dickens")
			     .add("year", year)
			     .add("month", month)
			     .add("monthyear", month + "-" + year)
			     .build();
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
		JsonObject tmp4 = Json.createObjectBuilder()
				.add("field", "year")
				.add("datatype", "categorical")
				.add("type", "integer")
				.add("unique", true)
				.build();
		JsonObject tmp5 = Json.createObjectBuilder()
				.add("field", "month")
				.add("datatype", "categorical")
				.add("type", "text")
				.add("unique", true)
				.build();
		JsonObject tmp6 = Json.createObjectBuilder()
				.add("field", "monthyear")
				.add("datatype", "categorical")
				.add("type", "text")
				.add("unique", true)
				.build();
		JsonArray structure = Json.createArrayBuilder()
			.add(tmp1)
			.add(tmp2)
			.add(tmp3)
			.add(tmp4)
			.add(tmp5)
			.add(tmp6)
			.build();

		JsonWriterFactory ffactory = Json.createWriterFactory(null);
		JsonWriter fjwriter = ffactory.createWriter(new FileWriter(new File(metadir.getPath() + "/field_descriptions.json"), true));
		fjwriter.writeArray(structure);
		fjwriter.close();
		iwriter.close();
		reader.close();
	}
}
