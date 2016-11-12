package camp.computer.clay.platform;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSValue;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import camp.computer.clay.Clay;
import camp.computer.clay.engine.World;
import camp.computer.clay.platform.communication.Internet;
import camp.computer.clay.platform.communication.UDPHost;
import camp.computer.clay.platform.graphics.PlatformRenderSurface;
import camp.computer.clay.platform.graphics.controls.NativeUi;
import camp.computer.clay.platform.sound.SpeechOutput;
import camp.computer.clay.platform.sound.ToneOutput;
import camp.computer.clay.platform.spatial.OrientationInput;
import camp.computer.clay.engine.entity.Entity;
import camp.computer.clay.engine.Event;
import camp.computer.clay.engine.component.Transform;

public class Application extends FragmentActivity implements PlatformInterface {

    // <SETTINGS>
    private static final boolean ENABLE_TONE_OUTPUT = false;
    private static final boolean ENABLE_SPEECH_OUTPUT = false;
    private static final boolean ENABLE_MOTION_INPUT = true;

    private static final long MESSAGE_SEND_FREQUENCY = 5000; // 500;

    public static boolean ENABLE_GEOMETRY_LABELS = false;

    /**
     * Hides the operating system's status and navigation bars. Setting this to false is helpful
     * during debugging.
     */
    private static final boolean ENABLE_FULLSCREEN = true;
    // </SETTINGS>

    public PlatformRenderSurface platformRenderSurface;

    private SpeechOutput speechOutput;

    private ToneOutput toneOutput;

    private OrientationInput orientationInput;

    private static Context context;

    private static Application applicationView;

    private Clay clay;

    private UDPHost UDPHost;

    private Internet networkResource;

