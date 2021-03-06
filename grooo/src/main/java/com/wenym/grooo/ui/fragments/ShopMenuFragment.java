/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wenym.grooo.ui.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;
import com.wenym.grooo.R;
import com.wenym.grooo.model.ecnomy.Food;
import com.wenym.grooo.model.ecnomy.Menu;
import com.wenym.grooo.provider.ShoppingBasket;
import com.wenym.grooo.provider.ExtraArgumentKeys;
import com.wenym.grooo.ui.activities.RestaurantDetailActivity;
import com.wenym.grooo.utils.Tools;
import com.wenym.grooo.widgets.BadgeView;
import com.wenym.grooo.widgets.CheckableTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class ShopMenuFragment extends Fragment implements StickyListHeadersListView.OnStickyHeaderChangedListener {


    private ArrayList<String> stickers;
    private ArrayList<String> stickers_left;
    private ArrayList<Food> foods;
    private ViewGroup anim_mask_layout;//动画层
    private BadgeView buyImg;//漂浮动画
    private View basketbar;//显示数量的图片
    private MenuListViewAdapter listAdpter;


    private StickyListHeadersListView content;
    private ListView headers;

    public static ShopMenuFragment newInstance(ArrayList<Menu> menus) {
        ShopMenuFragment fragment = new ShopMenuFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ExtraArgumentKeys.MENUS.toString(), new Gson().toJson(menus));
        fragment.setArguments(bundle);
        return fragment;
    }

    public MenuListViewAdapter getListAdpter() {
        return listAdpter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        String menuStr = getArguments().getString("menus");
        ArrayList<Menu> menus = new Gson().fromJson(menuStr, new TypeToken<ArrayList<Menu>>() {
        }.getType());
        this.foods = new ArrayList<Food>();
        this.stickers = new ArrayList<String>();
        this.stickers_left = new ArrayList<String>();
        for (Menu menu : menus) {
            this.foods.addAll(menu.getFoodlist());
            this.stickers_left.add(menu.getFoodclass());
            for (int i = 0; i < menu.getFoodlist().size(); i++) {
                stickers.add(menu.getFoodclass());
            }
        }
        basketbar = ((RestaurantDetailActivity) getActivity()).getBasket();
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(
                R.layout.fragment_shop_menu, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        content = (StickyListHeadersListView) view.findViewById(R.id.food_list_content_list);
        headers = (ListView) view.findViewById(R.id.food_list_header_list);
        setupView();
    }

    private void setupView() {
        listAdpter = new MenuListViewAdapter();
        content.setAdapter(listAdpter);
        content.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
                        ((RestaurantDetailActivity) getActivity()).toggleSlidingUpLayout(SlidingUpPanelLayout.PanelState.HIDDEN);
                    } else {
                        ((RestaurantDetailActivity) getActivity()).toggleSlidingUpLayout(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                } else {
                    ((RestaurantDetailActivity) getActivity()).toggleSlidingUpLayout(SlidingUpPanelLayout.PanelState.HIDDEN);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        content.setOnStickyHeaderChangedListener(ShopMenuFragment.this);

        headers.setAdapter(new HeaderListViewAdapter());
        headers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                content.setSelection(stickers.indexOf(stickers_left.get(position)));
            }
        });
    }


    @Override
    public void onStickyHeaderChanged(StickyListHeadersListView l, View header, int itemPosition, long headerId) {
        headers.setItemChecked(stickers_left.indexOf(stickers.get(itemPosition)), true);
    }


    public class HeaderListViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return stickers_left.size();
        }

        @Override
        public String getItem(int position) {
            return stickers_left.get(position);
        }

        @Override
        public long getItemId(int position) {
            return stickers_left.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HeaderHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_list_header, parent, false);
                holder = new HeaderHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (HeaderHolder) convertView.getTag();
            }
            holder.header.setText(getItem(position));
            return convertView;
        }

        public class HeaderHolder {
            public final CheckableTextView header;

            public HeaderHolder(View view) {
                header = (CheckableTextView) view.findViewById(R.id.food_list_item_header);
            }

        }
    }

    public class MenuListViewAdapter extends BaseAdapter implements StickyListHeadersAdapter {

        private ShoppingBasket basket;

        public MenuListViewAdapter() {
            super();
            basket = ShoppingBasket.getInstance();
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            HeaderHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_list_header_thin, parent, false);
                holder = new HeaderHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (HeaderHolder) convertView.getTag();
            }
            holder.header.setText(stickers.get(position));
            return convertView;
        }

        @Override
        public long getHeaderId(int position) {
            return stickers.get(position).hashCode();
        }


        @Override
        public int getCount() {
            return foods.size();
        }

        @Override
        public Food getItem(int position) {
            return foods.get(position);
        }

        @Override
        public long getItemId(int position) {
            return foods.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_list, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final Food food = foods.get(position);
            holder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    basket.addFood(food);
                    int[] start_location = new int[2];// 一个整型数组，用来存储按钮的在屏幕的X、Y坐标
                    v.getLocationInWindow(start_location);// 这是获取购买按钮的在屏幕的X、Y坐标（这也是动画开始的坐标）
//                    buyImg = new BadgeView(getActivity());
////                    buyNumView.setImageResource(R.drawable.default_photo);
//                    try {
//                        buyImg.setImageBitmap(getAddDrawBitMap(position));// 设置buyImg的图片
//                    }catch (NullPointerException e){
//                        Toasts.show("fuck");
//                    }
                    String price = "￥" + basket.getTotalPrice();
                    if (basket.isOkToPay().equals(ShoppingBasket.OrderStatus.NOTENOUGH)) {
                        price += "(还差￥" + basket.getNeededPrice() + ")";
                    }
                    ((TextView) basketbar.findViewById(R.id.basket_cost)).setText(price);
                    notifyDataSetChanged();
                    basket.getAdapter().notifyDataSetChanged();
                }
            });
            holder.minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    basket.minusFood(food);
                    String price = "￥" + basket.getTotalPrice();
                    if (basket.isOkToPay().equals(ShoppingBasket.OrderStatus.NOTENOUGH)) {
                        price += "(还差￥" + basket.getNeededPrice() + ")";
                    }
                    ((TextView) basketbar.findViewById(R.id.basket_cost)).setText(price);
                    notifyDataSetChanged();
                    basket.getAdapter().notifyDataSetChanged();
                }
            });
            holder.size.setText(String.valueOf(basket.getFoodBuyNum(food)));
            if (basket.getFoodBuyNum(food) > 0) {
                holder.minus.setVisibility(View.VISIBLE);
                holder.size.setVisibility(View.VISIBLE);
            } else {
                holder.minus.setVisibility(View.INVISIBLE);
                holder.size.setVisibility(View.INVISIBLE);
            }
            holder.name.setText(food.getName());
            holder.sales
                    .setText("月售：" + food.getNumpermonth());
            holder.price.setText("￥" + String.valueOf(food.getPrice()));
            if (!TextUtils.isEmpty(food.getImage())) {
                holder.image.setVisibility(View.VISIBLE);
                Picasso.with(holder.image.getContext()).load(food.getImage()).centerCrop().fit().into(holder.image);
            } else {
                holder.image.setVisibility(View.GONE);
            }
            return convertView;
        }

        public class ViewHolder {
            public final ImageView image;
            public final TextView name;
            public final TextView sales;
            public final TextView price;
            public final Button add;
            public final TextView size;
            public final Button minus;
            public final View mView;

            public ViewHolder(View view) {
                mView = view;
                name = (TextView) view.findViewById(R.id.food_list_item_name);
                add = (Button) view
                        .findViewById(R.id.food_list_item_add);
                size = (TextView) view.findViewById(R.id.food_list_item_num);
                minus = (Button) view
                        .findViewById(R.id.food_list_item_minus);
                sales = (TextView) view
                        .findViewById(R.id.food_list_item_buynums);
                price = (TextView) view
                        .findViewById(R.id.food_list_item_price);
                image = (ImageView) view
                        .findViewById(R.id.food_list_item_image);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + name.getText();
            }
        }

        public class HeaderHolder extends RecyclerView.ViewHolder {
            public final TextView header;

            public HeaderHolder(View view) {
                super(view);
                header = (TextView) view.findViewById(R.id.food_list_item_header);
            }

        }
    }

    private List<String> getRandomSublist(String[] array, int amount) {
        ArrayList<String> list = new ArrayList<>(amount);
        Random random = new Random();
        while (list.size() < amount) {
            list.add(array[random.nextInt(array.length)]);
        }
        return list;
    }

    /**
     * @param
     * @return void
     * @throws
     * @Description: 创建动画层
     */
    private ViewGroup createAnimLayout() {
        ViewGroup rootView = (ViewGroup) getActivity().getWindow().getDecorView();
        LinearLayout animLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        animLayout.setLayoutParams(lp);
        animLayout.setBackgroundResource(android.R.color.transparent);
        rootView.addView(animLayout);
        return animLayout;
    }

    private View addViewToAnimLayout(final ViewGroup vg, final View view,
                                     int[] location) {
        int x = location[0];
        int y = location[1];
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = x;
        lp.topMargin = y;
        view.setLayoutParams(lp);
        return view;
    }

    private void setAnim(final View v, int[] start_location) {
        anim_mask_layout = null;
        anim_mask_layout = createAnimLayout();
        anim_mask_layout.addView(v);// 把动画小球添加到动画层
        final View view = addViewToAnimLayout(anim_mask_layout, v,
                start_location);
        int[] end_location = new int[2];// 这是用来存储动画结束位置的X、Y坐标
        basketbar.findViewById(R.id.basket_icon).getLocationInWindow(end_location);// shopCart是那个购物车

        // 计算位移
        int endX = end_location[0] - start_location[0] + 60;// 动画位移的X坐标
        int endY = end_location[1] - start_location[1];// 动画位移的y坐标
        TranslateAnimation translateAnimationX = new TranslateAnimation(0,
                endX, 0, 0);
        translateAnimationX.setInterpolator(new LinearInterpolator());
        translateAnimationX.setRepeatCount(0);// 动画重复执行的次数
        translateAnimationX.setFillAfter(true);

        TranslateAnimation translateAnimationY = new TranslateAnimation(0, 0,
                0, endY);
        translateAnimationY.setInterpolator(new AccelerateInterpolator());
        translateAnimationY.setRepeatCount(0);// 动画重复执行的次数
        translateAnimationX.setFillAfter(true);

        AnimationSet set = new AnimationSet(false);
        set.setFillAfter(false);
        set.addAnimation(translateAnimationY);
        set.addAnimation(translateAnimationX);
        set.setDuration(800);// 动画的执行时间
        view.startAnimation(set);
        // 动画监听事件
        set.setAnimationListener(new Animation.AnimationListener() {
            // 动画的开始
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            // 动画的结束
            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.GONE);
                // buyNumView.setText(buyNum + "");//
                // buyNumView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
                // buyNumView.show();
            }
        });

    }

    public Bitmap getAddDrawBitMap(int position) {
        Tools tools = new Tools();
        BadgeView add = new BadgeView(getActivity());
        add.setText(String.valueOf(foods.get(position).getName()));
        return tools.convertViewToBitmap(add);
    }
}
