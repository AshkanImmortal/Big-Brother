package com.aetava.bigbrother.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable {

    public Uri uri;
    public int orientation;

    public Image(Uri uri, int orientation) {
        this.uri = uri;
        this.orientation = orientation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.uri, 0);
        dest.writeInt(this.orientation);
    }

    private Image(Parcel in) {
        this.uri = in.readParcelable(Uri.class.getClassLoader());
        this.orientation = in.readInt();
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        public Image[] newArray(int size) {
            return new Image[size];
        }
    };
}
