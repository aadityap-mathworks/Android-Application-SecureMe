//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.support.v4.app;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.ViewModelStore;
import android.arch.lifecycle.ViewModelStoreOwner;
import android.arch.lifecycle.Lifecycle.State;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.Build.VERSION;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.app.ActivityCompat.PermissionCompatDelegate;
import android.support.v4.app.ActivityCompat.RequestPermissionsRequestCodeValidator;
import android.support.v4.util.SparseArrayCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

public class FragmentActivity extends SupportActivity implements ViewModelStoreOwner, OnRequestPermissionsResultCallback, RequestPermissionsRequestCodeValidator {
    private static final String TAG = "FragmentActivity";
    static final String FRAGMENTS_TAG = "android:support:fragments";
    static final String NEXT_CANDIDATE_REQUEST_INDEX_TAG = "android:support:next_request_index";
    static final String ALLOCATED_REQUEST_INDICIES_TAG = "android:support:request_indicies";
    static final String REQUEST_FRAGMENT_WHO_TAG = "android:support:request_fragment_who";
    static final int MAX_NUM_PENDING_FRAGMENT_ACTIVITY_RESULTS = 65534;
    static final int MSG_RESUME_PENDING = 2;
    final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case 2:
                FragmentActivity.this.onResumeFragments();
                FragmentActivity.this.mFragments.execPendingActions();
                break;
            default:
                super.handleMessage(msg);
            }

        }
    };
    final FragmentController mFragments = FragmentController.createController(new FragmentActivity.HostCallbacks());
    private ViewModelStore mViewModelStore;
    boolean mCreated;
    boolean mResumed;
    boolean mStopped = true;
    boolean mRequestedPermissionsFromFragment;
    boolean mStartedIntentSenderFromFragment;
    boolean mStartedActivityFromFragment;
    int mNextCandidateRequestIndex;
    SparseArrayCompat<String> mPendingFragmentActivityResults;

    public FragmentActivity() {
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        this.mFragments.noteStateNotSaved();
        int requestIndex = requestCode >> 16;
        if (requestIndex != 0) {
            --requestIndex;
            String who = (String)this.mPendingFragmentActivityResults.get(requestIndex);
            this.mPendingFragmentActivityResults.remove(requestIndex);
            if (who == null) {
                Log.w("FragmentActivity", "Activity result delivered for unknown Fragment.");
            } else {
                Fragment targetFragment = this.mFragments.findFragmentByWho(who);
                if (targetFragment == null) {
                    Log.w("FragmentActivity", "Activity result no fragment exists for who: " + who);
                } else {
                    targetFragment.onActivityResult(requestCode & '\uffff', resultCode, data);
                }

            }
        } else {
            PermissionCompatDelegate delegate = ActivityCompat.getPermissionCompatDelegate();
            if (delegate == null || !delegate.onActivityResult(this, requestCode, resultCode, data)) {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    public void onBackPressed() {
        FragmentManager fragmentManager = this.mFragments.getSupportFragmentManager();
        boolean isStateSaved = fragmentManager.isStateSaved();
        if (!isStateSaved || VERSION.SDK_INT > 25) {
            if (isStateSaved || !fragmentManager.popBackStackImmediate()) {
                super.onBackPressed();
            }

        }
    }

    public void supportFinishAfterTransition() {
        ActivityCompat.finishAfterTransition(this);
    }

    public void setEnterSharedElementCallback(SharedElementCallback callback) {
        ActivityCompat.setEnterSharedElementCallback(this, callback);
    }

    public void setExitSharedElementCallback(SharedElementCallback listener) {
        ActivityCompat.setExitSharedElementCallback(this, listener);
    }

    public void supportPostponeEnterTransition() {
        ActivityCompat.postponeEnterTransition(this);
    }

    public void supportStartPostponedEnterTransition() {
        ActivityCompat.startPostponedEnterTransition(this);
    }

    @CallSuper
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        this.mFragments.dispatchMultiWindowModeChanged(isInMultiWindowMode);
    }

    @CallSuper
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        this.mFragments.dispatchPictureInPictureModeChanged(isInPictureInPictureMode);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mFragments.noteStateNotSaved();
        this.mFragments.dispatchConfigurationChanged(newConfig);
    }

    @NonNull
    public ViewModelStore getViewModelStore() {
        if (this.getApplication() == null) {
            throw new IllegalStateException("Your activity is not yet attached to the Application instance. You can't request ViewModel before onCreate call.");
        } else {
            if (this.mViewModelStore == null) {
                FragmentActivity.NonConfigurationInstances nc = (FragmentActivity.NonConfigurationInstances)this.getLastNonConfigurationInstance();
                if (nc != null) {
                    this.mViewModelStore = nc.viewModelStore;
                }

                if (this.mViewModelStore == null) {
                    this.mViewModelStore = new ViewModelStore();
                }
            }

            return this.mViewModelStore;
        }
    }

    public Lifecycle getLifecycle() {
        return super.getLifecycle();
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.mFragments.attachHost((Fragment)null);
        super.onCreate(savedInstanceState);
        FragmentActivity.NonConfigurationInstances nc = (FragmentActivity.NonConfigurationInstances)this.getLastNonConfigurationInstance();
        if (nc != null && nc.viewModelStore != null && this.mViewModelStore == null) {
            this.mViewModelStore = nc.viewModelStore;
        }

        if (savedInstanceState != null) {
            Parcelable p = savedInstanceState.getParcelable("android:support:fragments");
            this.mFragments.restoreAllState(p, nc != null ? nc.fragments : null);
            if (savedInstanceState.containsKey("android:support:next_request_index")) {
                this.mNextCandidateRequestIndex = savedInstanceState.getInt("android:support:next_request_index");
                int[] requestCodes = savedInstanceState.getIntArray("android:support:request_indicies");
                String[] fragmentWhos = savedInstanceState.getStringArray("android:support:request_fragment_who");
                if (requestCodes != null && fragmentWhos != null && requestCodes.length == fragmentWhos.length) {
                    this.mPendingFragmentActivityResults = new SparseArrayCompat(requestCodes.length);

                    for(int i = 0; i < requestCodes.length; ++i) {
                        this.mPendingFragmentActivityResults.put(requestCodes[i], fragmentWhos[i]);
                    }
                } else {
                    Log.w("FragmentActivity", "Invalid requestCode mapping in savedInstanceState.");
                }
            }
        }

        if (this.mPendingFragmentActivityResults == null) {
            this.mPendingFragmentActivityResults = new SparseArrayCompat();
            this.mNextCandidateRequestIndex = 0;
        }

        this.mFragments.dispatchCreate();
    }

    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == 0) {
            boolean show = super.onCreatePanelMenu(featureId, menu);
            show |= this.mFragments.dispatchCreateOptionsMenu(menu, this.getMenuInflater());
            return show;
        } else {
            return super.onCreatePanelMenu(featureId, menu);
        }
    }

    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View v = this.dispatchFragmentsOnCreateView(parent, name, context, attrs);
        return v == null ? super.onCreateView(parent, name, context, attrs) : v;
    }

    public View onCreateView(String name, Context context, AttributeSet attrs) {
        View v = this.dispatchFragmentsOnCreateView((View)null, name, context, attrs);
        return v == null ? super.onCreateView(name, context, attrs) : v;
    }

    final View dispatchFragmentsOnCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return this.mFragments.onCreateView(parent, name, context, attrs);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mViewModelStore != null && !this.isChangingConfigurations()) {
            this.mViewModelStore.clear();
        }

        this.mFragments.dispatchDestroy();
    }

    public void onLowMemory() {
        super.onLowMemory();
        this.mFragments.dispatchLowMemory();
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (super.onMenuItemSelected(featureId, item)) {
            return true;
        } else {
            switch(featureId) {
            case 0:
                return this.mFragments.dispatchOptionsItemSelected(item);
            case 6:
                return this.mFragments.dispatchContextItemSelected(item);
            default:
                return false;
            }
        }
    }

    public void onPanelClosed(int featureId, Menu menu) {
        switch(featureId) {
        case 0:
            this.mFragments.dispatchOptionsMenuClosed(menu);
        default:
            super.onPanelClosed(featureId, menu);
        }
    }

    protected void onPause() {
        super.onPause();
        this.mResumed = false;
        if (this.mHandler.hasMessages(2)) {
            this.mHandler.removeMessages(2);
            this.onResumeFragments();
        }

        this.mFragments.dispatchPause();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.mFragments.noteStateNotSaved();
    }

    public void onStateNotSaved() {
        this.mFragments.noteStateNotSaved();
    }

    protected void onResume() {
        super.onResume();
        this.mHandler.sendEmptyMessage(2);
        this.mResumed = true;
        this.mFragments.execPendingActions();
    }

    protected void onPostResume() {
        super.onPostResume();
        this.mHandler.removeMessages(2);
        this.onResumeFragments();
        this.mFragments.execPendingActions();
    }

    protected void onResumeFragments() {
        this.mFragments.dispatchResume();
    }

    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        if (featureId == 0 && menu != null) {
            boolean goforit = this.onPrepareOptionsPanel(view, menu);
            goforit |= this.mFragments.dispatchPrepareOptionsMenu(menu);
            return goforit;
        } else {
            return super.onPreparePanel(featureId, view, menu);
        }
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        return super.onPreparePanel(0, view, menu);
    }

    public final Object onRetainNonConfigurationInstance() {
        Object custom = this.onRetainCustomNonConfigurationInstance();
        FragmentManagerNonConfig fragments = this.mFragments.retainNestedNonConfig();
        if (fragments == null && this.mViewModelStore == null && custom == null) {
            return null;
        } else {
            FragmentActivity.NonConfigurationInstances nci = new FragmentActivity.NonConfigurationInstances();
            nci.custom = custom;
            nci.viewModelStore = this.mViewModelStore;
            nci.fragments = fragments;
            return nci;
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.markFragmentsCreated();
        Parcelable p = this.mFragments.saveAllState();
        if (p != null) {
            outState.putParcelable("android:support:fragments", p);
        }

        if (this.mPendingFragmentActivityResults.size() > 0) {
            outState.putInt("android:support:next_request_index", this.mNextCandidateRequestIndex);
            int[] requestCodes = new int[this.mPendingFragmentActivityResults.size()];
            String[] fragmentWhos = new String[this.mPendingFragmentActivityResults.size()];

            for(int i = 0; i < this.mPendingFragmentActivityResults.size(); ++i) {
                requestCodes[i] = this.mPendingFragmentActivityResults.keyAt(i);
                fragmentWhos[i] = (String)this.mPendingFragmentActivityResults.valueAt(i);
            }

            outState.putIntArray("android:support:request_indicies", requestCodes);
            outState.putStringArray("android:support:request_fragment_who", fragmentWhos);
        }

    }

    protected void onStart() {
        super.onStart();
        this.mStopped = false;
        if (!this.mCreated) {
            this.mCreated = true;
            this.mFragments.dispatchActivityCreated();
        }

        this.mFragments.noteStateNotSaved();
        this.mFragments.execPendingActions();
        this.mFragments.dispatchStart();
    }

    protected void onStop() {
        super.onStop();
        this.mStopped = true;
        this.markFragmentsCreated();
        this.mFragments.dispatchStop();
    }

    public Object onRetainCustomNonConfigurationInstance() {
        return null;
    }

    public Object getLastCustomNonConfigurationInstance() {
        FragmentActivity.NonConfigurationInstances nc = (FragmentActivity.NonConfigurationInstances)this.getLastNonConfigurationInstance();
        return nc != null ? nc.custom : null;
    }

    /** @deprecated */
    @Deprecated
    public void supportInvalidateOptionsMenu() {
        this.invalidateOptionsMenu();
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        writer.print(prefix);
        writer.print("Local FragmentActivity ");
        writer.print(Integer.toHexString(System.identityHashCode(this)));
        writer.println(" State:");
        String innerPrefix = prefix + "  ";
        writer.print(innerPrefix);
        writer.print("mCreated=");
        writer.print(this.mCreated);
        writer.print(" mResumed=");
        writer.print(this.mResumed);
        writer.print(" mStopped=");
        writer.print(this.mStopped);
        if (this.getApplication() != null) {
            LoaderManager.getInstance(this).dump(innerPrefix, fd, writer, args);
        }

        this.mFragments.getSupportFragmentManager().dump(prefix, fd, writer, args);
    }

    public void onAttachFragment(Fragment fragment) {
    }

    public FragmentManager getSupportFragmentManager() {
        return this.mFragments.getSupportFragmentManager();
    }

    /** @deprecated */
    @Deprecated
    public LoaderManager getSupportLoaderManager() {
        return LoaderManager.getInstance(this);
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        if (!this.mStartedActivityFromFragment && requestCode != -1) {
            checkForValidRequestCode(requestCode);
        }

        super.startActivityForResult(intent, requestCode);
    }

    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        if (!this.mStartedActivityFromFragment && requestCode != -1) {
            checkForValidRequestCode(requestCode);
        }

        super.startActivityForResult(intent, requestCode, options);
    }

    public void startIntentSenderForResult(IntentSender intent, int requestCode, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws SendIntentException {
        if (!this.mStartedIntentSenderFromFragment && requestCode != -1) {
            checkForValidRequestCode(requestCode);
        }

        super.startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags);
    }

    public void startIntentSenderForResult(IntentSender intent, int requestCode, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws SendIntentException {
        if (!this.mStartedIntentSenderFromFragment && requestCode != -1) {
            checkForValidRequestCode(requestCode);
        }

        super.startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options);
    }

    static void checkForValidRequestCode(int requestCode) {
        if ((requestCode & -65536) != 0) {
            throw new IllegalArgumentException("Can only use lower 16 bits for requestCode");
        }
    }

    public final void validateRequestPermissionsRequestCode(int requestCode) {
        if (!this.mRequestedPermissionsFromFragment && requestCode != -1) {
            checkForValidRequestCode(requestCode);
        }

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        this.mFragments.noteStateNotSaved();
        int index = requestCode >> 16 & '\uffff';
        if (index != 0) {
            --index;
            String who = (String)this.mPendingFragmentActivityResults.get(index);
            this.mPendingFragmentActivityResults.remove(index);
            if (who == null) {
                Log.w("FragmentActivity", "Activity result delivered for unknown Fragment.");
                return;
            }

            Fragment frag = this.mFragments.findFragmentByWho(who);
            if (frag == null) {
                Log.w("FragmentActivity", "Activity result no fragment exists for who: " + who);
            } else {
                frag.onRequestPermissionsResult(requestCode & '\uffff', permissions, grantResults);
            }
        }

    }

    public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode) {
        this.startActivityFromFragment(fragment, intent, requestCode, (Bundle)null);
    }

    public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode, @Nullable Bundle options) {
        this.mStartedActivityFromFragment = true;

        try {
            if (requestCode == -1) {
                ActivityCompat.startActivityForResult(this, intent, -1, options);
                return;
            }

            checkForValidRequestCode(requestCode);
            int requestIndex = this.allocateRequestIndex(fragment);
            ActivityCompat.startActivityForResult(this, intent, (requestIndex + 1 << 16) + (requestCode & '\uffff'), options);
        } finally {
            this.mStartedActivityFromFragment = false;
        }

    }

    public void startIntentSenderFromFragment(Fragment fragment, IntentSender intent, int requestCode, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws SendIntentException {
        this.mStartedIntentSenderFromFragment = true;

        try {
            if (requestCode == -1) {
                ActivityCompat.startIntentSenderForResult(this, intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options);
                return;
            }

            checkForValidRequestCode(requestCode);
            int requestIndex = this.allocateRequestIndex(fragment);
            ActivityCompat.startIntentSenderForResult(this, intent, (requestIndex + 1 << 16) + (requestCode & '\uffff'), fillInIntent, flagsMask, flagsValues, extraFlags, options);
        } finally {
            this.mStartedIntentSenderFromFragment = false;
        }

    }

    private int allocateRequestIndex(Fragment fragment) {
        if (this.mPendingFragmentActivityResults.size() >= 65534) {
            throw new IllegalStateException("Too many pending Fragment activity results.");
        } else {
            while(this.mPendingFragmentActivityResults.indexOfKey(this.mNextCandidateRequestIndex) >= 0) {
                this.mNextCandidateRequestIndex = (this.mNextCandidateRequestIndex + 1) % '\ufffe';
            }

            int requestIndex = this.mNextCandidateRequestIndex;
            this.mPendingFragmentActivityResults.put(requestIndex, fragment.mWho);
            this.mNextCandidateRequestIndex = (this.mNextCandidateRequestIndex + 1) % '\ufffe';
            return requestIndex;
        }
    }

    void requestPermissionsFromFragment(Fragment fragment, String[] permissions, int requestCode) {
        if (requestCode == -1) {
            ActivityCompat.requestPermissions(this, permissions, requestCode);
        } else {
            checkForValidRequestCode(requestCode);

            try {
                this.mRequestedPermissionsFromFragment = true;
                int requestIndex = this.allocateRequestIndex(fragment);
                ActivityCompat.requestPermissions(this, permissions, (requestIndex + 1 << 16) + (requestCode & '\uffff'));
            } finally {
                this.mRequestedPermissionsFromFragment = false;
            }

        }
    }

    private void markFragmentsCreated() {
        boolean reiterate;
        do {
            reiterate = markState(this.getSupportFragmentManager(), State.CREATED);
        } while(reiterate);

    }

    private static boolean markState(FragmentManager manager, State state) {
        boolean hadNotMarked = false;
        Collection<Fragment> fragments = manager.getFragments();
        Iterator var4 = fragments.iterator();

        while(var4.hasNext()) {
            Fragment fragment = (Fragment)var4.next();
            if (fragment != null) {
                if (fragment.getLifecycle().getCurrentState().isAtLeast(State.STARTED)) {
                    fragment.mLifecycleRegistry.markState(state);
                    hadNotMarked = true;
                }

                FragmentManager childFragmentManager = fragment.peekChildFragmentManager();
                if (childFragmentManager != null) {
                    hadNotMarked |= markState(childFragmentManager, state);
                }
            }
        }

        return hadNotMarked;
    }

    class HostCallbacks extends FragmentHostCallback<FragmentActivity> {
        public HostCallbacks() {
            super(FragmentActivity.this);
        }

        public void onDump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            FragmentActivity.this.dump(prefix, fd, writer, args);
        }

        public boolean onShouldSaveFragmentState(Fragment fragment) {
            return !FragmentActivity.this.isFinishing();
        }

        public LayoutInflater onGetLayoutInflater() {
            return FragmentActivity.this.getLayoutInflater().cloneInContext(FragmentActivity.this);
        }

        public FragmentActivity onGetHost() {
            return FragmentActivity.this;
        }

        public void onSupportInvalidateOptionsMenu() {
            FragmentActivity.this.supportInvalidateOptionsMenu();
        }

        public void onStartActivityFromFragment(Fragment fragment, Intent intent, int requestCode) {
            FragmentActivity.this.startActivityFromFragment(fragment, intent, requestCode);
        }

        public void onStartActivityFromFragment(Fragment fragment, Intent intent, int requestCode, @Nullable Bundle options) {
            FragmentActivity.this.startActivityFromFragment(fragment, intent, requestCode, options);
        }

        public void onStartIntentSenderFromFragment(Fragment fragment, IntentSender intent, int requestCode, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws SendIntentException {
            FragmentActivity.this.startIntentSenderFromFragment(fragment, intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options);
        }

        public void onRequestPermissionsFromFragment(@NonNull Fragment fragment, @NonNull String[] permissions, int requestCode) {
            FragmentActivity.this.requestPermissionsFromFragment(fragment, permissions, requestCode);
        }

        public boolean onShouldShowRequestPermissionRationale(@NonNull String permission) {
            return ActivityCompat.shouldShowRequestPermissionRationale(FragmentActivity.this, permission);
        }

        public boolean onHasWindowAnimations() {
            return FragmentActivity.this.getWindow() != null;
        }

        public int onGetWindowAnimations() {
            Window w = FragmentActivity.this.getWindow();
            return w == null ? 0 : w.getAttributes().windowAnimations;
        }

        public void onAttachFragment(Fragment fragment) {
            FragmentActivity.this.onAttachFragment(fragment);
        }

        @Nullable
        public View onFindViewById(int id) {
            return FragmentActivity.this.findViewById(id);
        }

        public boolean onHasView() {
            Window w = FragmentActivity.this.getWindow();
            return w != null && w.peekDecorView() != null;
        }
    }

    static final class NonConfigurationInstances {
        Object custom;
        ViewModelStore viewModelStore;
        FragmentManagerNonConfig fragments;

        NonConfigurationInstances() {
        }
    }
}
