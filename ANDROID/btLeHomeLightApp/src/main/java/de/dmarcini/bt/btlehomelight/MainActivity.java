package de.dmarcini.bt.btlehomelight;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Locale;

import de.dmarcini.bt.btleplaceholder.BuildConfig;
import de.dmarcini.bt.btleplaceholder.R;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
  private static final String TAG = MainActivity.class.getSimpleName();

  /**
   * Wenn die App erzeugt wird
   *
   * @param savedInstanceState
   */
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "erzeuge Application...");
      Log.e(TAG, "D E B U G Version");
    }
    //
    // Das Haupt Layout setzten
    //
    setContentView(R.layout.activity_main);
    // Die Toolbar finden
    Toolbar toolbar = ( Toolbar ) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    // die floating buttons machen
    makeFloatingButtons();
    // das Menü machen
    DrawerLayout          drawer = ( DrawerLayout ) findViewById(R.id.main_drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.setDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = ( NavigationView ) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);
  }

  /**
   * Erzeuge floating Knöpfe
   */
  private void makeFloatingButtons()
  {
    FloatingActionButton fab = ( FloatingActionButton ) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
      }
    });
  }

  /**
   * Wenn die zurück-Taste gedrückt wird
   */
  @Override
  public void onBackPressed()
  {
    DrawerLayout drawer = ( DrawerLayout ) findViewById(R.id.main_drawer_layout);
    if( drawer.isDrawerOpen(GravityCompat.START) )
    {
      drawer.closeDrawer(GravityCompat.START);
    }
    else
    {
      super.onBackPressed();
    }
  }

  /**
   * DAs Optionen Menü erzeugen (auch in der ActionBar)
   *
   * @param menu welches Menü
   * @return erfolgreich?
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    //
    // füge das MEnü ein, wenn ActionBar vorhanden dort auch
    //
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if( id == R.id.action_settings )
    {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @SuppressWarnings( "StatementWithEmptyBody" )
  @Override
  public boolean onNavigationItemSelected(MenuItem item)
  {
    // Handle navigation view item clicks here.
    int id = item.getItemId();
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, String.format(Locale.ENGLISH, "onNavigationDrawerItemSelected: id: <%d>...", id));
    }

//    if( getConnectionStatus() == ProjectConst.CONN_STATE_CONNECTED )
//    {
//      isOnline = true;
//    }
//    //
//    // das richtige Icon setzen
//    //
//    if( isOnline )
//    {
//      getActionBar().setLogo(ContentSwitcher.getProgItemForId(pItem.nId).resIdOnline);
//    }
//    else
//    {
//      // wenn der SPX OFFLINE ist, nur OFFLINE Funktionen freigeben
//      getActionBar().setLogo(ContentSwitcher.getProgItemForId(pItem.nId).resIdOffline);
//    }
//    //
//    // Argumente für die Fragmente füllen
//    //
//    arguments.putString(ProjectConst.ARG_ITEM_CONTENT, pItem.content);
//    arguments.putInt(ProjectConst.ARG_ITEM_ID, pItem.nId);

    switch( id )
    {
      case R.id.navColorCircle:
        // Farbkreis
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "onNavigationDrawerItemSelected: make color wheel fragment...");
        }
//        AreYouSureDialogFragment sureDial = new AreYouSureDialogFragment(getString(R.string.dialog_sure_exit));
//        sureDial.show(getFragmentManager().beginTransaction(), "programexit");
        break;
//      //
      case R.id.navColorOnlyWhite:
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "onNavigationDrawerItemSelected: make only white slider fragment...");
        }
//        if( isOnline )
//        {
//          //
//          // Der Benutzer wählt den Konfigurationseintrag für den SPX
//          //
//          Log.i(TAG, "onNavigationDrawerItemSelected: create SPX42PreferencesFragment...");
//          newFrag = new SPX42PreferencesFragment();
//          newFrag.setArguments(arguments);
//          mTitle = getString(R.string.conf_headline);
//          fTrans = getFragmentManager().beginTransaction();
//          fTrans.addToBackStack("SPX42PreferencesFragment");
//        }
        break;
      case R.id.navColorPresets:
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "onNavigationItemSelected: make color presets fragment...");
        }
        break;

      case R.id.navCommBT:
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "onNavigationItemSelected: make connection fragment...");
        }
        break;

      case R.id.navPropertys:
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "onNavigationItemSelected: make program propertys fragment...");
        }
        break;

      default:
        Log.e(TAG, "onNavigationItemSelected: unknown menuentry recived...");
    }
    DrawerLayout drawer = ( DrawerLayout ) findViewById(R.id.main_drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }
}
