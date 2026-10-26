package top.niunaijun.blackbox.entity.location;

import android.os.Parcel;
import android.os.Parcelable;

public class BLocation extends com.mundo.entity.location.BLocation {
    public BLocation() {
        super();
    }

    public BLocation(double latitude, double longitude) {
        super(latitude, longitude);
    }

    public BLocation(Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<BLocation> CREATOR = new Parcelable.Creator<BLocation>() {
        @Override
        public BLocation createFromParcel(Parcel source) {
            return new BLocation(source);
        }

        @Override
        public BLocation[] newArray(int size) {
            return new BLocation[size];
        }
    };
}
