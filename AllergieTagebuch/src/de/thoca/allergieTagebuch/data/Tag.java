package de.thoca.allergieTagebuch.data;

public class Tag
{		
	public Tag(int id, String tag, boolean isMedTag)
	{
		this.id = id;
		this.tag = tag.trim();		
		this.isMedTag = isMedTag;
	}
		
	public int id;
	public String tag;	
	public boolean isMedTag;
}
