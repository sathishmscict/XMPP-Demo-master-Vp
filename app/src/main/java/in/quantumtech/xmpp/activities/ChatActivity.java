package in.quantumtech.xmpp.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mstr.letschat.R;
import in.quantumtech.xmpp.adapters.MessageCursorAdapter;
import in.quantumtech.xmpp.databases.ChatContract;
import in.quantumtech.xmpp.databases.ChatContract.ChatMessageTable;
import in.quantumtech.xmpp.databases.ChatDbHelper;
import in.quantumtech.xmpp.databases.ConversationTableHelper;
import in.quantumtech.xmpp.listeners.OnTypingListener;
import in.quantumtech.xmpp.model.ChatAttachmentModel;
import in.quantumtech.xmpp.model.ChatAttachmentResponse;
import in.quantumtech.xmpp.providers.DatabaseContentProvider;
import in.quantumtech.xmpp.service.MessageService;
import in.quantumtech.xmpp.service.MessageService.LocalBinder;
import in.quantumtech.xmpp.tasks.OnAttachmentUpload;
import in.quantumtech.xmpp.tasks.Response.Listener;
import in.quantumtech.xmpp.tasks.SendFileTask;
import in.quantumtech.xmpp.tasks.SendLocationTask;
import in.quantumtech.xmpp.tasks.SendPlainTextTask;
import in.quantumtech.xmpp.utils.DBConstants;
import in.quantumtech.xmpp.utils.EventUtils;
import in.quantumtech.xmpp.utils.FilePath;
import in.quantumtech.xmpp.utils.PreferenceUtils;
import in.quantumtech.xmpp.utils.ShareUtil;
import in.quantumtech.xmpp.utils.Utils;
import in.quantumtech.xmpp.views.CircleProgressBar;
import in.quantumtech.xmpp.views.LocationView;
import in.quantumtech.xmpp.xmpp.SmackHelper;
import in.quantumtech.xmpp.xmpp.UserLocation;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickedListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smackx.xevent.MessageEventManager;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

