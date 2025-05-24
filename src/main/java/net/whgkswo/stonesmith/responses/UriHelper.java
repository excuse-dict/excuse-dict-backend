package net.whgkswo.stonesmith.responses;

import java.net.URI;

public class UriHelper {
    public static URI createURI(String basePath, long id){
        return URI.create(basePath + "/" + id);
    }
}
