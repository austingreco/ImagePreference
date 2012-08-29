/*
 * Copyright (C) 2012 Austin Greco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.austingreco.android.preference;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.RingtonePreference;
import android.provider.MediaStore.MediaColumns;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ImagePreference extends RingtonePreference {

	private static final String androidns = "http://schemas.android.com/apk/res/android";
	private static Drawable icon;
	ContentResolver contentResolver = getContext().getContentResolver();

	public ImagePreference( Context context ) {
		this( context, null, 0 );
	}

	public ImagePreference( Context context, AttributeSet attrs ) {
		this( context, attrs, 0 );
	}

	public ImagePreference( Context context, AttributeSet attrs, int defStyle ) {
		super( context, attrs, defStyle );

/*
		if( attrs != null ) {

			int arr[] = { 0 };
			TypedArray a = context.obtainStyledAttributes( attrs, arr, defStyle, 0 );
			for( int i=a.getIndexCount() ; i>=0 ; i-- ) {
				int attr = a.getIndex( i );
				switch( attr ) {
					case 0:
						iconRes = a.getResourceId( attr, 0 );
						Log.v( "OKz", "a=" + iconRes );
						break;
				}
			}
		}
*/

		if( android.os.Build.VERSION.SDK_INT >= 11 ) {
			icon = getIcon();
		} else {
			if( attrs != null ) {
				int iconRes = attrs.getAttributeResourceValue( androidns, "icon", 0 );
				if( iconRes != 0 ) {
					icon = context.getResources().getDrawable( iconRes );
				}
			}
		}
	}

	@Override
	protected void onBindView( View view ) {
		super.onBindView( view );
		reload( view );
	}

	public void reload( View view ) {

		ImageView iv = (ImageView)view.findViewById( android.R.id.icon );
		if( iv == null ) {
			Log.v( "OK", "iv=null" );
			LinearLayout layout = ( (LinearLayout)view.findViewById( android.R.id.widget_frame ) );
			if( layout == null ) {
				Log.v( "OK", "layout=null" );
				return;
			}
			layout.setVisibility( View.VISIBLE );
			int count = layout.getChildCount();
			if( count > 0 ) {
				layout.removeViews( 0, count );
			}

			RelativeLayout fl = new RelativeLayout( getContext() );

			iv = new ImageView( getContext() );
			fl.addView( iv );
			layout.addView( fl );
		} else {
			iv.setVisibility( View.VISIBLE );
		}

		String imgList = this.getPersistedString( "" );
		setImage( imgList, iv );
	}

	private HashMap<String, SoftReference<Bitmap>> images = new HashMap<String, SoftReference<Bitmap>>();

	private boolean setImage( String imgList, ImageView iv ) {
		if( imgList == null || "".equals( imgList ) ) {
			setDefaultIcon( iv );
		} else {
			String[] files = imgList.split( "," );
			// only show thumbnail of first image for now
			String filename = files[0];
			if( "".equals( filename ) ) {
				setDefaultIcon( iv );
			} else {
				if( images.containsKey( filename ) && images.get( filename ) != null ) {
					Log.d( "ImagePreference", "From cache: " + filename );
					Bitmap bmp = images.get( filename ).get();
					iv.setImageBitmap( bmp );
				} else {
					Log.d( "ImagePreference", "files=" + filename );
					Bitmap thumbnail = null;
					try {
						File f = new File( filename );
						if( f.exists() ) {
							Bitmap bmp = BitmapFactory.decodeFile( f.toString() );
							thumbnail = Bitmap.createScaledBitmap( bmp, 48, 48, true );
							images.put( filename, new SoftReference<Bitmap>( thumbnail ) );
						}
					} catch( Exception e ) {
					}
					
					if( thumbnail != null ) {
						iv.setImageBitmap( thumbnail );
						return true;
					} else {
						setDefaultIcon( iv );
					}
				}
			}
		}
		return false;
	}
	
	private void setDefaultIcon( ImageView iv ) {
		if( iv != null ) {
			if( icon != null ) {
				iv.setImageDrawable( icon );
			}
		}
	}

	@Override
	protected void onPrepareRingtonePickerIntent( Intent intent ) {
		intent.setAction( Intent.ACTION_PICK );
		intent.setType( "image/*" );
//		intent.setFlags( Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP );
	}

	@Override
	public boolean onActivityResult( int requestCode, int resultCode, Intent data ) {

		if( resultCode == Activity.RESULT_OK ) {
			if( super.onActivityResult( requestCode, resultCode, data ) ) {
				String uri = getFilePath( data.getData() );
				persistString( uri );
//				setSummary( getKey() + "=" + uri );
				return true;
			}
		}
		return false;
	}

	private String getFilePath( Uri uri ) {
		String path;
		String[] column = { MediaColumns.DATA };

		Cursor cursor = contentResolver.query( uri, column, null, null, null );
		cursor.moveToFirst();

		int index = cursor.getColumnIndex( column[0] );
		path = cursor.getString( index );
		cursor.close();

		return path;
	}
}
