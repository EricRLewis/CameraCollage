package com.example.homework4;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.RelativeLayout.LayoutParams;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MainActivity extends Activity implements TextureView.SurfaceTextureListener {
    
	private ScrollView sv;
	private RelativeLayout rl;
	
	private Camera mCamera;
	private TextureView mTextureView;
	private ArrayList<Bitmap> images;
	private float oldX = 0;
	private float oldY = 0;

	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	   
	    
	    Point size = new Point();
	    getWindowManager().getDefaultDisplay().getSize(size);
	
	    images = new ArrayList<Bitmap>();
	    sv = new ScrollView(this);
	    rl = new RelativeLayout(this);
	    //sv.setLayoutParams(new LayoutParams(size.x, size.y/2));
	    rl.setLayoutParams(new LayoutParams(size.x, size.y/2 + 230));
	    sv.addView(rl);
		
	    
	
	    setContentView(sv);
	    
	    //Create the TextureView
	    mTextureView = new TextureView(this);
	    mTextureView.setSurfaceTextureListener(this);
	    
	    
	    RelativeLayout.LayoutParams textureParams = new RelativeLayout.LayoutParams(size.x/4, size.y/3);
	    rl.addView(mTextureView, textureParams);
	    
	    mTextureView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				
				
				images.add(mTextureView.getBitmap());
				ImageView image = new ImageView(mTextureView.getContext());
				image.setImageBitmap(mTextureView.getBitmap());
				
				image.setOnTouchListener(new OnTouchListener() {
					
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						
						
						switch (event.getAction()){
						
						case MotionEvent.ACTION_DOWN:
							oldX = event.getRawX();
							oldY = event.getRawY();
							return true;

							
						case MotionEvent.ACTION_MOVE:
							if(images.size() <= 4){
								if (v.getY() + event.getRawY() - oldY <= sv.getHeight()){
									v.setTranslationX(event.getRawX() - oldX);
									v.setTranslationY(event.getRawY() - oldY);
								}
								

								
							}
							break;
							
						case MotionEvent.ACTION_UP:
							
							Point size = new Point();
						    getWindowManager().getDefaultDisplay().getSize(size);
						    RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(size.x/4, size.y/3);
						    newParams.leftMargin = (int) v.getX();
						    newParams.topMargin =(int) v.getY();
						    v.setTranslationX(0);
						    v.setTranslationY(0);
							v.setLayoutParams(newParams);
							return true;
						}
						return false;
					}
				});
				
				Point size = new Point();
			    getWindowManager().getDefaultDisplay().getSize(size);
			    
			    int texturePadLeft = size.x/4 * (images.size()%4);
			    int texturePadTop = size.y/3 * (images.size()/4);
			    
			    int imagePadLeft = (size.x/4) * ((images.size()-1)%4);
			    int imagePadTop = (size.y/3) * ((images.size() - 1)/4);
			    
			    
				RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(size.x/4, size.y/3);
				imageParams.leftMargin = imagePadLeft;
				imageParams.topMargin = imagePadTop;
				((RelativeLayout.LayoutParams) mTextureView.getLayoutParams()).leftMargin = texturePadLeft;
				((RelativeLayout.LayoutParams) mTextureView.getLayoutParams()).topMargin = texturePadTop;
				rl.addView(image, imageParams);
				
			}
		});
	    
	}
	

	
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
	    mCamera = Camera.open();
	    
	
	    try {
	        mCamera.setPreviewTexture(surface);
	        mCamera.startPreview();
	    } catch (IOException ioe) {
	        // Something bad happened
	    }
	}
	
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
	    // Ignored, Camera does all the work for us
	}
	
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
	    mCamera.stopPreview();
	    mCamera.release();
	    return true;
	}
	
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
	    // Invoked every time there's a new Camera preview frame
	}
	
	// This is necessary for the options menu to work in the fragments
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		return true;
	}
	
	// this happens when you select "Select on/off" from the options menu
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {


		//checks all of the possible cases from the options menu
		switch (item.getItemId()) {
		// Select on/off 
		case R.id.save:
			Bitmap combinedBitmap = combineImageIntoOne(images);
			return true;
		case R.id.clear:
			rl.removeAllViews();
			images = new ArrayList<Bitmap>();
			Point size = new Point();
		    getWindowManager().getDefaultDisplay().getSize(size);
			
			
			rl.addView(mTextureView, new RelativeLayout.LayoutParams(size.x/4, size.y/3));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private Bitmap combineImageIntoOne(ArrayList<Bitmap> bitmap) { 
		 int w = 0, h = 0; 
		 for (int i = 0; i < bitmap.size(); i++) { 
		 if (i < bitmap.size() - 1) { 
		 w = bitmap.get(i).getWidth() > bitmap.get(i + 1).getWidth() ? 
		 bitmap.get(i).getWidth() : bitmap.get(i + 1).getWidth(); 
		 } 
		 h += bitmap.get(i).getHeight(); 
		 } 
		 
		 Bitmap temp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); 
		 Canvas canvas = new Canvas(temp); 
		 int top = 0; 
		 for (int i = 0; i < bitmap.size(); i++) { 
		 Log.w("this", "Combine: "+i+"/"+bitmap.size()+1); 
		 
		 top = (i == 0 ? 0 : top+bitmap.get(i).getHeight()); 
		 canvas.drawBitmap(bitmap.get(i), 0f, top, null); 
		 } 
		 return temp; 
		}
	

	

}