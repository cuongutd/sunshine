package com.sunshineapp.cuong.sunshine;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    public void setIsTwoPane(boolean isTwoPane) {
        this.isTwoPane = isTwoPane;
    }

    public boolean isTwoPane() {
        return isTwoPane;
    }

    private boolean isTwoPane;



    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layout = -1;
        if (viewType == VIEW_TYPE_TODAY)
            layout = R.layout.list_item_forecast_today;
        else
            layout = R.layout.list_item_forecast;
        View view = LayoutInflater.from(context).inflate(layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return position==0 && !isTwoPane? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    /*
            This is where we fill-in the views with the contents of the cursor.
         */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder)view.getTag();

        if (cursor.getPosition()==0 && !isTwoPane)
            holder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
        else
            holder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));

        //date
        //if (cursor.getPosition()==0 && !isTwoPane) {

            holder.dateView.setText(Utility.getFriendlyDayString(context, cursor.getLong(ForecastFragment.COL_WEATHER_DATE)));
        //}else{
        //    holder.dateView.setText(Utility.getDayName(context, cursor.getLong(ForecastFragment.COL_WEATHER_DATE)));
        //}

        //forecast
        holder.descriptionView.setText(cursor.getString(ForecastFragment.COL_WEATHER_DESC));

        boolean isMetric = Utility.isMetric(mContext);
        //high temp
        holder.highTempView.setText(Utility.formatTemperature(context, cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP)));

        //low temp
        holder.lowTempView.setText(Utility.formatTemperature(context, cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP)));


    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
}