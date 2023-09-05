package com.cloudwebsoft.framework.web;


import nl.bitwalker.useragentutils.UserAgent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * get browser and OS information from user agent string
 * @author bw67
 *
 */
public class UserAgentParser {
    public static String getBrowser(String uaString){
        return getBrowserName(uaString);
        // UserAgent过时了
        // UserAgent userAgent = UserAgent.parseUserAgentString(uaString);
        // return userAgent.getBrowser().getName(); // IE11下取出来的是mozilla
        // return getAgentOf(uaString, browsers);
    }

    public static String getBrowserName(String agent) {
        if (agent == null) {
            return "unknown null";
        }
        agent = agent.toLowerCase();
        if (agent.indexOf("msie 7") > 0) {
            return "ie7";
        } else if (agent.indexOf("msie 8") > 0) {
            return "ie8";
        } else if (agent.indexOf("msie 9") > 0) {
            return "ie9";
        } else if (agent.indexOf("msie 10") > 0) {
            return "ie10";
        } else if (agent.indexOf("msie") > 0) {
            return "ie";
        }
        else if (agent.indexOf("edge") > 0) {
            return "edge";
        }
        else if (agent.indexOf("lbbrowser") > 0) {
            return "猎豹";
        }
        else if (agent.indexOf("opera") > 0) {
            return "opera";
        } else if (agent.indexOf("chrome") > 0) {
            return "chrome";
        } else if (agent.indexOf("firefox") > 0) {
            return "firefox";
        } else if (agent.indexOf("webkit") > 0) {
            return "webkit";
        } else if (agent.indexOf("trident") > 0 && agent.indexOf("rv:11") > 0) { // Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko
            return "ie11";
        } else if (agent.indexOf("android") > 0) {
            return "android";
        } else if (agent.indexOf("ios") > 0) {
            return "ios";
        }
        else if (agent.indexOf("mac os") > 0 || agent.indexOf("safari") > 0) {
            return "safari";
        } else {
            return "unknown";
        }
    }

    public static String getOS(String uaString){
        return getAgentOf(uaString, os);
    }

    private static String getAgentOf(String uaString, Agent[] agents) {
        String agent = "unknown";
        for(Agent a : agents){
            Matcher m = a.regx.matcher(uaString);
            if(m.find()){
                int n = m.groupCount();
                String name = (a.name != null) ? a.name : m.group(1);
                String v1 = (a.v1 != null) ? a.v1 : (n > 1 ? m.group(2) : "");
                String v2 = (a.v2 != null) ? a.v2 : (n > 2 ? m.group(3) : "");
                agent = (v2.length() > 0) ? name + " " + v1 + "." + v2 : name + " " + v1;
                for(int i=4;i<=n;i++){
                    if(m.group(i) != null){
                        agent = agent + "." + m.group(i);
                    }
                }
                agent.trim();
                break;
            }
        }
        return agent;
    }