    NativeUi nativeUi;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SpeechOutput.CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                speechOutput = new SpeechOutput(this);
            } else {
                Intent install = new Intent();
                install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);
            }
        }
    }

    public NativeUi getNativeUi() {
        return this.nativeUi;
    }

    /**
     * Called when the activity is getFirstEvent created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // "Return the context of the single, global Application object of the current process.
        // This generally should only be used if you need a Context whose lifecycle is separate
        // from the current context, that is tied to the lifetime of the process rather than the
        // current component." (Android Documentation)
        Application.context = getApplicationContext();

        // Sensor Interface
        if (ENABLE_MOTION_INPUT) {
            orientationInput = new OrientationInput(getApplicationContext());
        }

        if (ENABLE_FULLSCREEN) {
            startFullscreenService();
        }

        // Display Interface
        Application.applicationView = this;

        nativeUi = new NativeUi(this);

//        for (int i = 0; i < 100; i++) {
//            String outgoingMessage = "announce device " + UUID.randomUUID();
//            CRC16 CRC16 = new CRC16();
//            int seed = 0;
//            byte[] outgoingMessageBytes = outgoingMessage.getBytes();
//            int check = CRC16.calculate(outgoingMessageBytes, seed);
//            String outmsg =
//                    "\f" +
//                            String.valueOf(outgoingMessage.length()) + "\t" +
//                            String.valueOf(check) + "\t" +
//                            "text" + "\t" +
//                            outgoingMessage;
//            Log.v("CRC_Demo", "" + outmsg);
//        }

        setContentView(R.layout.activity_main);

        // World Surface
        platformRenderSurface = (PlatformRenderSurface) findViewById(R.id.app_surface_view);
        platformRenderSurface.onResume();

        // based on... try it! better performance? https://www.javacodegeeks.com/2011/07/android-game-development-basic-game_05.html
        //setContentView(visualizationSurface);

        // PathEntity Editor
        final RelativeLayout pathEditor = (RelativeLayout) findViewById(R.id.action_editor_view);
        pathEditor.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                pathEditor.setVisibility(View.GONE);
                return true;
            }
        });

        final Button pathEditorAddActionButton = (Button) findViewById(R.id.path_editor_add_action);
        pathEditorAddActionButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {

                int pointerIndex = ((motionEvent.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT);
                int pointerId = motionEvent.getPointerId(pointerIndex);
                //int touchAction = (motionEvent.getEvent () & MotionEvent.ACTION_MASK);
                int touchActionType = (motionEvent.getAction() & MotionEvent.ACTION_MASK);
                int pointCount = motionEvent.getPointerCount();

                // Update the state of the touched object based on the current pointerCoordinates interaction state.
                if (touchActionType == MotionEvent.ACTION_DOWN) {
                    // TODO:
                } else if (touchActionType == MotionEvent.ACTION_POINTER_DOWN) {
                    // TODO:
                } else if (touchActionType == MotionEvent.ACTION_MOVE) {
                    // TODO:
                } else if (touchActionType == MotionEvent.ACTION_UP) {

                    addPathExtensionAction();

                } else if (touchActionType == MotionEvent.ACTION_POINTER_UP) {
                    // TODO:
                } else if (touchActionType == MotionEvent.ACTION_CANCEL) {
                    // TODO:
                } else {
                    // TODO:
                }

                return true;
            }
        });

        // <Cache>
        // </Cache>

        // Clay
        clay = new Clay();

        clay.addPlatform(this); // Add the view provided by the host device.

        // UDP Datagram Server
        if (UDPHost == null) {
            UDPHost = new UDPHost("udp");
            clay.addHost(this.UDPHost);
            UDPHost.startServer();
        }

        // Internet Network Interface
        if (networkResource == null) {
            networkResource = new Internet();
            clay.addResource(this.networkResource);
        }

        /*
        // Descriptor Database
        SQLiteStoreHost sqliteStoreHost = new SQLiteStoreHost(getClay(), "sqlite");
        getClay().setStore(sqliteStoreHost);

        // Initialize content store
        getClay().getStore().erase();
        getClay().getCache().populate(); // alt. syntax: useClay().useCache().toPopulate();
        getClay().getStore().generate();
        getClay().getCache().populate();
        // getClay().simulateSession(true, 10, false);
        */

        // Prevent on-screen keyboard from pushing up content
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        // <CHAT_AND_CONTEXT_SCOPE>
        final RelativeLayout messageContentLayout = (RelativeLayout) findViewById(R.id.message_content_layout);
        final HorizontalScrollView messageContentLayoutPerspective = (HorizontalScrollView) findViewById(R.id.message_content_layout_perspective);
        final LinearLayout messageContent = (LinearLayout) findViewById(R.id.message_content);
        final TextView messageContentHint = (TextView) findViewById(R.id.message_content_hint);
        final RelativeLayout messageKeyboardLayout = (RelativeLayout) findViewById(R.id.message_keyboard_layout);
        final HorizontalScrollView messageKeyboardLayoutPerspective = (HorizontalScrollView) findViewById(R.id.message_keyboard_layout_perspective);
        final LinearLayout messageKeyboard = (LinearLayout) findViewById(R.id.message_keyboard);
        // </CHAT_AND_CONTEXT_SCOPE>

        // <CHAT>

        messageContentHint.setOnTouchListener(new View.OnTouchListener()

                                              {

                                                  @Override
                                                  public boolean onTouch(View v, MotionEvent event) {
                                                      messageContentHint.setVisibility(View.GONE);
                                                      showMessageKeyboard();
                                                      return false;
                                                  }
                                              }

        );

        // Hide scrollbars in keyboard
        messageKeyboardLayoutPerspective.setVerticalScrollBarEnabled(false);
        messageKeyboardLayoutPerspective.setHorizontalScrollBarEnabled(false);

        // Hide scrollbars in message content
        messageContentLayoutPerspective.setVerticalScrollBarEnabled(false);
        messageContentLayoutPerspective.setHorizontalScrollBarEnabled(false);

        generateKeyboard();

        // Set up interactivity
        messageContentLayout.setOnTouchListener(new View.OnTouchListener()

                                                {
                                                    @Override
                                                    public boolean onTouch(View v, MotionEvent motionEvent) {

                                                        int pointerIndex = ((motionEvent.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT);
                                                        int pointerId = motionEvent.getPointerId(pointerIndex);
                                                        //int touchAction = (motionEvent.getEvent () & MotionEvent.ACTION_MASK);
                                                        int touchActionType = (motionEvent.getAction() & MotionEvent.ACTION_MASK);
                                                        int pointCount = motionEvent.getPointerCount();

                                                        // Update the state of the touched object based on the current pointerCoordinates interaction state.
                                                        if (touchActionType == MotionEvent.ACTION_DOWN) {
                                                            // TODO:
                                                        } else if (touchActionType == MotionEvent.ACTION_POINTER_DOWN) {
                                                            // TODO:
                                                        } else if (touchActionType == MotionEvent.ACTION_MOVE) {
                                                            // TODO:
                                                        } else if (touchActionType == MotionEvent.ACTION_UP) {
                                                            showMessageKeyboard();
                                                        } else if (touchActionType == MotionEvent.ACTION_POINTER_UP) {
                                                            // TODO:
                                                        } else if (touchActionType == MotionEvent.ACTION_CANCEL) {
                                                            // TODO:
                                                        } else {
                                                            // TODO:
                                                        }

                                                        return true;
                                                    }

//            @Override
//            public boolean onTouch(CameraEntity v, MotionEvent event) {
//                int inType = timelineButton.getInputType(); // backup the input type
//                timelineButton.setInputType(InputType.TYPE_NULL); // disable soft input
//                timelineButton.onTouchEvent(event); // call native messagingThreadHandler
//                timelineButton.setInputType(inType); // restore input type
//                return true; // consume pointerCoordinates even
//            }
                                                }

        );

        messageContentLayout.setOnClickListener(new View.OnClickListener()

                                                {
                                                    @Override
                                                    public void onClick(View v) {


                                                    }
                                                }

        );
        // </CHAT>

        // Start the initial worker thread (runnable task) by posting through the messagingThreadHandler
        messagingThreadHandler.post(messaingThread);

        // Check availability of speech synthesis engine on Android host device.
        if (ENABLE_SPEECH_OUTPUT) {
            SpeechOutput.checkAvailability(this);
        }

        if (ENABLE_TONE_OUTPUT) {
            toneOutput = new ToneOutput();
        }

        hideChat();

        // <REDIS>
//        new JedisConnectToDatabaseTask().execute("pub-redis-14268.us-east-1-3.3.ec2.garantiadata.com:14268");

//        while (this.jedis == null) {
//            // Waiting for connection...
//        }

//        new Thread(
//                new RedisSubThread(this.jedis)
//        ).start();
        // </REDIS>


        // <REDIS>
//        RedisDBThread redisDB = new RedisDBThread();
//        redisDB.start();
        // </REDIS>

//        openFile("Host.json");

        // <JAVASCRIPT_ENGINE>
        // Reference: https://github.com/ericwlange/AndroidJSCore
        JSContext context = new JSContext();

        // Test 1
        context.property("a", 5);
        JSValue aValue = context.property("a");
        double a = aValue.toNumber();
        DecimalFormat df = new DecimalFormat(".#");
        Log.v("AndroidJSCore", (df.format(a))); // 5.0

        // Test 2
        context.evaluateScript("a = 10");
        JSValue newAValue = context.property("a");
        Log.v("AndroidJSCore", df.format(newAValue.toNumber())); // 10.0
        String script =
                "function factorial(x) { var f = 1; for(; x > 1; x--) f *= x; return f; }\n" +
                        "var fact_a = factorial(a);\n";
        context.evaluateScript(script);
        JSValue fact_a = context.property("fact_a");
        Log.v("AndroidJSCore", df.format(fact_a.toNumber())); // 3628800.0
        // </JAVASCRIPT_ENGINE>


        // <CHECK_HARDWARE_ACCELERATION>
        View view = findViewById(R.id.application_view);
        boolean isHardwareAccelerated = view.isHardwareAccelerated();
        Log.v("HardwareAcceleration", "isHardwareAccelerated: " + isHardwareAccelerated);
        // </CHECK_HARDWARE_ACCELERATION>
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_S: {
                nativeUi.openSettings();
                //your Action code
                return true;
            }

            case KeyEvent.KEYCODE_R: {
                World.getWorld().portableLayoutSystem.adjustLayout();
                return true;
            }

            case KeyEvent.KEYCODE_L: {
                // TODO: log
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // <VISUALIZATION>
        platformRenderSurface.onPause();
        // </VISUALIZATION>
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (UDPHost == null) {
            UDPHost = new UDPHost("udp");
        }
        if (!UDPHost.isActive()) {
            UDPHost.startServer();
        }

        // <VISUALIZATION>
        platformRenderSurface.onResume();
        // </VISUALIZATION>
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop speech generator
        if (speechOutput != null) {
            speechOutput.destroy();
        }
    }

    public static Context getContext() {
        return Application.context;
    }


    //----------------------------------------------------------------------------------------------

    // References:
    // - http://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext
    boolean isTitleEditorInitialized = false;

    public void openTitleEditor(String title) {
        final RelativeLayout titleEditor = (RelativeLayout) findViewById(R.id.title_editor_view);

        // Initialize Text
        final EditText titleText = (EditText) findViewById(R.id.title_editor_text);
        titleText.setText(title);

        // Configure Text Editor
        if (isTitleEditorInitialized == false) {

            /*
            // Set the font face
            Typeface type = Typeface.createFromAsset(getAssets(), "fonts/Dosis-Light.ttf");
            titleText.setTypeface(type);
            */

            // Configure to hide keyboard when a touch occurs anywhere except the text
            titleText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        hideKeyboard(v);
                    }
                }
            });

            // Configure touch interaction
            titleText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getId() == titleText.getId()) {

                        // Move the cursor to the end of the line
                        titleText.setSelection(titleText.getText().length());

                        // Show the cursor
                        titleText.setCursorVisible(true);
                    }
                }
            });

            isTitleEditorInitialized = true;
        }

        titleText.setCursorVisible(false);

        titleEditor.setVisibility(View.VISIBLE);

        /*
        // Now Set your animation
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        titleText.startAnimation(fadeInAnimation);

        // Callback to hide editor
        startTitleEditorService();
        */
    }

    public void setTitleEditor(String title) {
        // Update the Text
        final EditText titleText = (EditText) findViewById(R.id.title_editor_text);
        titleText.setText(title);

        /*
        // Callback to hide editor
        startTitleEditorService();
        */
    }

    public void closeTitleEditor() {
        final RelativeLayout titleEditor = (RelativeLayout) findViewById(R.id.title_editor_view);

        final EditText titleText = (EditText) findViewById(R.id.title_editor_text);

        titleEditor.setVisibility(View.INVISIBLE);

        /*
        // Now Set your animation
        Animation fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out_animation);

        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                titleEditor.setImageVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        titleText.startAnimation(fadeOutAnimation);
        */
    }

    private Handler titleEditorServiceHandler = new Handler();
    private Runnable titleEditorServiceRunnable = new Runnable() {
        @Override
        public void run() {
            // Do what you need to do.
            // e.g., foobar();
            closeTitleEditor();

//            // Uncomment this for periodic callback
//            if (enableFullscreenService) {
//                fullscreenServiceHandler.postDelayed(this, FULLSCREEN_SERVICE_PERIOD);
//            }
        }
    };

    private void startTitleEditorService() {
        titleEditorServiceHandler.postDelayed(titleEditorServiceRunnable, 5000);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // TODO: <BUILDER_SYSTEMS_HDL>
    public List<Entity> restoreHosts(String filename) {

        // e.g., filename = "Hosts.json"

        List<Entity> hostEntities = new ArrayList<>();

        // Open specified file HostEntity profiles
        String jsonString = null;
        try {
            InputStream inputStream = getContext().getAssets().open(filename);
            int fileSize = inputStream.available();
            byte[] fileBuffer = new byte[fileSize];
            inputStream.read(fileBuffer);
            inputStream.close();
            jsonString = new String(fileBuffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create JSON object from file contents
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonString);

            JSONObject hostObject = jsonObject.getJSONObject("host");
            String hostTitle = hostObject.getString("title");

            // HostEntity host = new HostEntity();

            Log.v("Configuration", "reading JSON name: " + hostTitle);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return hostEntities;
    }
    // TODO: </BUILDER_SYSTEMS_HDL>

    public void hideChat() {
        // <CHAT_AND_CONTEXT_SCOPE>
        final RelativeLayout messageContentLayout = (RelativeLayout) findViewById(R.id.message_content_layout);
        final HorizontalScrollView messageContentLayoutPerspective = (HorizontalScrollView) findViewById(R.id.message_content_layout_perspective);
        final LinearLayout messageContent = (LinearLayout) findViewById(R.id.message_content);
        final TextView messageContentHint = (TextView) findViewById(R.id.message_content_hint);
        final RelativeLayout messageKeyboardLayout = (RelativeLayout) findViewById(R.id.message_keyboard_layout);
        final HorizontalScrollView messageKeyboardLayoutPerspective = (HorizontalScrollView) findViewById(R.id.message_keyboard_layout_perspective);
        final LinearLayout messageKeyboard = (LinearLayout) findViewById(R.id.message_keyboard);
        // </CHAT_AND_CONTEXT_SCOPE>

        messageContentLayout.setVisibility(View.GONE);
    }

    private void showMessageKeyboard() {

        final RelativeLayout messageContentLayout = (RelativeLayout) findViewById(R.id.message_content_layout);
        final LinearLayout messageContent = (LinearLayout) findViewById(R.id.message_content);
        final RelativeLayout messageKeyboardLayout = (RelativeLayout) findViewById(R.id.message_keyboard_layout);
        final HorizontalScrollView messageKeyboardLayoutPerspective = (HorizontalScrollView) findViewById(R.id.message_keyboard_layout_perspective);
        final LinearLayout messageKeyboard = (LinearLayout) findViewById(R.id.message_keyboard);

        ViewGroup.MarginLayoutParams chatLayoutParams = (ViewGroup.MarginLayoutParams) messageContentLayout.getLayoutParams();

        ViewGroup.MarginLayoutParams chatKeyboardLayoutParams = (ViewGroup.MarginLayoutParams) messageKeyboardLayout.getLayoutParams();

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, r.getDisplayMetrics());
        //float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 14, r.getDisplayMetrics());

        // Reposition chat layout
        chatKeyboardLayoutParams.bottomMargin = chatLayoutParams.bottomMargin + messageContentLayout.getLayoutParams().height + 20; //h - (int) event.getRawY() - (int) (buttonHeight / 2.0f);

        if (messageKeyboardLayout.getVisibility() == View.GONE) {
            messageKeyboardLayout.setVisibility(View.VISIBLE);
        } else if (messageKeyboardLayout.getVisibility() == View.VISIBLE) {
            messageKeyboardLayout.setVisibility(View.GONE);
        }

        messageKeyboardLayout.requestLayout();
        messageKeyboardLayout.invalidate();
    }

    public Transform convertToVisiblePosition(android.graphics.Point point) {
        Transform visiblePosition = new Transform();
        return visiblePosition;
    }

    public float convertDipToPx(float dip) {
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
        return px;
    }

    private void generateKeyboard() {
        generateKeyboardKeys();
    }

    private void generateKeyboardKeys() {

        //final EditText messageContent = (EditText) findViewById(R.id.message_content);

        generateKeyboardKey("settings");
        generateKeyboardKey("\uD83D\uDD0D");
//        generateKeyboardKey("zoom/in");
//        generateKeyboardKey("zoom/out");
        generateKeyboardKey("camera");
        generateKeyboardKey("vibrate");
        generateKeyboardKey("timeline");
        generateKeyboardKey("help");
        generateKeyboardKey("chat");
    }

    private void generateKeyboardKey(String settings) {

        final RelativeLayout messageContentLayout = (RelativeLayout) findViewById(R.id.message_content_layout);
        final LinearLayout messageContent = (LinearLayout) findViewById(R.id.message_content);
        final RelativeLayout messageKeyboardLayout = (RelativeLayout) findViewById(R.id.message_keyboard_layout);
        final HorizontalScrollView messageKeyboardLayoutPerspective = (HorizontalScrollView) findViewById(R.id.message_keyboard_layout_perspective);
        final LinearLayout messageKeyboard = (LinearLayout) findViewById(R.id.message_keyboard);

        // <CHAT>

        // Add keys to keyboard
        final Button messageKey = new Button(getContext());
        messageKey.setText(settings);
        messageKey.setTextSize(12.0f);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(5, 0, 5, 0);
        messageKey.setPadding(0, 0, 0, 0);
        messageKey.setLayoutParams(layoutParams);
        messageKey.getLayoutParams().height = 100;
        messageKey.setBackgroundResource(R.drawable.chat_message_key);

        messageKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // final EditText chatEntry = (EditText) findViewById(R.id.chat_entry);
                appendToChatMessage(messageKey.getText().toString());

                validateChatMessage();
            }
        });

        messageKeyboard.addView(messageKey);
    }

    private void validateChatMessage() {

        final RelativeLayout messageContentLayout = (RelativeLayout) findViewById(R.id.message_content_layout);
        final LinearLayout messageContent = (LinearLayout) findViewById(R.id.message_content);
        final RelativeLayout messageKeyboardLayout = (RelativeLayout) findViewById(R.id.message_keyboard_layout);
        final HorizontalScrollView messageKeyboardLayoutPerspective = (HorizontalScrollView) findViewById(R.id.message_keyboard_layout_perspective);
        final LinearLayout messageKeyboard = (LinearLayout) findViewById(R.id.message_keyboard);
    }

    private void appendToChatMessage(String text) {
        final RelativeLayout messageContentLayout = (RelativeLayout) findViewById(R.id.message_content_layout);
        final HorizontalScrollView messageContentLayoutPerspective = (HorizontalScrollView) findViewById(R.id.message_content_layout_perspective);
        final LinearLayout messageContent = (LinearLayout) findViewById(R.id.message_content);
        final RelativeLayout messageKeyboardLayout = (RelativeLayout) findViewById(R.id.message_keyboard_layout);
        final HorizontalScrollView messageKeyboardLayoutPerspective = (HorizontalScrollView) findViewById(R.id.message_keyboard_layout_perspective);
        final LinearLayout messageKeyboard = (LinearLayout) findViewById(R.id.message_keyboard);
        // </CHAT_AND_CONTEXT_SCOPE>

        // <CHAT>

        // Add keys to keyboard
        final Button messageWord = new Button(getContext());
        messageWord.setText(text);
        messageWord.setTextSize(12.0f);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 0, 10, 0);
        messageWord.setPadding(0, 0, 0, 0);
        messageWord.setLayoutParams(layoutParams);
        messageWord.getLayoutParams().height = 100;
        messageWord.setBackgroundResource(R.drawable.chat_message_key);

        messageWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                messageContent.removeView(messageWord);

                // Unicode arrow symbols: https://en.wikipedia.org/wiki/Template:Unicode_chart_Arrows

                // final EditText chatEntry = (EditText) findViewById(R.id.chat_entry);
