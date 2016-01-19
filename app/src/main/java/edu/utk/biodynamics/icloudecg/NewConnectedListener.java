package edu.utk.biodynamics.icloudecg;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import zephyr.android.BioHarnessBT.BTClient;
import zephyr.android.BioHarnessBT.ConnectListenerImpl;
import zephyr.android.BioHarnessBT.ConnectedEvent;
import zephyr.android.BioHarnessBT.PacketTypeRequest;
import zephyr.android.BioHarnessBT.ZephyrPacketArgs;
import zephyr.android.BioHarnessBT.ZephyrPacketEvent;
import zephyr.android.BioHarnessBT.ZephyrPacketListener;
import zephyr.android.BioHarnessBT.ZephyrProtocol;

/**
 * Created by DSClifford on 8/8/2015.
 */

//Extracts and seperates data received from BT Packets

public class NewConnectedListener extends ConnectListenerImpl
{
	private Handler _OldHandler;
	private Handler _aNewHandler; 
	
	final int ECG_NUM_MSG = 0x1;
	
	final int GP_MSG_ID = 0x20;
	final int BREATHING_MSG_ID = 0x21;
	final int ECG_MSG_ID = 0x22;
	final int RtoR_MSG_ID = 0x24;
	final int ACCEL_100mg_MSG_ID = 0x2A;
	final int SUMMARY_MSG_ID = 0x2B;
	
	
	private int GP_HANDLER_ID = 0x20;
	
	private final int HEART_RATE = 0x100;
	private final int RESPIRATION_RATE = 0x101;
	private final int SKIN_TEMPERATURE = 0x102;
	private final int POSTURE = 0x103;
	private final int PEAK_ACCLERATION = 0x104;
	private final int ECG = 0x105;
	
	/*Creating the different Objects for different types of Packets*/
	private GeneralPacketInfo GPInfo = new GeneralPacketInfo();
	private ECGPacketInfo ECGInfoPacket = new ECGPacketInfo();
	private BreathingPacketInfo BreathingInfoPacket = new BreathingPacketInfo();
	private RtoRPacketInfo RtoRInfoPacket = new RtoRPacketInfo();
	private AccelerometerPacketInfo AccInfoPacket = new AccelerometerPacketInfo();
	private SummaryPacketInfo SummaryInfoPacket = new SummaryPacketInfo();
	
