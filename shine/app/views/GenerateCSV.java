package views;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import uk.bl.wa.shine.model.SearchData;


/**
 * Created by teg on 10/28/16.
 */
public class GenerateCSV {

    private static String NEWLINE="\n";
    private static String SEPARATOR=",";

    public static String getText(List<SearchData> list, String waybackUrl, String header1, String header2, String user, boolean full) throws Exception{

       StringBuffer csv= new StringBuffer();
       csv.append(header1+"\n");
       csv.append(header2+"\n");


       if (full){
        csv=generateFull(csv,list,waybackUrl);
       }
       else{
           csv=generateBrief(csv,list,waybackUrl);
       }
        return csv.toString();
    }


    private static String formatCsvEntry(String entry){
        if (entry == null){
            entry ="";
        }
        entry=entry.replaceAll("\"","\"\""); // "->""
        return "\""+ entry +"\"";
    }

    private static String formatDateCrawlDate(SearchData obj) throws Exception{
    String date = obj.getCrawlDate();
        if (date == null){
            return "";
        }
        DateFormat formatIn = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
        DateFormat formatOut= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d=formatIn.parse(date);
        return formatOut.format(d);
    }

    private static String formatUrl(SearchData obj, String waybackUrl) throws Exception{
        return waybackUrl+"/"+obj.getWaybackDate()+"/"+obj.getUrl();
    }


    private static StringBuffer generateBrief(StringBuffer buffer, List<SearchData> list, String waybackUrl) throws Exception{

        buffer.append("Title,Date,URL");
        buffer.append(NEWLINE);

        for (SearchData item: list ){
            buffer.append(formatCsvEntry(item.getTitle()));
            buffer.append(SEPARATOR);
            buffer.append(formatCsvEntry(formatDateCrawlDate(item)));
            buffer.append(SEPARATOR);
            buffer.append(formatCsvEntry(formatUrl(item,waybackUrl)));
            buffer.append(NEWLINE);
        }

        return buffer;
    }

        // {"@(d.getTitle)","@(d.getHost)","@(d.getPublicSuffix)","@(d.getCrawlYear)","@(d.getContentTypeNorm)","@(d.getContentLanguage)","@formatDate(d.getCrawlDate)","@(webArchiveUrl)/@(d.getWaybackDate)/@(d.getUrl)"
        private static StringBuffer generateFull(StringBuffer buffer, List<SearchData> list, String waybackUrl) throws Exception{

        buffer.append("Title, Host, Public Suffix, Crawl Year, Content Type, Content Language, Crawl Date, URL, Wayback Date, URL");
        buffer.append(NEWLINE);

        for (SearchData item: list ){
            buffer.append(formatCsvEntry(item.getTitle()));
            buffer.append(SEPARATOR);
            buffer.append(formatCsvEntry(item.getHost()));
            buffer.append(SEPARATOR);
            buffer.append(formatCsvEntry(item.getPublicSuffix()));
            buffer.append(SEPARATOR);
            buffer.append(formatCsvEntry(item.getCrawlYear()));
            buffer.append(SEPARATOR);
            buffer.append(formatCsvEntry(item.getContentTypeNorm()));
            buffer.append(SEPARATOR);
            buffer.append(formatCsvEntry(item.getContentLanguage()));
            buffer.append(SEPARATOR);
            buffer.append(formatCsvEntry(formatDateCrawlDate(item)));
            buffer.append(SEPARATOR);
            buffer.append(formatCsvEntry(formatUrl(item,waybackUrl)));
            buffer.append(NEWLINE);
        }

        return buffer;
    }

}
