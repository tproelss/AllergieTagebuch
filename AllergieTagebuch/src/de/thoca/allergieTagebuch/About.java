package de.thoca.allergieTagebuch;

import de.thoca.allergieTagebuch.data.TagebuchDbOpenHelper;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class About extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);      
        
        //TextView textView = (TextView) findViewById(R.id.lblAboutContent);
        //textView.setMovementMethod(LinkMovementMethod.getInstance());
        //textView.setText(Html.fromHtml(getResources().getString(R.string.about_content)));

    }
}
