package com.codetemplates.error;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringRes;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.Serializable;
import java.util.TreeSet;

import com.codetemplates.application.CodeTemplateApplication;
import com.codetemplates.logging.ShortToStringStyle;

/**
 * An error that occurred that has to be reported to a listener or on another thread.
 *
 * Note: errorCodes are used instead of having this class inherit from RuntimeException because
 *       a) they should never be thrown, and
 *       b) having an exception hierarchy that is required to implement Parcelable would be a lot of code for each error
 *          type with no added benefit.
 */
public class ErrorInfo implements Parcelable {

    public static final String BUILT_LOCATION_MESSAGE = "ErrorInfo built here";

    private int mErrorCode = 0;
    private String mDebugMessage;
    private String mUserMessage;
    private Bundle mContextData;
    private Throwable mException;

    /**
     * Use Builder.
     */
    protected ErrorInfo() {
    }

    /**
     * @return Code that can be used for custom logic flow.
     *         0 means no code has been set.
     *         Should be from an id defined in XML to avoid clashes.
     * @see #hasRootErrorCode To check for an error code use hasRootErrorCode as it will check causes too.
     */
    public int getErrorCode() {
        return mErrorCode;
    }

    public String getDebugMessage() {
        return mDebugMessage;
    }

    /**
     * @return Message to be displayed to the user. This might be a special message from the server via an HTTP error
     *         response.
     */
    public String getUserMessage() {
        return mUserMessage;
    }

    public Bundle getContextData() {
        return mContextData;
    }

    public Throwable getException() {
        return mException;
    }

    @Override
    public String toString() {
        // Note: do not use reflectionToString() because we want these names kept after obfuscation.
        ToStringBuilder builder = new ToStringBuilder(this, new ShortToStringStyle());
        if (mErrorCode != 0) {
            builder.append("errorCode", mErrorCode);
        }
        builder.append("debugMessage", mDebugMessage)
                .append("userMessage", mUserMessage);
        if (mContextData != null) {
            // output the contextData keys individually so even if there is a really long key we will get a chance to
            // see some of the other keys.
            for (String key : new TreeSet<String>(mContextData.keySet())) {
                String value = String.valueOf(mContextData.get(key));
                builder.append("contextData:"+key, StringUtils.abbreviate(value, 1000));
            }
        }
        if (mException != null) {
            builder.append("exception", ExceptionUtils.getStackTrace(mException));
        }
        return builder.build();
    }

    /**
     * Returns the root ErrorInfo that was added via setContextData(ErrorInfo)
     */
    public ErrorInfo getRootCause() {
        return scanRootsForErrorCode(null);
    }

    /**
     * @return the true if this ErrorInfo or any of its causes have a given errorCode.
     *         Analogous to ExceptionUtils.indexOfType() != -1
     */
    public boolean hasRootErrorCode(int errorCode) {
        return scanRootsForErrorCode(errorCode) != null;
    }

    private ErrorInfo scanRootsForErrorCode(Integer errorCode) {
        ErrorInfo root = this;
        Object causedBy;
        if (root.getContextData() == null) {
            causedBy = null;
        }
        else {
            causedBy = root.getContextData().get("caused_by");
        }
        int depth = 0; // avoid infinite recursion.
        while (causedBy instanceof ErrorInfo && depth++ < 10) {
            root = (ErrorInfo) causedBy;
            if (errorCode != null && root.getErrorCode() == errorCode) {
                return root;
            }
            if (root.getContextData() == null) {
                causedBy = null;
            }
            else {
                causedBy = root.getContextData().get("caused_by");
            }
        }
        if (errorCode != null && root.getErrorCode() != errorCode) {
            return null;
        }
        return root;
    }

    /**
     * Parcelable constructor.
     */
    private ErrorInfo(Parcel in) {
        mErrorCode = in.readInt();
        mDebugMessage = in.readString();
        mUserMessage = in.readString();
        mContextData = in.readBundle();
        mException = (Throwable) in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mErrorCode);
        out.writeString(mDebugMessage);
        out.writeString(mUserMessage);
        out.writeBundle(mContextData);
        out.writeSerializable(mException);
    }

    public static final Creator<ErrorInfo> CREATOR = new Creator<ErrorInfo>() {

        @Override
        public ErrorInfo createFromParcel(Parcel in) {
            return new ErrorInfo(in);
        }

        @Override
        public ErrorInfo[] newArray(int size) {
            return new ErrorInfo[size];
        }
    };

    public static class Builder {

        private ErrorInfo mError = new ErrorInfo();

        public Builder setErrorCode(int errorCode) {
            mError.mErrorCode = errorCode;
            return this;
        }

        public Builder setDebugMessage(String debugMessage) {
            mError.mDebugMessage = debugMessage;
            return this;
        }

        public Builder setUserMessage(String userMessage) {
            mError.mUserMessage = userMessage;
            return this;
        }

        public Builder setUserMessageId(@StringRes int messageId, Object... formatArgs) {
            mError.mUserMessage = CodeTemplateApplication.getApplication().getString(messageId, formatArgs);
            return this;
        }

        public Builder setContextData(Bundle contextData) {
            mError.mContextData = contextData;
            return this;
        }

        /**
         * Convenience method for creating a bundle and adding a serializable to it.
         */
        public Builder setContextData(Serializable contextData) {
            if (mError.mContextData == null) {
                mError.mContextData = new Bundle();
            }
            mError.mContextData.putSerializable("data", contextData);
            return this;
        }

        /**
         * Convenience method for creating a bundle and adding a serializable to it.
         */
        public Builder setContextData(Parcelable contextData) {
            if (mError.mContextData == null) {
                mError.mContextData = new Bundle();
            }
            mError.mContextData.putParcelable("data", contextData);
            return this;
        }

        /**
         * Chains ErrorInfo using contextData. Copies debugMessage and userMessage if it has not been set yet.
         */
        public Builder setContextData(ErrorInfo contextData) {
            if (mError.mContextData == null) {
                mError.mContextData = new Bundle();
            }
            mError.mContextData.putParcelable("caused_by", contextData);
            if (mError.mErrorCode == 0) {
                mError.mErrorCode = contextData.mErrorCode;
            }
            if (mError.mDebugMessage == null) {
                mError.mDebugMessage = contextData.mDebugMessage;
            }
            if (mError.mUserMessage == null) {
                mError.mUserMessage = contextData.mUserMessage;
            }
            return this;
        }

        public Builder setException(Throwable exception) {
            mError.mException = exception;
            return this;
        }

        public ErrorInfo build() {
            if (mError.mException == null) {
                mError.mException = new Exception(BUILT_LOCATION_MESSAGE);
            }
            return mError;
        }

    }
}
