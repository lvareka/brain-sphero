package cz.zcu.kiv.brainsphero;


import java.io.IOException;

import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.sphero.ConnectionListener;
import orbotix.sphero.Sphero;
import orbotix.view.calibration.CalibrationView;
import orbotix.view.connection.SpheroConnectionView;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.brainsphero.R;
import com.neurosky.thinkgear.TGDevice;

public class MainActivity extends Activity implements OnSeekBarChangeListener {
	protected ProgressBar pbAttention;
	protected ProgressBar pbMeditation;
	protected Button bMindwave;
	protected Button bSphero;
	protected SeekBar sbAttention;
	protected SeekBar sbMeditation;
	protected static TextView tvCommand;
	
	
	private BluetoothAdapter bluetoothAdapter;
	
	protected static int decisionThresholdM = 0;
	protected static int decisionThresholdA = 0;
	protected static int latestMeditation = 0;
	protected static int latestAttention = 0;
	protected static float latestSpeed = 0.0f;
	
	// MindWave reference
	TGDevice tgDevice = null;
    private Sphero mRobot = null;

    /** The Sphero Connection View */
	private SpheroConnectionView mSpheroConnectionView = null;
	
	private CalibrationView mCalibrationView;
	
	//SeekBar
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
	        boolean fromUser) {
		if (seekBar.equals(sbAttention)) {
			this.sbAttention = seekBar;
		}
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        
        pbAttention = (ProgressBar) findViewById(R.id.pb_attention);
        pbMeditation = (ProgressBar) findViewById(R.id.pb_meditation);
        pbAttention.setBackgroundColor(Color.RED);
        pbMeditation.setBackgroundColor(Color.GREEN);
        
        sbAttention = (SeekBar) findViewById(R.id.sbAttention);
        sbMeditation = (SeekBar) findViewById(R.id.sbMeditation);
        
        tvCommand = (TextView) findViewById(R.id.tv_command);
        
        bMindwave = (Button) findViewById(R.id.b_mindwave);
        bSphero = (Button) findViewById(R.id.b_sphero);
        
      //  bSphero.setEnabled(false);
        
        pbAttention .setMax(100);
        pbMeditation.setMax(100);
        
        //pbAttention.setProgress(50);
        

     /*   mCalibrationView = (CalibrationView)findViewById(R.id.CalibrationView);
        mCalibrationView.setColor(Color.WHITE);
        mCalibrationView.setCircleColor(Color.WHITE);
        mCalibrationView.enable();
        */
        
        
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
           	Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
        	finish();
        	return;
        } else {
        	/* create the TGDevice */
        	tgDevice = new TGDevice(bluetoothAdapter, handler);
        }  
        //pbAttention.setProgress(50);

        
    }
    

    protected void onCreateView() {
    	
    }
    
   

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
     @Override
    public void onDestroy() {
    	super.onDestroy();
    	if (tgDevice != null)
    		tgDevice.close();
    	  
    	if (mRobot != null) {
    		mRobot.disconnect();
    		/*try{
    			 unregisterReceiver(mRobotDiscoveredReceiver);
    	 }catch (RuntimeException e){
    			 Log.e("app", "Failed to unregister receiver. Likely not registered.");
    	 }*/
    	}
        
    }
    
    /**
     * Handles messages from TGDevice
     */
    private final Handler handler = new Handler() {
    		
        @Override
        public void handleMessage(Message msg) {
        	switch (msg.what) {
            case TGDevice.MSG_STATE_CHANGE:

                switch (msg.arg1) {
	                                
	                case TGDevice.STATE_CONNECTED:
	                  	tgDevice.start();
	                  	bSphero.setEnabled(true);
	                    break;
	                case TGDevice.STATE_CONNECTING:
	                	Toast.makeText(getApplicationContext(), "Neurosky: connecting..", Toast.LENGTH_SHORT).show();
	                  	//bSphero.setEnabled(false);
	                	break;
                	case TGDevice.STATE_DISCONNECTED:
	                	Toast.makeText(getApplicationContext(), "Neurosky: disconnected..", Toast.LENGTH_SHORT).show();
	                  	//bSphero.setEnabled(false);
	                	break;
                	case TGDevice.STATE_NOT_FOUND:
	                	Toast.makeText(getApplicationContext(), "Neurosky: not found..", Toast.LENGTH_SHORT).show();
	                  	//bSphero.setEnabled(false);
                		break;
                	case TGDevice.STATE_NOT_PAIRED:
	                	Toast.makeText(getApplicationContext(), "Neurosky: not paired..", Toast.LENGTH_SHORT).show();
	                  	//bSphero.setEnabled(false);
                		break;
               		default:
               			break;

	              
                }

                break;
            	          
            case TGDevice.MSG_ATTENTION:
            	MainActivity.latestAttention = msg.arg1;
            	if (mRobot != null) {
            		if (MainActivity.latestAttention > sbAttention.getProgress()) {
            			 // Roll robot
            			MainActivity.latestSpeed =  (MainActivity.latestAttention -  sbAttention.getProgress()) / 200.0f;
            	        mRobot.drive(0f, MainActivity.latestSpeed);
            		}
            		else {
            			mRobot.stop();
            		}
            	} 
            	//  inform the user about the current intention
            	
        		if (MainActivity.latestAttention > sbAttention.getProgress()) {
        			// Roll robot
        			//Toast.makeText(MainActivity.this, "Roll..", Toast.LENGTH_LONG).show();
        			tvCommand.setText("Roll.. Last: " + MainActivity.latestAttention + ", threshold: " +  sbAttention.getProgress() + ", speed: " + MainActivity.latestSpeed);
        		}
        		else {
        			//Toast.makeText(MainActivity.this, "Stop..", Toast.LENGTH_LONG).show();
        			tvCommand.setText("Stop.. Last: " + MainActivity.latestAttention + ", threshold: " +  sbAttention.getProgress() + ", speed: " + MainActivity.latestSpeed);
        		}
            
            	// TODO: check threshold and run/stop the Sphero ball
            	
            	pbAttention.setProgress(MainActivity.latestAttention);
            	break;
            case TGDevice.MSG_MEDITATION:
            	MainActivity.latestMeditation = msg.arg1;
            	
            	// TODO: check threshold and run/stop the Sphero ball
            	pbMeditation.setProgress(MainActivity.latestMeditation);
            	break;
            case TGDevice.MSG_LOW_BATTERY:
            	Toast.makeText(getApplicationContext(), "Neurosky: Low battery!", Toast.LENGTH_SHORT).show();
            	break;
            case TGDevice.MSG_POOR_SIGNAL:
            	Toast.makeText(getApplicationContext(), "Neurosky: Poor signal!", Toast.LENGTH_SHORT).show();
            	break;
            default:
            	break;
        }
        }
    };
    
    public void mindWaveConnect(View view) {
    	if(tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED)
    		tgDevice.connect(false);   
    }
    
    public void spheroConnect(View view) {
    	Toast.makeText(this, "Connecting to Sphero..", Toast.LENGTH_LONG).show(); 
    	if (mSpheroConnectionView == null) {
   	     	mSpheroConnectionView = (SpheroConnectionView) findViewById(R.id.sphero_connection_view);
   	        mSpheroConnectionView.addConnectionListener(new ConnectionListener() {

            @Override
            public void onConnected(Robot robot) {
                 //SpheroConnectionView is made invisible on connect by default
            	 // Hides the Sphero Connection View
                mSpheroConnectionView.setVisibility(View.INVISIBLE);
                
                mRobot = (Sphero) robot;
                mCalibrationView.setRobot(mRobot);
               	Toast.makeText(getApplicationContext(), "Sphero: Connected", Toast.LENGTH_SHORT).show();
                 //mRobot.startCalibration();
                // mRobot.rotateToHeading(angle);
                // mRobot.stopCalibration(true);
                // Toast.makeText(this, "Sphero: Connected...", Toast.LENGTH_LONG).show(); 
            }

            @Override
            public void onConnectionFailed(Robot sphero) {
                 // let the SpheroConnectionView handle or hide it and do something here...
            	  Toast.makeText(getApplicationContext(), "Sphero: Connection failed...", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDisconnected(Robot sphero) {
          	  Toast.makeText(getApplicationContext(), "Sphero: Disconnected...", Toast.LENGTH_LONG).show();
                mSpheroConnectionView.startDiscovery();
            }
         });
    	}
    }

    /** Called when the user comes back to this app */
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list of Spheros
        if (mSpheroConnectionView != null)
        	mSpheroConnectionView.startDiscovery();
        
    }


    /** Called when the user presses the back or home button */
    @Override
    protected void onPause() {
        super.onPause();
        // Disconnect Robot properly
      /*  if (mRobot != null) {
        	try {
				mRobot.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }*/
        
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        if (mRobot != null) {
        	mRobot.disconnect(); // Disconnect Robot properly
        }
        //RobotProvider.getDefaultProvider().disconnectControlledRobots();
        if (tgDevice != null)
    		tgDevice.close();
        
    }

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

}
