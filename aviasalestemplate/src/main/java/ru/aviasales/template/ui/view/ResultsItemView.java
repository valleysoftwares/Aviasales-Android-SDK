package ru.aviasales.template.ui.view;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.BuildConfig;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Currency;
import java.util.Map;

import ru.aviasales.core.search.object.TicketData;
import ru.aviasales.template.R;
import ru.aviasales.template.api.AirlineLogoApi;
import ru.aviasales.template.api.params.AirlineLogoParams;
import ru.aviasales.template.utils.CurrencyUtils;
import ru.aviasales.template.utils.StringUtils;
import ru.aviasales.template.utils.Utils;

public class ResultsItemView extends CardView {

	private TextView tvPrice;
	private TextView tvCurrency;
	private ImageView ivAirlineLogo;
	private ResultsItemRouteView rlDirectRoute;
	private ResultsItemRouteView rlReturnRoute;

	public ResultsItemView(Context context) {
		super(context);
	}

	public ResultsItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ResultsItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		tvPrice = (TextView) findViewById(R.id.tv_price);
		tvCurrency = (TextView) findViewById(R.id.tv_currency);
		ivAirlineLogo = (ImageView) findViewById(R.id.iv_airline);

		rlDirectRoute = (ResultsItemRouteView) findViewById(R.id.direct_route);
		rlReturnRoute = (ResultsItemRouteView) findViewById(R.id.return_route);

	}

	public void setTicketData(TicketData ticketData, Context context) {

		Map<String, Double> currencies = getCurrencyRates();

		tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP,  32);
		changeTextSize(ticketData.getPrice());

		tvPrice.setText(StringUtils.formatPriceInAppCurrency(ticketData.getPrice(), getAppCurrency(), currencies));

		tvCurrency.setText(getAppCurrency());

		try {
			final AirlineLogoParams params = new AirlineLogoParams();
			params.setContext(context);
			params.setIata(ticketData.getMainAirline());
			params.setImage(ivAirlineLogo);
			params.setWidth(context.getResources().getDimensionPixelSize(R.dimen.airline_logo_width));
			params.setHeight(context.getResources().getDimensionPixelSize(R.dimen.airline_logo_height));
			new AirlineLogoApi().getAirlineLogo(setAdditionalParamsToImageLoader(params));
		} catch (Exception e) {
			Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
			// skip if something wrong with it
		}

		rlDirectRoute.setRouteData(ticketData.getDirectFlights());

		if (ticketData.withoutReturn()) {
			rlReturnRoute.setVisibility(View.GONE);
		} else {
			rlReturnRoute.setRouteData(ticketData.getReturnFlights());
		}
	}

	public void setAlternativePrice(Integer price) {
		tvPrice.setText(StringUtils.formatPriceInAppCurrency(price, getAppCurrency(), getCurrencyRates()));
	}

	private Map<String, Double> getCurrencyRates() {
		return CurrencyUtils.getCurrencyRates();
	}

	protected AirlineLogoParams setAdditionalParamsToImageLoader(AirlineLogoParams params) {
		return params;
	}

	protected String getAppCurrency() {
		return CurrencyUtils.getAppCurrency(getContext());
	}

	public ImageView getIvAirlineLogo() {
		return ivAirlineLogo;
	}

	private void changeTextSize(int price) {
		final String priceText = StringUtils.formatPriceInAppCurrency(price, getAppCurrency(), getCurrencyRates());
		changePriceTextViewSizeIfNeeded(priceText);
	}

	private void changePriceTextViewSizeIfNeeded(String priceText) {
		int width = ((ViewGroup) tvPrice.getParent()).getWidth() == 0 ?
					getMeasuredMaxPriceTextViewWidth() : ((ViewGroup) tvPrice.getParent()).getWidth();

		if (width != 0) {
			float finalTextSizeInPx = tvPrice.getTextSize();
			int maxWidth = width - getResources().getDimensionPixelSize(R.dimen.airline_logo_width);
			int tvPriceRightMargin = Utils.convertDPtoPixels(getContext(), 3);
			int tvCurrencyMarginLeft = Utils.convertDPtoPixels(getContext(), 4);
			int currencyWidth = (int) tvCurrency.getPaint().measureText(CurrencyUtils.getAppCurrency(getContext()));
			Paint textPaint = new Paint(tvPrice.getPaint());
			int priceWidth = (int) (currencyWidth + tvPriceRightMargin + textPaint.measureText(priceText)) + tvCurrencyMarginLeft;
			while (priceWidth >= maxWidth) {
				finalTextSizeInPx -= Utils.convertDPtoPixels(getContext(), 1);
				textPaint.setTextSize(finalTextSizeInPx);
				priceWidth = (int) (currencyWidth + tvPriceRightMargin + textPaint.measureText(priceText)) + tvCurrencyMarginLeft;
			}

			tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalTextSizeInPx);
		}

	}

	private int getMeasuredMaxPriceTextViewWidth() {
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) getContext()
				.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(metrics);
		Point size = new Point();
		Display display = windowManager.getDefaultDisplay();
		if(Build.VERSION.SDK_INT >= 13) {
			display.getSize(size);
		} else {
			size.set(display.getWidth(), display.getHeight());
		}
		int cardViewMarginLeft = getResources().getDimensionPixelSize(R.dimen.results_item_margin_left);
		int cardViewMarginRight = getResources().getDimensionPixelSize(R.dimen.results_item_margin_right);

		int screenWidth = size.x - cardViewMarginLeft - cardViewMarginRight;
		measure(MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.AT_MOST),
				MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.AT_MOST));
		return ((ViewGroup) tvPrice.getParent()).getMeasuredWidth();
	}

}