    private static Agent[] browsers = new Agent[]{
            new Agent("^(Opera)/(\\d+)\\.(\\d+) \\(Nintendo Wii",null,null,"Wii"),
            new Agent("(SeaMonkey|Camino)/(\\d+)\\.(\\d+)\\.?([ab]?\\d+[a-z]*)",null,null,null),
            new Agent("(Pale[Mm]oon)/(\\d+)\\.(\\d+)\\.?(\\d+)?",null,null,"Pale Moon (Firefox Variant)"),
            new Agent("(Fennec)/(\\d+)\\.(\\d+)\\.?([ab]?\\d+[a-z]*)",null,null,"Firefox Mobile"),
            new Agent("(Fennec)/(\\d+)\\.(\\d+)(pre)",null,null,"Firefox Mobile"),
            new Agent("(Fennec)/(\\d+)\\.(\\d+)",null,null,"Firefox Mobile"),
            new Agent("Mobile.*(Firefox)/(\\d+)\\.(\\d+)",null,null,"Firefox Mobile"),
            new Agent("(Namoroka|Shiretoko|Minefield)/(\\d+)\\.(\\d+)\\.(\\d+(?:pre)?)",null,null,"Firefox ($1)"),
            new Agent("(Firefox)/(\\d+)\\.(\\d+)(a\\d+[a-z]*)",null,null,"Firefox Alpha"),
            new Agent("(Firefox)/(\\d+)\\.(\\d+)(b\\d+[a-z]*)",null,null,"Firefox Beta"),
            new Agent("(Firefox)-(?:\\d+\\.\\d+)?/(\\d+)\\.(\\d+)(a\\d+[a-z]*)",null,null,"Firefox Alpha"),
            new Agent("(Firefox)-(?:\\d+\\.\\d+)?/(\\d+)\\.(\\d+)(b\\d+[a-z]*)",null,null,"Firefox Beta"),
            new Agent("(Namoroka|Shiretoko|Minefield)/(\\d+)\\.(\\d+)([ab]\\d+[a-z]*)?",null,null,"Firefox ($1)"),
            new Agent("(Firefox).*Tablet browser (\\d+)\\.(\\d+)\\.(\\d+)",null,null,"MicroB"),
            new Agent("(MozillaDeveloperPreview)/(\\d+)\\.(\\d+)([ab]\\d+[a-z]*)?",null,null,null),
            new Agent("(Flock)/(\\d+)\\.(\\d+)(b\\d+?)",null,null,null),
            new Agent("(RockMelt)/(\\d+)\\.(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(Navigator)/(\\d+)\\.(\\d+)\\.(\\d+)",null,null,"Netscape"),
            new Agent("(Navigator)/(\\d+)\\.(\\d+)([ab]\\d+)",null,null,"Netscape"),
            new Agent("(Netscape6)/(\\d+)\\.(\\d+)\\.(\\d+)",null,null,"Netscape"),
            new Agent("(MyIBrow)/(\\d+)\\.(\\d+)",null,null,"My Internet Browser"),
            new Agent("(Opera Tablet).*Version/(\\d+)\\.(\\d+)(?:\\.(\\d+))?",null,null,null),
            new Agent("(Opera)/.+Opera Mobi.+Version/(\\d+)\\.(\\d+)",null,null,"Opera Mobile"),
            new Agent("(Opera Mobi)",null,null,"Opera Mobile"),
            new Agent("(Opera Mini)/(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(Opera Mini)/att/(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(Opera)/9.80.*Version/(\\d+)\\.(\\d+)(?:\\.(\\d+))?",null,null,null),
            new Agent("(webOSBrowser)/(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(webOS)/(\\d+)\\.(\\d+)",null,null,"webOSBrowser"),
            new Agent("(wOSBrowser).+TouchPad/(\\d+)\\.(\\d+)",null,null,"webOS TouchPad"),
            new Agent("(luakit)",null,null,"LuaKit"),
            new Agent("(Lightning)/(\\d+)\\.(\\d+)([ab]?\\d+[a-z]*)",null,null,null),
            new Agent("(Firefox)/(\\d+)\\.(\\d+)\\.(\\d+(?:pre)?) \\(Swiftfox\\)",null,null,"Swiftfox"),
            new Agent("(Firefox)/(\\d+)\\.(\\d+)([ab]\\d+[a-z]*)? \\(Swiftfox\\)",null,null,"Swiftfox"),
            new Agent("(rekonq)",null,null,"Rekonq"),
            new Agent("(conkeror|Conkeror)/(\\d+)\\.(\\d+)\\.?(\\d+)?",null,null,"Conkeror"),
            new Agent("(konqueror)/(\\d+)\\.(\\d+)\\.(\\d+)",null,null,"Konqueror"),
            new Agent("(WeTab)-Browser",null,null,null),
            new Agent("(Comodo_Dragon)/(\\d+)\\.(\\d+)\\.(\\d+)",null,null,"Comodo Dragon"),
            new Agent("(YottaaMonitor|BrowserMob|HttpMonitor|YandexBot|Slurp|BingPreview|PagePeeker|ThumbShotsBot|WebThumb|URL2PNG|ZooShot|GomezA|Catchpoint bot|Willow Internet Crawler|Google SketchUp|Read%20Later)",null,null,null),
            new Agent("(Kindle)/(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(Symphony) (\\d+).(\\d+)",null,null,null),
            new Agent("(Minimo)",null,null,null),
            new Agent("(CrMo)/(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)",null,null,"Chrome Mobile"),
            new Agent("(CriOS)/(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)",null,null,"Chrome Mobile iOS"),
            new Agent("(Chrome)/(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+) Mobile",null,null,"Chrome Mobile"),
            new Agent("(chromeframe)/(\\d+)\\.(\\d+)\\.(\\d+)",null,null,"Chrome Frame"),
            new Agent("(UC Browser)(\\d+)\\.(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(SLP Browser)/(\\d+)\\.(\\d+)",null,null,"Tizen Browser"),
            new Agent("(Epiphany)/(\\d+)\\.(\\d+).(\\d+)",null,null,null),
            new Agent("(SE 2\\.X) MetaSr (\\d+)\\.(\\d+)",null,null,"Sogou Explorer"),
            new Agent("(FlyFlow)/(\\d+)\\.(\\d+)",null,null,"Baidu Explorer"),
            new Agent("(Pingdom.com_bot_version_)(\\d+)\\.(\\d+)",null,null,"PingdomBot"),
            new Agent("(facebookexternalhit)/(\\d+)\\.(\\d+)",null,null,"FacebookBot"),
            new Agent("(Twitterbot)/(\\d+)\\.(\\d+)",null,null,"TwitterBot"),
            new Agent("(AdobeAIR|Chromium|FireWeb|Jasmine|ANTGalio|Midori|Fresco|Lobo|PaleMoon|Maxthon|Lynx|OmniWeb|Dillo|Camino|Demeter|Fluid|Fennec|Shiira|Sunrise|Chrome|Flock|Netscape|Lunascape|WebPilot|Vodafone|NetFront|Netfront|Konqueror|SeaMonkey|Kazehakase|Vienna|Iceape|Iceweasel|IceWeasel|Iron|K-Meleon|Sleipnir|Galeon|GranParadiso|Opera Mini|iCab|NetNewsWire|ThunderBrowse|Iris|UP\\.Browser|Bunjaloo|Google Earth|Raven for Mac)/(\\d+)\\.(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(Bolt|Jasmine|IceCat|Skyfire|Midori|Maxthon|Lynx|Arora|IBrowse|Dillo|Camino|Shiira|Fennec|Phoenix|Chrome|Flock|Netscape|Lunascape|Epiphany|WebPilot|Opera Mini|Opera|Vodafone|NetFront|Netfront|Konqueror|Googlebot|SeaMonkey|Kazehakase|Vienna|Iceape|Iceweasel|IceWeasel|Iron|K-Meleon|Sleipnir|Galeon|GranParadiso|iCab|NetNewsWire|Space Bison|Stainless|Orca|Dolfin|BOLT|Minimo|Tizen Browser|Polaris|Abrowser)/(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(iRider|Crazy Browser|SkipStone|iCab|Lunascape|Sleipnir|Maemo Browser) (\\d+)\\.(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(iCab|Lunascape|Opera|Android|Jasmine|Polaris|BREW) (\\d+)\\.(\\d+)\\.?(\\d+)?",null,null,null),
            new Agent("(Android) Donut","2","1",null),
            new Agent("(Android) Eclair","1","2",null),
            new Agent("(Android) Froyo","2","2",null),
            new Agent("(Android) Gingerbread","3","2",null),
            new Agent("(Android) Honeycomb",null,"3",null),
            new Agent("(IEMobile)[ /](\\d+)\\.(\\d+)",null,null,"IE Mobile"),
            new Agent("(MSIE) (\\d+)\\.(\\d+).*XBLWP7",null,null,"IE Large Screen"),
            new Agent("(Firefox)/(\\d+)\\.(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(Firefox)/(\\d+)\\.(\\d+)(pre|[ab]\\d+[a-z]*)?",null,null,null),
            new Agent("(Obigo)InternetBrowser",null,null,null),
            new Agent("(Obigo)\\-Browser",null,null,null),
            new Agent("(Obigo|OBIGO)[^\\d]*(\\d+)(?:.(\\d+))?",null,null,null),
            new Agent("(MAXTHON|Maxthon) (\\d+)\\.(\\d+)",null,null,"Maxthon"),
            new Agent("(Maxthon|MyIE2|Uzbl|Shiira)",null,"0",null),
            new Agent("(PLAYSTATION) (\\d+)",null,null,"PlayStation"),
            new Agent("(PlayStation Portable)[^\\d]+(\\d+).(\\d+)",null,null,null),
            new Agent("(BrowseX) \\((\\d+)\\.(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(POLARIS)/(\\d+)\\.(\\d+)",null,null,"Polaris"),
            new Agent("(Embider)/(\\d+)\\.(\\d+)",null,null,"Polaris"),
            new Agent("(BonEcho)/(\\d+)\\.(\\d+)\\.(\\d+)",null,null,"Bon Echo"),
            new Agent("(iPod).+Version/(\\d+)\\.(\\d+)\\.(\\d+)",null,null,"Mobile Safari"),
            new Agent("(iPod).*Version/(\\d+)\\.(\\d+)",null,null,"Mobile Safari"),
            new Agent("(iPhone).*Version/(\\d+)\\.(\\d+)\\.(\\d+)",null,null,"Mobile Safari"),
            new Agent("(iPhone).*Version/(\\d+)\\.(\\d+)",null,null,"Mobile Safari"),
            new Agent("(iPad).*Version/(\\d+)\\.(\\d+)\\.(\\d+)",null,null,"Mobile Safari"),
            new Agent("(iPad).*Version/(\\d+)\\.(\\d+)",null,null,"Mobile Safari"),
            new Agent("(iPod|iPhone|iPad);.*CPU.*OS (\\d+)(?:_\\d+)?_(\\d+).*Mobile",null,null,"Mobile Safari"),
            new Agent("(iPod|iPhone|iPad)",null,null,"Mobile Safari"),
            new Agent("(AvantGo) (\\d+).(\\d+)",null,null,null),
            new Agent("(Avant)",null,"1",null),
            new Agent("^(Nokia)",null,null,"Nokia Services (WAP) Browser"),
            new Agent("(NokiaBrowser)/(\\d+)\\.(\\d+).(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(NokiaBrowser)/(\\d+)\\.(\\d+).(\\d+)",null,null,null),
            new Agent("(NokiaBrowser)/(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(BrowserNG)/(\\d+)\\.(\\d+).(\\d+)",null,null,"NokiaBrowser"),
            new Agent("(Series60)/5\\.0","0","7","NokiaBrowser"),
            new Agent("(Series60)/(\\d+)\\.(\\d+)",null,null,"Nokia OSS Browser"),
            new Agent("(S40OviBrowser)/(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)",null,null,"Nokia Series 40 Ovi Browser"),
            new Agent("(Nokia)[EN]?(\\d+)",null,null,null),
            new Agent("(PlayBook).+RIM Tablet OS (\\d+)\\.(\\d+)\\.(\\d+)",null,null,"Blackberry WebKit"),
            new Agent("(Black[bB]erry).+Version/(\\d+)\\.(\\d+)\\.(\\d+)",null,null,"Blackberry WebKit"),
            new Agent("(Black[bB]erry)\\s?(\\d+)",null,null,"Blackberry"),
            new Agent("(OmniWeb)/v(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(Blazer)/(\\d+)\\.(\\d+)",null,null,"Palm Blazer"),
            new Agent("(Pre)/(\\d+)\\.(\\d+)",null,null,"Palm Pre"),
            new Agent("(Links) \\((\\d+)\\.(\\d+)",null,null,null),
            new Agent("(QtWeb) Internet Browser/(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(Silk)/(\\d+)\\.(\\d+)(?:\\.([0-9\\-]+))?",null,null,null),
            new Agent("(AppleWebKit)/(\\d+)\\.?(\\d+)?\\+ .* Safari",null,null,"WebKit Nightly"),
            new Agent("(Version)/(\\d+)\\.(\\d+)(?:\\.(\\d+))?.*Safari/",null,null,"Safari"),
            new Agent("(Safari)/\\d+",null,null,null),
            new Agent("(OLPC)/Update(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(OLPC)/Update()\\.(\\d+)",null,"0",null),
            new Agent("(SEMC\\-Browser)/(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(Teleca)",null,null,"Teleca Browser"),
            new Agent("(MSIE) (\\d+)\\.(\\d+)",null,null,"IE"),
            new Agent("(Nintendo 3DS).* Version/(\\d+)\\.(\\d+)(?:\\.(\\w+))",null,null,null)};

