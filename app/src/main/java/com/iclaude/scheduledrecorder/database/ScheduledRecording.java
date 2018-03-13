package com.iclaude.scheduledrecorder.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Table "scheduled_recordings".
 */

@Entity(tableName = "scheduled_recordings",
        indices = {@Index("start_time"), @Index(value = {"start_time", "end_time"})})
public class ScheduledRecording implements Comparable<ScheduledRecording>, Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "start_time")
    private long start;
    @ColumnInfo(name = "end_time")
    private long end;

    // Constructor for an existing scheduled recording (it already has an id).
    public ScheduledRecording(int id, long start, long end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    // Constructor to create a new scheduled recording.
    @Ignore
    public ScheduledRecording(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    @Override
    public int compareTo(@NonNull ScheduledRecording scheduledRecording) {
        return (int) (getStart() - scheduledRecording.getStart());
    }

    // Implementation of Parcelable interface.
    protected ScheduledRecording(Parcel in) {
        id = in.readInt();
        start = in.readLong();
        end = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeLong(start);
        dest.writeLong(end);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ScheduledRecording> CREATOR = new Parcelable.Creator<ScheduledRecording>() {
        @Override
        public ScheduledRecording createFromParcel(Parcel in) {
            return new ScheduledRecording(in);
        }

        @Override
        public ScheduledRecording[] newArray(int size) {
            return new ScheduledRecording[size];
        }
    };
}
