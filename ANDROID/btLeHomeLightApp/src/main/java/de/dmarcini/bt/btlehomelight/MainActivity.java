package de.dmarcini.bt.btlehomelight;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.Locale;

import de.dmarcini.bt.btlehomelight.dialogs.AreYouSureDialogFragment;
import de.dmarcini.bt.btlehomelight.dialogs.EditModuleNameDialogFragment;
import de.dmarcini.bt.btlehomelight.fragments.BTConnectFragment;
import de.dmarcini.bt.btlehomelight.fragments.ColorCircleFragment;
import de.dmarcini.bt.btlehomelight.fragments.LightRootFragment;
import de.dmarcini.bt.btlehomelight.fragments.PlaceholderFragment;
import de.dmarcini.bt.btlehomelight.fragments.SystemPreferencesFragment;
import de.dmarcini.bt.btlehomelight.fragments.WhiteOnlyFragment;
import de.dmarcini.bt.btlehomelight.interfaces.IBtCommand;
import de.dmarcini.bt.btlehomelight.interfaces.IBtServiceListener;
import de.dmarcini.bt.btlehomelight.interfaces.INoticeDialogListener;
import de.dmarcini.bt.btlehomelight.service.BluetoothLowEnergyService;
import de.dmarcini.bt.btlehomelight.service.BluetoothLowEnergyService.LocalBinder;
import de.dmarcini.bt.btlehomelight.utils.BlueThoothMessage;
import de.dmarcini.bt.btlehomelight.utils.HomeLightSysConfig;

