package com.shahardror.wetrip;

public class Upload {
    boolean fromExternal;
    String uploadStr;

    public Upload(boolean fromExternal, String uploadStr) {
        this.fromExternal = fromExternal;
        this.uploadStr = uploadStr;
    }

    public boolean isFromExternal() {
        return fromExternal;
    }

    public void setFromExternal(boolean fromExternal) {
        this.fromExternal = fromExternal;
    }

    public String getUploadStr() {
        return uploadStr;
    }

    public void setUploadStr(String uploadStr) {
        this.uploadStr = uploadStr;
    }
}
