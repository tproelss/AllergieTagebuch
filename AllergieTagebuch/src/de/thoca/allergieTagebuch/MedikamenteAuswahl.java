package de.thoca.allergieTagebuch;

import java.util.ArrayList;

import de.thoca.allergieTagebuch.data.Tag;
import de.thoca.allergieTagebuch.data.TagsHelper;

public class MedikamenteAuswahl extends TagAuswahlBase
{
	@Override
	protected ArrayList<Tag> GetTagsForDay(long dayId)
	{
		return TagsHelper.GetMedTagsForDay(dayId);
	}
	
	protected String[] GetAllTags()
	{
		return TagsHelper.GetAllMedTags();
	}
	
	protected void DeleteTag(String tagName)
	{
		TagsHelper.DeleteMedTag(tagName);
	}
	
	protected int GetTagIdForName(String tagName)
	{
		return TagsHelper.GetMedTagIdForName(tagName);
	}
	
	protected Tag AddNewTagToDB(String tagName)
	{
		return TagsHelper.AddNewMedTag(tagName);
	}
	
	protected int IsMedTag()
	{
		return 1;
	}
}
