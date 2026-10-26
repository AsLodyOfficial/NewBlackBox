package top.niunaijun.blackbox.fake.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.Slog;
import top.niunaijun.blackbox.app.BActivityThread;


public class GoogleAccountManagerProxy extends ClassInvocationStub {
    public static final String TAG = "GoogleAccountManagerProxy";

    public GoogleAccountManagerProxy() {
        super();
    }

    @Override
    protected Object getWho() {
        try {
            Context context = BlackBoxCore.getContext();
            if (context != null) {
                return AccountManager.get(context);
            }
        } catch (Exception e) {
            Slog.w(TAG, "Failed to get AccountManager instance", e);
        }
        return null;
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @ProxyMethod("getAccounts")
    public static class GetAccounts extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Account[] bAccounts = top.niunaijun.blackbox.fake.frameworks.BAccountManager.get().getAccountsAsUser(null);
            return bAccounts != null ? bAccounts : new Account[0];
        }
    }

    @ProxyMethod("getAccountsByType")
    public static class GetAccountsByType extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            String accountType = (args != null && args.length > 0 && args[0] instanceof String) ? (String) args[0] : null;
            Account[] bAccounts = top.niunaijun.blackbox.fake.frameworks.BAccountManager.get().getAccountsAsUser(accountType);
            return bAccounts != null ? bAccounts : new Account[0];
        }
    }

    @ProxyMethod("getPassword")
    public static class GetPassword extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Slog.d(TAG, "GoogleAccountManager: Handling getPassword call");
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "GoogleAccountManager: GetPassword error, returning mock password", e);
                return "mock_google_password";
            }
        }
    }

    @ProxyMethod("getUserData")
    public static class GetUserData extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Slog.d(TAG, "GoogleAccountManager: Handling getUserData call");
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "GoogleAccountManager: GetUserData error, returning mock data", e);
                return "mock_user_data";
            }
        }
    }

    @ProxyMethod("addAccount")
    public static class AddAccount extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Slog.d(TAG, "GoogleAccountManager: Handling addAccount call");
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "GoogleAccountManager: AddAccount error, returning settings intent", e);
                Bundle result = new Bundle();
                android.content.Intent fallbackIntent = new android.content.Intent("android.settings.ADD_ACCOUNT_SETTINGS");
                fallbackIntent.putExtra("account_types", new String[]{"com.google"});
                fallbackIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                result.putParcelable(android.accounts.AccountManager.KEY_INTENT, fallbackIntent);
                return result;
            }
        }
    }

    @ProxyMethod("removeAccount")
    public static class RemoveAccount extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Slog.d(TAG, "GoogleAccountManager: Handling removeAccount call");
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "GoogleAccountManager: RemoveAccount error, returning true", e);
                return true; 
            }
        }
    }

    @ProxyMethod("hasFeatures")
    public static class HasFeatures extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Slog.d(TAG, "GoogleAccountManager: Handling hasFeatures call");
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "GoogleAccountManager: HasFeatures error, returning true", e);
                return true; 
            }
        }
    }

    @ProxyMethod("getAuthToken")
    public static class GetAuthToken extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Slog.d(TAG, "GoogleAccountManager: Handling getAuthToken call");
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "GoogleAccountManager: GetAuthToken error", e);
                throw e;
            }
        }
    }

    @ProxyMethod("invalidateAuthToken")
    public static class InvalidateAuthToken extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Slog.d(TAG, "GoogleAccountManager: Handling invalidateAuthToken call");
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "GoogleAccountManager: InvalidateAuthToken error, ignoring", e);
                return null;
            }
        }
    }

    @ProxyMethod("peekAuthToken")
    public static class PeekAuthToken extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Slog.d(TAG, "GoogleAccountManager: Handling peekAuthToken call");
                Object result = method.invoke(who, args);
                if (result != null) return result;
                if (args != null && args.length >= 2 && args[0] instanceof Account && args[1] instanceof String) {
                    return top.niunaijun.blackbox.fake.frameworks.BAccountManager.get().peekAuthToken((Account) args[0], (String) args[1]);
                }
                return null;
            } catch (Exception e) {
                Slog.w(TAG, "GoogleAccountManager: PeekAuthToken error", e);
                return null;
            }
        }
    }

    @ProxyMethod("setPassword")
    public static class SetPassword extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Slog.d(TAG, "GoogleAccountManager: Handling setPassword call");
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "GoogleAccountManager: SetPassword error, ignoring", e);
                return null;
            }
        }
    }

    @ProxyMethod("setUserData")
    public static class SetUserData extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Slog.d(TAG, "GoogleAccountManager: Handling setUserData call");
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "GoogleAccountManager: SetUserData error, ignoring", e);
                return null;
            }
        }
    }

    @ProxyMethod("getAuthenticatorTypes")
    public static class GetAuthenticatorTypes extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Slog.d(TAG, "GoogleAccountManager: Handling getAuthenticatorTypes call");
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "GoogleAccountManager: GetAuthenticatorTypes error, returning mock types", e);
                return new String[]{"com.google"};
            }
        }
    }

    @ProxyMethod("isAccountPresent")
    public static class IsAccountPresent extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Slog.d(TAG, "GoogleAccountManager: Handling isAccountPresent call");
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "GoogleAccountManager: IsAccountPresent error, returning true", e);
                return true; 
            }
        }
    }

    @ProxyMethod("getUserData")
    public static class GetUserDataByKey extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Slog.d(TAG, "GoogleAccountManager: Handling getUserData call with key");
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "GoogleAccountManager: GetUserData error, returning mock data", e);
                return "mock_user_data_value";
            }
        }
    }

    


    
    private static boolean isGoogleApp() {
        try {
            Context context = BlackBoxCore.getContext();
            if (context != null) {
                String packageName = context.getPackageName();
                return packageName != null && (
                    packageName.startsWith("com.google.") ||
                    packageName.startsWith("com.android.") ||
                    packageName.contains("gms") ||
                    packageName.contains("google")
                );
            }
        } catch (Exception e) {
            Slog.w(TAG, "Failed to check if current package is Google app", e);
        }
        return false;
    }
}
