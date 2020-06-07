package com.china.app.remover.detector.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.china.app.remover.detector.R;
import com.china.app.remover.detector.listner.OnItemClickListener;
import com.china.app.remover.detector.model.AppInfo;
import com.china.app.remover.detector.model.ModelAlternative;

import java.util.ArrayList;
import java.util.List;

public class AlterNativeAdapter extends RecyclerView.Adapter<AlterNativeAdapter.ViewHolder> {
    private List<ModelAlternative> appInfos;
    Context context;

    public AlterNativeAdapter(Context context2, List<ModelAlternative> list) {
        this.context = context2;
        this.appInfos = list;
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_alternative, viewGroup, false));
    }

    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        final ModelAlternative item = getItem(i);
        if (item.isTitle()) {
            viewHolder.tvChinaAppTitle.setVisibility(View.VISIBLE);
            viewHolder.cardOurApp.setVisibility(View.GONE);
            viewHolder.tvChinaAppTitle.setText("Alternative Of " + item.getName());
        } else {
            viewHolder.tvChinaAppTitle.setVisibility(View.GONE);
            viewHolder.cardOurApp.setVisibility(View.VISIBLE);
            viewHolder.appsName.setText(item.getName());

        }
        viewHolder.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + item.getUrl()));
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException unused) {
                    context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://play.google.com/store/apps/details?id=" + item.getUrl())));
                }
            }
        });

    }

    private ModelAlternative getItem(int i) {
        return this.appInfos.get(i);
    }

    public int getItemCount() {
        return this.appInfos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView appsName;
        final ImageView send;
        final TextView tvChinaAppTitle;
        final CardView cardOurApp;

        ViewHolder(View view) {
            super(view);
            this.appsName = (TextView) view.findViewById(R.id.appsName);
            this.send = (ImageView) view.findViewById(R.id.send);
            this.tvChinaAppTitle = (TextView) view.findViewById(R.id.tvChinaAppTitle);
            this.cardOurApp = (CardView) view.findViewById(R.id.cardOurApp);
        }
    }
}