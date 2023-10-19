package io.grpc.leaderless.utils;

import java.util.ArrayList;
import java.util.HashMap;

public class SingletonInstance {
    public static HashMap<String, String> dbTable = new HashMap<>();
    public static int serverID = 0;
}
