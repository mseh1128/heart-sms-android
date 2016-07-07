/*
 * Copyright (C) 2016 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.klinker.messenger.fragment;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.EdgeEffectCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import xyz.klinker.messenger.R;
import xyz.klinker.messenger.adapter.MessageListAdapter;
import xyz.klinker.messenger.data.Contact;
import xyz.klinker.messenger.data.Message;
import xyz.klinker.messenger.util.ColorUtil;

/**
 * Fragment for displaying messages for a certain conversation.
 */
public class MessageListFragment extends Fragment {

    private static final String ARG_NAME = "name";
    private static final String ARG_PHONE_NUMBER = "phone_number";
    private static final String ARG_COLOR = "color";
    private static final String ARG_COLOR_DARKER = "color_darker";
    private static final String ARG_COLOR_ACCENT = "color_accent";

    private View appBarLayout;
    private Toolbar toolbar;
    private View sendBar;
    private EditText messageEntry;
    private ImageButton attach;
    private FloatingActionButton send;
    private RecyclerView messageList;
    private MessageListAdapter adapter;

    public static MessageListFragment newInstance(Contact contact) {
        MessageListFragment fragment = new MessageListFragment();

        Bundle args = new Bundle();
        args.putString(ARG_NAME, contact.name);
        args.putString(ARG_PHONE_NUMBER, contact.phoneNumber);
        args.putInt(ARG_COLOR, contact.color);
        args.putInt(ARG_COLOR_DARKER, contact.colorDarker);
        args.putInt(ARG_COLOR_ACCENT, contact.colorAccent);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_message_list, parent, false);

        appBarLayout = view.findViewById(R.id.app_bar_layout);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        sendBar = view.findViewById(R.id.send_bar);
        messageEntry = (EditText) view.findViewById(R.id.message_entry);
        attach = (ImageButton) view.findViewById(R.id.attach);
        send = (FloatingActionButton) view.findViewById(R.id.send);
        messageList = (RecyclerView) view.findViewById(R.id.message_list);

        initToolbar();
        initSendbar();
        initRecycler();

        animateViewIn(appBarLayout);
        animateViewIn(sendBar);

        return view;
    }

    private void animateViewIn(View view) {
        view.animate().alpha(1f).translationY(0).setDuration(250)
                .setStartDelay(75).setInterpolator(new DecelerateInterpolator()).setListener(null);
    }

    private void initToolbar() {
        String name = getArguments().getString(ARG_NAME);
        String phoneNumber = getArguments().getString(ARG_PHONE_NUMBER);
        int color = getArguments().getInt(ARG_COLOR);
        int colorDarker = getArguments().getInt(ARG_COLOR_DARKER);

        toolbar.setTitle(name);
        toolbar.setBackgroundColor(color);

        if (!getResources().getBoolean(R.bool.pin_drawer)) {
            final DrawerLayout drawerLayout = (DrawerLayout) getActivity()
                    .findViewById(R.id.drawer_layout);
            toolbar.setNavigationIcon(R.drawable.ic_menu);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        TextView nameView = (TextView) getActivity().findViewById(R.id.drawer_header_reveal_name);
        TextView phoneNumberView = (TextView) getActivity()
                .findViewById(R.id.drawer_header_reveal_phone_number);
        nameView.setText(name);
        phoneNumberView.setText(phoneNumber);

        ColorUtil.adjustStatusBarColor(colorDarker, getActivity());
        ColorUtil.adjustDrawerColor(colorDarker, getActivity());
    }

    private void initSendbar() {
        String firstName = getArguments().getString(ARG_NAME).split(" ")[0];
        String hint = getResources().getString(R.string.type_message_to, firstName);
        messageEntry.setHint(hint);

        messageEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;

                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                        keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) ||
                        actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    handled = true;
                }

                return handled;
            }
        });

        send.setBackgroundTintList(ColorStateList.valueOf(getArguments().getInt(ARG_COLOR_ACCENT)));
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void initRecycler() {
        messageList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private boolean invoked = false;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // only invoke this once
                if (invoked) {
                    return;
                } else {
                    invoked = true;
                }

                try {
                    int color = getArguments().getInt(ARG_COLOR);
                    final Class<?> clazz = RecyclerView.class;

                    for (final String name : new String[] {"ensureTopGlow", "ensureBottomGlow"}) {
                        Method method = clazz.getDeclaredMethod(name);
                        method.setAccessible(true);
                        method.invoke(messageList);
                    }

                    for (final String name : new String[] {"mTopGlow", "mBottomGlow"}) {
                        final Field field = clazz.getDeclaredField(name);
                        field.setAccessible(true);
                        final Object edge = field.get(messageList);
                        final Field fEdgeEffect = edge.getClass().getDeclaredField("mEdgeEffect");
                        fEdgeEffect.setAccessible(true);
                        ((EdgeEffect) fEdgeEffect.get(edge)).setColor(color);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });



        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setStackFromEnd(true);
        messageList.setLayoutManager(manager);

        adapter = new MessageListAdapter(Message.getFakeMessages(),
                getArguments().getInt(ARG_COLOR), manager);
        messageList.setAdapter(adapter);

        messageList.animate().alpha(1f).setDuration(100).setStartDelay(250).setListener(null);
    }

    private void sendMessage() {
        String message = messageEntry.getText().toString().trim();

        if (message.length() > 0) {
            Message m = new Message(Message.TYPE_SENT, message, System.currentTimeMillis());
            adapter.addMessage(m);
            messageEntry.setText(null);
        }
    }

}
