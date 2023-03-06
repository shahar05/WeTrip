package com.shahardror.wetrip;

import java.util.ArrayList;

public class Trip implements Comparable<Trip> {

    String attractionName;
    String title;
    String userID;

    String meetingTime;
    String meetingDate;
    String meetingPoint;

    String extraDetails;
    long  timePosted;
    String imageURL;
    ArrayList<String> participantsID;

    double longitudeOfAttraction;
    double latitudeOfAttraction;

    public Trip(String attractionName, String title, String userID, String meetingTime, String meetingDate, String meetingPoint, String extraDetails, long timePosted, String imageURL, ArrayList<String> participantsID, double longitudeOfAttraction, double latitudeOfAttraction) {
        this.attractionName = attractionName;
        this.title = title;
        this.userID = userID;
        this.meetingTime = meetingTime;
        this.meetingDate = meetingDate;
        this.meetingPoint = meetingPoint;
        this.extraDetails = extraDetails;
        this.timePosted = timePosted;
        this.imageURL = imageURL;
        this.participantsID = participantsID;
        this.longitudeOfAttraction = longitudeOfAttraction;
        this.latitudeOfAttraction = latitudeOfAttraction;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(String meetingTime) {
        this.meetingTime = meetingTime;
    }

    public String getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(String meetingDate) {
        this.meetingDate = meetingDate;
    }

    public String getMeetingPoint() {
        return meetingPoint;
    }

    public void setMeetingPoint(String meetingPoint) {
        this.meetingPoint = meetingPoint;
    }

    public String getExtraDetails() {
        return extraDetails;
    }

    public void setExtraDetails(String extraDetails) {
        this.extraDetails = extraDetails;
    }

    public long getTimePosted() {
        return timePosted;
    }

    public void setTimePosted(long timePosted) {
        this.timePosted = timePosted;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public ArrayList<String> getParticipantsID() {
        return participantsID;
    }

    public void setParticipantsID(ArrayList<String> participantsID) {
        this.participantsID = participantsID;
    }

    public double getLongitudeOfAttraction() {
        return longitudeOfAttraction;
    }

    public void setLongitudeOfAttraction(double longitudeOfAttraction) {
        this.longitudeOfAttraction = longitudeOfAttraction;
    }

    public double getLatitudeOfAttraction() {
        return latitudeOfAttraction;
    }

    public void setLatitudeOfAttraction(double latitudeOfAttraction) {
        this.latitudeOfAttraction = latitudeOfAttraction;
    }

    @Override
    public int compareTo(Trip o) {
        return Long.compare(o.timePosted,this.timePosted);
    }
}
