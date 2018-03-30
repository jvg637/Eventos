package org.example.eventos.modelo.instagram;

/**
 * Created by jordi 
 */


/*

https://www.instagram.com/developer/endpoints/users/

http://www.jsonschema2pojo.org

GET/tags/tag-name

        RESPONSE
        https://api.instagram.com/v1/tags/{tag-name}?access_token=ACCESS-TOKEN

        {
        "data":
         {
        "media_count": 472,
        "name": "nofilter",
        }

        }

        */


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class RespuestaGetTags {


    public class Data {

        @SerializedName("media_count")
        @Expose
        private Integer mediaCount;
        @SerializedName("name")
        @Expose
        private String name;

        public Integer getMediaCount() {
            return mediaCount;
        }

        public void setMediaCount(Integer mediaCount) {
            this.mediaCount = mediaCount;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @SerializedName("data")
    @Expose
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

}


