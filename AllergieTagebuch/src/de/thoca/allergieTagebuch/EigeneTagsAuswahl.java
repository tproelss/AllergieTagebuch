package de.thoca.allergieTagebuch;

import java.util.ArrayList;

import de.thoca.allergieTagebuch.data.Tag;
import de.thoca.allergieTagebuch.data.TagsHelper;

public class EigeneTagsAuswahl extends TagAuswahlBase
{
	@Override
	protected ArrayList<Tag> GetTagsForDay(long dayId)
	{
		return TagsHelper.GetEigeneTagsForDay(dayId);
	}
	
	protected String[] GetAllTags()
	{
		return TagsHelper.GetAllEigeneTags();
	}
	
	protected void DeleteTag(String tagName)
	{
		TagsHelper.DeleteEigenenTag(tagName);
	}
	
	protected int GetTagIdForName(String tagName)
	{
		return TagsHelper.GetEigenenTagIdForName(tagName);
	}
	
	protected Tag AddNewTagToDB(String tagName)
	{
		return TagsHelper.AddNewEigenenTag(tagName);
	}
	
	protected int IsMedTag()
	{
		return 0;
	}
}
