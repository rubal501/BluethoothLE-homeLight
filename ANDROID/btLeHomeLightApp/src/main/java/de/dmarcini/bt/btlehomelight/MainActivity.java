package de.dmarcini.bt.btlehomelight;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

import java.util.Locale;

import de.dmarcini.bt.btlehomelight.interfaces.IBtServiceListener;
import de.dmarcini.bt.btlehomelight.service.BluetoothLowEnergyService;
import de.dmarcini.bt.btlehomelight.service.BluetoothLowEnergyService.LocalBinder;
import de.dmarcini.bt.btlehomelight.utils.BlueThoothMessage;
import de.dmarcini.bt.btlehomelight.utils.BluetoothModulConfig;
import de.dmarcini.bt.btlehomelight.utils.HomeLightSysConfig;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
  private static final String                    TAG         = MainActivity.class.getSimpleName();
  //
  // Ein Messagehandler, der vom Service kommende Messages bearbeitet
  //
  @SuppressLint( "HandlerLeak" )
  private final        Handler                   mHandler    = new Handler()
  {
    @Override
    public void handleMessage(Message msg)
    {
      if( !(msg.obj instanceof BlueThoothMessage) )
      {
        Log.e(TAG, "Handler::handleMessage: Recived Message is NOT type of BtServiceMessage!");
        return;
      }
      BlueThoothMessage smsg = ( BlueThoothMessage ) msg.obj;
      if( smsg.getData() != null && smsg.getData().length() > 0 && BuildConfig.DEBUG )
      {
        Log.d(TAG, "BT message: <" + smsg.getData() + ">");
      }
      if( msgHandler != null )
      {
        msgHandler.handleMessages( smsg );
      }
    }
  };
  private              BluetoothModulConfig      btConfig    = new BluetoothModulConfig();
  private              BluetoothLowEnergyService mService    = null;
  private              LocalBinder               binder      = null;
  //
  // Lebensdauer des Service wird beim binden / unbinden benutzt
  //
  private final        ServiceConnection         mConnection = new ServiceConnection()
  {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "onServiceConnected()...");
      }
      binder = ( LocalBinder ) service;
      mService = binder.getService();
      binder.registerServiceHandler(mHandler);
    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "onServiceDisconnected...");
      }
      if( mService != null && binder != null )
      {
        if( BuildConfig.DEBUG )
        {
          Log.d(TAG, "onServiceDisconnected...unregister Handler...");
        }
        binder.unregisterServiceHandler();
      }
      mService = null;
      binder = null;
    }
  };
  private              IBtServiceListener        msgHandler  = null;

  @Override
  public void onStart()
  {
    super.onStart();
    //
    // Der Service muss noch gestartet werden
    //
    Intent gattServiceIntent = new Intent(this, BluetoothLowEnergyService.class);
    bindService(gattServiceIntent, mConnection, BIND_AUTO_CREATE);
  }

  @Override
  protected void onStop()
  {
    super.onStop();
    try
    {
      //
      // BT Verbindung trennen
      //
      btConfig.getBluetoothService().disconnect();
    }
    catch( NullPointerException ex )
    {
      Log.e(TAG, "onStop: Oups, Null Pointer....");
    }
  }

  /**
   * Versuche wieder das Gerät zu verbinden
   */
  private void tryReconnectToDevice()
  {
    // TODO: eine Möglichkeit machen, dass reconnect immer dann passiert, wenn es vorher eine Verbindung gab oder Verbinden mit dem letzten Gerät eingestellt ist

    if( (btConfig.getBluetoothService() != null) && (btConfig.getDeviceAddress() != null) )
    {
      final boolean result = btConfig.getBluetoothService().connect(btConfig.getDeviceAddress());
      Log.d(TAG, "Connect request result=" + result);
    }
  }


  @Override
  protected void onDestroy()
  {
    super.onDestroy();
    unbindService(mConnection);
  }

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
    //##############################################################################################
    //
    // Ist Bluethooth LE (4.0) unterstützt?
    //
    if( !getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) )
    {
      Toast.makeText(this, R.string.main_btle_not_supported, Toast.LENGTH_SHORT).show();
      finish();
    }
    //
    // initialisiere den Adapter
    //
    final BluetoothManager bluetoothManager = ( BluetoothManager ) getSystemService(Context.BLUETOOTH_SERVICE);
    btConfig.setBluetoothAdapter(bluetoothManager.getAdapter());
    //
    // Ist ein BT Adapter vorhanden?
    //
    if( btConfig.getBluethoothAdapter() == null )
    {
      Toast.makeText(this, R.string.main_btle_not_supported, Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    //
    // Systemeinstellungen einlesen
    //
    HomeLightSysConfig.readSysPrefs(getResources(), PreferenceManager.getDefaultSharedPreferences(this));
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

  @Override
  public void onResume()
  {
    super.onResume();
    //
    // Systemeinstellungen einlesen
    //
    HomeLightSysConfig.readSysPrefs(getResources(), PreferenceManager.getDefaultSharedPreferences(this));
    //
    // Stelle sicher, dass der BT Adapter aktiviert wurde
    // erzeuge einen Intend (eine Absicht) und schicke diese an das System
    //
    if( !btConfig.getBluethoothAdapter().isEnabled() )
    {
      //
      // erzeuge die Nachricht ans System, der "Rest" ist dann bei onActivityResult
      //
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBtIntent, ProjectConst.REQUEST_ENABLE_BT);
    }
    else
    {
      //
      // versuche zu verbinden (wenn das was zu verbinden ist)
      //
      tryReconnectToDevice();
    }
  }

  @Override
  public void onPause()
  {
    super.onPause();
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
      case R.id.navColorEqualizer:
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "onNavigationItemSelected: make color equalizer fragment...");
        }
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

      case R.id.navQuit:
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "onNavigationItemSelected: quit app...");
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
