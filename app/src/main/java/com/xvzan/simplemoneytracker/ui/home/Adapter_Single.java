package com.xvzan.simplemoneytracker.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.icu.util.Currency;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.xvzan.simplemoneytracker.MainActivity;
import com.xvzan.simplemoneytracker.R;
import com.xvzan.simplemoneytracker.dbsettings.mTra;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.Sort;

public class Adapter_Single extends RecyclerView.Adapter<Adapter_Single.SingleTraHolder> implements FastScroller.BubbleTextGetter {

    private Context mContext;
    private SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd");
    private NumberFormat numberFormat;
    private double d_Double;
    private OrderedRealmCollection<mTra> mTraList;
    Long[] longs;
    private Long[] tempLongs;
    //private String accstr;
    private int accOrder;
    private Realm realminstance;

    Adapter_Single(Context context, int order, Realm instance) {
        this.mContext = context;
        accOrder = order;
        numberFormat = NumberFormat.getCurrencyInstance();
        d_Double = Math.pow(10d, Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits());
        realminstance = instance;
        mTraList = realminstance.where(mTra.class).equalTo("accU.order", accOrder).or().equalTo("accB.order", accOrder).findAll().sort("mDate", Sort.ASCENDING);
        if (mTraList.size() >= 512) {
            tempLongs = new Long[32];
            tempLongs[0] = realminstance.where(mTra.class).equalTo("accU.order", accOrder).findAllAsync().sum("uAm").longValue() + realminstance.where(mTra.class).equalTo("accB.order", accOrder).findAllAsync().sum("bAm").longValue();
            for (int i = 0; i < 31; i++) {
                tempLongs[i + 1] = tempLongs[i] - getAmount(mTraList.size() - i - 1);
            }
        }
    }

    @Override
    public Date getDateToShowInBubble(final int pos) {
        return mTraList.get(pos).getmDate();
    }

    @Override
    public Adapter_Single.SingleTraHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SingleTraHolder(LayoutInflater.from(mContext).inflate(R.layout.transaction_single, parent, false));
    }

    @Override
    public void onBindViewHolder(Adapter_Single.SingleTraHolder holder, final int position) {
        if (mTraList.get(position).getAccU().getOrder() == accOrder) {
            holder.tsAccount.setText(mTraList.get(position).getAccB().getAname());
            holder.tsAmount.setText(numberFormat.format(mTraList.get(position).getuAm() / d_Double));
            if (mTraList.get(position).getuAm() < 0)
                holder.tsAmount.setTextColor(Color.RED);
            else
                holder.tsAmount.setTextColor(holder.tsDate.getTextColors());
        } else {
            holder.tsAccount.setText(mTraList.get(position).getAccU().getAname());
            holder.tsAmount.setText(numberFormat.format(mTraList.get(position).getbAm() / d_Double));
            if (mTraList.get(position).getbAm() < 0)
                holder.tsAmount.setTextColor(Color.RED);
            else
                holder.tsAmount.setTextColor(holder.tsDate.getTextColors());
        }
        if (longs == null) {
            if (tempLongs != null && tempLongs[0] != null) {
                if (mTraList.size() - position < 32) {
                    if (tempLongs[mTraList.size() - position - 1] < 0)
                        holder.tsTotal.setTextColor(Color.RED);
                    else
                        holder.tsTotal.setTextColor(holder.tsDate.getTextColors());
                    holder.tsTotal.setText(numberFormat.format(tempLongs[mTraList.size() - position - 1] / d_Double));
                } else {
                    holder.tsTotal.setText(R.string.calculating);
                }
            }
        } else if (longs[position] != null) {
            if (longs[position] < 0)
                holder.tsTotal.setTextColor(Color.RED);
            else
                holder.tsTotal.setTextColor(holder.tsDate.getTextColors());
            holder.tsTotal.setText(numberFormat.format(longs[position] / d_Double));
        }
        holder.tsDate.setText(sdf.format(mTraList.get(position).getmDate()));
        holder.tsEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) mContext).mTraToEdit = mTraList.get(position);
                Navigation.findNavController(v).navigate(R.id.nav_edit_tran);
            }
        });
    }

    private Long getAmount(int pos) {
        if (mTraList.get(pos).getAccU().getOrder() == accOrder) {
            return mTraList.get(pos).getuAm();
        } else {
            return mTraList.get(pos).getbAm();
        }
    }

    @Override
    public int getItemCount() {
        return mTraList.size();
    }

    class SingleTraHolder extends RecyclerView.ViewHolder {
        TextView tsDate;
        TextView tsAccount;
        TextView tsAmount;
        TextView tsTotal;
        ImageButton tsEdit;

        SingleTraHolder(View itemView) {
            super(itemView);
            tsDate = itemView.findViewById(R.id.tsDate);
            tsAccount = itemView.findViewById(R.id.tsAccount);
            tsAmount = itemView.findViewById(R.id.tsAmount);
            tsTotal = itemView.findViewById(R.id.tsTotal);
            tsEdit = itemView.findViewById(R.id.bt_ts_edit);
        }
    }
}
