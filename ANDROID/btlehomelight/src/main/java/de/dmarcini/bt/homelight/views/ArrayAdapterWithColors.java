/******************************************************************************
 *                                                                            *
 *      project: ANDROID                                                      *
 *      module: btlehomelight                                                 *
 *      class: ArrayAdapterWithColors                                         *
 *      date: 2016-01-08                                                      *
 *                                                                            *
 *      Copyright (C) 2016  Dirk Marciniak                                    *
 *                                                                            *
 *      This program is free software: you can redistribute it and/or modify  *
 *      it under the terms of the GNU General Public License as published by  *
 *      the Free Software Foundation, either version 3 of the License, or     *
 *      (at your option) any later version.                                   *
 *                                                                            *
 *      This program is distributed in the hope that it will be useful,       *
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *      GNU General Public License for more details.                          *
 *                                                                            *
 *      You should have received a copy of the GNU General Public License     *
 *      along with this program.  If not, see <http://www.gnu.org/licenses/   *
 *                                                                            *
 ******************************************************************************/

//@formatter:off
//@formatter:on
/**
 * Eigener Arrayadapter, der Icons beinhaltet
 * <p/>
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
 * <p/>
 * <p/>
 * Stand: 23.12.2012
 */
package de.dmarcini.bt.homelight.views;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.dmarcini.bt.homelight.R;

/**
 * Das Objekt leitet sich vom ArrayAdapter ab, erzeugt Adapter mit Icons
 * <p/>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p/>
 *         Stand: 10.11.2013
 */
public class ArrayAdapterWithColors extends ArrayAdapter<String>
{
  ArrayList<Integer> colorList;

  public ArrayAdapterWithColors(Context context, int textViewResourceId, String[] objects, ArrayList<Integer> colorList)
  {
    super(context, textViewResourceId);
    //
    // Alle Objekte erst einmal durchgehen
    //
    for( int i = 0; i < objects.length; i++ )
    {
      super.add(objects[ i ]);
    }
    this.colorList = colorList;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent)
  {
    ViewHolder     holder        = null;
    String         colorItemName = getItem(position);
    LayoutInflater mInflater     = ( LayoutInflater ) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    if( convertView == null )
    {
      convertView = mInflater.inflate(R.layout.array_with_color_adapter_view, parent, false);
      holder = new ViewHolder();
      holder.txtTitle = ( TextView ) convertView.findViewById(R.id.arrayListTextView);
      holder.imageView = ( ImageView ) convertView.findViewById(R.id.arrayListColorView);
      convertView.setTag(holder);
    }
    else
    {
      holder = ( ViewHolder ) convertView.getTag();
    }
    holder.txtTitle.setText(colorItemName);
    try
    {
      // Wenn alles gut geht die Hintergrundfarbe zeigen
      holder.imageView.setBackgroundColor(colorList.get(position));
    }
    catch( IndexOutOfBoundsException | NullPointerException ex )
    {
      // wenn keine Liste oder die Liste nicht im bereich, GRAU zeigen
      holder.imageView.setBackgroundColor(0x80a0a0a0);
    }
    return convertView;
  }

  @Override
  public View getDropDownView(int position, View convertView, ViewGroup parent)
  {
    return (getView(position, convertView, parent));
  }

  /* private view holder class */
  private class ViewHolder
  {
    public ImageView imageView;
    public TextView  txtTitle;
  }
}
