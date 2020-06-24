package my.doubleclick.repo;

public class Item {
    String mFullName;
    String mUrl;
    String mUpdatedDate;

    public Item() {
    }

    public String getFullName() {
        return mFullName;
    }

    public void setFullName(String fullname) {
        mFullName = fullname;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getUpdatedDate() {
        return mUpdatedDate;
    }

    public void setUpdatedDate(String updatedDt) {
        mUpdatedDate = updatedDt;
    }
}
