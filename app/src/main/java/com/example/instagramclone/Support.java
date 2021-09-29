package com.example.instagramclone;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Support {
    public static String calculateTimeAgo(String strDate){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss") ;
        String suffix = "ago";
        try
        {
            Date past = sdf.parse(strDate);
            Date now = new Date();

            System.out.println(now);
            System.out.println(past);
            long seconds= TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
            long minutes=TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
            long hours=TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
            long days=TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());

            if(seconds < 20){
                return "just now"  ;
            }
            else if (seconds < 60 && seconds >=20) {
                return seconds + " Seconds " + suffix;
            } else if (minutes < 60) {
                return minutes + " Minutes "+suffix;
            } else if (hours < 24) {
                return hours + " Hours "+suffix;
            } else if (days>= 7) {
                if (days > 360) {
                    return (days / 360) + " Years " + suffix;
                } else if (days > 30) {
                    return (days / 30) + " Months " + suffix;
                } else {
                    return (days / 7) + " Week " + suffix;
                }
            } else if (days < 7) {
                return days+" Days "+suffix;
            }
        }
        catch (Exception j){
            j.printStackTrace();
        }
        return null ;
    }


}