	private PacketTypeRequest RqPacketType = new PacketTypeRequest();
	public NewConnectedListener(Handler handler, Handler _NewHandler) {
		super(handler, null);
		_OldHandler= handler;
		_aNewHandler = _NewHandler;

		// TODO Auto-generated constructor stub

	}
	public void Connected(ConnectedEvent<BTClient> eventArgs) {
		//System.out.println(String.format("Connected to BioHarness %s.", eventArgs.getSource().getDevice().getName()));
		/*Use this object to enable or disable the different Packet types*/
		RqPacketType.GP_ENABLE = true;
		RqPacketType.ECG_ENABLE = true;
		RqPacketType.RtoR_ENABLE = true;
		RqPacketType.BREATHING_ENABLE = true;
		RqPacketType.LOGGING_ENABLE = true;
		Log.d("Debug","Entered ConnectedEvent");
		
		
		//Creates a new ZephyrProtocol object and passes it the BTComms object
		ZephyrProtocol _protocol = new ZephyrProtocol(eventArgs.getSource().getComms(), RqPacketType);
		//ZephyrProtocol _protocol = new ZephyrProtocol(eventArgs.getSource().getComms(), );
		_protocol.addZephyrPacketEventListener(new ZephyrPacketListener() {
			public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
             //   Log.d("Debu","Zeph Pac Received");
				ZephyrPacketArgs msg = eventArgs.getPacket();
				byte CRCFailStatus;
				byte RcvdBytes;
				
				
				
				CRCFailStatus = msg.getCRCStatus();
				RcvdBytes = msg.getNumRvcdBytes() ;
				int MsgID = msg.getMsgID();
				byte [] DataArray = msg.getBytes();	
				switch (MsgID)
				{

				case GP_MSG_ID:
				//general packet data
					//***************Displaying the Heart Rate********************************
					int HRate =  GPInfo.GetHeartRate(DataArray);
					Message text1 = _aNewHandler.obtainMessage(HEART_RATE);
					Bundle b1 = new Bundle();
					b1.putInt("HeartRate", HRate);
					text1.setData(b1);
					_aNewHandler.sendMessage(text1);

					//***************Displaying the Respiration Rate********************************
					double RespRate = GPInfo.GetRespirationRate(DataArray);
					
					text1 = _aNewHandler.obtainMessage(RESPIRATION_RATE);
					b1.putDouble("RespirationRate", RespRate);
					text1.setData(b1);
					_aNewHandler.sendMessage(text1);
					//System.out.println("Respiration Rate is "+ RespRate);
					
					//***************Displaying the Skin Temperature*******************************

					String BatteryStatus = Byte.toString(GPInfo.GetBatteryStatus(DataArray));
					 text1 = _aNewHandler.obtainMessage(SKIN_TEMPERATURE);
					//Bundle b1 = new Bundle();
					b1.putString("SkinTemperature", String.valueOf(BatteryStatus));
					text1.setData(b1);
					_aNewHandler.sendMessage(text1);
					//System.out.println("Skin Temperature is "+ SkinTempDbl);
					
					//***************Displaying the Posture******************************************					

				int PostureInt = GPInfo.GetPosture(DataArray);
				text1 = _aNewHandler.obtainMessage(POSTURE);
				b1.putString("Posture", String.valueOf(PostureInt));
				text1.setData(b1);
				_aNewHandler.sendMessage(text1);
				//System.out.println("Posture is "+ PostureInt);	
				//***************Displaying the Peak Acceleration******************************************

				double PeakAccDbl = GPInfo.GetPeakAcceleration(DataArray);
				text1 = _aNewHandler.obtainMessage(PEAK_ACCLERATION);
				b1.putString("PeakAcceleration", String.valueOf(PeakAccDbl));
				text1.setData(b1);
				_aNewHandler.sendMessage(text1);
				//System.out.println("Peak Acceleration is "+ PeakAccDbl);	
				
				double ECGDbl = GPInfo.GetECGAmplitude(DataArray);
				text1 = _aNewHandler.obtainMessage(ECG);
				b1.putString("ECG", String.valueOf(ECGDbl));
				text1.setData(b1);
				_aNewHandler.sendMessage(text1);
				//System.out.println("ECG is "+ ECGDbl);
				
				byte ROGStatus = GPInfo.GetROGStatus(DataArray);
				//System.out.println("ROG Status is "+ ROGStatus);
				
					break;
				case BREATHING_MSG_ID:
					/*Do what you want. Printing Sequence Number for now*/
					//System.out.println("Breathing Packet Sequence Number is "+BreathingInfoPacket.GetSeqNum(DataArray));
					break;
				case ECG_MSG_ID:
					//ECG data
					short ECG[] =  ECGInfoPacket.GetECGSamples(DataArray);
					double EcgNoise = GPInfo.GetECGNoise(DataArray);
					Message text2 = _aNewHandler.obtainMessage(ECG_MSG_ID);
					Bundle b2 = new Bundle();
					b2.putShortArray("ECG", ECG);
					text2.setData(b2);
					_aNewHandler.sendMessage(text2);
					
					//System.out.println("ECG Packet Sequence Number is "+ECGInfoPacket.GetSeqNum(DataArray));
					break;
				case RtoR_MSG_ID:
					//RtoR data
					int RtoR[] =  RtoRInfoPacket.GetRtoRSamples(DataArray);
					Message text3 = _aNewHandler.obtainMessage(RtoR_MSG_ID);
					Bundle b3 = new Bundle();
					b3.putIntArray("RtoR", RtoR);
					text3.setData(b3);
					_aNewHandler.sendMessage(text3);
					
					//System.out.println("R to R Packet Sequence Number is "+RtoRInfoPacket.GetSeqNum(DataArray));
					break;
				case ACCEL_100mg_MSG_ID:
					/*Do what you want. Printing Sequence Number for now*/
					//System.out.println("Accelerometry Packet Sequence Number is "+AccInfoPacket.GetSeqNum(DataArray));
					break;
				case SUMMARY_MSG_ID:
					/*Do what you want. Printing Sequence Number for now*/
					//System.out.println("Summary Packet Sequence Number is "+SummaryInfoPacket.GetSeqNum(DataArray));
					break;
					
				}
			
			}
		});
	}
	
}