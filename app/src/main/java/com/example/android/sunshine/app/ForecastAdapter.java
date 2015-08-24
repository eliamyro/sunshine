package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;

/**
 * Created by Elias Myronidis on 24/6/2015.
 */

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.support.v7.widget.RecyclerView}.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE = 1;

    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseTodayLayout;

    private Cursor mCursor;
    final private Context mContext;

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ForecastAdapterViewHolder extends RecyclerView.ViewHolder {
        public final ImageView mIconView;
        public final TextView mDateView;
        public final TextView mDescriptionView;
        public final TextView mHighTempView;
        public final TextView mLowTempView;

        public ForecastAdapterViewHolder(View view) {
            super(view);
            mIconView = (ImageView) view.findViewById(R.id.list_item_icon);
            mDateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            mDescriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            mHighTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            mLowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }

    }

    public ForecastAdapter(Context context) {
        mContext = context;
    }

    /*
       This takes advantage of the fact that the viewGroup passed to onCreateViewHolder is the
       RecyclerView that will be used to contain the view, so that it can get the current
       ItemSelectionManager from the view.
       One could implement this pattern without modifying RecyclerView by taking advantage
       of the view tag to store the ItemChoiceManager.
    */
    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewGroup instanceof RecyclerView) {
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TYPE_TODAY:
                    layoutId = R.layout.list_item_forecast_today;
                    break;

                case VIEW_TYPE_FUTURE:
                    layoutId = R.layout.list_item_forecast;
                    break;
            }
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            view.setFocusable(true);
            return new ForecastAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to recyclerview selection");
        }

    }


    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder forecastAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);

        // Read weather icon ID from cursor
        int weatherId = mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int defaultImage;

        switch (getItemViewType(position)) {
            case VIEW_TYPE_TODAY:
                // Get weather icon
                defaultImage = Utility.getArtResourceForWeatherCondition(weatherId);
                break;

            default:
                defaultImage = Utility.getIconResourceForWeatherCondition(weatherId);
                break;
        }

        if (Utility.usingLocalGraphics(mContext)) {
            forecastAdapterViewHolder.mIconView.setImageResource(defaultImage);
        } else {
            Glide.with(mContext)
                    .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
                    .error(defaultImage)
                    .crossFade()
                    .into(forecastAdapterViewHolder.mIconView);
        }


        // Read date from cursor
        long dateInMillis = mCursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        // Find TextView and set formatted date on it
        forecastAdapterViewHolder.mDateView.setText(Utility.getFriendlyDayString(mContext, dateInMillis));

        // Get description from weather condition ID
        String description = Utility.getStringForWeatherCondition(mContext, weatherId);
        forecastAdapterViewHolder.mDescriptionView.setText(description);
        forecastAdapterViewHolder.mDescriptionView.setContentDescription(mContext.getString(R.string.a11y_forecast, description));

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(mContext);

        // Read high temperature from cursor
        double high = mCursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        String highString = Utility.formatTemperature(mContext, high, isMetric);
        forecastAdapterViewHolder.mHighTempView.setText(highString);
        forecastAdapterViewHolder.mHighTempView.setContentDescription(mContext.getString(R.string.a11y_high_temp, highString));

        // Read low temperature from cursor
        double low = mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        String lowString = Utility.formatTemperature(mContext, low, isMetric);
        forecastAdapterViewHolder.mLowTempView.setText(lowString);
        forecastAdapterViewHolder.mLowTempView.setContentDescription(mContext.getString(R.string.a11y_low_temp, lowString));
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE;
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }


//    /**
//     * Prepare the weather high/lows for presentation.
//     */
//    private String formatHighLows(double high, double low) {
//        boolean isMetric = Utility.isMetric(mContext);
//        String highLowStr = Utility.formatTemperature(mContext, high, isMetric) + "/" + Utility.formatTemperature(mContext, low, isMetric);
//        return highLowStr;
//    }
//
//    /*
//        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
//        string.
//     */
//    private String convertCursorRowToUXFormat(Cursor cursor) {
//        // get row indices for our cursor
////        int idx_max_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
////        int idx_min_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
////        int idx_date = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
////        int idx_short_desc = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);
//
//        String highAndLow = formatHighLows(
//                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
//                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));
//
//        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
//                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
//                " - " + highAndLow;
//    }












}