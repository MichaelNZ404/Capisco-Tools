package capisco.lucene_extractor;

public class luceneInfo {
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