    private static Agent[] os = new Agent[]{
            new Agent("(Android) (\\d+)\\.(\\d+)(?:[.\\-]([a-z0-9]+))?",null,null,null),
            new Agent("(Android)\\-(\\d+)\\.(\\d+)(?:[.\\-]([a-z0-9]+))?",null,null,null),
            new Agent("(Android) Donut","1","2",null),
            new Agent("(Android) Eclair","2","1",null),
            new Agent("(Android) Froyo","2","2",null),
            new Agent("(Android) Gingerbread","2","3",null),
            new Agent("(Android) Honeycomb","3",null,null),
            new Agent("(Windows Phone 6\\.5)",null,null,null),
            new Agent("(Windows (?:NT 5\\.2|NT 5\\.1))",null,null,"Windows XP"),
            new Agent("(XBLWP7)",null,null,"Windows Phone OS"),
            new Agent("(Windows NT 10\\.0)",null,null,"Windows 10"),
            new Agent("(Windows NT 6\\.1)",null,null,"Windows 7"),
            new Agent("(Windows NT 6\\.0)",null,null,"Windows Vista"),
            new Agent("(Windows 98|Windows XP|Windows ME|Windows 95|Windows CE|Windows 7|Windows NT 4\\.0|Windows Vista|Windows 2000)",null,null,null),
            new Agent("(Windows NT 6\\.2)",null,null,"Windows 8"),
            new Agent("(Windows NT 5\\.0)",null,null,"Windows 2000"),
            new Agent("(Windows Phone OS) (\\d+)\\.(\\d+)",null,null,null),
            new Agent("(Windows ?Mobile)",null,null,"Windows Mobile"),
            new Agent("(WinNT4.0)",null,null,"Windows NT 4.0"),
            new Agent("(Win98)",null,null,"Windows 98"),
            new Agent("(Tizen)/(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(Mac OS X) (\\d+)[_.](\\d+)(?:[_.](\\d+))?",null,null,null),
            new Agent("(?:PPC|Intel) (Mac OS X)",null,null,null),
            new Agent("(CPU OS|iPhone OS) (\\d+)_(\\d+)(?:_(\\d+))?",null,null,"iOS"),
            new Agent("(iPhone|iPad|iPod); Opera",null,null,"iOS"),
            new Agent("(iPhone|iPad|iPod).*Mac OS X.*Version/(\\d+)\\.(\\d+)",null,null,"iOS"),
            new Agent("(CrOS) [a-z0-9_]+ (\\d+)\\.(\\d+)(?:\\.(\\d+))?",null,null,"Chrome OS"),
            new Agent("(Debian)-(\\d+)\\.(\\d+)\\.(\\d+)(?:\\.(\\d+))?",null,null,null),
            new Agent("(Linux Mint)(?:/(\\d+))?",null,null,null),
            new Agent("(Mandriva)(?: Linux)?/(\\d+)\\.(\\d+)\\.(\\d+)(?:\\.(\\d+))?",null,null,null),
            new Agent("(Symbian[Oo][Ss])/(\\d+)\\.(\\d+)",null,null,"Symbian OS"),
            new Agent("(Symbian/3).+NokiaBrowser/7\\.3",null,null,"Symbian^3 Anna"),
            new Agent("(Symbian/3).+NokiaBrowser/7\\.4",null,null,"Symbian^3 Belle"),
            new Agent("(Symbian/3)",null,null,"Symbian^3"),
            new Agent("(Series 60|SymbOS|S60)",null,null,"Symbian OS"),
            new Agent("(MeeGo)",null,null,null),
            new Agent("(Symbian [Oo][Ss])",null,null,"Symbian OS"),
            new Agent("(Black[Bb]erry)[0-9a-z]+/(\\d+)\\.(\\d+)\\.(\\d+)(?:\\.(\\d+))?",null,null,"BlackBerry OS"),
            new Agent("(Black[Bb]erry).+Version/(\\d+)\\.(\\d+)\\.(\\d+)(?:\\.(\\d+))?",null,null,"BlackBerry OS"),
            new Agent("(RIM Tablet OS) (\\d+)\\.(\\d+)\\.(\\d+)",null,null,"BlackBerry Tablet OS"),
            new Agent("(Play[Bb]ook)",null,null,"BlackBerry Tablet OS"),
            new Agent("(Black[Bb]erry)",null,null,"Blackberry OS"),
            new Agent("(webOS|hpwOS)/(\\d+)\\.(\\d+)(?:\\.(\\d+))?",null,null,"webOS"),
            new Agent("(SUSE|Fedora|Red Hat|PCLinuxOS)/(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(SUSE|Fedora|Red Hat|Puppy|PCLinuxOS|CentOS)/(\\d+)\\.(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(Ubuntu|Kindle|Bada|Lubuntu|BackTrack|Red Hat|Slackware)/(\\d+)\\.(\\d+)",null,null,null),
            new Agent("(PlayStation Vita) (\\d+)\\.(\\d+)",null,null,null),
            new Agent("(Windows|OpenBSD|FreeBSD|NetBSD|Ubuntu|Kubuntu|Android|Arch Linux|CentOS|WeTab|Slackware)",null,null,null),
            new Agent("(Linux|BSD)",null,null,null)};


    private static class Agent{
        public Pattern regx;
        public String v2;
        public String v1;
        public String name;

        public Agent(String regx, String v2, String v1, String name){
            this.regx = Pattern.compile(regx, Pattern.DOTALL
                    | Pattern.CASE_INSENSITIVE);
            this.v2 = v2;
            this.v1 = v1;
            this.name = name;
        }
    }
}