//                messageContent.addPlatform(messageKey);
//                contextScope.setText("✓");
//                contextScope.setText("☉");
                //contextScope.setText("☌"); // When dragging to connect path
            }
        });

        messageContent.addView(messageWord);

        messageContentLayoutPerspective.postDelayed(new Runnable() {
            public void run() {
                messageContentLayoutPerspective.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 100L);
    }

    private void addPathExtensionAction() {

        final TextView actionConstruct = new TextView(getContext());
        actionConstruct.setText("Event (<PortEntity> <PortEntity> ... <PortEntity>)\nExpose: <PortEntity> <PortEntity> ... <PortEntity>");
        int horizontalPadding = (int) convertDipToPx(20);
        int verticalPadding = (int) convertDipToPx(10);
        actionConstruct.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        actionConstruct.setBackgroundColor(Color.parseColor("#44000000"));

        final LinearLayout pathPatchActionList = (LinearLayout) findViewById(R.id.path_editor_action_list);

        actionConstruct.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {

                int touchActionType = (motionEvent.getAction() & MotionEvent.ACTION_MASK);

                if (touchActionType == MotionEvent.ACTION_DOWN) {
                    // TODO:
                } else if (touchActionType == MotionEvent.ACTION_POINTER_DOWN) {
                    // TODO:
                } else if (touchActionType == MotionEvent.ACTION_MOVE) {
                    // TODO:
                } else if (touchActionType == MotionEvent.ACTION_UP) {

                    pathPatchActionList.removeView(actionConstruct);

                } else if (touchActionType == MotionEvent.ACTION_POINTER_UP) {
                    // TODO:
                } else if (touchActionType == MotionEvent.ACTION_CANCEL) {
                    // TODO:
                } else {
                    // TODO:
                }

                return true;
            }
        });

        pathPatchActionList.addView(actionConstruct);
    }

    // <FULLSCREEN_SERVICE>
    public static final int FULLSCREEN_SERVICE_PERIOD = 2000;

    private boolean enableFullscreenService = false;

    private Handler fullscreenServiceHandler = new Handler();
    private Runnable fullscreenServiceRunnable = new Runnable() {
        @Override
        public void run() {
            // Do what you need to do.
            // e.g., foobar();
            hideNativeUiControls();

            // Uncomment this for periodic callback
            if (enableFullscreenService) {
                fullscreenServiceHandler.postDelayed(this, FULLSCREEN_SERVICE_PERIOD);
            }
        }
    };

    private void startFullscreenService() {
        enableFullscreenService = true;
        fullscreenServiceHandler.postDelayed(fullscreenServiceRunnable, Event.MINIMUM_HOLD_DURATION);
    }

    public void stopFullscreenService() {
        enableFullscreenService = false;
    }

    /**
     * References:
     * - http://stackoverflow.com/questions/9926767/is-there-a-way-to-hide-the-system-navigation-bar-in-android-ics
     */
    private void hideNativeUiControls() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
    // </FULLSCREEN_SERVICE>

    // Create the Handler object. This will be run on the main thread by default.
    private Handler messagingThreadHandler = new Handler();

    // Define the code block to be executed
    private Runnable messaingThread = new Runnable() {
        @Override
        public void run() {
            // Action the outgoing messages
            clay.update();

            // Repeat this the same runnable code block again another 2 seconds
            messagingThreadHandler.postDelayed(messaingThread, MESSAGE_SEND_FREQUENCY);
        }
    };

    @Override
    public void setClay(Clay clay) {
        this.clay = clay;
    }

    @Override
    public Clay getClay() {
        return this.clay;
    }

    // TODO: Rename to something else and make a getPlatform() function specific to the
    // TODO: (cont'd) display interface.
    public static Application getView() {
        return Application.applicationView;
    }

    public PlatformRenderSurface getPlatformRenderSurface() {
        return this.platformRenderSurface;
    }

    // TODO: Delete!
    public double getFramesPerSecond() {
        return getPlatformRenderSurface().getPlatformRenderer().getFramesPerSecond();
    }

    public SpeechOutput getSpeechOutput() {
        return this.speechOutput;
    }

    public ToneOutput getToneOutput() {
        return this.toneOutput;
    }

    public OrientationInput getOrientationInput() {
        return this.orientationInput;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            Log.v("Application", "ENTER");
            // TODO: Open "hidden" settings options!
            return true;
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    ;
}