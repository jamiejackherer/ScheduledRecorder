package com.iclaude.scheduledrecorder.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;


/**
 * Table "saved_recordings".
 * TODO: index fields for searches (see DAO class)
 */

@Entity(tableName = "saved_recordings")
public class Recording implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "recording_name")
    @NonNull
    private String name;

    @ColumnInfo(name = "file_path")
    @NonNull
    private String path;

    @ColumnInfo(name = "length")
    private long length;

    @ColumnInfo(name = "time_added")
    private long timeAdded;

    // Constructor for existing Recording (it already has an id).
    public Recording(int id, @NonNull String name, @NonNull String path, long length, long timeAdded) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.length = length;
        this.timeAdded = timeAdded;
    }

    // Constructor to create a new Recording.
    @Ignore
    public Recording(@NonNull String name, @NonNull String path, long length, long timeAdded) {
        this.name = name;
        this.path = path;
        this.length = length;
        this.timeAdded = timeAdded;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getPath() {
        return path;
    }

    public void setPath(@NonNull String path) {
        this.path = path;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(long timeAdded) {
        this.timeAdded = timeAdded;
    }

    // Implementation of Parcelable interface.
    protected Recording(Parcel in) {
        id = in.readInt();
        name = in.readString();
        path = in.readString();
        length = in.readLong();
        timeAdded = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(path);
        dest.writeLong(length);
        dest.writeLong(timeAdded);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Recording> CREATOR = new Parcelable.Creator<Recording>() {
        @Override
        public Recording createFromParcel(Parcel in) {
            return new Recording(in);
        }

        @Override
        public Recording[] newArray(int size) {
            return new Recording[size];
        }
    };
}