//import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ChatActivity extends AppCompatActivity
        implements OnClickListener, Listener<Boolean>,
        LoaderManager.LoaderCallbacks<Cursor>, TextWatcher,
        AbsListView.MultiChoiceModeListener, OnAttachmentUpload, OnTypingListener {

    private static final String LOG_TAG = "ChatActivity";
    public static final String EXTRA_DATA_NAME_TO = "com.mstr.letschat.To";
    public static final String EXTRA_DATA_NAME_NICKNAME = "com.mstr.letschat.Nickname";
    public static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    private static final int REQUEST_PLACE_PICKER = 1;
    private static final int REQUEST_IMAGE_PICKER = 2;
    private static final int REQUEST_VIDEO_PICKER = 3;
    public static final String EXTRA_DATA_NAME_CHAT_TYPE = "com.mstr.letschat.ChatType";
    private ViewGroup rootView;
    private String to;
    private String nickname;
    private ArrayList<String> data;
    private TextView title;
    private String chatType = DBConstants.TYPE_SINGLE_CHAT;
    ImageView mImageView_emoji;
    //MessageListenerImpl messageListener;
    private EmojiEditText messageText;
    private ImageButton sendButton;
    private ListView messageListView;
    private CardView attachOptionsContainer;
    private Button attachLocationButton;
    private Button attachVideoButton;
    private Button attachGalleryButton;
    private Button recordVideo;
    private Button captureImage;
    private MessageCursorAdapter adapter;
    private Uri fileUri;
    private String filePath; //store image path if image captured through FileProvider.
    private MessageService messageService;
    private boolean bound = false;
    private Listener<Boolean> sendLocationListener;
    private EmojiPopup emojiPopup;
    private TextView typing;
    private ArrayList<ChatAttachmentModel> attachments = new ArrayList<>();
    private HorizontalScrollView attachment_scroll_view;
    private LinearLayout attachmentPreviewContainerLayout;
    private IntentFilter intentFilter;
    public static final String ACTION_MESSAGE_DELIVER_STATUS = "action_message_deliver_status";
    public static final String ACTION_MESSAGE_SEEN_STATUS = "action_message_seen_status";
    private String latestMessageId; //this is id of last message.
    private List<SmackHelper.GroupUserModel> groupUsers = new ArrayList<>();

    private AbsListView.RecyclerListener recyclerListener = new AbsListView.RecyclerListener() {
        @Override
        public void onMovedToScrapHeap(View view) {
            if (view instanceof LocationView) {
                ((LocationView) view).onMovedToScrapHeap();
            }
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            messageService = ((LocalBinder) service).getService();
            messageService.startConversation(to);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setIntent();
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_MESSAGE_DELIVER_STATUS);
        intentFilter.addAction(ACTION_MESSAGE_SEEN_STATUS);
        SmackHelper.getInstance(this).setOnTypingListener(this);
        rootView = (ViewGroup) findViewById(R.id.main_view_group);
        typing = (TextView) findViewById(R.id.typing_status);
        messageText = (EmojiEditText) findViewById(R.id.et_message);
        attachment_scroll_view = (HorizontalScrollView) findViewById(R.id.attachment_scroll_view);
        attachmentPreviewContainerLayout = (LinearLayout) findViewById(R.id.layout_attachment_preview_container);
        messageText.addTextChangedListener(this);
        sendButton = (ImageButton) findViewById(R.id.btn_send);
        sendButton.setOnClickListener(this);
        //sendButton.setEnabled(false);

        mImageView_emoji = (ImageView) findViewById(R.id.img_popup);
        mImageView_emoji.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                emojiPopup.toggle();
            }
        });

        messageListView = (ListView) findViewById(R.id.message_list);
        adapter = new MessageCursorAdapter(this, null, 0);
        messageListView.setAdapter(adapter);
        messageListView.setRecyclerListener(recyclerListener);
        messageListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        messageListView.setMultiChoiceModeListener(this);

        initAttachOptions();

        setTitle(null);

        title = (TextView) toolbar.findViewById(R.id.title);
        title.setText(nickname);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getLoaderManager().initLoader(0, null, this);

        initTaskListeners();


        //////////////////////////////////////online/offline///////////////////////

        ArrayList<HashMap<String, String>> usersList = new ArrayList<HashMap<String, String>>();
        Roster roster = Roster.getInstanceFor(SmackHelper.getInstance(this).getXmpptcpConnection());

        Collection<RosterEntry> entries = roster.getEntries();
        Presence presence;

        for (RosterEntry entry : entries) {
            presence = roster.getPresence(entry.getUser());
            HashMap<String, String> map = new HashMap<String, String>();
            Presence entryPresence = roster.getPresence(entry.getUser());

            Presence.Type type = entryPresence.getType();

            map.put("USER", entry.getName().toString());
            map.put("STATUS", type.toString());
            Log.e("USER", entry.getName().toString());

            usersList.add(map);
        }


        setUpEmojiPopup();

        if (data != null) {
            for (String text : data) {
                new SendPlainTextTask(this, this, to, nickname, text, chatType, DBConstants.NORMAL).execute();
            }
            data = null;
        }
        if (DBConstants.TYPE_GROUP_CHAT.equals(chatType)) {
            FrameLayout titleLayout = (FrameLayout) toolbar.findViewById(R.id.title_layout);
            titleLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ChatActivity.this, GroupInfoActivity.class);
                    intent.putExtra("name", nickname);
                    intent.putExtra("jid", to);
                    startActivity(intent);
                }
            });
        }
        messageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) adapter.getItem(position);
                cursor.moveToPosition(position);
                String toForward = cursor.getString(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_FORWARD));
                final String fileUrl = Utils.getFullPath(cursor.getString(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_MEDIA_URL)));
                Intent intent = new Intent(ChatActivity.this, PreviewActivity.class);
                String intentType = "photo";
                if (fileUrl != null) {
                    final String extensionFromUrl = MimeTypeMap.getFileExtensionFromUrl(fileUrl);
                    if (extensionFromUrl != null) {
                        if (extensionFromUrl.contains("jpeg") || extensionFromUrl.contains(ShareUtil.IMAGE_JPEG) || extensionFromUrl.contains(ShareUtil.IMAGE_PNG)) {
                            intentType = "photo";
                        } else if (extensionFromUrl.contains(ShareUtil.VIDEO_EXT)) {
                            intentType = "video";
                        } else if (extensionFromUrl.contains(ShareUtil.GIF)) {
                            intentType = "gif";
                        }
                    }
                    intent.putExtra("type", intentType);
                    intent.putExtra("url", fileUrl);
                    intent.putExtra("forward", toForward); //in case if url is worng then we will extract url from this json.
                    startActivity(intent);
                }

            }
        });

    }

    private void setIntent() {
        to = getIntent().getStringExtra(EXTRA_DATA_NAME_TO);
        nickname = getIntent().getStringExtra(EXTRA_DATA_NAME_NICKNAME);
        chatType = getIntent().getStringExtra(EXTRA_DATA_NAME_CHAT_TYPE);
        if (chatType == null) {
            chatType = DBConstants.TYPE_SINGLE_CHAT;
        }
        data = getIntent().getStringArrayListExtra("data");
        getIntent().removeExtra("data");

        if (DBConstants.TYPE_GROUP_CHAT.equals(chatType)) {
            try {
                List<String> mucInfo = SmackHelper.getInstance(this).getMUCInfo(to);
                for (String s : mucInfo) {
                    groupUsers.add(SmackHelper.getInstance(this).getGroupUserInfo(s));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initTaskListeners() {
        sendLocationListener = new Listener<Boolean>() {
            @Override
            public void onResponse(Boolean result) {
            }

            @Override
            public void onErrorResponse(Exception exception) {
            }
        };

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // bind to MessageService
        Intent intent = new Intent(this, MessageService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (bound) {
            if (messageService != null) {
                messageService.stopConversation();
            }
            unbindService(serviceConnection);
            bound = false;
        }
    }

    @Override
    public void onClick(View v) {
        setAttachOptionsVisibility(View.INVISIBLE);
        if (v == sendButton) {

            String text1 = messageText.getText().toString().trim();
            if (!TextUtils.isEmpty(text1)) {

                new SendPlainTextTask(this, this, to, nickname, text1, chatType, DBConstants.NORMAL).execute();

            }

            if (attachments.size() > 0) {
                for (ChatAttachmentModel model : attachments) {
                    Type type = new TypeToken<ChatAttachmentModel>() {
                    }.getType();
                    Gson gson = new GsonBuilder().create();
                    String toJson = gson.toJson(model, type);
                    for (ChatAttachmentResponse response : model.getResponse()) {
                        if (ShareUtil.IMAGE_JPEG.equalsIgnoreCase(response.getType())) {
                            new SendPlainTextTask(this, this, to, nickname, toJson, chatType, ShareUtil.IMAGE_JPEG).execute();
                        } else if (ShareUtil.IMAGE_PNG.equalsIgnoreCase(response.getType())) {
                            new SendPlainTextTask(this, this, to, nickname, toJson, chatType, ShareUtil.IMAGE_PNG).execute();
                        } else if (ShareUtil.VIDEO.equalsIgnoreCase(response.getType())) {
                            new SendPlainTextTask(this, this, to, nickname, toJson, chatType, ShareUtil.VIDEO).execute();
                        }
                    }
                }
                attachmentPreviewContainerLayout.removeAllViews();
                attachmentPreviewContainerLayout.setVisibility(View.GONE);
                attachment_scroll_view.setVisibility(View.GONE);
                attachments.clear();
            }
            return;
        }

        if (v == attachLocationButton) {
            sendLocation();
            return;
        }

        if (v == attachGalleryButton) {
            pickImage();
            return;
        }


        if (v == attachVideoButton) {
            pickVideo();
            return;
        }
        if (v == recordVideo) {
            recordVideo();
        }
        if (v == captureImage) {
            pickCapture();
        }

    }

    public void recordVideo() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
        File file = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
        fileUri = FileProvider.getUriForFile(this, Utils.AUTHORITY, file);
        filePath = file.getAbsolutePath();
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 8);
        // set video quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
        // start the video capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_attach:
                setAttachOptionsVisibility(attachOptionsContainer.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private String getMessage() {
        return messageText.getText().toString();
    }

    @Override
    public void onResponse(Boolean result) {
        clearText();
    }

    @Override
    public void onErrorResponse(Exception exception) {
        clearText();
    }

    private void clearText() {
        messageText.setText("");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{
                ChatMessageTable._ID,
                ChatMessageTable.COLUMN_NAME_MESSAGE,
                ChatMessageTable.COLUMN_NAME_TIME,
                ChatMessageTable.COLUMN_NAME_TYPE,
                ChatMessageTable.COLUMN_NAME_STATUS,
                ChatMessageTable.COLUMN_NAME_ADDRESS,
                ChatMessageTable.COLUMN_NAME_LATITUDE,
                ChatMessageTable.COLUMN_NAME_LONGITUDE,
                ChatMessageTable.COLUMN_NAME_MEDIA_URL,
                ChatMessageTable.COLUMN_NAME_THUMB_URL,
                ChatMessageTable.COLUMN_NAME_GROUP_NAME,
                ChatMessageTable.COLUMN_NAME_NICKNAME,
                ChatMessageTable.COLUMN_NAME_SUBJECT,
                ChatMessageTable.COLUMN_NAME_DOWNLOAD_STATUS,
                ChatMessageTable.COLUMN_NAME_FILE_LENGTH,
                ChatMessageTable.COLUMN_NAME_FORWARD,
                ChatMessageTable.COLUMN_NAME_STANZA_ID
        };

        return new CursorLoader(this, ChatMessageTable.CONTENT_URI, projection,
                ChatMessageTable.COLUMN_NAME_JID + "=?", new String[]{to}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        if (DBConstants.TYPE_GROUP_CHAT.equals(chatType)) {
            Cursor cursor = getContentResolver().query(ChatContract.ConversationTable.CONTENT_URI, new String[]{ChatContract.ConversationTable.COLUMN_NAME_NICKNAME, ChatContract.ConversationTable.COLUMN_NAME_NAME},
                    ChatContract.ConversationTable.COLUMN_NAME_NAME + "=?", new String[]{to}, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    nickname = cursor.getString(cursor.getColumnIndex(ChatContract.ConversationTable.COLUMN_NAME_NICKNAME));
                    title.setText(nickname);
                }
                cursor.close();
            }
        }
        if (data != null) {
            if (data.moveToNext()) {
                latestMessageId = data.getString(data.getColumnIndex(ChatMessageTable.COLUMN_NAME_STANZA_ID));
                if (latestMessageId != null) {
                    try {
                        SmackHelper.getInstance(ChatActivity.this).getEventManager().sendDisplayedNotification(to, latestMessageId);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if (chatType.equals(DBConstants.TYPE_GROUP_CHAT)) {
            if (!mTyping) {
                mTyping = true;
                MessageEventManager event = MessageEventManager.getInstanceFor(SmackHelper.getInstance(this).getXmpptcpConnection());
                for (SmackHelper.GroupUserModel model : groupUsers) {
                    //recipient id
                    try {
                        //send group id with typing status.
                        //send own name with group id.
                        String name = PreferenceUtils.getNickname(this) + "^" + to;
                        event.sendComposingNotification(model.getJid(), name);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
            }

        } else {
            if (!mTyping) {
                mTyping = true;
                MessageEventManager event = MessageEventManager.getInstanceFor(SmackHelper.getInstance(this).getXmpptcpConnection());
                //recipient id
                try {
                    event.sendComposingNotification(to, DBConstants.TYPE_SINGLE_CHAT);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }

        }
        mTypingHandler.removeCallbacks(onTypingTimeout);
        mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
    }

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;

            mTyping = false;
            MessageEventManager event = MessageEventManager.getInstanceFor(SmackHelper.getInstance(ChatActivity.this).getXmpptcpConnection());
            if (DBConstants.TYPE_GROUP_CHAT.equals(chatType)) {
                for (SmackHelper.GroupUserModel model : groupUsers) {
                    try {
                        event.sendCancelledNotification(model.getJid(), to);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    event.sendCancelledNotification(to, DBConstants.TYPE_SINGLE_CHAT);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private boolean mTyping = false;
    private static final int TYPING_TIMER_LENGTH = 500;
    private Handler mTypingHandler = new Handler();

    @Override
    public void afterTextChanged(Editable s) {

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void showAttachOptions() {
        if (Utils.hasLollipop()) {
            int cx = attachOptionsContainer.getWidth() / 2;
            int cy = attachOptionsContainer.getHeight() / 2;
            float endRadius = (float) Math.hypot(cx, cy);

            Animator animator = ViewAnimationUtils.createCircularReveal(attachOptionsContainer, cx, cy, 0, endRadius);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(getResources().getInteger(R.integer.attach_views_anim_duration));
            animator.addListener(new AnimatorListenerAdapter() {
            });
            attachOptionsContainer.setVisibility(View.VISIBLE);
            animator.start();
        } else {
            attachOptionsContainer.setVisibility(View.VISIBLE);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void hideAttachOptions() {
        if (Utils.hasLollipop()) {
            int cx = attachOptionsContainer.getWidth() / 2;
            int cy = attachOptionsContainer.getHeight() / 2;
            float startRadius = (float) Math.hypot(cx, cy);

            Animator animator = ViewAnimationUtils.createCircularReveal(attachOptionsContainer, cx, cy, startRadius, 0);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    attachOptionsContainer.setVisibility(View.INVISIBLE);
                }
            });
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(getResources().getInteger(R.integer.attach_views_anim_duration));
            animator.start();
        } else {
            attachOptionsContainer.setVisibility(View.INVISIBLE);
        }
    }

    private boolean setAttachOptionsVisibility(int visibility) {
        if (attachOptionsContainer.getVisibility() == visibility) {
            return false;
        }

        if (visibility == View.VISIBLE) {
            showAttachOptions();

            return true;
        } else if (visibility == View.INVISIBLE) {
            hideAttachOptions();
            return true;
        }

        return false;
    }


    @Override
    public void onBackPressed() {
        if (!setAttachOptionsVisibility(View.INVISIBLE)) {
            super.onBackPressed();
        }

        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.dismiss();
        } else {
            super.onBackPressed();
        }


    }


    //@SuppressFBWarnings(value = "SIC_INNER_SHOULD_BE_STATIC_ANON", justification = "Sample app we do not care")
    private void setUpEmojiPopup() {
        emojiPopup = EmojiPopup.Builder.fromRootView(rootView).setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener() {
            @Override
            public void onEmojiBackspaceClicked(final View v) {
                Log.d("MainActivity", "Clicked on Backspace");
            }
        }).setOnEmojiClickedListener(new OnEmojiClickedListener() {
            @Override
            public void onEmojiClicked(final Emoji emoji) {
                Log.d("MainActivity", "Clicked on emoji");
            }
        }).setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
            @Override
            public void onEmojiPopupShown() {
                mImageView_emoji.setImageResource(R.drawable.ic_keyboard);
            }
        }).setOnSoftKeyboardOpenListener(new OnSoftKeyboardOpenListener() {
            @Override
            public void onKeyboardOpen(final int keyBoardHeight) {
                Log.d("MainActivity", "Opened soft keyboard");
            }
        }).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
            @Override
            public void onEmojiPopupDismiss() {
                mImageView_emoji.setImageResource(R.drawable.emoji_people);
            }
        }).setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener() {
            @Override
            public void onKeyboardClose() {
                emojiPopup.dismiss();
            }
        }).build(messageText);
    }


    private void initAttachOptions() {
        attachOptionsContainer = (CardView) findViewById(R.id.attach_options_container);
        attachLocationButton = (Button) findViewById(R.id.attach_location);
        attachLocationButton.setOnClickListener(this);
        attachVideoButton = (Button) findViewById(R.id.attach_video);
        attachVideoButton.setOnClickListener(this);
        attachGalleryButton = (Button) findViewById(R.id.attach_from_gallery);
        attachGalleryButton.setOnClickListener(this);
        recordVideo = (Button) findViewById(R.id.attach_recorder);
        recordVideo.setOnClickListener(this);
        captureImage = (Button) findViewById(R.id.attach_camera);
        captureImage.setOnClickListener(this);
    }

    public static PendingIntent getNotificationPendingIntent(Context context, String to, String nickname) {
        TaskStackBuilder taskStackbuilder = TaskStackBuilder.create(context);
        taskStackbuilder.addParentStack(ChatActivity.class);
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_DATA_NAME_TO, to);
        intent.putExtra(ChatActivity.EXTRA_DATA_NAME_NICKNAME, nickname);
        taskStackbuilder.addNextIntent(intent);

        return taskStackbuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void sendLocation() {
        // Construct an intent for the place picker
        try {

            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            // Start the intent by requesting a result,
            // identified by a request code.
            startActivityForResult(intent, REQUEST_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException e) {
            Log.e(LOG_TAG, e.toString(), e);
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(LOG_TAG, e.toString(), e);
        }
    }


    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setType("video/*");
        startActivityForResult(intent, REQUEST_VIDEO_PICKER);
    }

    private void pickCapture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        fileUri = FileProvider.getUriForFile(this, Utils.AUTHORITY, file);
        filePath = file.getAbsolutePath();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    private void pickImage() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickIntent, REQUEST_IMAGE_PICKER);
    }

    public File getOutputMediaFileUri(int type) {
        return getOutputMediaFile(type);
    }

    private static File getOutputMediaFile(int type) {
        // External sdcard location
        File imageDir = new File(Environment.getExternalStorageDirectory() + "/.xmpp/Images");
        File videoDir = new File(Environment.getExternalStorageDirectory() + "/.xmpp/Videos");
        // Create the storage directory if it does not exist
        if (!imageDir.exists()) {
            if (!imageDir.mkdirs()) {
                //Log.d("IMAGE_DIRECTORY_NAME", "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }
        if (!videoDir.exists()) {
            if (!videoDir.mkdirs()) {
                //Log.d("IMAGE_DIRECTORY_NAME", "Oops! Failed create " + videoDir + " directory");
                return null;
            }
        }

        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {

            mediaFile = new File(imageDir.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(videoDir.getAbsolutePath() + "/" + System.currentTimeMillis() + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }

    @Override
    public void onComplete(ChatAttachmentModel chatAttachmentModel) {
        attachments.add(chatAttachmentModel);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PLACE_PICKER:
                    // The user has selected a place. Extract the name and address.
                    Place place = PlacePicker.getPlace(this, data);

                    new SendLocationTask(sendLocationListener, this, to, nickname, new UserLocation(place), DBConstants.NORMAL).execute();
                    break;

                case REQUEST_IMAGE_PICKER:
                    sendFile(data.getData(), ShareUtil.IMAGE_JPEG);
                    break;
                case REQUEST_VIDEO_PICKER:
                    Uri uri = Uri.parse(FilePath.getPath(ChatActivity.this, data.getData()));
                    sendFile(uri, ShareUtil.VIDEO);
                    break;
                case CAMERA_CAPTURE_IMAGE_REQUEST_CODE:
                    sendFile(fileUri, ShareUtil.IMAGE_JPEG);
                    break;
                case CAMERA_CAPTURE_VIDEO_REQUEST_CODE:
                    sendFile(fileUri, ShareUtil.VIDEO);
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendFile(final Uri fileUri, String type) {
        LayoutInflater inflater = LayoutInflater.from(ChatActivity.this);
        final View inflate = inflater.inflate(R.layout.item_attachment_preview, null);
        ImageView imageView = (ImageView) inflate.findViewById(R.id.image_attachment_preview);
        final ImageButton cancelBtn = (ImageButton) inflate.findViewById(R.id.button_attachment_preview_delete);
        CircleProgressBar progressBar = (CircleProgressBar) inflate.findViewById(R.id.uploading_progress);
        Glide.with(ChatActivity.this).load(fileUri).into(imageView);
        cancelBtn.setTag(fileUri);
        String realPathFromURI = "";
        if (ShareUtil.IMAGE_JPEG.equalsIgnoreCase(type)) {
            try {
                LinkedHashMap<String, Object> hashMap = Utils.compressImage(String.valueOf(fileUri), filePath, this);
                File file = (File) hashMap.get("file");
                realPathFromURI = file.getAbsolutePath();
                String mimeType = Utils.getMimeType(file.getAbsolutePath());
                if (mimeType != null) {
                    if (mimeType.contains("jpeg") || mimeType.contains(ShareUtil.IMAGE_JPEG)) {
                        new SendFileTask(this, file, ShareUtil.IMAGE_JPEG, progressBar, this).execute();
                    } else if (mimeType.contains(ShareUtil.IMAGE_PNG)) {
                        new SendFileTask(this, file, ShareUtil.IMAGE_PNG, progressBar, this).execute();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (ShareUtil.VIDEO.equalsIgnoreCase(type)) {
            if (fileUri != null) {
                if (String.valueOf(fileUri).contains(Utils.AUTHORITY)) {
                    realPathFromURI = filePath;
                } else {
                    realPathFromURI = fileUri.getPath();
                }
            }

            new SendFileTask(this, new File(realPathFromURI), ShareUtil.VIDEO, progressBar, this).execute();
        }
        final String finalRealPathFromURI = realPathFromURI;
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < attachments.size(); i++) {
                    if (finalRealPathFromURI.equalsIgnoreCase(attachments.get(i).getResponse().get(0).getLocalPath())) {
                        attachments.remove(i);
                        attachmentPreviewContainerLayout.removeView(inflate);
                        if (attachmentPreviewContainerLayout.getChildCount() < 1) {
                            attachmentPreviewContainerLayout.setVisibility(View.GONE);
                            attachment_scroll_view.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
        attachmentPreviewContainerLayout.addView(inflate);
        attachmentPreviewContainerLayout.setVisibility(View.VISIBLE);
        attachment_scroll_view.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        mode.setTitle(String.valueOf(messageListView.getCheckedItemCount()));
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.chat_select_context, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                deleteMessages();
                mode.finish(); // Action picked, so close the CAB
                return true;
            case R.id.action_copy:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip;
                long[] ids = messageListView.getCheckedItemIds();
                ArrayList<String> texts = new ArrayList<>(ids.length);
                for (long id : ids) {
                    ChatDbHelper dbHelper = ChatDbHelper.getInstance(ChatActivity.this);
                    texts.add(dbHelper.getSelectedChats((int) id));
                    //texts.add(dbHelper.getSelectedMedia((int) id));
                }
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < texts.size(); i++) {
                    if (i == texts.size() - 1) {
                        stringBuilder.append(texts.get(i));
                    } else {
                        stringBuilder.append(texts.get(i)).append("\n");
                    }
                }
                clip = ClipData.newPlainText("label", stringBuilder.toString());
                //String value=stringBuilder.toString();
                clipboard.setPrimaryClip(clip);
                mode.finish(); // Action picked, so close the CAB

                return true;

            case R.id.action_Forward:

                long[] mIds = messageListView.getCheckedItemIds();
                ArrayList<String> mTexts = new ArrayList<>(mIds.length);
                for (long id : mIds) {
                    ChatDbHelper dbHelper = ChatDbHelper.getInstance(ChatActivity.this);
                    mTexts.add(dbHelper.getSelectedChats((int) id));
                }

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("action", "forward");
                intent.putExtra("data", mTexts);
                startActivity(intent);
                mode.finish();
                return true;
            default:
                return false;
        }

    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    private void deleteMessages() {
        SparseBooleanArray positions = messageListView.getCheckedItemPositions();
        boolean isLatestMessageDeleted = positions.get(adapter.getCount() - 1);

        long[] ids = messageListView.getCheckedItemIds();
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        for (long id : ids) {
            operations.add(ContentProviderOperation.newDelete(ChatMessageTable.CONTENT_URI)
                    .withSelection(ChatMessageTable._ID + " =? ", new String[]{String.valueOf(id)}).build());
        }

        try {
            getContentResolver().applyBatch(DatabaseContentProvider.AUTHORITY, operations);
        } catch (Exception e) {
            Log.e(LOG_TAG, "delete messages error ", e);
        }

        if (isLatestMessageDeleted) {
            Cursor cursor = getContentResolver().query(ChatMessageTable.CONTENT_URI, new String[]{"MAX(_id) AS max_id"},
                    ChatMessageTable.COLUMN_NAME_JID + "=?", new String[]{to}, null);
            if (cursor.moveToFirst()) {
                int maxId = cursor.getInt(0);
                cursor = getContentResolver().query(ChatMessageTable.CONTENT_URI, new String[]{ChatMessageTable.COLUMN_NAME_MESSAGE, ChatMessageTable.COLUMN_NAME_TIME},
                        ChatMessageTable._ID + "=?", new String[]{String.valueOf(maxId)}, null);
                if (cursor.moveToFirst()) {
                    String latestMessage = cursor.getString(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_MESSAGE));
                    long timeMillis = cursor.getLong(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_TIME));
                    getContentResolver().update(ChatContract.ConversationTable.CONTENT_URI, ConversationTableHelper.newUpdateContentValues(latestMessage, timeMillis),
                            ChatContract.ConversationTable.COLUMN_NAME_NAME + "=?", new String[]{to});
                } else {
                    getContentResolver().delete(ChatContract.ConversationTable.CONTENT_URI, ChatContract.ConversationTable.COLUMN_NAME_NAME + "=?", new String[]{to});
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (ACTION_MESSAGE_DELIVER_STATUS.equals(intent.getAction())) {
                    getLoaderManager().restartLoader(0, null, ChatActivity.this);
                } else if (ACTION_MESSAGE_SEEN_STATUS.equals(intent.getAction())) {
                    getLoaderManager().restartLoader(0, null, ChatActivity.this);
                }
            }
        }

    };

    @Override
    public void startTyping(final String jid, final String groupUserName,final String groupUserJid, final int count) {
        if (jid.equals(to)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (groupUserName != null && groupUserJid != null) {
                        if (!SmackHelper.getFullJID(PreferenceUtils.getUser(ChatActivity.this)).equals(groupUserJid)) {
                            //typing in group
                            typing.setVisibility(View.VISIBLE);
                            if (count > 1) {
                                typing.setText(groupUserName + " and " + (count - 1) + " others are " + EventUtils.TYPING_MSG);
                            } else {
                                typing.setText(groupUserName + " is " + EventUtils.TYPING_MSG);
                            }
                        }
                    } else {
                        typing.setVisibility(View.VISIBLE);
                        typing.setText(EventUtils.TYPING_MSG);
                    }
                }
            });
        }
    }

    @Override
    public void stopTyping(String jid, final String groupUserName) {

        if (jid.equals(to)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (groupUserName != null) {
                        //typing in group
                        typing.setText("");
                        typing.setVisibility(View.GONE);
                    } else {
                        typing.setText("");
                        typing.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}