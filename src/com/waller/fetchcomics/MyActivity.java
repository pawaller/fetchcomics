package com.waller.fetchcomics;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import java.net.URL;

import java.util.Date;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import android.support.v4.app.ShareCompat;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.*;

	

public class MyActivity extends Activity implements OnClickListener, OnLongClickListener {

	
	

	//Class variables
    private SharedPreferences settings;

	ScrollView scroll;
	TextView tv;
	LinearLayout linear;
	ImageView iv;
	HorizontalScrollView hsv;
	
	String storedDate;
	int width;
	int height;
	//boolean connected;
	ImageView image1;
	public int index;
	String[] feeds;
	TextView text;
	Boolean flag;
    public String[] comics = new String[39];//was 21
    public  int numberOfComics = 0;
    public int size;
	
	View viewTemp;
	
	Context context=this;
	
	private AlertDialog.Builder addComic;
	private AlertDialog.Builder deleteComic;
	
  ProgressDialog progressDialog;
   
	
	Date d = new Date();
	String todaysDate = (String) DateFormat.format("EE dd-MMM-yyyy", d.getTime());
	String deviceName = android.os.Build.MODEL;
	//String deviceVersion = android.os.Build.VERSION.RELEASE;
	int deviceVersion = android.os.Build.VERSION.SDK_INT;
	Point screen = new Point();
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus){
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus & (deviceVersion >= 19 )) {
	     scroll.setSystemUiVisibility(
		 View.SYSTEM_UI_FLAG_LAYOUT_STABLE
		 | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
		 | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		 | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		 | View.SYSTEM_UI_FLAG_FULLSCREEN
		 | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
		 );
		}
	} 
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        BitmapFactory.Options options = new BitmapFactory.Options(); options.inPurgeable = true;
        
        addComic = new AlertDialog.Builder(this);//add a comic alert (triggered by short click)
        addComic.setTitle(R.string.addAComic);
        addComic.setItems(R.array.comics, new DialogInterface.OnClickListener() {
   
         public void onClick(DialogInterface dialog, int item) {
      	// do something      item = returned item selected
         	((ViewGroup)image1.getParent()).removeView(image1);// delete existing "add a comic" view
     
         	comics[numberOfComics]= feeds[item];
         	 hsv = new HorizontalScrollView(MyActivity.this);
        	  linear.addView(hsv);
        	  iv = new ImageView(MyActivity.this);
    		  hsv.addView(iv);
   
    		 image1 = new ImageView(MyActivity.this);
 	       image1.setImageResource(R.drawable.add_a_comic);
 	       image1.setOnClickListener(MyActivity.this);		      
 	       linear.addView(image1);
    		  
         	new DownloadRSSTask().execute(new Comic(feeds[item],numberOfComics));  //provide rssfeed url and index
      
        numberOfComics++;
         
         }});//end of add a comic alert
     
     deleteComic = new AlertDialog.Builder(MyActivity.this); //delete this comic alert (triggered by long click)
     deleteComic.setTitle(R.string.deleteThisComic);
     deleteComic.setCancelable(false)
             .setNeutralButton((R.string.delete), new DialogInterface.OnClickListener() {// Delete
  	         public void onClick(DialogInterface dialog, int id) {               
       
  	           int i = ((ViewGroup) viewTemp.getParent().getParent()).indexOfChild((View) viewTemp.getParent());
  	    
  	          comics[i]=""; 
  	          numberOfComics--;
  	         ((ViewGroup)viewTemp.getParent().getParent()).removeView((View) viewTemp.getParent());//remove the parent view
  	       ((ViewGroup)viewTemp.getParent()).removeView(viewTemp);//remove the view
  	      for (int t=0; t<20; t++) {  //sort the comic array, we dont care if the last slot is empty
  	    	 if (comics[t]==""){
  	    		 comics[t]= comics[t+1];
  	    		 comics[t+1]="";
  	    	  }
  	      }
  	      
  	     
  		   }      
  	   })       
  		   .setNegativeButton((R.string.cancel), new DialogInterface.OnClickListener() {// Cancel
  			   public void onClick(DialogInterface dialog, int id) {
  			
         }
  			  
     })
              .setPositiveButton((R.string.share), new DialogInterface.OnClickListener() {// Share
 		   public void onClick(DialogInterface dialog, int id) {
 			   
 			  
 			   Bitmap bmp = viewTemp.getDrawingCache();
 			   //try saving to the sdcard...
 			   
 			   String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
 			    OutputStream outStream = null;
 			    File file = new File(extStorageDirectory, "sharedComic.png");
 			    try {
 			     outStream = new FileOutputStream(file);
 			    // bmp = Bitmap.createScaledBitmap(bmp,1024,300 ,true);  //now scale bitmap for mms/email etc
 			     bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
 			     outStream.flush();
 			     outStream.close();
 			    }
 			    catch(Exception e)
 			    {}
 			
 			   File shareComic =  new File(Environment.getExternalStorageDirectory(),"sharedComic.png");  

 			   Intent share = ShareCompat.IntentBuilder.from(MyActivity.this)
                        .setSubject("Today's Comic")
                        .setText("Shared by Fetch Comics")
                        .setStream(Uri.fromFile(shareComic))
                        .setType("image/jpeg")
                        .getIntent()
                       // .setPackage("com.google.android.apps.plus")
                        ;
 			   startActivity(Intent.createChooser(share, "Share Comic"));
 			 //  startActivity(share);
 		   }
   })
      
     ; //end of delete this comic alert
    	
     
    }//end of onCreate method
    
   @Override
   protected void onStart(){
	   super.onStart();
	   if (isOnline()){//check to see if we have an internet connection
       	
	       //if true then do the following 
	        feeds = getResources().getStringArray(R.array.feeds); 
	        
	        //restore preferences
	        SharedPreferences settings =getPreferences(0);
	         numberOfComics = settings.getInt("numberOfComics", 0);
	         size = settings.getInt("size",3);
	      storedDate = settings.getString("storedDate", "");
	      
	      Display display = getWindowManager().getDefaultDisplay();
	    	// width = display.getWidth();
	    	// height = display.getHeight();
			 display.getRealSize(screen);
			 height =  screen.x;
			 width = screen.y;
	    	/* if (height > 900 & height < 1025){ //tablet fix 1024
	    		 height = 1024;
	    	 }
	    	 if (height > 1025){ //tablet fix 1280
	    		 height = 1280;
	    	 } */
	    	// if (deviceName.equals ("Nexus 5") & width == 1080) height = 1794; //fix for Nexus 5
	  
	    	  // create layout
	    	 setTitle("Fetch Comics    " + todaysDate);
			//setTitle("H"+ height +"W"+ width );
	        scroll = new ScrollView(this);
	      
	       linear = new LinearLayout(this);
	       linear.setOrientation(LinearLayout.VERTICAL);
	       scroll.addView(linear);
	       image1 = new ImageView(this);
	       image1.setImageResource(R.drawable.add_a_comic);
	       image1.setOnClickListener(this);
	       setContentView(scroll);
	     

	      //get each comic stored in preferences file
	     for (int i=0; i < numberOfComics; i++){
	    	  hsv = new HorizontalScrollView(MyActivity.this); 
	    	  linear.addView(hsv);
	    	 comics[i] = settings.getString("feed"+i, "");
	    	 new DownloadRSSTask().execute(new Comic(comics[i],i));  //provide rssfeed url and index
	   	  } 
	     linear.addView(image1); //"add a comic" view
	   
	        }
	        else //if we dont have an internet connection...
	        {
	        try {
	    AlertDialog alertDialog = new AlertDialog.Builder(this).create();
	    alertDialog.setTitle("Info");
	    alertDialog.setMessage("Comic feed not available, please check your internet connectivity and try again");
	    alertDialog.setIcon(R.drawable.icon);
	    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog, int which) {
	        // finish();
	    	   System.gc();
	    	   System.exit(0);
 }
	    });

	    alertDialog.show();
	    }
	    catch(Exception e)
	    {
	      //  Log.d(Constants.TAG, "Show Dialog: "+e.getMessage());
	    }
	        }
   }//end of onStart method
    
    /* Called when the activity is stopped. */ 
    @Override
    protected void onStop(){
       super.onStop();

      // We need an Editor object to make preference changes.
      // All objects are from android.context.Context
      SharedPreferences settings = getPreferences(0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putInt("numberOfComics", numberOfComics);
      editor.putInt("size", size);
      editor.putString("storedDate", todaysDate);
     for (int i=0; i<numberOfComics; i++){
      editor.putString("feed"+i, comics[i]);
     }
      editor.commit(); // Commit the edits!
      System.gc();
    }//end of onStop method
    
 /*   /** Called when the activity is destroyed. *//* 
    @Override
    protected void onDestroy(){
       super.onDestroy();

    }//end of onDestroy method
  */  
    /** Called when the activity is paused. */ 
    @Override
    protected void onPause(){
       super.onPause();

      System.gc();
       
    }//end of onPause method
	

	/** Called when the activity is resumed. */ 
	@Override
	protected void onResume(){
		super.onResume();
    System.gc();
	}//end of onResume method
	
/*	*//** Called when the activity is restarted. *//* 
	@Override
	protected void onRestart(){
		super.onRestart();
     
	}//end of onRestart method
*/	
	public void onClick(View v) {
		AlertDialog myalert = addComic.create();//
        myalert.show();
	}//end of onClick method
	
	public boolean onLongClick(View v) {
		viewTemp = v;
		AlertDialog myalert = deleteComic.create();//
		myalert.show();
		
		return true;	
	}//end of onLongClick method
	
	 class DownloadRSSTask extends AsyncTask<Comic, Void, Bitmap> {// receive a Comic Object object containing rss feed and index
		 ProgressDialog progressDialog;
		 String rssPattern = getString(R.string.rssPattern);  // 
		   public HorizontalScrollView hsv;
			public ImageView iv;
	    	ImageView imageView;
	    	
			Bitmap scaledBitmap;
	    	Bitmap bitmap;
		 int comicIndex;
		 
		 @Override
		 protected void onPreExecute(){
		 progressDialog = new ProgressDialog(MyActivity.this);
		 progressDialog.setMessage("Loading Comics...");
		 progressDialog.setCancelable(false);
			  progressDialog.show();
		 }
		
		  @Override
		  protected Bitmap doInBackground(Comic... comic) {  // Varags so could be multiple values but we are only sending 1 object in this case, comic[0]

			  
			  
			  
			  
				StringBuilder feed = new StringBuilder();
			//	tempComic = comic[0];
			//	int index = comic[0].index;
				  comicIndex = comic[0].comicIndex;
					DefaultHttpClient client = new DefaultHttpClient();
					HttpGet httpGet = new HttpGet(comic[0].url);
				   try {
						HttpResponse execute = client.execute(httpGet);
						InputStream content = execute.getEntity().getContent();

						BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
						String s;
						while ((s = buffer.readLine()) != null) {
							feed.append(s);
						}
				
					}   catch (Exception e) {
					//	new AlertDialog.Builder(context).setTitle("Unable to fetch Comics").setMessage("Please check your Internet connection").setNeutralButton("OK", null).show();
						     e.printStackTrace();
			            }	
				   
				   int rssIndex = feed.indexOf(rssPattern);
					CharSequence rssString =feed.subSequence(rssIndex, rssIndex+40);
					String imageUrl = "http://www.arcamax.com"+ rssString.toString();
					
					  try { 
				             URL url = new URL(imageUrl);
						
						  HttpURLConnection conn= (HttpURLConnection)url.openConnection();
			 	         conn.setDoInput(true);
			 	        conn.connect();
			 	        InputStream is = conn.getInputStream();
			 	        bitmap = BitmapFactory.decodeStream(is);
			 	
			 	        }        catch (IOException e) {
			 	        	new AlertDialog.Builder(context).setTitle("Unable to fetch Comics").setMessage("Please check your Internet connection").setNeutralButton("OK", null).show();
			 	                e.printStackTrace();
			 	                }
			 	  
			 	         return bitmap;// send to onPostExecute method
					
		  } // end of doInBackground method

		 
		  
		   @Override
		    protected void onPostExecute(Bitmap bitmap) {
			   
			//   progressDialog.dismiss();
			
		      	((ViewGroup)image1.getParent()).removeView(image1);// delete existing "add a comic" view
		
		      	
		      	HorizontalScrollView hsv = (HorizontalScrollView) linear.getChildAt(comicIndex);
		      	hsv.removeAllViews();
			
		      	iv = new ImageView(MyActivity.this);
		      	hsv.addView(iv);
		      	
				iv.setOnLongClickListener(MyActivity.this);
		
				if (width > height){ //we are in landscape mode
			
					scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, bitmap.getHeight() * width / bitmap.getWidth(), true);
				}
				else { //we are in portrait mode
					scaledBitmap = Bitmap.createScaledBitmap(bitmap,height,bitmap.getHeight() * height / bitmap.getWidth() ,true);
				}
				bitmap.recycle();
				iv.setDrawingCacheEnabled(true);
				iv.setImageBitmap(scaledBitmap);	
				
			       //add the "add a comic" view underneath the new comic
			       image1 = new ImageView(MyActivity.this);
			       image1.setImageResource(R.drawable.add_a_comic);
			       image1.setOnClickListener(MyActivity.this);		      
			       linear.addView(image1);
			       
			       progressDialog.dismiss();  
			       return;
			       
			} // end of onPostExecute method
		} // end of DownloadRSSTask class
	    
	
	 private class Comic {								// create a new class called Comic
	
			public String url;								// this will contain a url pointing to an image
	
			public int comicIndex;                              // this will contain a url pointing to an rss feed
	
			
			   public Comic (String feed, int index) {	// creator method (called when new object is made)
											// takes 2 arguments, an ImageView and a String
			 	url = feed;	
			    comicIndex = index;
		
			   }//end of creator method							

		} // end of Comic class
	    
	 public boolean isOnline() {
		    ConnectivityManager cm =
		        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo netInfo = cm.getActiveNetworkInfo();
		    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
		   
		        return true;
		    }
		    return false;
		}//end of isOnline
		    
	
	
	
}//end of MyActivity class
