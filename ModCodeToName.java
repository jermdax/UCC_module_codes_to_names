import java.net.URL;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.PrintWriter;
import java.util.Scanner;

public class ModCodeToName
{
  public static void main(String[] args)
  {
    URL url;
    InputStream is = null;
    BufferedReader br;
    String line;
    StringBuilder sb = new StringBuilder();
    Map<String, String> mappingHtml = new HashMap<String, String>();

    try
    {
        if(args.length < 1)
        {
          Scanner sc = new Scanner(System.in);
          System.out.println("Please give the URL of your timetable:");
          if(sc.hasNextLine())
          {
            args = new String[1];
            args[0] = sc.nextLine();
          }
        }
        if(args[0].startsWith("www"))
          args[0] = "http://" + args[0];
        url = new URL(args[0]);
        is = url.openStream();  // throws an IOException
        br = new BufferedReader(new InputStreamReader(is));

        while ((line = br.readLine()) != null)
        {
            sb.append(line);
        }
        String timetableHtml = sb.toString();
        Matcher m = Pattern.compile("[A-Z]{2}[0-9]{4}").matcher(timetableHtml);
        while(m.find())
        {
          String modCode = m.group();
          String modCodePrefix = modCode.substring(0, 2);
          if(mappingHtml.get(modCodePrefix) == null)
          {
            try
            {
              mappingHtml.put(modCodePrefix, getMappingHtml(modCodePrefix));
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
          }
          //get the name from the code
          Matcher matcher = Pattern.compile(">" + modCode + ".*?(?=</a>)").matcher(mappingHtml.get(modCodePrefix));
          if(! matcher.find())
          {
            System.err.println("couldnt find the module mapping");
            System.exit(1);
          }
          String name = matcher.group().substring(7);
          timetableHtml = timetableHtml.replaceAll(modCode, name);
        }
        //TODO write to file instead of stdout
        String filename = "timetable.html";
        writeFile(timetableHtml, filename);
        System.out.println("Saved file as " + filename);
    } catch (MalformedURLException mue) {
         mue.printStackTrace();
    } catch (IOException ioe) {
         ioe.printStackTrace();
    }
    finally
    {
        try
        {
            if (is != null) is.close();
        }
        catch (IOException ioe)
        {
            // nothing to see here
        }
    }
  }

  public static void writeFile(String toWrite, String name)
  {
    try
    {
      PrintWriter pw = new PrintWriter(name, "UTF-8");
      pw.print(toWrite);
      pw.close();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  public static String getMappingHtml(String prefix) throws Exception
  {
    StringBuilder sb = new StringBuilder();
    URL url = new URL("http://www.ucc.ie/modules/descriptions/" + prefix + ".html");
    InputStream is = url.openStream();  // throws an IOException
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String line;

    while ((line = br.readLine()) != null)
    {
      sb.append(line);
    }
    return sb.toString();
  }
}

