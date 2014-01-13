package com.revo.widget.calc;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.widget.RemoteViews;

public class CalcAppWidgetProvider extends AppWidgetProvider{
	final static String ACTION_BUTTONS = "com.revowidget.action.WIDGET_BUTTONS";

	StringBuffer sb = new StringBuffer();

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onDisabled(Context context) {
		// TODO Auto-generated method stub
		super.onDisabled(context);
	}

	@Override
	public void onEnabled(Context context) {
		// TODO Auto-generated method stub
		super.onEnabled(context);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		super.onReceive(context, intent);
				
		if(intent.getAction().equals(ACTION_BUTTONS)) {
			String value = intent.getExtras().getString("button_value");
			Logic logic = new Logic(context, value);
			String text = "";
			if(value.equals("=")) {
				text = logic.onEnter();
			} else if(value.equals("ac")) {
				text = logic.onAC();
			}
			else {
				text = logic.getDisplay();
			}
			
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
			views.setTextViewText(R.id.display, text);
			
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			appWidgetManager.updateAppWidget(new ComponentName(context, getClass()), views);
		}
		
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// TODO Auto-generated method stub
		//super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		setButtonOnclickListener(context, appWidgetManager, appWidgetIds);
		
	}
	
	private void setButtonOnclickListener(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		for (int i = 0; i < appWidgetIds.length; i++) { //这里必须用一个循环更新所有widget，不然按键事件失效
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
			
			Intent intent = new Intent(/*context, ButtonReceiver.class*/);
			intent.setAction(ACTION_BUTTONS);			
			PendingIntent pendingIntent;
			TypedArray buttons = context.getResources().obtainTypedArray(R.array.simple_buttons);	
			TypedArray button_values = context.getResources().obtainTypedArray(R.array.simple_buttons_value);
			for (int m = 0; m < buttons.length(); m++) {
				int buttonId = buttons.getResourceId(m, 0);
				intent.putExtra("button_value", button_values.getString(m)); //将button的值传入				
				pendingIntent = PendingIntent.getBroadcast(
						context, buttonId, intent, PendingIntent.FLAG_UPDATE_CURRENT); //必须传入buutonID，否则获取的是最后一个button的值
				remoteViews.setOnClickPendingIntent(buttonId, pendingIntent);
			}			
			buttons.recycle();
			button_values.recycle();
	
			appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
		}
		
		
	}
	

}
