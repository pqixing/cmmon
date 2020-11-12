package com.pqixing.tvhome;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class HomeAdapter extends BaseAdapter {
    private final PackageManager pm;
    List<Item> infos;
    int selectItem = 0;
    Drawable colorSelect ;
    Drawable colorNorMal = new ColorDrawable(Color.TRANSPARENT);

    public HomeAdapter(Context context,PackageManager pm) {
        this.pm = pm;
        colorSelect =context.getResources().getDrawable(R.drawable.item_bg);
    }

    @Override
    public int getCount() {
        return infos == null ? 0 : infos.size();
    }

    @Override
    public Item getItem(int position) {
        return infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View inflate = convertView==null?View.inflate(parent.getContext(), R.layout.item_adapter, null):convertView;
        final Item item = getItem(position);
        ((ImageView) inflate.findViewById(R.id.ivIcon)).setImageDrawable(item.icon);
        ((TextView) inflate.findViewById(R.id.tvName)).setText(item.name);
        inflate.setBackground(position==selectItem?colorSelect:colorNorMal);
        inflate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem = position;
                v.getContext().startActivity(item.intent);
                notifyDataSetChanged();
            }
        });
        return inflate;
    }
}