public class MainActivity extends AppCompatActivity implements IBtCommand, INoticeDialogListener, NavigationView.OnNavigationItemSelectedListener
{
  private static final String                    TAG         = MainActivity.class.getSimpleName();
  private       LocalBinder        binder      = null;
  private       IBtServiceListener msgHandler  = null;
  //
  // Ein Messagehandler, der vom Service kommende Messages bearbeitet
  //
  @SuppressLint( "HandlerLeak" )
  private final Handler            mHandler    = new Handler()
  {
    @Override
    public void handleMessage(Message msg)
    {
      if( !(msg.obj instanceof BlueThoothMessage) )
      {
        Log.e(TAG, "Handler::handleMessage: Recived Message is NOT type of BlueThoothMessage!");
        return;
      }
      BlueThoothMessage smsg = ( BlueThoothMessage ) msg.obj;
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, String.format(Locale.ENGLISH, "Message Typ %s recived.", ProjectConst.getMsgName(smsg.getMsgType())));
      }
      if( smsg.getData() != null && smsg.getData().length() > 0 && BuildConfig.DEBUG )
      {
        Log.d(TAG, "Handler::handleMessage: <" + smsg.getData() + ">");
      }
      if( msgHandler != null )
      {
        msgHandler.handleMessages(smsg);
      }
    }
  };
  //
  // Lebensdauer des Service wird beim binden / unbinden benutzt
  //
  private final ServiceConnection  mConnection = new ServiceConnection()
  {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "onServiceConnected()...");
      }
      binder = ( LocalBinder ) service;
      if( binder == null )
      {
        return;
      }
      binder.registerServiceHandler(mHandler);
    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "onServiceDisconnected...");
      }
      if( binder != null )
      {
        if( BuildConfig.DEBUG )
        {
          Log.d(TAG, "onServiceDisconnected...unregister Handler...");
        }
        binder.unregisterServiceHandler();
      }
      binder = null;
    }
  };

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
      if( binder != null )
      {
        binder.disconnect();
      }
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
    if( (binder != null) && (HomeLightSysConfig.getLastConnectedDeviceAddr() != null) && HomeLightSysConfig.isAutoReconnect() )
    {
      Log.i(TAG, String.format(Locale.ENGLISH, "request to reconnect to device <%s>", HomeLightSysConfig.getLastConnectedDeviceAddr()));
      binder.connectTo(HomeLightSysConfig.getLastConnectedDeviceAddr());
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
    //
    // Ist ein BT Adapter vorhanden?
    //
    if( bluetoothManager == null )
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
    //
    // Zunächst mal das Verbindungsfragment einstellen
    //
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onNavigationItemSelected: make and insert connection fragment...");
    }
    LightRootFragment   newFrag = new BTConnectFragment();
    FragmentTransaction fTrans  = getFragmentManager().beginTransaction();
    fTrans.replace(R.id.main_container, newFrag);
    fTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN | FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    fTrans.commit();
    msgHandler = newFrag;
    drawer.closeDrawer(GravityCompat.START);
  }

  @Override
  public void onResume()
  {
    super.onResume();
    //
    // Systemeinstellungen neu einlesen
    //
    HomeLightSysConfig.readSysPrefs(getResources(), PreferenceManager.getDefaultSharedPreferences(this));
    NavigationView navigationView = ( NavigationView ) findViewById(R.id.nav_view);
    //
    // Anzeigen oder verstecken der Menüpunkte nach Systemeinstellungen
    //
    navigationView.getMenu().findItem(R.id.navColorCircle).setVisible(HomeLightSysConfig.isShowColorWheel());
    navigationView.getMenu().findItem(R.id.navColorBrightness).setVisible(HomeLightSysConfig.isShowBrightnessOnly());
    navigationView.getMenu().findItem(R.id.navColorEqualizer).setVisible(HomeLightSysConfig.isShowEqualizer());
    navigationView.getMenu().findItem(R.id.navColorPresets).setVisible(HomeLightSysConfig.isShowColorPresets());
    //
    // Stelle sicher, dass der BT Adapter aktiviert wurde
    // erzeuge einen Intend (eine Absicht) und schicke diese an das System
    //
    if( !(( BluetoothManager )getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isEnabled() )
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
    //
    // Vorerst nur der Platzhalter für ein Spielerchen später
    //
    FloatingActionButton fab = ( FloatingActionButton ) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        setModulPause();
        Snackbar.make(view, getResources().getString(R.string.main_modul_pause), Snackbar.LENGTH_LONG).setAction("Action", null).show();
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
   * Das Optionen Menü erzeugen (auch in der ActionBar)
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
    FragmentTransaction fTrans = null;
    LightRootFragment            newFrag = null;
    // Handle navigation view item clicks here.
    int id = item.getItemId();
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, String.format(Locale.ENGLISH, "onNavigationDrawerItemSelected: id: <%d>...", id));
    }
    //
    // Entsprechend der selektierten Navigator-Id
    //
    switch( id )
    {
      case R.id.navColorCircle:
        // Farbkreis
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "onNavigationDrawerItemSelected: make color wheel fragment...");
        }
        newFrag = new ColorCircleFragment();
        fTrans = getFragmentManager().beginTransaction();
        break;

      case R.id.navColorBrightness:
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "onNavigationDrawerItemSelected: make only brightness slider fragment...");
        }
        newFrag = new WhiteOnlyFragment();
        fTrans = getFragmentManager().beginTransaction();
        break;

      case R.id.navColorEqualizer:
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "onNavigationItemSelected: make color equalizer fragment...");
        }
        newFrag = new PlaceholderFragment();
        fTrans = getFragmentManager().beginTransaction();
        break;

      case R.id.navColorPresets:
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "onNavigationItemSelected: make color presets fragment...");
        }
        newFrag = new PlaceholderFragment();
        fTrans = getFragmentManager().beginTransaction();
        break;

      case R.id.navCommBT:
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "onNavigationItemSelected: make connection fragment...");
        }
        newFrag = new BTConnectFragment();
        fTrans = getFragmentManager().beginTransaction();
        break;

      case R.id.navPropertys:
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "onNavigationItemSelected: make program propertys fragment...");
        }
        SystemPreferencesFragment pFrag = new SystemPreferencesFragment();
        fTrans = getFragmentManager().beginTransaction();
        fTrans.replace(R.id.main_container, pFrag);
        fTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN | FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fTrans.commit();
        fTrans = null;
        break;

      case R.id.navQuit:
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "onNavigationItemSelected: quit app...");
        }
        Bundle bu = new Bundle();
        bu.putString(AreYouSureDialogFragment.HEADLINE, getString(R.string.dialog_sure_exit));
        AreYouSureDialogFragment sureDial = new AreYouSureDialogFragment();
        sureDial.setArguments(bu);
        sureDial.show(getFragmentManager().beginTransaction(), "programexit");
        break;

      default:
        Log.e(TAG, "onNavigationItemSelected: unknown menuentry recived...");
    }
    if( fTrans != null )
    {
      if( BuildConfig.DEBUG)
      {
        Log.v(TAG, "onNavigationItemSelected: replace new Fragment...");
      }
      fTrans.replace(R.id.main_container, newFrag);
      fTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN | FragmentTransaction.TRANSIT_FRAGMENT_FADE);
      fTrans.commit();
    }
    msgHandler = newFrag;
    if( BuildConfig.DEBUG)
    {
      if( msgHandler == null )
      {
        Log.w(TAG, "set messgeHandler to null");
      }
      else
      {
        if( newFrag != null )
        {
          Log.i(TAG, "set messgeHandler to " + newFrag.getClass().getSimpleName());
        }
        else
        {
          Log.e(TAG, "set messgeHandler to unknown type of fragment");
        }
      }
    }
    DrawerLayout drawer = ( DrawerLayout ) findViewById(R.id.main_drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  /**
   * Suche nach BTLE Geräten (wenn uuidArr != null nach allen)
   *
   * @param uuidArr
   * @return konnte der Vorgang gestartet werden?
   */
  @Override
  public boolean discoverDevices(String[] uuidArr)
  {
    if( binder != null )
    {
      return binder.discoverDevices(uuidArr);
    }
    return false;
  }

  /**
   * Stoppe die Erkundung der BTLE Geräte falls gerade in Arbeit
   */
  @Override
  public void stopDiscoverDevices()
  {
    if( binder != null )
    {
      binder.stopDiscoverDevices();
    }
  }

  /**
   * Berbinde zu einem BTLE Modul
   *
   * @param addr Adresse des Modules
   */
  @Override
  public void connectTo(String addr)
  {
    if( binder != null )
    {
      HomeLightSysConfig.setLastConnectedDeviceAddr(getResources(), PreferenceManager.getDefaultSharedPreferences(this), addr);
      binder.connectTo(addr);
    }
  }

  /**
   * Trenne explizit die Verbindung mit einem BTLE Modul
   */
  @Override
  public void disconnect()
  {
    if( binder != null )
    {
      HomeLightSysConfig.setLastConnectedDeviceAddr(getResources(), PreferenceManager.getDefaultSharedPreferences(this), null);
      binder.disconnect();
    }
  }

  /**
   * Frage nach dem Onlinestatus des Services
   */
  @Override
  public int askModulOnlineStatus()
  {
    if( binder != null )
    {
      return binder.askModulOnlineStatus();
    }
    return( ProjectConst.STATUS_CONNECT_ERROR );
  }

  /**
   * Frage welches Modul verbunden ist
   *
   * @return Moduladresse oder NULL
   */
  @Override
  public BluetoothDevice askConnectedModul()
  {
    if( binder != null )
    {
      return binder.askConnectedModul();
    }
    return( null );
  }

  /**
   * Frage (noch einmal) nach dem Modultyp
   */
  @Override
  public void askModulForType()
  {
    if( binder != null )
    {
      binder.askModulForType();
    }
  }

  /**
   * Fragt das Modul nach seinem Namen
   */
  @Override
  public void askModulForName()
  {
    if( binder != null )
    {
      binder.askModulForName();
    }
  }

  /**
   * gib Modulneman zurück, wenn im Service schon ermittelt
   *
   * @return Modulname
   */
  @Override
  public String getConnectedModulName()
  {
    if( binder != null )
    {
      return (binder.getConnectedModulName());
    }
    return null;
  }

  /**
   * Frage das Modul nach der aktuellen RGBW Einstellung (Roh)
   */
  @Override
  public void askModulForRGBW()
  {
    if( binder != null )
    {
      binder.askModulForRGBW();
    }
  }

  /**
   * Schaltet das Modul dunkel oder hell
   */
  @Override
  public void setModulPause()
  {
    if( binder != null )
    {
      binder.setModulPause();
    }
  }

  /**
   * Setze Farben als RGB
   *
   * @param rgbw RGB Werte
   */
  @Override
  public void setModulRawRGBW(short[] rgbw)
  {
    if( binder != null )
    {
      binder.setModulRawRGBW(rgbw);
    }
  }

  /**
   * Setze Farben als RGB, Modul kalibriert nach RGBW
   *
   * @param rgbw RGB Werte, White wird ignoriert
   */
  @Override
  public void setModulRGB4Calibrate(short[] rgbw)
  {
    if( binder != null )
    {
      binder.setModulRGB4Calibrate(rgbw);
    }
  }

  /**
   * Setze den neuen Modulnamen
   *
   * @param newName der Neue Name
   */
  @Override
  public void setModuleName(String newName)
  {
    if( binder != null )
    {
      binder.setModuleName(newName);
    }
  }

  @Override
  public void onDialogPositiveClick(DialogFragment dialog)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "Positive dialog click!");
    }
    //
    // war es ein AreYouSureDialogFragment Dialog?
    //
    if( dialog instanceof AreYouSureDialogFragment )
    {
      AreYouSureDialogFragment aDial = ( AreYouSureDialogFragment ) dialog;
      //
      // War der Tag für den Dialog zum Exit des Programmes?
      //
      if( aDial.getTag().equals("programexit") )
      {
        Log.i(TAG, "User will close app...");
        Toast.makeText(this, R.string.toast_exit, Toast.LENGTH_SHORT).show();
        if( binder != null )
        {
          binder.disconnect();
        }
        if( BluetoothAdapter.getDefaultAdapter() != null )
        {
          // Preferences -> Programmeinstellungen soll das automatisch passieren?
          SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
          if( HomeLightSysConfig.isDiableBTonEXIT() )
          {
            if( BuildConfig.DEBUG )
            {
              Log.d(TAG, "disable BT Adapter on exit!");
            }
            BluetoothAdapter.getDefaultAdapter().disable();
          }
        }
        // Code nach stackoverflow
        // online geht das nicht....
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
        finish();
      }
    }
    //
    // Soll der Name des Modules geändert werden?
    //
    else if( dialog instanceof EditModuleNameDialogFragment )
    {
      EditModuleNameDialogFragment edDial = ( EditModuleNameDialogFragment ) dialog;
      if( edDial.getTag().equals("changeModuleName") )
      {
        // Das ist der Dialog, erfrage den Neuen Namen
        String newModuleName = edDial.getModuleName();
        if( getConnectedModulName() != null && !getConnectedModulName().equals(newModuleName) )
        {
          Log.i(TAG, "try set new module name...");
          setModuleName(newModuleName);
          // Nach einer Wartezeit Verbindung trennen!
          if( mHandler != null )
          {
            mHandler.postDelayed(new Runnable()
            {
              public void run()
              {
                disconnect();
              }
            }, 1200);
          }
        }

      }

    }
  }

  @Override
  public void onDialogNegativeClick(DialogFragment dialog)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "Positive negative click!");
    }

  }
}
