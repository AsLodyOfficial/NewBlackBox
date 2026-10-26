package top.niunaijun.blackbox.fake.service;

import android.accounts.Account;
import android.accounts.IAccountManagerResponse;
import android.content.Context;
import android.os.Bundle;

import java.lang.reflect.Method;
import java.util.Map;

import black.android.accounts.BRIAccountManagerStub;
import black.android.os.BRServiceManager;
import top.niunaijun.blackbox.fake.frameworks.BAccountManager;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.Slog;


public class IAccountManagerProxy extends BinderInvocationStub {
    public static final String TAG = "IAccountManagerProxy";

    public IAccountManagerProxy() {
        super(BRServiceManager.get().getService(Context.ACCOUNT_SERVICE));
    }

    @Override
    protected Object getWho() {
        return BRIAccountManagerStub.get().asInterface(BRServiceManager.get().getService(Context.ACCOUNT_SERVICE));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.ACCOUNT_SERVICE);
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isMethodHookEnabled(method.getName())) {
            try {
                return super.invoke(proxy, method, args);
            } catch (Throwable t) {
                Slog.w(TAG, "hook for " + method.getName() + " threw: " + t.getMessage());
            }
        }
        Slog.w(TAG, "IAccountManagerProxy isolated unhooked call: " + method.getName());
        try {
            Object who = getWho();
            if (who != null) {
                return method.invoke(who, args);
            }
        } catch (Throwable t) {
            Slog.w(TAG, "Fallback to real AccountManager failed for " + method.getName(), t);
        }
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof IAccountManagerResponse) {
                    try {
                        ((IAccountManagerResponse) arg).onError(android.accounts.AccountManager.ERROR_CODE_REMOTE_EXCEPTION, "Isolated AccountManager");
                    } catch (Throwable ignored) {
                    }
                    break;
                }
            }
        }
        Class<?> returnType = method.getReturnType();
        if (returnType.equals(boolean.class) || returnType.equals(Boolean.class)) {
            return false;
        }
        if (returnType.equals(Account[].class)) {
            return new Account[0];
        }
        if (returnType.equals(int.class) || returnType.equals(Integer.class)) {
            return 0;
        }
        if (returnType.equals(Map.class)) {
            return java.util.Collections.emptyMap();
        }
        if (returnType.equals(Bundle.class)) {
            return new Bundle();
        }
        return null;
    }

    @ProxyMethod("getPassword")
    public static class getPassword extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return BAccountManager.get().getPassword((Account) args[0]);
        }
    }

    @ProxyMethod("getUserData")
    public static class getUserData extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return BAccountManager.get().getUserData((Account) args[0], (String) args[1]);
        }
    }

    @ProxyMethod("getAuthenticatorTypes")
    public static class getAuthenticatorTypes extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return BAccountManager.get().getAuthenticatorTypes();
        }
    }

    @ProxyMethod("getAccountsForPackage")
    public static class getAccountsForPackage extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return BAccountManager.get().getAccountsForPackage((String) args[0], (int) args[1]);
        }
    }

    @ProxyMethod("getAccountsByTypeForPackage")
    public static class getAccountsByTypeForPackage extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return BAccountManager.get().getAccountsByTypeForPackage((String) args[0], (String) args[1]);
        }
    }

    @ProxyMethod("getAccountByTypeAndFeatures")
    public static class getAccountByTypeAndFeatures extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().getAccountByTypeAndFeatures((IAccountManagerResponse) args[0], (String) args[1], (String[]) args[2]);
            return 0;
        }
    }

    @ProxyMethod("getAccountsByFeatures")
    public static class getAccountsByFeatures extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().getAccountsByFeatures((IAccountManagerResponse) args[0], (String) args[1], (String[]) args[2]);
            return 0;
        }
    }

    @ProxyMethod("getAccountsAsUser")
    public static class getAccountsAsUser extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            String type = (args != null && args.length > 0 && args[0] instanceof String) ? (String) args[0] : null;
            return BAccountManager.get().getAccountsAsUser(type);
        }
    }

    @ProxyMethod("addAccountExplicitly")
    public static class addAccountExplicitly extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return BAccountManager.get().addAccountExplicitly((Account) args[0], (String) args[1], (Bundle) args[2]);
        }
    }

    @ProxyMethod("removeAccountAsUser")
    public static class removeAccountAsUser extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().removeAccountAsUser((IAccountManagerResponse) args[0], (Account) args[1], (boolean) args[2]);
            return 0;
        }
    }

    @ProxyMethod("removeAccountExplicitly")
    public static class removeAccountExplicitly extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return BAccountManager.get().removeAccountExplicitly((Account) args[0]);
        }
    }

    @ProxyMethod("copyAccountToUser")
    public static class copyAccountToUser extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().copyAccountToUser((IAccountManagerResponse) args[0], (Account) args[1], (int) args[2], (int) args[3]);
            return 0;
        }
    }

    @ProxyMethod("invalidateAuthToken")
    public static class invalidateAuthToken extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().invalidateAuthToken((String) args[0], (String) args[1]);
            return 0;
        }
    }

    @ProxyMethod("peekAuthToken")
    public static class peekAuthToken extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return BAccountManager.get().peekAuthToken((Account) args[0], (String) args[1]);
        }
    }

    @ProxyMethod("setAuthToken")
    public static class setAuthToken extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().setAuthToken((Account) args[0], (String) args[1], (String) args[2]);
            return 0;
        }
    }

    @ProxyMethod("setPassword")
    public static class setPassword extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().setPassword((Account) args[0], (String) args[1]);
            return 0;
        }
    }

    @ProxyMethod("clearPassword")
    public static class clearPassword extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().clearPassword((Account) args[0]);
            return 0;
        }
    }

    @ProxyMethod("setUserData")
    public static class setUserData extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().setUserData((Account) args[0], (String) args[1], (String) args[2]);
            return 0;
        }
    }

    @ProxyMethod("updateAppPermission")
    public static class updateAppPermission extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().updateAppPermission((Account) args[0], (String) args[1], (int) args[2], (boolean) args[3]);
            return 0;
        }
    }

    @ProxyMethod("getAuthToken")
    public static class getAuthToken extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().getAuthToken((IAccountManagerResponse) args[0],
                    (Account) args[1],
                    (String) args[2],
                    (boolean) args[3],
                    (boolean) args[4],
                    (Bundle) args[5]);
            return 0;
        }
    }

    @ProxyMethod("addAccount")
    public static class addAccount extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().addAccount((IAccountManagerResponse) args[0],
                    (String) args[1],
                    (String) args[2],
                    (String[]) args[3],
                    (boolean) args[4],
                    (Bundle) args[5]);
            return 0;
        }
    }

    @ProxyMethod("addAccountAsUser")
    public static class addAccountAsUser extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().addAccountAsUser((IAccountManagerResponse) args[0],
                    (String) args[1],
                    (String) args[2],
                    (String[]) args[3],
                    (boolean) args[4],
                    (Bundle) args[5]);
            return 0;
        }
    }

    @ProxyMethod("updateCredentials")
    public static class updateCredentials extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().updateCredentials((IAccountManagerResponse) args[0],
                    (Account) args[1],
                    (String) args[2],
                    (boolean) args[3],
                    (Bundle) args[4]);
            return 0;
        }
    }

    @ProxyMethod("editProperties")
    public static class editProperties extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().editProperties((IAccountManagerResponse) args[0],
                    (String) args[1],
                    (boolean) args[2]);
            return 0;
        }
    }

    @ProxyMethod("confirmCredentialsAsUser")
    public static class confirmCredentialsAsUser extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().confirmCredentialsAsUser((IAccountManagerResponse) args[0],
                    (Account) args[1],
                    (Bundle) args[2],
                    (boolean) args[3]);
            return 0;
        }
    }

    @ProxyMethod("accountAuthenticated")
    public static class accountAuthenticated extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().accountAuthenticated((Account) args[0]);
            return 0;
        }
    }

    @ProxyMethod("getAuthTokenLabel")
    public static class getAuthTokenLabel extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().getAuthTokenLabel((IAccountManagerResponse) args[0],
                    (String) args[1],
                    (String) args[2]);
            return 0;
        }
    }

    @ProxyMethod("getPackagesAndVisibilityForAccount")
    public static class getPackagesAndVisibilityForAccount extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return BAccountManager.get().getPackagesAndVisibilityForAccount((Account) args[0]);
        }
    }

    @ProxyMethod("addAccountExplicitlyWithVisibility")
    public static class addAccountExplicitlyWithVisibility extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return BAccountManager.get().addAccountExplicitlyWithVisibility((Account) args[0],
                    (String) args[1],
                    (Bundle) args[2],
                    (Map) args[3]
            );
        }
    }

    @ProxyMethod("setAccountVisibility")
    public static class setAccountVisibility extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return BAccountManager.get().setAccountVisibility((Account) args[0],
                    (String) args[1],
                    (int) args[2]
            );
        }
    }

    @ProxyMethod("getAccountVisibility")
    public static class getAccountVisibility extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return BAccountManager.get().getAccountVisibility((Account) args[0],
                    (String) args[1]
            );
        }
    }

    @ProxyMethod("getAccountsAndVisibilityForPackage")
    public static class getAccountsAndVisibilityForPackage extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return BAccountManager.get().getAccountsAndVisibilityForPackage((String) args[0],
                    (String) args[1]
            );
        }
    }

    @ProxyMethod("registerAccountListener")
    public static class registerAccountListener extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().registerAccountListener((String[]) args[0],
                    (String) args[1]
            );
            return 0;
        }
    }

    @ProxyMethod("unregisterAccountListener")
    public static class unregisterAccountListener extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().unregisterAccountListener((String[]) args[0],
                    (String) args[1]
            );
            return 0;
        }
    }

    @ProxyMethod("startAddAccountSession")
    public static class startAddAccountSession extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            BAccountManager.get().addAccount((IAccountManagerResponse) args[0],
                    (String) args[1],
                    (String) args[2],
                    (String[]) args[3],
                    (boolean) args[4],
                    (Bundle) args[5]);
            return 0;
        }
    }

    @ProxyMethod("finishSession")
    public static class finishSession extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 0 && args[0] instanceof IAccountManagerResponse) {
                try {
                    Bundle result = (args.length > 1 && args[1] instanceof Bundle) ? (Bundle) args[1] : new Bundle();
                    ((IAccountManagerResponse) args[0]).onResult(result);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            return 0;
        }
    }

    @ProxyMethod("hasFeatures")
    public static class hasFeatures extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 0 && args[0] instanceof IAccountManagerResponse) {
                try {
                    Bundle result = new Bundle();
                    result.putBoolean(android.accounts.AccountManager.KEY_BOOLEAN_RESULT, true);
                    ((IAccountManagerResponse) args[0]).onResult(result);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            return 0;
        }
    }

    @ProxyMethod("isCredentialsUpdateSuggested")
    public static class isCredentialsUpdateSuggested extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 0 && args[0] instanceof IAccountManagerResponse) {
                try {
                    Bundle result = new Bundle();
                    result.putBoolean(android.accounts.AccountManager.KEY_BOOLEAN_RESULT, false);
                    ((IAccountManagerResponse) args[0]).onResult(result);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            return 0;
        }
    }

    @ProxyMethod("getAccounts")
    public static class getAccounts extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return BAccountManager.get().getAccountsAsUser(null);
        }
    }

    @ProxyMethod("getAccountsByType")
    public static class getAccountsByType extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            String type = args != null && args.length > 0 && args[0] instanceof String ? (String) args[0] : null;
            return BAccountManager.get().getAccountsAsUser(type);
        }
    }

    @ProxyMethod("someUserHasAccount")
    public static class someUserHasAccount extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 0 && args[0] instanceof Account) {
                Account target = (Account) args[0];
                Account[] accounts = BAccountManager.get().getAccountsAsUser(target.type);
                if (accounts != null) {
                    for (Account a : accounts) {
                        if (a != null && a.equals(target)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    @ProxyMethod("hasAccountAccess")
    public static class hasAccountAccess extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 0 && args[0] instanceof Account) {
                Account target = (Account) args[0];
                Account[] accounts = BAccountManager.get().getAccountsAsUser(target.type);
                if (accounts != null) {
                    for (Account a : accounts) {
                        if (a != null && a.equals(target)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    @ProxyMethod("finishSessionAsUser")
    public static class finishSessionAsUser extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 0 && args[0] instanceof IAccountManagerResponse) {
                try {
                    Bundle result = (args.length > 1 && args[1] instanceof Bundle) ? (Bundle) args[1] : new Bundle();
                    ((IAccountManagerResponse) args[0]).onResult(result);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            return 0;
        }
    }

    @ProxyMethod("startUpdateCredentialsSession")
    public static class startUpdateCredentialsSession extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 0 && args[0] instanceof IAccountManagerResponse) {
                try {
                    Bundle result = new Bundle();
                    result.putBoolean(android.accounts.AccountManager.KEY_BOOLEAN_RESULT, true);
                    ((IAccountManagerResponse) args[0]).onResult(result);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            return 0;
        }
    }
}
