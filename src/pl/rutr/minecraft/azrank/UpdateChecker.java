/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.rutr.minecraft.azrank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import pl.rutr.minecraft.azrank.AZRank;

/**
 *
 * @author Rutr <artuczapl at gmail.com>
 */
public class UpdateChecker implements Runnable {
    private AZRank azrank;
    private boolean working=false;
    private boolean updating=false;
    private static String userAgent;
    
    UpdateChecker(AZRank plugin)
    {
        azrank=plugin;
        Server server = azrank.getServer();
        if(server!=null){
            userAgent="CraftBukkit/" +
                azrank.getServer().getBukkitVersion() +
            " AZRank/"+azrank.getDescription().getVersion() +
            " (;;"+System.getProperty("os.name") +
            "/" + System.getProperty("os.version") +
            "/" + System.getProperty("os.arch") +
            ") Java/"+System.getProperty("java.version");
        } else
        {
            AZRank.log.severe("[AZRank] Error 102");
            userAgent = "Java"+System.getProperty("java.version");
        }
        
    }
    
    @Override
    public void run() {
        if(!working)
        {
            working=true;
            
            String verStr = azrank.getDescription().getVersion();
            azrank.debugmsg("Checkign updates... for version: " + verStr);
            String[] ver = verStr.split("\\.");
            if(ver.length>0)
            {
                String[] letters = {"a","b","c","d"};
                try {
                    String version2="";
                    for(int i = 0; i < (ver.length>3 ? 4 : ver.length); i++)
                    {
                        version2 += "&" + letters[i] + "="+URLEncoder.encode(ver[i],"UTF-8");
                    }

                    URL address = new URL("http://updater.azrank.minecraft.rutr.pl/?protocol=1"+version2);
                    URLConnection con = address.openConnection();
                    con.setRequestProperty("User-Agent", userAgent);
                    
                    BufferedReader in = new BufferedReader(
                                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    String[] lines= {"", "", "", ""};
                    int i=0;
                    while ((inputLine = in.readLine()) != null && i<4)
                    {
                        lines[i] = inputLine;
                        //System.out.println("" + i + ": " + inputLine);
                        i++;
                    }
                    in.close();
                    
                    if(lines[0].equals("ok") )
                    {
                        if(lines[1].equals("-2"))
                        {
                            azrank.debugmsg("Error when parsing version of AZRank");
                        }
                        else if(lines[1].equals("-1"))
                        {
                            azrank.debugmsg("Checked update! You have newer version than the last!(Error ?)");
                        }
                        else if(lines[1].equals("0"))
                        {
                            azrank.debugmsg("Checked update! You have latest version!");
                        }
                        else if(lines[1].equals("1"))
                        {
                            azrank.debugmsg("Checked update! Your version is out of date! UPDATING...!");
                            update(lines[2]);
                            
                        }
                        else if(lines[1].equals("2"))
                        {
                            azrank.debugmsg("Checked update! Your version is out of date! but you have turned off auto updating");
                        }
                        else
                        {
                            azrank.debugmsg("Error when parsing !");  
                        }
                    }
                    else
                    {
                        azrank.debugmsg("Error when parsing !");  
                    }

                } catch (IOException ex) {
                    AZRank.log.log(Level.SEVERE, null, ex);
                }
                
            }
            else
            {
                azrank.debugmsg("[ERROR] Invalid version!");
            }
            
            
            working=false;
        }
        else
        {
            azrank.debugmsg("[TmpRankChecker] Still checking! new check canceled!");
        }
    }
    
    public boolean isUpdating()
    {
        return updating;
    }
    
    private boolean update(String url)
    {
        
        if(downloadUpdate(url, azrank.updateFile, userAgent))
        {
            if(copyUpdate(azrank.updateFile))
            {
                AZRank.log.info("[AZRank] Please restart server to finish update!");
                return true;
            }
            else
                return false;
        }
        else
            return false;
        
    }
    
    private static boolean downloadUpdate(String url, File file, String userAgent)
    {
        URL address;
        try {
            String message="";
            address = new URL(url);
            message="[UPDATER]connecting...";
            
            URLConnection conn = address.openConnection();
            conn.setRequestProperty("User-Agent", userAgent);
            InputStream inStream = conn.getInputStream();
            
            AZRank.log.log(Level.INFO, "{0}[OK]", message);
            
            AZRank.log.log(Level.INFO, "Downloading...");
            FileOutputStream fileOutStream= new FileOutputStream( file );
            int oneChar;
            long count=0;
            int length = conn.getContentLength();
            float p,lastp=0;
            long started = Calendar.getInstance().getTimeInMillis();
            long last=started;
            long now;
            long timeDiff;
            int czas = 2000,czas2 = 10000;
            int avaliable;
            byte[] b;
            int ile;
            
            //ONE test byte
            oneChar=inStream.read();
            fileOutStream.write(oneChar);
            count++;
            now = Calendar.getInstance().getTimeInMillis();
            p = count*100/length;
            AZRank.log.log(Level.INFO, "[AZRank][UPDATER]{0}%  {1}/{2}KiB in: {3}/{4}", new Object[]{p, count/1024, length/1024, now - last, now - started});
            
            while ((oneChar=inStream.read()) != -1)
            {
                
                fileOutStream.write(oneChar);
                count++;
                
                avaliable=inStream.available();
                b = new byte[avaliable];
                ile = inStream.read(b);
                fileOutStream.write(b);
                count+=ile;
                
                now = Calendar.getInstance().getTimeInMillis();
                timeDiff = now - last;
                
                p = count*100/length;
                if((timeDiff>czas && p-lastp>=4) || timeDiff>czas2 )
                {
                     AZRank.log.log(Level.INFO, "[AZRank][UPDATER]{0}%  {1}/{2}KiB in: {3}/{4}", new Object[]{p, count/1024, length/1024, now - last, now - started});
                    
                    lastp=p;
                    last=now;
                }
                
            }
            inStream.close();
            fileOutStream.close();
            
            now = Calendar.getInstance().getTimeInMillis();
            p = count*100/length;
            AZRank.log.log(Level.INFO, "[AZRank][UPDATER]{0}%  {1}/{2}KiB in: {3}/{4}", new Object[]{p, count/1024, length/1024, now - last, now - started});
            
            
            return true;
        } catch (IOException ex) {
            System.out.println( ex);
            return false;
        }
        
        
        
        
    }

    private static boolean copyUpdate(File updateFile) {
        try {
            File target = new File(AZRank.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            AZRank.log.log(Level.INFO, "[AZRank]file URI: "+target.getCanonicalPath());

            InputStream in = new FileInputStream(updateFile);
            OutputStream out = new FileOutputStream(target);
            // Transfer bytes from in to out
            AZRank.log.log(Level.INFO, "[AZRank] Coping update....");
            try {
               int oc, count=0;
                while ((oc=in.read()) != -1)
                {
                    out.write(oc);
                    count++;
                }
                in.close();
                out.close();
                AZRank.log.log(Level.INFO, "[AZRank]Update Successful copied! Bytes:{0}!", count);
                return true;
            } catch (IOException ex) {
                AZRank.log.log(Level.SEVERE, null, ex);
                return false;
            }

            
        } catch (FileNotFoundException ex) {
            AZRank.log.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            AZRank.log.log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            AZRank.log.log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

}
