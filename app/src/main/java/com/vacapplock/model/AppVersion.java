package com.vacapplock.model;

public class AppVersion {

    private String version;
    private String url;
    private String releaseNotes;
    private String releaseDate;


    public AppVersion(String version, String url, String releaseNotes, String releaseDate, boolean mandatory, String sha512) {
        this.version = version;
        this.url = url;
        this.releaseNotes = releaseNotes;
        this.releaseDate = releaseDate;

    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    @Override
    public String toString() {
        return "AppVersion{" +
                "version='" + version + '\'' +
                ", url='" + url + '\'' +
                ", releaseNotes='" + releaseNotes + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                '}';
    }
}
